.PHONY: init
init:
	[ -d .venv/bin ] || python3 -m venv .venv
	@[ -d .venv/bin ] && echo source .venv/bin/activate || echo source .venv/Scripts/activate

.PHONY: install
install:
	pip install --editable . --config-setting editable_mode=strict

.PHONY: ci
ci:
	pip install --group dev --editable . --config-setting editable_mode=strict
