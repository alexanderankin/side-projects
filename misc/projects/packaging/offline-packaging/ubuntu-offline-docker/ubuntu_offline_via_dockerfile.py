import subprocess
from grp import getgrnam
from os import walk
from os.path import getsize, join
from pathlib import Path
from random import choice
from shutil import which
from string import ascii_letters


def setup_log():
    from logging import DEBUG, basicConfig, getLogger

    basicConfig(level=DEBUG)
    logger = getLogger(__name__)
    return logger


log = setup_log()

rsync = which("rsync")
if rsync is None:
    log.error("no rsync in path, exiting")
    raise Exception("missing rsync")
log.info("using rsync from %s", rsync)


class DebFileCache:
    def __init__(
        self,
        cache_dir: Path = Path.home() / ".cache" / "ubuntu_offline",
    ):
        self.cache_dir = cache_dir
        self.cache_dir.mkdir(parents=True, exist_ok=True)

    def get_size(self) -> str:
        return self._human_readable_size(self._get_total_size(self.cache_dir))

    @staticmethod
    def _get_total_size(some_path):
        total = 0
        for dirpath, _, filenames in walk(some_path, followlinks=False):
            for f in filenames:
                fp = join(dirpath, f)
                try:
                    total += getsize(fp)
                except OSError:
                    pass  # Skip files that can't be accessed
        return total

    @staticmethod
    def _human_readable_size(size):
        for unit in ["B", "K", "M", "G", "T"]:
            if size < 1024.0:
                return f"{size:.1f}{unit}"
            size /= 1024.0
        return f"{size:.1f}P"


deb_cache = DebFileCache()
log.info("deb cache size is currently %s", deb_cache.get_size())

host_area, host_zone = Path("/etc/timezone").read_text().strip().split("/")
subprocess.run(
    [
        "docker",
        "build",
        "-f",
        "ubuntu_offline_via_dockerfile.Dockerfile",
        str(Path(__file__).parent),
        "-t",
        "test",
        "--build-arg",
        f"GID={getgrnam('sudo').gr_gid}",
        "--build-arg",
        f"host_area={host_area}",
        "--build-arg",
        f"host_zone={host_zone}",
    ],
    check=True,
)

container_name = "".join(choice(ascii_letters) for _ in range(20))
subprocess.run(
    ["docker", "run", "-d", "--name", container_name, "test"],
    check=True,
)

try:
    subprocess.run(
        [
            "docker",
            "cp",
            f"{container_name}:/var/lib/apt/lists",
            str(deb_cache.cache_dir),
        ],
        check=True,
    )
    subprocess.run(
        [
            "docker",
            "cp",
            f"{container_name}:/var/cache/apt/archives",
            str(deb_cache.cache_dir),
        ],
        check=True,
    )
finally:
    subprocess.run(
        ["docker", "rm", "-f", container_name],
        check=True,
    )

user_name = Path.home().name
writable_mount = Path(f"/media/{user_name}/writable")
if not writable_mount.exists():
    raise Exception("not rsync-ing, no 'writable'")

noble = writable_mount / "noble"
if not noble.exists():
    raise Exception("not rsync-ing, no 'writable/noble'")

noble_stat = noble.stat()
if (
    noble_stat.st_uid != 0
    or noble_stat.st_gid != getgrnam("sudo").gr_gid
    or noble_stat.st_mode != 0o42775
):
    raise Exception(
        f'sudo chmod 755 "{noble}" && sudo chmod g+rwxs "$_" && sudo chown root:sudo "$_"'
    )

subprocess.run(
    [
        "rsync",
        "--size-only",
        "--recursive",
        "--verbose",
        f"{deb_cache.cache_dir}/",
        f"{noble}/",
    ],
    check=True,
)
