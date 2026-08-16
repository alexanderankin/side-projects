.PHONY: all
all: single_host

build/disk.img:
	@mkdir -p build
	qemu-img create -f qcow2 $@ 20G

.PHONY: build_ipxe-embedded
build_ipxe-embedded:
	$(MAKE) -C ipxe-embedded

build/ipxe.pxe: build_ipxe-embedded
	@mkdir -p build
	@#cd build; wget -N 'https://boot.ipxe.org/ipxe.pxe'
	cp ipxe-embedded/ipxe/src/bin/ipxe.pxe $@

.PHONY: uefi
uefi: build/code build/vars

build/code: find-ovmf.sh
	@mkdir -p build
	cp $$(bash find-ovmf.sh | jq -r .code) $@
build/vars: find-ovmf.sh
	@mkdir -p build
	cp $$(bash find-ovmf.sh | jq -r .vars) $@

UBUNTU_BASE := https://cloud-images.ubuntu.com/resolute/current
.PHONY: ubuntu
ubuntu: build/resolute-server-cloudimg-amd64-initrd-generic build/resolute-server-cloudimg-amd64-vmlinuz-generic build/boot.ipxe

build/resolute-server-cloudimg-amd64-initrd-generic:
	@mkdir -p build
	cd build && wget -N '$(UBUNTU_BASE)/unpacked/resolute-server-cloudimg-amd64-initrd-generic'

build/resolute-server-cloudimg-amd64-vmlinuz-generic:
	@mkdir -p build
	cd build && wget -N '$(UBUNTU_BASE)/unpacked/resolute-server-cloudimg-amd64-vmlinuz-generic'

# pre-emptively added in case we can do stuff
build/resolute-server-cloudimg-amd64.img:
	@mkdir -p build
	cd build && wget -N '$(UBUNTU_BASE)/resolute-server-cloudimg-amd64.img'

# can unpack/overwrite sda?
build/resolute-server-cloudimg-amd64.raw.gz: build/resolute-server-cloudimg-amd64.img
	qemu-img convert -f qcow2 -O raw $< build/resolute-server-cloudimg-amd64.raw
	gzip -f build/resolute-server-cloudimg-amd64.raw

build/resolute-mini-iso-amd64.iso:
	@mkdir -p build
	cd build && wget -N https://cdimage.ubuntu.com/ubuntu-mini-iso/resolute/daily-live/current/resolute-mini-iso-amd64.iso

build/boot.ipxe: boot.ipxe
	cp $< $@

.PHONY: ipxe_scripts
ipxe_scripts: build/arch.ipxe

build/arch.ipxe: arch.ipxe
	cp $< $@

.PHONY: single_host
single_host: build/disk.img build/ipxe.pxe ipxe_scripts ubuntu
	./single-host.sh
