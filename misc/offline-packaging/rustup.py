import subprocess
from dataclasses import dataclass, field
from datetime import datetime
from os import environ
from os.path import getmtime
from pathlib import Path
from textwrap import dedent
from typing import Literal
from urllib.parse import urlparse
from zipfile import ZipFile, ZipInfo

import toml
from requests import get, head


def setup_log():
    from logging import DEBUG, basicConfig, getLogger

    basicConfig(level=DEBUG)
    logger = getLogger(__name__)
    return logger


log = setup_log()


class WgetNCache:
    cache_dir: Path
    wget_ignore_timestamps: bool
    keys: list[str]

    def __init__(
        self,
        cache_name: str = "rustup.py",
        wget_ignore_timestamps: bool = True,
    ):
        self.cache_dir = Path.home() / ".cache" / cache_name
        self.wget_ignore_timestamps = wget_ignore_timestamps
        self.keys = []

        destination = self.cache_dir
        destination.parent.mkdir(parents=True, exist_ok=True)

    def get(self, url: str, key: str, append: bool = True):
        if append:
            self.keys.append(key)

        destination = self.cache_dir / key
        if self.wget_ignore_timestamps:
            if not destination.exists():
                # log.debug("ignoring timestamps: doesn't exist")
                destination.write_bytes(get(url).content)
            # else:
            #     log.debug("ignoring timestamps: exists")
            return

        head_response = head(url)
        last_modified = head_response.headers["last-modified"]
        last_modified_datetime = datetime.strptime(
            last_modified, "%a, %d %b %Y %H:%M:%S %Z"
        )
        try:
            file_datetime = datetime.fromtimestamp(getmtime(destination))
            file_exists = True
        except FileNotFoundError:
            file_datetime = datetime.fromtimestamp(0)
            file_exists = False

        needs_downloaded = not file_exists or last_modified_datetime > file_datetime
        # fmt: off
        log.info(
            "file %s from %s will need downloading: %s (it exists %s) because last modified %s is newer than file time %s",
            destination, url, needs_downloaded, file_exists, last_modified_datetime, file_datetime
        )
        # fmt: on
        if needs_downloaded:
            get_response = get(url)
            destination.write_bytes(get_response.content)


wget_cache = WgetNCache(wget_ignore_timestamps=False)


# noinspection SpellCheckingInspection
@dataclass
class Config:
    arch_requested: str | None = None

    version_requested: str | None = None
    """latest if None"""

    rust_up_update_root: str = field(
        default_factory=lambda: environ.get(
            "RUSTUP_UPDATE_ROOT", "https://static.rust-lang.org/rustup"
        )
    )
    """same meaning as in rust up"""

    rust_up_dist_server: str = field(
        default_factory=lambda: environ.get(
            "RUSTUP_DIST_SERVER", "https://static.rust-lang.org"
        )
    )
    """same meaning as in rust up"""


config = Config(version_requested="1.84.0")

# fmt:off
wget_cache.get("https://sh.rustup.rs", "rustup-init.sh", append=False)
(wget_cache.cache_dir / "rustup-init.sh").chmod(mode=0o755)
arch = (config.arch_requested or subprocess.run(wget_cache.cache_dir / "rustup-init.sh", check=True, encoding="utf-8", stdout=subprocess.PIPE, env={"RUSTUP_INIT_SH_PRINT": "arch"}).stdout.strip())
# fmt:on


def url_path_to_file_name(url: str) -> str:
    return urlparse(url).path.replace("/", "__")


def file_name_to_url_path(path: str) -> str:
    return path.replace("__", "/")


ext = ".exe" if "windows" in arch else ""
rust_up_url = f"{config.rust_up_update_root}/dist/{arch}/rustup-init{ext}"
wget_cache.get(rust_up_url, url_path_to_file_name(rust_up_url), append=False)

ToolChain = Literal["stable", "beta", "nightly", "none"]

manifest_toolchain: ToolChain = "stable"

# fmt:off
manifest_url = f"{config.rust_up_dist_server}/dist/channel-rust-{manifest_toolchain}.toml"
manifest_url_actual = manifest_url if config.version_requested is None else f"{config.rust_up_dist_server}/dist/channel-rust-{config.version_requested}.toml"
wget_cache.get(manifest_url_actual, url_path_to_file_name(manifest_url))
# fmt:on

