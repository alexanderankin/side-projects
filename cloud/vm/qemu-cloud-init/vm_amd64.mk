VM_AMD64_TYPE_NAME = $($(ARCH_NAMING_TYPE)_arch_amd64)
ifneq ($VM_AMD64_TYPE_NAME,)

VM_AMD64_IMAGE = $(subst __ARCH__,$(VM_AMD64_TYPE_NAME),$(IMG_URL_TEMPLATE))
VM_AMD64_IMAGE_FILE = $(lastword $(subst /, ,$(VM_AMD64_IMAGE)))

build/$(VM_AMD64_IMAGE_FILE)_: build/.check_wget
	$(shell cd build; wget -N $(VM_AMD64_IMAGE))
	touch $@

# this must happen every time if you want a clean vm
.PHONY: build/vm_amd64_$(NAME).img
build/vm_amd64_$(NAME).img: build/$(VM_AMD64_IMAGE_FILE)_
	qemu-img create -f qcow2 -b $(VM_AMD64_IMAGE_FILE) -F qcow2 build/vm_amd64_$(NAME).img 20G

.PHONY: clean_vm_amd64
clean_vm_amd64:
	rm -f build/vm_amd64_$(NAME).img

# "Ctrl+a, x" exits the vm
start_vm_amd64: clean_vm build/vm_amd64_$(NAME).img build/seed.iso build/.check-package_qemu-system-x86
	qemu-system-x86_64 \
	  -cpu host \
	  -m 4096 \
	  -smp 2 \
	  -enable-kvm \
	  -drive file=build/vm_amd64_$(NAME).img,format=qcow2,if=virtio \
	  -drive file=build/seed.iso,media=cdrom,file.locking=off \
	  -netdev user,id=net0,hostfwd=tcp::$(SSH_PORT)-:22,hostfwd=tcp::0-:9090 \
	  -device virtio-net-pci,netdev=net0 \
	  $(UI_OPTIONS)

start_vm_amd64_virtual: clean_vm build/vm_amd64_$(NAME).img build/seed.iso build/.check_qemu-system-x86_64
	qemu-system-x86_64 \
	  -cpu qemu64 \
	  -m 4096 \
	  -smp 2 \
	  -drive file=build/vm_amd64_$(NAME).img,format=qcow2,if=virtio \
	  -drive file=build/seed.iso,media=cdrom,file.locking=off \
	  -netdev user,id=net0,hostfwd=tcp::$(SSH_PORT)-:22,hostfwd=tcp::0-:9090 \
	  -device virtio-net-pci,netdev=net0 \
	  $(UI_OPTIONS)

ifneq ($(filter $(shell uname -m),amd64 x86_64),)
start_vm_amd64_best: start_vm_amd64
else
start_vm_amd64_best: start_vm_amd64_virtual
endif

endif
