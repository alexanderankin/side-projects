.PHONY: init
init:
	[ -d .venv/bin ] || python3 -m venv .venv
	@[ -d .venv/bin ] && echo source .venv/bin/activate || echo source .venv/Scripts/activate

.PHONY: install
install:
	[ -d .venv/bin ] && . .venv/bin/activate || . .venv/Scripts/activate; pip install --editable . --config-setting editable_mode=strict

.PHONY: ci
ci:
	[ -d .venv/bin ] && . .venv/bin/activate || . .venv/Scripts/activate; pip install --group dev --editable . --config-setting editable_mode=strict


.PHONY: lint
lint:
	[ -d .venv/bin ] && . .venv/bin/activate || . .venv/Scripts/activate; ruff check ./python_db_pg_crud_migrations/; ruff format ./python_db_pg_crud_migrations/; ty check ./python_db_pg_crud_migrations/


.PHONY: test
test:
	[ -d .venv/bin ] && . .venv/bin/activate || . .venv/Scripts/activate; pytest tests/
