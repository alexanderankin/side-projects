#!/usr/bin/env python3

import subprocess
import threading
import time
from pathlib import Path
import logging

logging.basicConfig(level=logging.DEBUG)
log = logging.getLogger(__name__)


def run(*args: str) -> None:
    log.info("run: %s", " ".join(args))
    subprocess.run(args, check=True)


def _consume_remaining_output(stream):
    """Safely drains the stream in the background so the buffer never blocks."""
    try:
        for _ in stream:
            pass  # Read and immediately discard lines
    except Exception:
        pass
    finally:
        stream.close()


def main() -> None:
    log.info("starting")
    root = Path("tftp-root")
    root.mkdir(exist_ok=True)
    log.debug("created root dir %s", root)

    (root / "hello.txt").write_text("hello\n")
    Path("foo.txt").write_text("upload-test\n")

    log.info("+ starting TFTP server")
    server = subprocess.Popen([
        "python3",
        "tftp.py",
        "server",
        "--port", "10069",
        "--root", str(root),
    ], stdout=subprocess.PIPE)
    log.info("started subprocess")

    try:
        count = 0
        while b"TFTP server listening on udp" not in (line := server.stdout.readline()):
            print(f"waiting for server and got line: {line}")
            count += 1
            if count > 10:
                raise Exception("could not do it")

        threading.Thread(target=_consume_remaining_output, args=(server.stdout,), daemon=True).start()

        run(
            "python3", "tftp.py",
            "get",
            "127.0.0.1",
            "hello.txt",
            "downloaded.txt",
            "--port", "10069",
        )

        run(
            "python3", "tftp.py",
            "put",
            "127.0.0.1",
            "foo.txt",
            "foo.txt",
            "--port", "10069",
        )

        print("\nTests completed successfully.")
        print("downloaded.txt:", Path("downloaded.txt").read_text().strip())
        print("tftp-root/foo.txt:", (root / "foo.txt").read_text().strip())

    finally:
        print("+ stopping TFTP server")
        server.terminate()

        try:
            server.wait(timeout=2)
        except subprocess.TimeoutExpired:
            server.kill()
            server.wait()


if __name__ == "__main__":
    main()
