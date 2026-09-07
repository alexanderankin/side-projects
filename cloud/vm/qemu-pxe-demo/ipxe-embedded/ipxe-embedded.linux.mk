ipxe/src/bin/ipxe.pxe: ipxe/src/embed.ipxe
	$(MAKE) -C ipxe/src bin/ipxe.pxe EMBED=embed.ipxe
