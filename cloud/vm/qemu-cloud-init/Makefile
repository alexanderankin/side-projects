.PHONY: all
all: build/seed.iso

UBUNTU_CODENAME ?= resolute
IMG_BASE_URL    ?= https://cloud-images.ubuntu.com/$(UBUNTU_CODENAME)/current/$(UBUNTU_CODENAME)-server-cloudimg
SSH_PORT        ?= 2222
UI_MODE         ?= serial

ifeq ($(UI_MODE),serial)
  UI_OPTIONS ?= -display none -serial mon:stdio

else ifeq ($(UI_MODE),headless)
  ifndef NAME
    $(error NAME must be defined when UI_MODE=headless)
  endif
  UI_OPTIONS ?= -daemonize -pidfile build/$(NAME) -display none

else
  $(error invalid UI_MODE=$(UI_MODE); expected serial or headless)
endif

# checks
check_%: $(CHECK_PREFIX)%
build/.check_%:
	@mkdir -p build
	@command -v $* >/dev/null 2>&1 || { echo "$* not found in PATH" >&2; exit 1; }
	@echo "$* found"
	@touch $@

check-package_%: $(CHECK_UP_PREFIX)%
build/.check-package_%:
	@mkdir -p build
	@dpkg -L $* >/dev/null 2>&1 || { echo "$* not found in dpkg -L" >&2; exit 1; }
	@echo "$* found"
	@touch $@

build/user-data: build/.check_openssl build/.check_cloud-localds ./create-user-data.sh
	./create-user-data.sh > $@

build/seed.iso: build/user-data
	cloud-localds $@ build/user-data

clean:
	find build/ -type f -not -name '*cloudimg*.img*' -delete

.PHONY: clean_vm
clean_vm:
ifndef NAME
	$(error NAME must be defined for clean_vm)
endif
ifneq ($(wildcard build/$(NAME)),)
	pid="$$(cat build/$(NAME))" && kill $$pid && { wait $$pid || echo "no need to wait"; } && rm -f build/$(NAME)
else
	@:
endif

include vm_*.mk

start_all:
	if [ -f build/amd64 ]; then :; else UI_MODE='headless' NAME=amd64 SSH_PORT=2022 $(MAKE) start_vm_amd64_best; fi
	if [ -f build/arm64 ]; then :; else UI_MODE='headless' NAME=arm64 SSH_PORT=2023 $(MAKE) start_vm_arm64_best; fi
	if [ -f build/arm ]; then :; else UI_MODE='headless' NAME=arm SSH_PORT=2024 $(MAKE) start_vm_arm; fi
	if [ -f build/riscv64 ]; then :; else UI_MODE='headless' NAME=riscv64 SSH_PORT=2025 $(MAKE) start_vm_riscv64; fi

restart_all:
	UI_MODE='headless' NAME=amd64 SSH_PORT=2022 $(MAKE) start_vm_amd64_virtual
	UI_MODE='headless' NAME=arm64 SSH_PORT=2023 $(MAKE) start_vm_arm64_virtual
	UI_MODE='headless' NAME=arm SSH_PORT=2024 $(MAKE) start_vm_arm
	UI_MODE='headless' NAME=riscv64 SSH_PORT=2025 $(MAKE) start_vm_riscv64

clean_all:
	if [ -f build/amd64 ]; then pid=$$(cat build/amd64); kill $$pid && { wait $$pid || echo "wait exited"; }; fi
	if [ -f build/arm64 ]; then pid=$$(cat build/arm64); kill $$pid && { wait $$pid || echo "wait exited"; }; fi
	if [ -f build/arm ]; then pid=$$(cat build/arm); kill $$pid && { wait $$pid || echo "wait exited"; }; fi
	if [ -f build/riscv64 ]; then pid=$$(cat build/riscv64); kill $$pid && { wait $$pid || echo "wait exited"; }; fi

#	  # from vagrant:
#	  -netdev tap,fd=33,vhost=true,vhostfd=35,id=hostua-net-0 \
#	  -device virtio-net-pci,netdev=hostua-net-0,id=ua-net-0,mac=52:54:00:01:01:01,bus=pci.0,addr=0x5 \
#	  -netdev tap,fd=36,vhost=true,vhostfd=37,id=hostua-net-1 \
#	  -device virtio-net-pci,netdev=hostua-net-1,id=ua-net-1,mac=52:54:00:01:01:02,bus=pci.0,addr=0x6 \
#
#	  # adapted from vagrant:
#	  -netdev tap,id=net1,ifname=tap0 \
#	  -device virtio-net-pci,netdev=net1 \
