all:

.venv/bin/activate:
	python3 -m venv .venv

pytest: .venv/bin/activate
	. .venv/bin/activate; pytest

format:
	. .venv/bin/activate; ruff format

IMAGE ?= ../../../../../../cloud/vm/qemu-cloud-init/build/resolute-server-cloudimg-arm64.img

qemu-offline-test: .venv/bin/activate
	. .venv/bin/activate; qemu-offline-test --project ../ --image $(IMAGE)
