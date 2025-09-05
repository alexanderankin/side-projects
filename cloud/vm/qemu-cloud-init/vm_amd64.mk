build/noble-server-cloudimg-amd64.img_: build/.check_wget
	$(shell cd build; wget -N https://cloud-images.ubuntu.com/noble/current/noble-server-cloudimg-amd64.img)
	touch $@

# this must happen every time if you want a clean vm
.PHONY: build/vm_amd64.img
build/vm_amd64.img: build/noble-server-cloudimg-amd64.img_
	qemu-img create -f qcow2 -b noble-server-cloudimg-amd64.img -F qcow2 build/vm_amd64.img

.PHONY: clean_vm_amd64
clean_vm_amd64:
	rm -f build/vm_amd64.img

# "Ctrl+a, x" exits the vm
start_vm_amd64: build/vm_amd64.img build/seed.iso build/.check-package_qemu-system-x86
	qemu-system-x86_64 \
	  -cpu host \
	  -m 4096 \
	  -smp 2 \
	  -enable-kvm \
	  -drive file=build/vm_amd64.img,format=qcow2,if=virtio \
	  -cdrom build/seed.iso \
	  -netdev user,id=net0,hostfwd=tcp::2222-:22,hostfwd=tcp::9090-:9090 \
	  -device virtio-net-pci,netdev=net0 \
	  -display none \
	  -serial mon:stdio
