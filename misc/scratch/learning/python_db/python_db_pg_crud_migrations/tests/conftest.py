import os
import socket
import subprocess
import time

import pytest
import requests
from faker import Faker
from testcontainers.postgres import PostgresContainer


def _get_free_port() -> int:
    with socket.socket() as s:
        s.bind(("127.0.0.1", 0))
        s.listen(1)
        return s.getsockname()[1]


@pytest.fixture(scope="session")
def faker():
    return Faker()


@pytest.fixture(scope="session")
def postgres_url():
    with PostgresContainer("postgres:16-alpine") as postgres:
        yield postgres.get_connection_url(driver="psycopg")


@pytest.fixture(scope="session")
def api_base_url(postgres_url: str):
    port = _get_free_port()
    env = os.environ.copy()

    # Adjust this if your app expects a different setting name.
    env["DATABASE_URL"] = postgres_url

    proc = subprocess.Popen(
        [
            "python",
            "-m",
            "uvicorn",
            "python_db_pg_crud_migrations.api:app",
            "--host",
            "127.0.0.1",
            "--port",
            str(port),
        ],
        env=env,
        # stdout=subprocess.PIPE,
        # stderr=subprocess.STDOUT,
        text=True,
    )

    base_url = f"http://127.0.0.1:{port}"

    try:
        deadline = time.time() + 30
        last_error = None

        while time.time() < deadline:
            try:
                # Lifespan runs migrations on startup, so this also waits for DB readiness.
                response = requests.get(f"{base_url}/categories", timeout=1.5)
                if response.status_code in (200, 404, 500):
                    break
            except Exception as exc:
                last_error = exc
                time.sleep(0.5)
        else:
            output = ""
            if proc.stdout:
                output = proc.stdout.read()
            raise RuntimeError(f"API did not start. Last error: {last_error}\n{output}")

        yield base_url

    finally:
        proc.terminate()
        try:
            proc.wait(timeout=10)
        except subprocess.TimeoutExpired:
            proc.kill()
