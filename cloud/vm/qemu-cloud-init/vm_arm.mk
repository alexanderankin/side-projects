build/noble-server-cloudimg-armhf.img_: build/.check_wget
	$(shell cd build; wget -N https://cloud-images.ubuntu.com/noble/current/noble-server-cloudimg-armhf.img)
	touch $@

# this must happen every time if you want a clean vm
.PHONY: build/vm_arm.img
build/vm_arm.img: build/noble-server-cloudimg-armhf.img_
	qemu-img create -f qcow2 -b noble-server-cloudimg-armhf.img -F qcow2 build/vm_arm.img 20G

.PHONY: clean_vm_arm
clean_vm_arm:
	rm -f build/vm_arm.img

# "Ctrl+a, x" exits the vm
# todo homebrew path version of this
start_vm_arm: build/vm_arm.img build/seed.iso build/.check_qemu-system-arm build/.check-package_qemu-efi-arm
	qemu-system-arm \
	  -machine virt \
	  -cpu cortex-a15 \
	  -bios /usr/share/AAVMF/AAVMF32_CODE.fd \
	  -m 3072 \
	  -smp 2 \
	  -drive file=build/vm_arm.img,format=qcow2,if=virtio \
	  -cdrom build/seed.iso \
	  -netdev user,id=net0,hostfwd=tcp::2222-:22,hostfwd=tcp::9090-:9090 \
	  -device virtio-net-pci,netdev=net0 \
	  -display none \
	  -serial mon:stdio
