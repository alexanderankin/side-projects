ipxe/src/bin/ipxe.pxe: ipxe/src/embed.ipxe ipxe-embedded-docker-image/Dockerfile
	docker build -t ipxe-embedded-docker-image --build-arg CROSS_COMPILER_NAME=$$(./print-cross-compiler-name.sh $(target_arch)) ./ipxe-embedded-docker-image
	docker run --rm -it --name ipxe-embedded-docker \
	  -v ./ipxe:/ipxe \
	  --user $$(id -u):$$(id -g) \
	  ipxe-embedded-docker-image \
	  make -j -C /ipxe/src $$(./print-cross-compiler-ipxe-target.sh $(target_arch)) EMBED=embed.ipxe CROSS_COMPILE=$$(./print-cross-compiler-prefix.sh $(target_arch))-linux-gnu-
	if [ $@ != "ipxe/src/$$(./print-cross-compiler-ipxe-target.sh $(target_arch))" ]; then cp "ipxe/src/$$(./print-cross-compiler-ipxe-target.sh $(target_arch))" $@; fi
