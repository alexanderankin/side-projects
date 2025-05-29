.PHONY: all
all: build/seed.iso

# checks
check_%: $(CHECK_PREFIX)%
build/.check_%:
	@mkdir -p build
	@command -v $* >/dev/null 2>&1 || { echo "$* not found in PATH" >&2; exit 1; }
	@echo "$* found"
	@touch $@

build/.dependencies: build/.check_mkpasswd build/.check_cloud-localds

build/user-data: build/.dependencies
	./create-user-data.sh > $@

build/seed.iso: build/user-data
	cloud-localds $@ build/user-data

clean:
	find build/ -type f -not -name '*cloudimg*.img*' -delete

build/noble-server-cloudimg-amd64.img_: build/.check_wget
	$(shell cd build; wget -N https://cloud-images.ubuntu.com/noble/current/noble-server-cloudimg-amd64.img)
	touch $@

# this must happen every time if you want a clean vm
.PHONY: build/vm.img
build/vm.img: build/noble-server-cloudimg-amd64.img_
	$(shell cd build; cp noble-server-cloudimg-amd64.img vm.img)

# "Ctrl+a, x" exits the vm
start_vm: build/vm.img build/seed.iso
	qemu-system-x86_64 \
	  -cpu host \
	  -m 4096 \
	  -smp 2 \
	  -enable-kvm \
	  -drive file=build/vm.img,format=qcow2,if=virtio \
	  -cdrom build/seed.iso \
	  -netdev user,id=net0,hostfwd=tcp::2222-:22 \
	  -device virtio-net-pci,netdev=net0 \
	  -display none \
	  -serial mon:stdio

#	  # from vagrant:
#	  -netdev tap,fd=33,vhost=true,vhostfd=35,id=hostua-net-0 \
#	  -device virtio-net-pci,netdev=hostua-net-0,id=ua-net-0,mac=52:54:00:01:01:01,bus=pci.0,addr=0x5 \
#	  -netdev tap,fd=36,vhost=true,vhostfd=37,id=hostua-net-1 \
#	  -device virtio-net-pci,netdev=hostua-net-1,id=ua-net-1,mac=52:54:00:01:01:02,bus=pci.0,addr=0x6 \
#
#	  # adapted from vagrant:
#	  -netdev tap,id=net1,ifname=tap0 \
#	  -device virtio-net-pci,netdev=net1 \
