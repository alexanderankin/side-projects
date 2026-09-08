.PHONY: all
all: run_raw_image_boot

build/disk.img:
	@mkdir -p build
	qemu-img create -f qcow2 $@ 20G

build/desktop-disk.img:
	@mkdir -p build
	qemu-img create -f qcow2 $@ 32G

ipxe-embedded/ipxe/src/bin/ipxe.pxe:
	$(MAKE) -C ipxe-embedded

build/ipxe.pxe: ipxe-embedded/ipxe/src/bin/ipxe.pxe
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
ubuntu: build/resolute-server-cloudimg-amd64.raw \
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
	bash -c 'if ! [[ -f build/resolute-server-cloudimg-amd64.img ]] && [[ -f ../qemu-cloud-init/build/resolute-server-cloudimg-amd64.img ]]; then rsync -a ../qemu-cloud-init/build/resolute-server-cloudimg-amd64.img build/resolute-server-cloudimg-amd64.img; fi'
	cd build && wget -N '$(UBUNTU_BASE)/resolute-server-cloudimg-amd64.img'

# can unpack/overwrite sda?
build/resolute-server-cloudimg-amd64.raw: build/resolute-server-cloudimg-amd64.img
	qemu-img convert -f qcow2 -O raw $< $@

build/resolute-server-cloudimg-amd64.raw.gz: build/resolute-server-cloudimg-amd64.raw
	gzip -1 -f $<

build/image-version.ipxe: build/resolute-server-cloudimg-amd64.img
	@version=$$(sha256sum $< | cut -d ' ' -f 1); \
		printf '#!ipxe\nset deploy-version %s\n' "$$version" > $@

build/deploy-overlay.cpio.gz: deploy-initrd/deploy-image deploy-initrd/deploy-image.service deploy-initrd/udhcpc-script cloud-init/user-data cloud-init/meta-data cloud-init/network-config build/busy-box/busybox
	@mkdir -p build
	@overlay=$$(mktemp -d); \
		trap 'rm -r "$$overlay"' EXIT; \
		mkdir -p "$$overlay/usr/bin" "$$overlay/etc/systemd/system/initrd.target.wants" "$$overlay/seed"; \
		cp build/busy-box/busybox "$$overlay/usr/bin/busybox"; \
		cp deploy-initrd/deploy-image "$$overlay/usr/bin/deploy-image"; \
		cp deploy-initrd/udhcpc-script "$$overlay/usr/bin/deploy-udhcpc-script"; \
		cp deploy-initrd/deploy-image.service "$$overlay/etc/systemd/system/deploy-image.service"; \
		ln -s ../deploy-image.service "$$overlay/etc/systemd/system/initrd.target.wants/deploy-image.service"; \
		cp cloud-init/user-data cloud-init/meta-data cloud-init/network-config "$$overlay/seed/"; \
		chmod 0755 "$$overlay/usr/bin/busybox" "$$overlay/usr/bin/deploy-image" "$$overlay/usr/bin/deploy-udhcpc-script"; \
		(cd "$$overlay" && find . -print0 | cpio --null --create --format=newc 2>/dev/null) | gzip -1 > $@

#build/resolute-mini-iso-amd64.iso:
#	@mkdir -p build
#	cd build && wget -N https://cdimage.ubuntu.com/ubuntu-mini-iso/resolute/daily-live/current/resolute-mini-iso-amd64.iso

build/ubuntu-26.04-live-server-amd64.iso:
	@mkdir -p build
	cd build && wget -N https://releases.ubuntu.com/resolute/ubuntu-26.04-live-server-amd64.iso

build/server-installer-vmlinuz: build/ubuntu-26.04-live-server-amd64.iso
	@mkdir -p build
	xorriso -osirrox on -indev $< -extract /casper/vmlinuz $@

build/server-installer-initrd: build/ubuntu-26.04-live-server-amd64.iso
	@mkdir -p build
	xorriso -osirrox on -indev $< -extract /casper/initrd $@

build/ubuntu-26.04-desktop-amd64.iso:
	@mkdir -p build
	cd build && wget -N https://releases.ubuntu.com/resolute/ubuntu-26.04-desktop-amd64.iso

