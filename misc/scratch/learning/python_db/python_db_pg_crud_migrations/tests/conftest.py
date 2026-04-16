import os
import socket
import subprocess
import sys
import threading
import time

import pytest
import requests
import uvicorn
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

    # 1. Set env BEFORE import
    os.environ["DATABASE_URL"] = postgres_url

    # 2. Ensure fresh import (avoid stale config)
    if "python_db_pg_crud_migrations.api" in sys.modules:
        del sys.modules["python_db_pg_crud_migrations.api"]

    from python_db_pg_crud_migrations.api import app

    # 3. Build uvicorn server (no subprocess)
    config = uvicorn.Config(
        app,
        host="127.0.0.1",
        port=port,
        log_level="info",
        lifespan="on",
    )
    server = uvicorn.Server(config)

    thread = threading.Thread(target=server.run, daemon=True)
    thread.start()

    base_url = f"http://127.0.0.1:{port}"

    # 4. Wait for startup
    deadline = time.time() + 30
    last_error = None

    while time.time() < deadline:
        try:
            r = requests.get(f"{base_url}/categories", timeout=1)
            if r.status_code in (200, 404, 500):
                break
        except Exception as exc:
            last_error = exc
            time.sleep(0.3)
    else:
        raise RuntimeError(f"Server did not start: {last_error}")

    try:
        yield base_url
    finally:
        # 5. Clean shutdown
        server.should_exit = True
        thread.join(timeout=10)
