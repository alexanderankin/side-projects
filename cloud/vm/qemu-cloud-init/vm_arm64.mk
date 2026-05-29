build/resolute-server-cloudimg-arm64.img_: build/.check_wget
	$(shell cd build; wget -N $(IMG_BASE_URL)-arm64.img)
	touch $@

# this must happen every time if you want a clean vm
.PHONY: build/vm_arm64.img
build/vm_arm64.img: build/resolute-server-cloudimg-arm64.img_
	qemu-img create -f qcow2 -b resolute-server-cloudimg-arm64.img -F qcow2 build/vm_arm64.img 20G

.PHONY: clean_vm_arm64
clean_vm_arm64:
	rm -f build/vm_arm64.img

# "Ctrl+a, x" exits the vm
start_vm_arm64: clean_vm build/vm_arm64.img build/seed.iso build/.check_qemu-system-aarch64
	qemu-system-aarch64 \
	  -machine virt,highmem=on \
	  -accel hvf \
	  -cpu host \
	  -m 4096 \
	  -smp 2 \
	  -bios /opt/homebrew/share/qemu/edk2-aarch64-code.fd \
	  -drive file=build/vm_arm64.img,format=qcow2,if=virtio \
	  -drive file=build/seed.iso,media=cdrom,file.locking=off \
	  -netdev user,id=net0,hostfwd=tcp::$(SSH_PORT)-:22,hostfwd=tcp::0-:9090 \
	  -device virtio-net-pci,netdev=net0 \
	  $(UI_OPTIONS)

# cpu max is slow because exposes difficult to emulate features
# options: cortex-a57, cortex-a72, cortex-a76, neoverse-n1

start_vm_arm64_virtual: clean_vm build/vm_arm64.img build/seed.iso build/.check-package_qemu-system-arm build/.check-package_qemu-efi-aarch64
	qemu-system-aarch64 \
	  -machine virt,highmem=on \
	  -cpu neoverse-n1 \
	  -m 4096 \
	  -smp 2 \
	  -bios /usr/share/qemu-efi-aarch64/QEMU_EFI.fd \
	  -drive file=build/vm_arm64.img,format=qcow2,if=virtio \
	  -drive file=build/seed.iso,media=cdrom,file.locking=off \
	  -netdev user,id=net0,hostfwd=tcp::$(SSH_PORT)-:22,hostfwd=tcp::0-:9090 \
	  -device virtio-net-pci,netdev=net0 \
	  $(UI_OPTIONS)


ifeq ($(shell uname -m), aarch64)
start_vm_arm64_best: start_vm_arm64
else
start_vm_arm64_best: start_vm_arm64_virtual
endif
