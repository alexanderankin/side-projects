build/resolute-server-cloudimg-amd64.img_: build/.check_wget
	$(shell cd build; wget -N $(IMG_BASE_URL)-amd64.img)
	touch $@

# this must happen every time if you want a clean vm
.PHONY: build/vm_amd64.img
build/vm_amd64.img: build/resolute-server-cloudimg-amd64.img_
	qemu-img create -f qcow2 -b resolute-server-cloudimg-amd64.img -F qcow2 build/vm_amd64.img 20G

.PHONY: clean_vm_amd64
clean_vm_amd64:
	rm -f build/vm_amd64.img

# "Ctrl+a, x" exits the vm
start_vm_amd64: clean_vm build/vm_amd64.img build/seed.iso build/.check-package_qemu-system-x86
	qemu-system-x86_64 \
	  -cpu host \
	  -m 4096 \
	  -smp 2 \
	  -enable-kvm \
	  -drive file=build/vm_amd64.img,format=qcow2,if=virtio \
	  -cdrom build/seed.iso \
	  -netdev user,id=net0,hostfwd=tcp::$(SSH_PORT)-:22,hostfwd=tcp::0-:9090 \
	  -device virtio-net-pci,netdev=net0 \
	  $(UI_OPTIONS)

start_vm_amd64_virtual: clean_vm build/vm_amd64.img build/seed.iso build/.check_qemu-system-x86_64
	qemu-system-x86_64 \
	  -cpu qemu64 \
	  -m 4096 \
	  -smp 2 \
	  -drive file=build/vm_amd64.img,format=qcow2,if=virtio \
	  -cdrom build/seed.iso \
	  -netdev user,id=net0,hostfwd=tcp::$(SSH_PORT)-:22,hostfwd=tcp::0-:9090 \
	  -device virtio-net-pci,netdev=net0 \
	  $(UI_OPTIONS)
