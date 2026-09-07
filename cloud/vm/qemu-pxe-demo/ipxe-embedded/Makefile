.PHONY: all
all: ipxe/src/bin/ipxe.pxe

ipxe/.git/HEAD:
	git clone https://github.com/ipxe/ipxe.git

ipxe/src/embed.ipxe: embed.pxe ipxe/.git/HEAD
	cp $< $@

# our kernel
kernel_name := $(or $(KERNEL_NAME),$(shell uname --kernel-name))
$(info ipxe-embedded/Makefile: kernel_name is $(kernel_name))

# our arch
arch := $(or $(NORMALIZED_ARCH),$(shell uname -m))
ifneq ($(filter $(arch),amd64 x86_64),)
  normalized_arch := amd64
else ifneq ($(filter $(arch),arm64 aarch64),)
  normalized_arch := arm64
else
  $(error normalized_arch $(arch) is not supported)
endif
$(info ipxe-embedded/Makefile: normalized_arch is $(normalized_arch))

# target arch
target_arch := $(or $(TARGET_ARCH),amd64)
valid_target_arches := native amd64 arm64
ifeq ($(filter $(target_arch),$(valid_target_arches)),)
  $(error target_arch $(target_arch) not in $(valid_target_arches))
endif
$(info ipxe-embedded/Makefile: target_arch is $(target_arch))

# switch on our kernel
ifeq ($(kernel_name),Linux)
	include ipxe-embedded.linux.mk
else
	include ipxe-embedded.non-linux.mk
endif
