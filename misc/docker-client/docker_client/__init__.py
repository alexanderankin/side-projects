from os import PathLike

from docker_client.clients import RequestClient, RequestsClient


def requests_client(docker_socket: str | PathLike = "/var/run/docker.sock") -> RequestClient:
    return RequestsClient(docker_socket=docker_socket)
