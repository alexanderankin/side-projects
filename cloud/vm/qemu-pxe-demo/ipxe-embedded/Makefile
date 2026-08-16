.PHONY: all
all: ipxe/src/bin/ipxe.pxe

ipxe/.git/HEAD:
	git clone https://github.com/ipxe/ipxe.git

ipxe/src/embed.ipxe: embed.pxe
	cp $< $@

ipxe/src/bin/ipxe.pxe: ipxe/src/embed.ipxe
	$(MAKE) -C ipxe/src bin/ipxe.pxe EMBED=embed.ipxe
