import pprint
from json import dumps
from socket import SOCK_STREAM, AF_UNIX, socket
from os import PathLike
from typing import Any

import requests
from requests import Request, Session
from requests.adapters import HTTPAdapter
from urllib3.connection import HTTPConnection
from urllib3.connectionpool import HTTPConnectionPool

from docker_client.clients.clients import RequestClient


class UnixSocketConnection(HTTPConnection):
    def __init__(self, socket_path: str | PathLike):
        super().__init__("localhost")
        self.socket_path = socket_path

    def connect(self):
        self.sock = socket(AF_UNIX, SOCK_STREAM)
        self.sock.connect(str(self.socket_path))


class UnixSocketConnectionPool(HTTPConnectionPool):
    def __init__(self, socket_path: str | PathLike):
        super().__init__("localhost")
        self.socket_path = socket_path

    def _new_conn(self):
        return UnixSocketConnection(self.socket_path)


class UnixSocketAdapter(HTTPAdapter):
    def __init__(self, socket_path: str | PathLike):
        super().__init__()
        self.socket_path = socket_path
    # DEPRECATED: Users should move to get_connection_with_tls_context for all subclasses of HTTPAdapter using Requests>=2.32.2.
    # def get_connection(self, url, proxies=None):
    #    return UnixSocketConnectionPool()

    def get_connection_with_tls_context(self, request, verify, proxies=None, cert=None):
        return UnixSocketConnectionPool(self.socket_path)


class RequestsClient(RequestClient):
    def __init__(self, docker_socket: str | PathLike = "/var/run/docker.sock"):
        self.session = Session()
        self.prefix = "http://docker-socket"
        self.session.mount(self.prefix, UnixSocketAdapter(str(docker_socket)))

    def exchange(self, method: str, url: str, body: dict[str, Any]) -> dict[str, Any]:
        full_url = f"{self.prefix}{url}"

        data = dumps(body) if body else None
        request = Request(
            method=method,
            url=full_url,
            # headers=headers,
            data=data,
            # params=params,
            # auth=auth,
            # hooks=hooks,
        )
        response = self.session.send(request.prepare())
        return response.json()

#
# session = requests.Session()
# session.mount("http://snapd/", UnixSocketAdapter())
# response = session.get("http://snapd/v2/system-info")
# pprint.pprint(response.json())
