VM_RISCV64_TYPE_NAME = $($(ARCH_NAMING_TYPE)_arch_riscv64)
ifneq ($(VM_RISCV64_TYPE_NAME),)

VM_RISCV64_IMAGE = $(subst __ARCH__,$(VM_RISCV64_TYPE_NAME),$(IMG_URL_TEMPLATE))
VM_RISCV64_IMAGE_FILE = $(lastword $(subst /, ,$(VM_RISCV64_IMAGE)))

build/$(VM_RISCV64_IMAGE_FILE)_: build/.check_wget
	$(shell cd build; wget -N $(VM_RISCV64_IMAGE))
	touch $@

# this must happen every time if you want a clean vm
.PHONY: build/vm_riscv64_$(NAME).img
build/vm_riscv64_$(NAME).img: build/$(VM_RISCV64_IMAGE_FILE)_
	qemu-img create -f qcow2 -b $(VM_RISCV64_IMAGE_FILE) -F qcow2 build/vm_riscv64_$(NAME).img 20G

.PHONY: clean_vm_riscv64
clean_vm_riscv64:
	rm -f build/vm_riscv64_$(NAME).img

# "Ctrl+a, x" exits the vm
# todo homebrew version of this
start_vm_riscv64: clean_vm build/vm_riscv64_$(NAME).img build/seed.iso build/.check_qemu-system-riscv64 build/.check-package_qemu-system-misc build/.check-package_u-boot-qemu build/.check-package_opensbi
	qemu-system-riscv64 \
	  -machine virt \
	  -cpu rv64 \
	  -bios /usr/lib/riscv64-linux-gnu/opensbi/generic/fw_dynamic.bin \
	  -kernel /usr/lib/u-boot/qemu-riscv64_smode/uboot.elf \
	  -m 3072 \
	  -smp 2 \
	  -drive file=build/vm_riscv64_$(NAME).img,format=qcow2,if=virtio \
	  -drive file=build/seed.iso,format=raw,if=virtio,file.locking=off \
	  -netdev user,id=net0,hostfwd=tcp::$(SSH_PORT)-:22,hostfwd=tcp::0-:9090 \
	  -device virtio-net-pci,netdev=net0 \
	  $(UI_OPTIONS)

endif
