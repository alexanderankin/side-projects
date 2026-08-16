.PHONY: all
all: single_host

build/disk.img:
	@mkdir -p build
	rm -f build/.installed
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
ubuntu: build/resolute-server-cloudimg-amd64.raw.gz \
  build/resolute-server-cloudimg-amd64-initrd-generic \
  build/resolute-server-cloudimg-amd64-vmlinuz-generic \
  build/deploy-overlay.cpio.gz build/image-version.ipxe

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
	gzip -1 -f build/resolute-server-cloudimg-amd64.raw

build/image-version.ipxe: build/resolute-server-cloudimg-amd64.img
	@version=$$(sha256sum $< | cut -d ' ' -f 1); \
		printf '#!ipxe\nset deploy-version %s\n' "$$version" > $@

build/deploy-overlay.cpio.gz: deploy-initrd/deploy-image deploy-initrd/deploy-image.service deploy-initrd/udhcpc-script cloud-init/user-data cloud-init/meta-data
	@mkdir -p build
	@overlay=$$(mktemp -d); \
		trap 'rm -r "$$overlay"' EXIT; \
		mkdir -p "$$overlay/usr/bin" "$$overlay/etc/systemd/system/initrd.target.wants" "$$overlay/seed"; \
		cp /usr/bin/busybox "$$overlay/usr/bin/busybox"; \
		cp deploy-initrd/deploy-image "$$overlay/usr/bin/deploy-image"; \
		cp deploy-initrd/udhcpc-script "$$overlay/usr/bin/deploy-udhcpc-script"; \
		cp deploy-initrd/deploy-image.service "$$overlay/etc/systemd/system/deploy-image.service"; \
		ln -s ../deploy-image.service "$$overlay/etc/systemd/system/initrd.target.wants/deploy-image.service"; \
		cp cloud-init/user-data cloud-init/meta-data "$$overlay/seed/"; \
		chmod 0755 "$$overlay/usr/bin/busybox" "$$overlay/usr/bin/deploy-image" "$$overlay/usr/bin/deploy-udhcpc-script"; \
		(cd "$$overlay" && find . -print0 | cpio --null --create --format=newc 2>/dev/null) | gzip -1 > $@

#build/resolute-mini-iso-amd64.iso:
#	@mkdir -p build
#	cd build && wget -N https://cdimage.ubuntu.com/ubuntu-mini-iso/resolute/daily-live/current/resolute-mini-iso-amd64.iso

build/ubuntu-26.04-live-server-amd64.iso:
	@mkdir -p build
	cd build && wget -N https://releases.ubuntu.com/resolute/ubuntu-26.04-live-server-amd64.iso

build/installer-vmlinuz: build/ubuntu-26.04-live-server-amd64.iso
	@mkdir -p build
	xorriso -osirrox on -indev $< -extract /casper/vmlinuz $@

build/installer-initrd: build/ubuntu-26.04-live-server-amd64.iso
	@mkdir -p build
	xorriso -osirrox on -indev $< -extract /casper/initrd $@

build/autoinstall/user-data: autoinstall/user-data
	@mkdir -p build/autoinstall
	cp $< $@

build/autoinstall/meta-data: autoinstall/meta-data
	@mkdir -p build/autoinstall
	cp $< $@

.PHONY: ipxe_scripts
ipxe_scripts: build/boot.ipxe build/arch.ipxe

build/boot.ipxe: boot.ipxe
	cp $< $@

build/arch.ipxe: arch.ipxe
	cp $< $@

#build/memdisk:
#	@mkdir -p build
#	@# apt-get install -y syslinux
#	cp /usr/lib/syslinux/memdisk $@

.PHONY: single_host
single_host: build/disk.img build/ipxe.pxe ipxe_scripts ubuntu start_server
	./single-host.sh

.PHONY: iso_install
iso_install: build/disk.img build/ipxe.pxe ipxe_scripts build/ubuntu-26.04-live-server-amd64.iso build/installer-vmlinuz build/installer-initrd build/autoinstall/user-data build/autoinstall/meta-data start_server
	./single-host.sh

# DO NOT MODIFY _server target recipes, they are intentionally simple
build/server:
	systemd-run --user --unit=my-http-server --same-dir python3 -m http.server -d build/ && touch $@
.PHONY: start_server stop_server
start_server: build/server
stop_server: build/server
	systemctl --user stop my-http-server && rm $<

clean: stop_server
	@mkdir -p build
	find build -mindepth 1 -not -path '*.iso' -not -path '*.img' -delete
	rm -vf build/disk.img
