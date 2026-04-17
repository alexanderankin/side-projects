import base64
import json
import re
import subprocess
import sys

import pytest


def run_hey(url: str, n: int = 1000, c: int = 100, method: str = "GET", body: str = None):
    cmd = [
        "hey",
        "-n", str(n),
        "-c", str(c),
        "-m", method,
    ]

    if body:
        cmd += [
            "-d", body,
            "-H", "Content-Type: application/json",
        ]

    cmd.append(url)

    proc = subprocess.Popen(
        cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        bufsize=1,
    )

    output = []
    for line in proc.stdout:
        sys.stdout.write(line)
        sys.stdout.flush()
        output.append(line)

    proc.wait()

    if proc.returncode != 0:
        raise RuntimeError("hey failed")

    return "".join(output)


def parse_hey(output: str):
    rps_match = re.search(r"Requests/sec:\s+([\d.]+)", output)
    avg_match = re.search(r"Average:\s+([\d.]+)\s+secs", output)

    if not rps_match or not avg_match:
        raise ValueError("Failed to parse hey output")

    return {
        "rps": float(rps_match.group(1)),
        "avg_latency": float(avg_match.group(1)),
    }


def make_bulk_payload(mode: str, n: int = 100):
    payload = [
        {
            "x": i,
            "y": i * 2,
            "z": i * 3,
            **({"data_bytes": base64.b64encode(f"payload-{i}".encode()).decode()}
               if mode == "sqlalchemy" else
               {"data_base64": base64.b64encode(f"payload-{i}".encode()).decode()}),
        }
        for i in range(n)
    ]
    return json.dumps(payload)


@pytest.mark.parametrize("url_prefix", ["sqlalchemy", "manual"])
def test_get_benchmark(url_prefix):
    base = "http://localhost:8000"
    url = f"{base}/{url_prefix}/data-points?limit=100"

    raw = run_hey(url)
    metrics = parse_hey(raw)
    print(f"\n[test_get_benchmark: {url_prefix}] -> {metrics}")


@pytest.mark.parametrize("url_prefix", ["sqlalchemy", "manual"])
def test_bulk_insert_benchmark(url_prefix):
    base = "http://localhost:8000"
    url = f"{base}/{url_prefix}/data-points/bulk"

    payload = make_bulk_payload(mode=url_prefix, n=100)  # fixed batch size
    raw = run_hey(url=url, n=200, c=10, method="POST", body=payload)
    metrics = parse_hey(raw)
    print(f"\n[test_bulk_insert_benchmark: {url_prefix}] -> {metrics}")