build/desktop-installer-vmlinuz: build/ubuntu-26.04-desktop-amd64.iso
	@mkdir -p build
	xorriso -osirrox on -indev $< -extract /casper/vmlinuz $@

build/desktop-installer-initrd: build/ubuntu-26.04-desktop-amd64.iso
	@mkdir -p build
	xorriso -osirrox on -indev $< -extract /casper/initrd $@

build/desktop-nfs-overlay.cpio.gz: desktop-initrd/casper.conf
	@mkdir -p build
	@overlay=$$(mktemp -d); \
		trap 'rm -r "$$overlay"' EXIT; \
		mkdir -p "$$overlay/etc"; \
		cp $< "$$overlay/etc/casper.conf"; \
		(cd "$$overlay" && find . -print0 | cpio --null --create --format=newc 2>/dev/null) | gzip -1 > $@

build/autoinstall/user-data: autoinstall/user-data
	@mkdir -p build/autoinstall
	cp $< $@

build/autoinstall/meta-data: autoinstall/meta-data
	@mkdir -p build/autoinstall
	cp $< $@

build/autoinstall-desktop/user-data: autoinstall-desktop/user-data
	@mkdir -p build/autoinstall-desktop
	cp $< $@

build/autoinstall-desktop/meta-data: autoinstall-desktop/meta-data
	@mkdir -p build/autoinstall-desktop
	cp $< $@

build/autoinstall-desktop/network-config: autoinstall-desktop/network-config
	@mkdir -p build/autoinstall-desktop
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

.PHONY: run_raw_image_boot
run_raw_image_boot: build/disk.img build/ipxe.pxe ipxe_scripts ubuntu start_server
	@cp *.ipxe build/ # hacks - todo fixme
	sed -e 's/^set default-choice .*/set default-choice ubuntu/' -i build/boot.ipxe
	./single-host.sh

.PHONY: run_offline_raw_image_boot
run_offline_raw_image_boot: build/disk.img build/ipxe.pxe ipxe_scripts ubuntu start_server
	@cp *.ipxe build/ # hacks - todo fixme
	sed -e 's/^set default-choice .*/set default-choice ubuntu/' -i build/boot.ipxe
	sed -e 's#http://$${next-server}:8000#http://10.0.2.100:8000#g' -i build/boot.ipxe
	./single-host.sh restrict

.PHONY: run_iso_install
run_iso_install: build/disk.img build/ipxe.pxe ipxe_scripts build/ubuntu-26.04-live-server-amd64.iso build/server-installer-vmlinuz build/server-installer-initrd build/autoinstall/user-data build/autoinstall/meta-data start_server
	@cp *.ipxe build/ # hacks - todo fixme
	sed -e 's/^set default-choice .*/set default-choice ubuntu-iso/' -i build/boot.ipxe
	./single-host.sh

.PHONY: run_desktop_iso_install
run_desktop_iso_install: build/desktop-disk.img build/ipxe.pxe ipxe_scripts build/desktop-installer-vmlinuz build/desktop-installer-initrd build/desktop-nfs-overlay.cpio.gz build/autoinstall-desktop/user-data build/autoinstall-desktop/meta-data build/autoinstall-desktop/network-config start_server start_nfs_server
	@cp *.ipxe build/ # hacks - todo fixme
	sed -e 's/^set default-choice .*/set default-choice ubuntu-desktop/' -i build/boot.ipxe
	sed -e 's#$${next-server}:/exports#10.0.2.100:/exports#g' -i build/boot.ipxe
	DISK_IMAGE=build/desktop-disk.img ./single-host.sh

.PHONY: run_offline_desktop_iso_install
run_offline_desktop_iso_install: build/desktop-disk.img build/ipxe.pxe ipxe_scripts build/desktop-installer-vmlinuz build/desktop-installer-initrd build/desktop-nfs-overlay.cpio.gz build/autoinstall-desktop/user-data build/autoinstall-desktop/meta-data start_server start_nfs_server
	@cp *.ipxe build/ # hacks - todo fixme
	sed -e 's/^set default-choice .*/set default-choice ubuntu-desktop/' -i build/boot.ipxe
	sed -e 's#http://$${next-server}:8000#http://10.0.2.100:8000#g' -i build/boot.ipxe
	sed -e 's#$${next-server}:/#10.0.2.100:/#g' -i build/boot.ipxe
	DISK_IMAGE=build/desktop-disk.img ./single-host.sh restrict

