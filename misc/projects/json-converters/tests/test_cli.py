from subprocess import PIPE, Popen

import pytest

TEST_CLI_CASES = [
    pytest.param(["json2yaml"], '{"a": true}', "a: true\n", "", id="json2yaml 1"),
    pytest.param(["json2yaml"], '{"b": false}', "b: false\n", "", id="json2yaml 2"),
    pytest.param(
        ["json2yaml"],
        '[1, 2, {"b": [1]}]',
        "- 1\n- 2\n- b:\n  - 1\n",
        "",
        id="json2yaml 3",
    ),
    pytest.param(
        ["yaml2json"],
        "a: on\nb: off",
        '{"a": true, "b": false}\n',
        "",
        id="yaml2json 1",
    ),
    pytest.param(
        ["toml2json"], "[a]\nb = false", '{"a": {"b": false}}\n', "", id="toml2json 1"
    ),
    pytest.param(
        ["csv2json"], "a,b\n1,2", '[{"a": "1", "b": "2"}]\n', "", id="csv2json 1"
    ),
    pytest.param(
        ["json2csv"], '[{"a": "1", "b": "2"}]\n', "a,b\n1,2\n", "", id="json2csv 1"
    ),
    pytest.param(
        ["json2csv"],
        '{"a": "1", "b": "2"}\n',
        "",
        "json2csv --no-headers expects a JSON array of arrays\n",
        id="json2csv errors on object with no headers",
    ),
    pytest.param(
        ["json2csv", "--headers"],
        '{"a": "1", "b": "2"}\n',
        "",
        "json2csv --headers expects a JSON array of arrays\n",
        id="json2csv errors on object with headers",
    ),
]


@pytest.mark.parametrize("args,cmd_input,cmd_output,cmd_err", TEST_CLI_CASES)
def test_cli(args: list[str], cmd_input: str, cmd_output: str, cmd_err: str):
    p = Popen(args, stdin=PIPE, stdout=PIPE)
    out, err = p.communicate(cmd_input.encode())
    assert out.decode() == cmd_output
    assert err is None or err.decode() == cmd_err
