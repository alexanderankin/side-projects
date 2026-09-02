#!/usr/bin/env python3
"""
Minimal TFTP client/server (RFC 1350-ish).

Usage:
  # Server (use 1069 to avoid root)
  python tftp.py server --host 0.0.0.0 --port 1069 --root ./tftp-root

  # Download
  python tftp.py get 127.0.0.1 remote.bin local.bin --port 1069

  # Upload
  python tftp.py put 127.0.0.1 local.bin remote.bin --port 1069
"""

from __future__ import annotations

import argparse
import os
import socket
import struct
import threading
from pathlib import Path

RRQ, WRQ, DATA, ACK, ERROR = 1, 2, 3, 4, 5
BLOCK_SIZE = 512
TIMEOUT = 2.0
RETRIES = 5


def packet_request(op: int, filename: str) -> bytes:
    return struct.pack("!H", op) + filename.encode() + b"\0octet\0"


def packet_data(block: int, data: bytes) -> bytes:
    return struct.pack("!HH", DATA, block & 0xFFFF) + data


def packet_ack(block: int) -> bytes:
    return struct.pack("!HH", ACK, block & 0xFFFF)


def packet_error(code: int, message: str) -> bytes:
    return struct.pack("!HH", ERROR, code) + message.encode() + b"\0"


def parse_request(pkt: bytes) -> tuple[int, str]:
    if len(pkt) < 4:
        raise ValueError("short request")

    op = struct.unpack("!H", pkt[:2])[0]
    parts = pkt[2:].split(b"\0")

    if op not in (RRQ, WRQ) or len(parts) < 3:
        raise ValueError("invalid request")

    filename = parts[0].decode(errors="strict")
    mode = parts[1].decode(errors="ignore").lower()

    if mode != "octet":
        raise ValueError("only octet mode is supported")

    return op, filename


def safe_path(root: Path, filename: str) -> Path:
    root = root.resolve()
    target = (root / filename.lstrip("/\\")).resolve()

    if target != root and root not in target.parents:
        raise ValueError("path traversal rejected")

    return target


def wait_for(sock: socket.socket, expected_op: int, expected_block: int):
    for _ in range(RETRIES):
        try:
            pkt, addr = sock.recvfrom(65535)
        except socket.timeout:
            yield None, None
            continue

        if len(pkt) >= 4:
            op, block = struct.unpack("!HH", pkt[:4])
            if op == expected_op and block == (expected_block & 0xFFFF):
                yield pkt, addr
                return

            if op == ERROR:
                msg = pkt[4:].rstrip(b"\0").decode(errors="replace")
                raise RuntimeError(f"TFTP error {block}: {msg}")

    raise TimeoutError("TFTP transfer timed out")


# ---------- Client ----------

def client_get(host: str, port: int, remote: str, local: str) -> None:
    server = (host, port)

    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
        sock.settimeout(TIMEOUT)
        request = packet_request(RRQ, remote)

        expected = 1
        peer = server

        with open(local, "wb") as f:
            while True:
                received = None

                for attempt in range(RETRIES):
                    if expected == 1:
                        sock.sendto(request, peer)
                    else:
                        sock.sendto(packet_ack(expected - 1), peer)

                    try:
                        pkt, addr = sock.recvfrom(65535)
                    except socket.timeout:
                        continue

                    if len(pkt) < 4:
                        continue

                    op, block = struct.unpack("!HH", pkt[:4])

                    if op == ERROR:
                        raise RuntimeError(
                            pkt[4:].rstrip(b"\0").decode(errors="replace")
                        )

                    if op == DATA and block == (expected & 0xFFFF):
                        peer = addr  # server selects transfer TID
                        received = pkt[4:]
                        break

                if received is None:
                    raise TimeoutError("download timed out")

                f.write(received)
                sock.sendto(packet_ack(expected), peer)

                if len(received) < BLOCK_SIZE:
                    break

                expected = (expected + 1) & 0xFFFF

    print(f"downloaded {remote!r} -> {local!r}")


def client_put(host: str, port: int, local: str, remote: str) -> None:
    server = (host, port)

    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
        sock.settimeout(TIMEOUT)

        req = packet_request(WRQ, remote)
        peer = server

        # WRQ must receive ACK 0.
        for _ in range(RETRIES):
            sock.sendto(req, server)
            try:
                pkt, addr = sock.recvfrom(65535)
            except socket.timeout:
                continue

            if len(pkt) >= 4:
                op, block = struct.unpack("!HH", pkt[:4])

                if op == ERROR:
                    raise RuntimeError(
                        pkt[4:].rstrip(b"\0").decode(errors="replace")
                    )

                if op == ACK and block == 0:
                    peer = addr
                    break
        else:
            raise TimeoutError("server did not acknowledge WRQ")

        block = 1

        with open(local, "rb") as f:
            while True:
                data = f.read(BLOCK_SIZE)
                outgoing = packet_data(block, data)

                acknowledged = False

                for _ in range(RETRIES):
                    sock.sendto(outgoing, peer)

                    try:
                        pkt, addr = sock.recvfrom(65535)
                    except socket.timeout:
                        continue

                    if addr != peer or len(pkt) < 4:
                        continue

                    op, ack_block = struct.unpack("!HH", pkt[:4])

                    if op == ERROR:
                        raise RuntimeError(
                            pkt[4:].rstrip(b"\0").decode(errors="replace")
                        )

                    if op == ACK and ack_block == (block & 0xFFFF):
                        acknowledged = True
                        break

                if not acknowledged:
                    raise TimeoutError(f"timeout waiting for ACK {block}")

                if len(data) < BLOCK_SIZE:
                    break

                block = (block + 1) & 0xFFFF

    print(f"uploaded {local!r} -> {remote!r}")


