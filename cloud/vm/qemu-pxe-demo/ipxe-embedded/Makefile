.PHONY: all
all: ipxe/src/bin/ipxe.pxe ipxe/src/bin-x86_64-efi/ipxe.efi

ipxe/.git/HEAD:
	git clone https://github.com/ipxe/ipxe.git

ipxe/src/embed.ipxe: embed.pxe | ipxe/.git/HEAD
	cp $< $@

ipxe/src/bin/ipxe.pxe: ipxe/src/embed.ipxe
	$(MAKE) -C ipxe/src bin/ipxe.pxe EMBED=embed.ipxe

ipxe/src/bin-x86_64-efi/ipxe.efi: ipxe/src/embed.ipxe
	$(MAKE) -C ipxe/src bin-x86_64-efi/ipxe.efi EMBED=embed.ipxe