SERVER_NAME ?= qemu-pxe-demo-server
build/server:
	docker run -d --name $(SERVER_NAME) -p 8000:80 -v ./build:/usr/share/nginx/html:ro nginx:alpine && touch $@
.PHONY: start_server stop_server
start_server: build/server
stop_server: build/server
	-docker stop $(SERVER_NAME)
	-docker rm -f $(SERVER_NAME)
	rm -f build/server

NFS_SERVER_NAME ?= qemu-pxe-demo-nfs-server
NFS_SERVER_IMAGE ?= qemu-pxe-demo-nfs
NFS_DATA_VOLUME ?= qemu-pxe-demo-desktop-iso
build/nfs-server-image: nfs-server.Dockerfile nfs-ganesha.conf nfs-ganesha-entrypoint.sh
	@mkdir -p build
	docker build -t $(NFS_SERVER_IMAGE) -f nfs-server.Dockerfile .
	touch $@

build/desktop-nfs-volume: build/ubuntu-26.04-desktop-amd64.iso | build/nfs-server-image
	docker volume create $(NFS_DATA_VOLUME)
	docker run --rm --entrypoint xorriso -v $(abspath $<):/installer.iso:ro -v $(NFS_DATA_VOLUME):/exports $(NFS_SERVER_IMAGE) -osirrox on -indev /installer.iso -extract / /exports
	touch $@

build/nfs-server: build/nfs-server-image build/desktop-nfs-volume
	docker run -d --name $(NFS_SERVER_NAME) --cap-add DAC_READ_SEARCH -p 30111:111 -p 32049:2049 -p 30048:20048 -v $(NFS_DATA_VOLUME):/exports:ro $(NFS_SERVER_IMAGE)
	touch $@

.PHONY: start_nfs_server stop_nfs_server
start_nfs_server: build/nfs-server
stop_nfs_server:
	-docker stop $(NFS_SERVER_NAME)
	-docker rm -f $(NFS_SERVER_NAME)
	rm -f build/nfs-server

.PHONY: remove_nfs_data
remove_nfs_data: stop_nfs_server
	-docker volume rm $(NFS_DATA_VOLUME)
	rm -f build/desktop-nfs-volume

clean: stop_server remove_nfs_data
	@mkdir -p build
	find build -mindepth 1 -not -path '*.iso' -not -path '*.img' -not -path '*-generic' -delete
	rm -vf build/disk.img build/desktop-disk.img

build/busy-box/.downloaded:
	#tar -xf archive.tar.gz --strip-components=1 -C /target/directory
	mkdir -p build/busy-box
	wget 'https://busybox.net/downloads/busybox-1.38.0.tar.bz2' -qO- | tar -xjf - --strip-components=1 -C build/busy-box
	touch $@

build/busy-box/busybox: build/busy-box/.downloaded
	cat busybox-builder.Dockerfile | docker build -t busybox-builder -
	@#docker build -t ipxe-embedded-docker-image --build-arg CROSS_COMPILER_NAME=$$(./print-cross-compiler-name.sh) ./ipxe-embedded-docker-image
	docker run --rm -it --name busybox-build -v ./build/busy-box:/busy-box --user $$(id -u):$$(id -g) busybox-builder \
	  make -C /busy-box ARCH=amd64 CROSS_COMPILE=x86_64-linux-gnu- defconfig
	sed 's/^CONFIG_TC=.*/CONFIG_TC=n/' -i build/busy-box/.config
	docker run --rm -it --name busybox-build -v ./build/busy-box:/busy-box --user $$(id -u):$$(id -g) busybox-builder \
	  make -j -C /busy-box ARCH=amd64 CROSS_COMPILE=x86_64-linux-gnu- busybox
	@#$(MAKE) -C /busy-box ARCH=arm64 CROSS_COMPILE=aarch64-linux-gnu-
