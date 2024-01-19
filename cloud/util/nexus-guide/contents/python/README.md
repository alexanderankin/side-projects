to use the python modules in this folder, you must install poetry:

# https://python-poetry.org/docs/#installing-manually

```shell
python3 -m venv ~/.poetry-venv
[[ -d ~/.poetry-venv/bin ]] && source ~/.poetry-venv/bin/activate
[[ -d ~/.poetry-venv/Scripts ]] && source ~/.poetry-venv/Scripts/activate
pip install -U pip setuptools
pip install poetry

echo '' >> .bashrc
echo 'export PATH=$PATH:~/.poetry-venv/bin' >> .bashrc
# optional
#echo 'source <(poetry completions bash)' >> .bashrc
```
