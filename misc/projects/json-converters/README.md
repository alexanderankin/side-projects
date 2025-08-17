 json-conv

CLI converters that read from **stdin** and write to **stdout**:

- `json2yaml`
- `yaml2json`
- `toml2json`
- `json2toml`
- `json2csv [--[no-]headers]`
- `csv2json [--[no-]headers]`

All commands are synonyms for `json-conv <command>`; e.g., `json2yaml` is dispatched the same as `json-conv json2yaml`.

## Install

```shell
PKG=json-conv; BINS=(json2yaml yaml2json toml2json json2toml json2csv csv2json); { [[ -d ~/.$PKG-venv ]] || python -m venv ~/.$PKG-venv; } && . ~/.$PKG-venv/bin/activate && pip install -U pip wheel && pip install 'git+https://github.com/alexanderankin/side-projects.git@main#egg='"$PKG"'&subdirectory=misc/projects/json-converters' && for b in "${BINS[@]}"; do ln -fvrs $(which $b) ~/.local/bin; done && deactivate
```

## Install as library (with Poetry)

```bash
poetry add json-conv
# or from source:
poetry install
```

## Usage

```shell
# JSON -> YAML
cat data.json | json2yaml

# YAML -> JSON
cat data.yaml | yaml2json

# TOML -> JSON
cat pyproject.toml | toml2json | jq .

# JSON -> TOML
cat data.json | json2toml

# JSON -> CSV with header row (default)
cat data.json | json2csv > data.csv

# JSON array-of-arrays -> CSV without header row
cat rows.json | json2csv --no-headers

# CSV with header row -> JSON array of objects (default)
cat data.csv | csv2json | jq .

# CSV without header row -> JSON array-of-arrays
cat data_no_header.csv | csv2json --no-headers | jq .
```


## CSV header semantics

* csv2json:
  * `--headers` (default): treats first CSV row as header. Output is a JSON array of objects. 
  * `--no-headers`: treats all rows as data. Output is a JSON array of arrays.
* json2csv:
  * `--headers` (default): expects a JSON array of objects; prints a header row from keys of the first object (union of keys across rows is not enforced to keep behavior predictable). 
  * `--no-headers`: expects a JSON array of arrays; prints rows only.
