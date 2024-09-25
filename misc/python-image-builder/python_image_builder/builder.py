from json.decoder import JSONDecodeError
import time
from dataclasses import dataclass, field
from functools import cached_property
from pathlib import Path
from typing import Any

from httpx import Client, Request
from httpx._auth import FunctionAuth
from python_image_builder.http_requests import BearerAuth

REALM_FIELDS = {"realm", "service"}


_DEFAULT_REPO = "docker.io/library"
_DEFAULT_REPO_URL = "registry-1.docker.io/library"
_OTHER_REPO_CHARS = (".", ":")


@dataclass
class ContainerImage:
    image: str
    tag: str = "latest"
    repository: str = _DEFAULT_REPO
    insecure: bool = False

    @staticmethod
    def parse(image: str, insecure: bool = False) -> "ContainerImage":
        if ":" in image:
            image, tag = image.rsplit(":", 1)
        else:
            tag = "latest"

        if "/" in image and any(c in image.split("/")[0] for c in _OTHER_REPO_CHARS):
            repo, image = image.rsplit("/", 1)
        else:
            repo = _DEFAULT_REPO

        return ContainerImage(image=image, tag=tag, repository=repo, insecure=insecure)

    @property
    def repo(self):
        return Repo(repository=self.repository, insecure=self.insecure)

    @property
    def repo_url(self):
        return self.repo.repo_url


@dataclass(frozen=True)
class Repo:
    repository: str = _DEFAULT_REPO
    insecure: bool = False

    @cached_property
    def repository_scheme(self) -> str:
        scheme = "http" if self.insecure else "https"
        return scheme

    @cached_property
    def repository_url(self) -> str:
        if self.repository == _DEFAULT_REPO:
            repository = _DEFAULT_REPO_URL
        else:
            repository = self.repository
        return repository

    @cached_property
    def repository_host(self) -> str:
        return self.repository_url[: self.repository_url.index("/")]

    @cached_property
    def repository_path(self) -> str:
        return self.repository_url[self.repository_url.index("/") + 1 :]

    @cached_property
    def repo_url(self):
        return f"{self.repository_scheme}://{self.repository_host}"


@dataclass
class Realm:
    realm: str
    service: str


@dataclass
class RepoAuthCache:
    client: Client
    _cache: dict[tuple[Repo, str], str] = field(default_factory=dict)
    _realm_cache: dict[Repo, Realm] = field(default_factory=dict)

    def __post_init__(self):
        self._realm_cache[Repo()] = Realm(
            "https://auth.docker.io/token", "registry.docker.io"
        )

    def token_for_pulling(self, image: ContainerImage):
        """
        https://$REGISTRY/v2/token?service=$REGISTRY_SERVICE&scope=repository:$IMAGE:pull
        https://$REGISTRY/v2/library/$IMAGE/manifests/$TAG
        curl -v https://registry-1.docker.io/v2/library/nginx/manifests/latest
        """

        repo = image.repo
        realm = self.get_realm(repo)

        token_key = (repo, image.image)
        old_token = self._cache.get(token_key)
        if self._is_token_valid(old_token, int(time.time())):
            token = old_token
        else:
            token = self._new_token(realm, f"{repo.repository_path}/{image.image}")
            self._cache[token_key] = token

        return token

    def get_realm(self, repo: Repo) -> Realm:
        realm = self._realm_cache.get(repo)
        if realm is None:
            realm = self._fetch_realm(repo)
            self._realm_cache[repo] = realm
        return realm

    def _fetch_realm(self, repo: Repo) -> Realm:
        response = self.client.get(url=f"{repo.repo_url}/v2/")
        auth = response.headers.get("www-authenticate")
        realm = self._parse_auth_challenge(auth)
        if realm is None:
            raise Exception(f"could not parse auth {auth} for repo {repo}")
        return realm

    @staticmethod
    def _parse_auth_challenge(challenge: str) -> Realm | None:
        """
        'Bearer realm="https://auth.docker.io/token",service="registry.docker.io"'
        """
        if not isinstance(challenge, str):
            return None

        parts = challenge.split(" ")
        if len(parts) != 2:
            return None

        if parts[0].strip().lower() != "bearer":
            return None

        scheme_parts = parts[1].strip().split(",")
        scheme_params = [p.split("=") for p in scheme_parts]
        scheme_params = [p for p in scheme_params if len(p) == 2]
        params: dict[str, str] = {k: v for k, v in scheme_params if k}

        realm = params.get("realm")
        service = params.get("service")

        if not realm or not service:
            return None

        realm = realm.lstrip("'\"").rstrip("'\"")
        service = service.lstrip("'\"").rstrip("'\"")

        return Realm(realm=realm, service=service)

    @staticmethod
    def _is_token_valid(token: str, now: int):
        if not isinstance(token, str):
            return False
        token_parts = token.split(".")
        if len(token_parts) < 2:
            return False
        from base64 import b64decode
        from json import loads

        token_body_ser = b64decode(token_parts[1] + "=" * 4)
        try:
            token_body: dict[str, Any] = loads(token_body_ser)
        except JSONDecodeError:
            return False
        exp = token_body.get("exp")
        if not isinstance(exp, int):
            return False
        return now < exp

    def _new_token(self, realm: Realm, image: str, action: str = "pull") -> str:
        url = f"{realm.realm}?service={realm.service}&scope=repository:{image}:{action}"
        print(url)
        response = self.client.get(url)
        response.raise_for_status()
        body: dict[str, Any] = response.json()
        token = body.get("token")
        if not isinstance(token, str):
            raise Exception(
                f"could not get token from response {body} -> {token} from {url}"
            )
        return token


@dataclass
class RepoClient:
    client: Client = field(default_factory=Client)
    auth_cache: RepoAuthCache | None = None

    def __post_init__(self):
        if self.auth_cache is None:
            self.auth_cache = RepoAuthCache(client=self.client)

    @staticmethod
    def _url(docker_image: ContainerImage, path: str):
        return f"https://{docker_image.repository}/{path.lstrip('/')}"

    def get_image(self, docker_image: ContainerImage):
        repo = docker_image.repo
        token = self.auth_cache.token_for_pulling(docker_image)
        realm = self.auth_cache.get_realm(repo)

        # https://registry-1.docker.io/v2/library/nginx/manifests/latest
        manifest = self._get_manifest(docker_image, repo, token=token)
        return manifest

    def _get_manifest(self, image: ContainerImage, repo: Repo, token: str):
        url = (
            f"{repo.repo_url}/v2/{repo.repository_path}/{image.image}/manifests/latest"
        )
        print(url)
        response = self.client.get(url, auth=BearerAuth(token=token))
        print(response.json())
        # response.raise_for_status()
        return url


# repo_client = RepoClient()
# nginx = repo_client.get_image(ContainerImage("nginx"))
# print(nginx)


@dataclass
class ImageCache:
    repo_client: RepoClient = field(default_factory=RepoClient)
    cache_dir: str = str(Path.home() / ".cache" / "pib")

    def __post_init__(self):
        Path(self.cache_dir).mkdir(parents=True, exist_ok=True)


# image_cache = ImageCache()