# ---------- Server ----------

def serve_rrq(peer, root: Path, filename: str) -> None:
    try:
        path = safe_path(root, filename)

        if not path.is_file():
            raise FileNotFoundError(filename)

        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
            sock.settimeout(TIMEOUT)

            block = 1
            with path.open("rb") as f:
                while True:
                    data = f.read(BLOCK_SIZE)
                    pkt = packet_data(block, data)

                    ok = False
                    for _ in range(RETRIES):
                        sock.sendto(pkt, peer)

                        try:
                            reply, addr = sock.recvfrom(65535)
                        except socket.timeout:
                            continue

                        if addr != peer or len(reply) < 4:
                            continue

                        op, ack_block = struct.unpack("!HH", reply[:4])
                        if op == ACK and ack_block == (block & 0xFFFF):
                            ok = True
                            break

                    if not ok:
                        return

                    if len(data) < BLOCK_SIZE:
                        return

                    block = (block + 1) & 0xFFFF

    except FileNotFoundError:
        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
            s.sendto(packet_error(1, "File not found"), peer)
    except Exception as exc:
        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
            s.sendto(packet_error(0, str(exc)), peer)


def serve_wrq(peer, root: Path, filename: str) -> None:
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.settimeout(TIMEOUT)

    try:
        path = safe_path(root, filename)
        path.parent.mkdir(parents=True, exist_ok=True)

        # Avoid silently replacing existing server files.
        if path.exists():
            sock.sendto(packet_error(6, "File already exists"), peer)
            return

        with path.open("xb") as f:
            sock.sendto(packet_ack(0), peer)
            expected = 1

            while True:
                pkt = None

                for _ in range(RETRIES):
                    try:
                        candidate, addr = sock.recvfrom(65535)
                    except socket.timeout:
                        # Retransmit previous ACK.
                        sock.sendto(packet_ack(expected - 1), peer)
                        continue

                    if addr != peer or len(candidate) < 4:
                        continue

                    op, block = struct.unpack("!HH", candidate[:4])

                    if op == DATA and block == (expected & 0xFFFF):
                        pkt = candidate
                        break

                    # Duplicate DATA: re-ACK it.
                    if op == DATA and block == ((expected - 1) & 0xFFFF):
                        sock.sendto(packet_ack(block), peer)

                if pkt is None:
                    raise TimeoutError("upload timed out")

                _, block = struct.unpack("!HH", pkt[:4])
                data = pkt[4:]

                f.write(data)
                sock.sendto(packet_ack(block), peer)

                if len(data) < BLOCK_SIZE:
                    return

                expected = (expected + 1) & 0xFFFF

    except FileExistsError:
        sock.sendto(packet_error(6, "File already exists"), peer)
    except Exception as exc:
        try:
            sock.sendto(packet_error(0, str(exc)), peer)
        except OSError:
            pass
    finally:
        sock.close()


def run_server(host: str, port: int, root: str) -> None:
    root_path = Path(root)
    root_path.mkdir(parents=True, exist_ok=True)
    root_path = root_path.resolve()

    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
        sock.bind((host, port))
        print(f"TFTP server listening on udp://{host}:{port}, root={root_path}")

        while True:
            pkt, peer = sock.recvfrom(65535)

            try:
                op, filename = parse_request(pkt)
            except Exception:
                sock.sendto(packet_error(4, "Illegal TFTP operation"), peer)
                continue

            target = serve_rrq if op == RRQ else serve_wrq
            threading.Thread(
                target=target,
                args=(peer, root_path, filename),
                daemon=True,
            ).start()


def main() -> None:
    p = argparse.ArgumentParser()
    sub = p.add_subparsers(dest="cmd", required=True)

    s = sub.add_parser("server")
    s.add_argument("--host", default="0.0.0.0")
    s.add_argument("--port", type=int, default=1069)
    s.add_argument("--root", default="./tftp-root")

    g = sub.add_parser("get")
    g.add_argument("host")
    g.add_argument("remote")
    g.add_argument("local")
    g.add_argument("--port", type=int, default=1069)

    u = sub.add_parser("put")
    u.add_argument("host")
    u.add_argument("local")
    u.add_argument("remote")
    u.add_argument("--port", type=int, default=1069)

    args = p.parse_args()

    if args.cmd == "server":
        run_server(args.host, args.port, args.root)
    elif args.cmd == "get":
        client_get(args.host, args.port, args.remote, args.local)
    elif args.cmd == "put":
        client_put(args.host, args.port, args.local, args.remote)


if __name__ == "__main__":
    main()
