VM_ARM64_TYPE_NAME = $($(ARCH_NAMING_TYPE)_arch_arm64)
ifneq ($(VM_ARM64_TYPE_NAME),)

VM_ARM64_IMAGE = $(subst __ARCH__,$(VM_ARM64_TYPE_NAME),$(IMG_URL_TEMPLATE))
VM_ARM64_IMAGE_FILE = $(lastword $(subst /, ,$(VM_ARM64_IMAGE)))

build/$(VM_ARM64_IMAGE_FILE)_: build/.check_wget
	$(shell cd build; wget -N $(VM_ARM64_IMAGE))
	touch $@

# this must happen every time if you want a clean vm
.PHONY: build/vm_arm64_$(NAME).img
build/vm_arm64_$(NAME).img: build/$(VM_ARM64_IMAGE_FILE)_
	qemu-img create -f qcow2 -b $(VM_ARM64_IMAGE_FILE) -F qcow2 build/vm_arm64_$(NAME).img 20G

.PHONY: clean_vm_arm64
clean_vm_arm64:
	rm -f build/vm_arm64_$(NAME).img

# "Ctrl+a, x" exits the vm
start_vm_arm64: clean_vm build/vm_arm64_$(NAME).img build/seed.iso build/.check_qemu-system-aarch64
	qemu-system-aarch64 \
	  -machine virt,highmem=on \
	  -accel hvf \
	  -cpu host \
	  -m 4096 \
	  -smp 2 \
	  -bios /opt/homebrew/share/qemu/edk2-aarch64-code.fd \
	  -drive file=build/vm_arm64_$(NAME).img,format=qcow2,if=virtio \
	  -drive file=build/seed.iso,media=cdrom,file.locking=off \
	  -netdev user,id=net0,hostfwd=tcp::$(SSH_PORT)-:22,hostfwd=tcp::0-:9090 \
	  -device virtio-net-pci,netdev=net0 \
	  $(UI_OPTIONS)

# cpu max is slow because exposes difficult to emulate features
# options: cortex-a57, cortex-a72, cortex-a76, neoverse-n1

start_vm_arm64_virtual: clean_vm build/vm_arm64_$(NAME).img build/seed.iso # build/.check-package_qemu-system-arm build/.check-package_qemu-efi-aarch64
	qemu-system-aarch64 \
	  -machine virt,highmem=on \
	  -cpu neoverse-n1 \
	  -m 4096 \
	  -smp 2 \
	  -bios /usr/share/qemu-efi-aarch64/QEMU_EFI.fd \
	  -drive file=build/vm_arm64_$(NAME).img,format=qcow2,if=virtio \
	  -drive file=build/seed.iso,media=cdrom,file.locking=off \
	  -netdev user,id=net0,hostfwd=tcp::$(SSH_PORT)-:22,hostfwd=tcp::0-:9090 \
	  -device virtio-net-pci,netdev=net0 \
	  $(UI_OPTIONS)


ifneq ($(filter $(shell uname -m),aarch64 arm64),)
start_vm_arm64_best: start_vm_arm64
else
start_vm_arm64_best: start_vm_arm64_virtual
endif

endif
