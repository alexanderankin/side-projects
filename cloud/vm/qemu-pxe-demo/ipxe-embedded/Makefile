IPXE_COMMIT := e6d0a97c05d238c17eeae5116cb6e9c0fc9fdb56

.PHONY: all prepare ipxe/src/bin/ipxe.pxe ipxe/src/bin-x86_64-efi/ipxe.efi
all: ipxe/src/bin/ipxe.pxe ipxe/src/bin-x86_64-efi/ipxe.efi

ipxe/.git/HEAD:
	git clone https://github.com/ipxe/ipxe.git

prepare: ipxe/.git/HEAD
	git -C ipxe cat-file -e $(IPXE_COMMIT)^{commit} || git -C ipxe fetch origin $(IPXE_COMMIT)
	git -C ipxe checkout --detach $(IPXE_COMMIT)

# Use the same bootstrap for embedded local builds and external autoexec.ipxe.
ipxe/src/embed.ipxe: embed.pxe | ipxe/.git/HEAD
	cp $< $@

ipxe/src/bin/ipxe.pxe: prepare ipxe/src/embed.ipxe
	$(MAKE) -C ipxe/src bin/ipxe.pxe EMBED=embed.ipxe

ipxe/src/bin-x86_64-efi/ipxe.efi: prepare ipxe/src/embed.ipxe
	$(MAKE) -C ipxe/src bin-x86_64-efi/ipxe.efi EMBED=embed.ipxe
