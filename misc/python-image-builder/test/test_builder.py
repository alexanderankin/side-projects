from typing import cast

import pytest
from httpx import Client
from python_image_builder.builder import RepoAuthCache


@pytest.fixture
def repo_auth_cache():
    return RepoAuthCache(client=Client())


def test_repo_auth_cache_parse_auth_challenge(repo_auth_cache: RepoAuthCache):
    cache = repo_auth_cache
    realm = cache._parse_auth_challenge(
        'Bearer realm="https://auth.docker.io/token",service="registry.docker.io"'
    )

    assert realm is not None
    assert realm.realm == "https://auth.docker.io/token"
    assert realm.service == "registry.docker.io"


def test_repo_auth_cache_is_token_valid(repo_auth_cache: RepoAuthCache):
    assert not repo_auth_cache._is_token_valid(cast(str, None), 0)

    token = "header-here.eyJhY2Nlc3MiOlt7ImFjdGlvbnMiOlsicHVsbCJdLCJuYW1lIjoibGlicmFyeS9uZ2lueCIsInBhcmFtZXRlcnMiOnsicHVsbF9saW1pdCI6IjEwMCIsInB1bGxfbGltaXRfaW50ZXJ2YWwiOiIyMTYwMCJ9LCJ0eXBlIjoicmVwb3NpdG9yeSJ9XSwiYXVkIjoicmVnaXN0cnkuZG9ja2VyLmlvIiwiZXhwIjoxNzI3MjU1MjA1LCJpYXQiOjE3MjcyNTQ5MDUsImlzcyI6ImF1dGguZG9ja2VyLmlvIiwianRpIjoiZGNrcl9qdGlfRTh0V3VvX2RFVmlPdG9sYXJ6Y3FMdG1lbURrPSIsIm5iZiI6MTcyNzI1NDYwNSwic3ViIjoiIn0.secret-part-here"
    assert not repo_auth_cache._is_token_valid(token, 1727258910)
    assert repo_auth_cache._is_token_valid(token, 1727255105)
