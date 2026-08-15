.PHONY: all
all: single_host

build/disk.img:
	@mkdir -p build
	qemu-img create -f qcow2 $@ 20G

build/ipxe.pxe:
	@mkdir -p build
	cd build; wget -N 'https://boot.ipxe.org/ipxe.pxe'

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
ubuntu: build/initrd build/vmlinuz build/ubuntu.ipxe

build/initrd:
	@mkdir -p build
	wget -O $@ '$(UBUNTU_BASE)/unpacked/resolute-server-cloudimg-amd64-initrd-generic'

build/vmlinuz:
	@mkdir -p build
	wget -O $@ '$(UBUNTU_BASE)/unpacked/resolute-server-cloudimg-amd64-vmlinuz-generic'

build/ubuntu.ipxe: boot.pxe
	cp $< $@

.PHONY: single_host
single_host: build/disk.img build/ipxe.pxe ubuntu
	./single-host.sh
