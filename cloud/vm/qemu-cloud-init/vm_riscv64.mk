build/noble-server-cloudimg-riscv64.img_: build/.check_wget
	$(shell cd build; wget -N https://cloud-images.ubuntu.com/noble/current/noble-server-cloudimg-riscv64.img)
	touch $@

# this must happen every time if you want a clean vm
.PHONY: build/vm_riscv64.img
build/vm_riscv64.img: build/noble-server-cloudimg-riscv64.img_
	qemu-img create -f qcow2 -b noble-server-cloudimg-riscv64.img -F qcow2 build/vm_riscv64.img

.PHONY: clean_vm_riscv64
clean_vm_riscv64:
	rm -f build/vm_riscv64.img

# "Ctrl+a, x" exits the vm
# todo homebrew version of this
start_vm_riscv64: build/vm_riscv64.img build/seed.iso build/.check_qemu-system-riscv64 build/.check-package_qemu-system-misc build/.check-package_u-boot-qemu build/.check-package_opensbi
	qemu-system-riscv64 \
	  -machine virt \
	  -cpu rv64 \
	  -bios /usr/lib/riscv64-linux-gnu/opensbi/generic/fw_jump.elf \
	  -kernel /usr/lib/u-boot/qemu-riscv64_smode/uboot.elf \
	  -m 3072 \
	  -smp 2 \
	  -drive file=build/vm_riscv64.img,format=qcow2,if=virtio \
	  -drive file=build/seed.iso,format=raw,if=virtio \
	  -netdev user,id=net0,hostfwd=tcp::2222-:22,hostfwd=tcp::9090-:9090 \
	  -device virtio-net-pci,netdev=net0 \
	  -display none \
	  -serial mon:stdio
