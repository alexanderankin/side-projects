import pprint

from docker_client import requests_client


def test_create_container():
    client = requests_client()
    containers = client.exchange("GET", "/containers/json?all=true", {})
    pprint.pprint(containers)
