FROM ubuntu:26.04
ENV DEBIAN_FRONTEND=noninteractive LIBGUESTFS_BACKEND=direct
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3-guestfs libguestfs-tools linux-image-generic busybox-static \
    qemu-utils e2fsprogs ca-certificates gzip cpio \
    && rm -rf /var/lib/apt/lists/*
COPY prepare.py /usr/local/bin/prepare-desktop-image
