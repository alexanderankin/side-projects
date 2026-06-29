VM_ARM_TYPE_NAME = $($(ARCH_NAMING_TYPE)_arch_arm)
ifneq ($(VM_ARM_TYPE_NAME),)

VM_ARM_IMAGE = $(subst __ARCH__,$(VM_ARM_TYPE_NAME),$(IMG_URL_TEMPLATE))
VM_ARM_IMAGE_FILE = $(lastword $(subst /, ,$(VM_ARM_IMAGE)))

build/$(VM_ARM_IMAGE_FILE)_: build/.check_wget
	$(shell cd build; wget -N $(VM_ARM_IMAGE))
	touch $@

# this must happen every time if you want a clean vm
.PHONY: build/vm_arm_$(NAME).img
build/vm_arm_$(NAME).img: build/$(VM_ARM_IMAGE_FILE)_
	qemu-img create -f qcow2 -b $(VM_ARM_IMAGE_FILE) -F qcow2 build/vm_arm_$(NAME).img 20G

.PHONY: clean_vm_arm
clean_vm_arm:
	rm -f build/vm_arm_$(NAME).img

# "Ctrl+a, x" exits the vm
# todo homebrew path version of this
start_vm_arm: clean_vm build/vm_arm_$(NAME).img build/seed.iso build/.check_qemu-system-arm build/.check-package_qemu-efi-arm
	qemu-system-arm \
	  -machine virt \
	  -cpu cortex-a15 \
	  -bios /usr/share/AAVMF/AAVMF32_CODE.fd \
	  -m 3072 \
	  -smp 2 \
	  -drive file=build/vm_arm_$(NAME).img,format=qcow2,if=virtio \
	  -drive file=build/seed.iso,media=cdrom,file.locking=off \
	  -netdev user,id=net0,hostfwd=tcp::$(SSH_PORT)-:22,hostfwd=tcp::0-:9090 \
	  -device virtio-net-pci,netdev=net0 \
	  $(UI_OPTIONS)

start_vm_arm_brew: clean_vm build/vm_arm_$(NAME).img build/seed.iso build/.check_qemu-system-arm
	rm -f build/edk2-arm-vars.fd
	cp /opt/homebrew/share/qemu/edk2-arm-vars.fd build/edk2-arm-vars.fd
	qemu-system-arm \
	  -machine virt \
	  -cpu cortex-a15 \
	  -drive if=pflash,format=raw,readonly=on,file=/opt/homebrew/share/qemu/edk2-arm-code.fd \
	  -drive if=pflash,format=raw,file=build/edk2-arm-vars.fd \
	  -m 3072 \
	  -smp 2 \
	  -drive file=build/vm_arm_$(NAME).img,format=qcow2,if=virtio \
	  -drive file=build/seed.iso,media=cdrom,file.locking=off \
	  -netdev user,id=net0,hostfwd=tcp::$(SSH_PORT)-:22,hostfwd=tcp::0-:9090 \
	  -device virtio-net-pci,netdev=net0 \
	  $(UI_OPTIONS)

endif
