from collections import defaultdict
from json import dumps
from pathlib import Path


def main():
    data = Path(__file__).parent.joinpath('top-packages-data.txt').read_text()
    sections = to_sections(data)
    packages = {s1 for s in sections.values() for s1 in s}
    print(packages)


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
    >>> print(dumps(sections, indent=4))
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