manifest_sha_url = f"{manifest_url}.sha256"
manifest_sha_url_actual = f"{manifest_url_actual}.sha256"
wget_cache.get(manifest_sha_url_actual, url_path_to_file_name(manifest_sha_url))

manifest = toml.load(wget_cache.cache_dir / url_path_to_file_name(manifest_url))

COMPONENTS = ["cargo", "clippy", "rust-docs", "rust-std", "rustc", "rustfmt"]


def get_component_url_from_manifest(each_component, manifest):
    if each_component in manifest["pkg"]:
        component_url = manifest["pkg"][each_component]["target"][arch].get("xz_url")
    elif each_component in manifest["renames"]:
        each_component = manifest["renames"][each_component]["to"]
        component_url = manifest["pkg"][each_component]["target"][arch].get("xz_url")
    else:
        raise Exception(f"no component {each_component}, even in renames")
    return component_url


for each_component in COMPONENTS:
    component_url = get_component_url_from_manifest(each_component, manifest)
    wget_cache.get(component_url, url_path_to_file_name(component_url))

output = Path(__file__).parent / "bootstrapper.zip"
if output.exists():
    output.unlink()

# fmt:off
with ZipFile(output, "w") as zip_f:
    rust_up_zip_file = ZipInfo.from_file(wget_cache.cache_dir / url_path_to_file_name(rust_up_url), "./offline-rustup/" + file_name_to_url_path(url_path_to_file_name(rust_up_url)))
    rust_up_zip_file.external_attr = 0o755 << 16
    zip_f.writestr(rust_up_zip_file, compresslevel=0, data=(wget_cache.cache_dir / url_path_to_file_name(rust_up_url)).read_bytes())

    rust_up_zip_file = ZipInfo("offline-rustup-init.sh")
    rust_up_zip_file.date_time = rust_up_zip_file.date_time
    rust_up_zip_file.external_attr = 0o755 << 16
    rust_up_zip_file_data = dedent(f"""
        #!/usr/bin/env bash
        if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
        set -eux -o pipefail
        full="$(readlink -f "$BASH_SOURCE")"; dir="${{full%\/*}}"; file="${{full##*/}}";
        cd "$dir"

        python3 -m http.server 8080 --directory ./offline-rustup & :;
        server_pid=$!
        trap "kill $server_pid" EXIT
        
        while ! (echo > /dev/tcp/localhost/8080) >/dev/null 2>&1; do echo "Waiting for port 8080 on localhost..."; sleep 1; done
        export RUSTUP_DIST_SERVER=http://localhost:8080
        ./offline-rustup/rustup/dist/{arch}/rustup-init -y
    """).strip() + "\n"
    zip_f.writestr(rust_up_zip_file, compresslevel=0, data=rust_up_zip_file_data)

    manifest_zip_file = ZipInfo.from_file(wget_cache.cache_dir / url_path_to_file_name(manifest_url), "./offline-rustup/" + file_name_to_url_path(url_path_to_file_name(manifest_url)))
    zip_f.writestr(manifest_zip_file, compresslevel=0, data=(wget_cache.cache_dir / url_path_to_file_name(manifest_url)).read_bytes())

    manifest_sha_zip_file = ZipInfo.from_file(wget_cache.cache_dir / url_path_to_file_name(manifest_sha_url), "./offline-rustup/" + file_name_to_url_path(url_path_to_file_name(manifest_sha_url)))
    zip_f.writestr(manifest_sha_zip_file, compresslevel=0, data=(wget_cache.cache_dir / url_path_to_file_name(manifest_sha_url)).read_bytes())

    for each_component in COMPONENTS:
        component_url = get_component_url_from_manifest(each_component, manifest)
        cargo_zip_file = ZipInfo.from_file(wget_cache.cache_dir / url_path_to_file_name(component_url), "./offline-rustup/" + file_name_to_url_path(url_path_to_file_name(component_url)))
        zip_f.writestr(cargo_zip_file, compresslevel=0, data=(wget_cache.cache_dir / url_path_to_file_name(component_url)).read_bytes())
# fmt:on
