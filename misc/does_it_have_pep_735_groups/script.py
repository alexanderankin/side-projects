from collections import defaultdict
from dataclasses import dataclass
from json import loads
from pathlib import Path
from typing import List
from zipfile import ZipFile

from requests import get

from ignore_properties import ignore_properties


def main():
    data = Path(__file__).parent.joinpath('top-packages-data.txt').read_text()
    sections = to_sections(data)
    packages = {s1 for s in sections.values() for s1 in s}
    package_jsons = [download_package_json(p) for p in packages]
    # packages_extras = {p.info.name: p.extras() for p in package_jsons}
    for p in package_jsons:
        print(p.info.name)
        print(p.extras())

    # prints:
    """
    grpcio-status
    []
    certifi
    []
    requests
    ['security', 'socks', 'use_chardet_on_py3']
    pip
    []
    six
    []
    botocore
    ['crt']
    aiobotocore
    ['awscli', 'boto3']
    PyYAML
    []
    packaging
    []
    wheel
    ['test']
    cryptography
    ['docs', 'docstest', 'nox', 'pep8test', 'sdist', 'ssh', 'test', 'test-randomorder']
    numpy
    []
    boto3
    ['crt']
    setuptools
    ['certs', 'docs', 'ssl', 'testing', 'testing-integration']
    idna
    []
    s3fs
    ['awscli', 'boto3']
    s3transfer
    ['crt']
    charset-normalizer
    ['unicode_backport']
    typing-extensions
    []
    urllib3
    ['brotli', 'socks', 'zstd']
    python-dateutil
    []
    """


@dataclass
class Url:
    url: str = None
    filename: str = None
    # noinspection SpellCheckingInspection
    packagetype: str = None


@dataclass
class Info:
    name: str


@dataclass
class PackageJson:
    urls: List[Url]
    info: Info

    def __post_init__(self):
        if self.urls and len(self.urls) and not isinstance(self.urls[0], Url):
            self.urls = [ignore_properties(Url, u) for u in self.urls]
        if self.info and not isinstance(self.info, Info):
            self.info = ignore_properties(Info, self.info)

    def download_first_wheel_url_or_fail(self):
        whl = next(iter(u for u in self.urls if u.filename.endswith('.whl')))
        path = Path(__file__).parent.joinpath('data', whl.filename)
        return whl, download_it(whl.url, path)

    def extras(self):
        whl, downloaded_file = self.download_first_wheel_url_or_fail()
        meta_data_file = '-'.join(whl.filename.split('-')[0:2])
        meta_data_file = f'{meta_data_file}.dist-info/METADATA'

        # https://stackoverflow.com/a/10909016
        zip_file = ZipFile(downloaded_file)

        if meta_data_file in zip_file.namelist():
            md_lines = zip_file.read(meta_data_file).decode('utf-8').split('\n')
            pe = 'Provides-Extra: '
            provides_extra = [m[len(pe):] for m in md_lines if m.startswith(pe)]
            return provides_extra
        else:
            raise Exception(whl)


def download_package_json(package_name: str) -> PackageJson:
    # e.g. testcontainers
    url = f'https://pypi.org/pypi/{package_name}/json'
    path = Path(__file__).parent.joinpath('data', f'{package_name}.json')
    downloaded_file = download_it(url, path)
    return ignore_properties(PackageJson, loads(downloaded_file.read_text()))


def download_it(url: str, path: Path) -> Path:
    if path.exists():
        return path
    # https://stackoverflow.com/a/16696317
    with get(url, stream=True) as r:
        r.raise_for_status()
        with path.open('wb') as f:
            for chunk in r.iter_content(chunk_size=8192):
                # If you have chunk encoded response uncomment if
                # and set chunk_size parameter to None.
                # if chunk:
                f.write(chunk)
    return path


def to_sections(data):
    """
    https://pypistats.org/top as of 2024-01-24

    parses a file like:

    past day:
    boto3
    urllib3

    past month:
    urllib3

    to produce:
    print(dumps(sections, indent=4))
    {
        "past day:": [
            "boto3",
            "urllib3",
        ],
        "past month:": [
        ]
    }

    :param data: the string contents of the file
    :return: dict as above
    """
    sections = defaultdict(list)
    cur = None
    for index, line in enumerate(data.split("\n")):
        if index == 0: continue  # noqa
        if not line:
            cur = None
        elif line.startswith('past'):
            cur = line
        else:
            sections[cur].append(line)
    return sections


if __name__ == '__main__':
    main()
