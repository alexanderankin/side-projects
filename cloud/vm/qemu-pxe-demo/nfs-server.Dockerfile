FROM ubuntu:26.04

RUN apt-get update \
 && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends netbase nfs-ganesha nfs-ganesha-vfs xorriso \
 && rm -rf /var/lib/apt/lists/*

COPY nfs-ganesha.conf /etc/ganesha/ganesha.conf
COPY nfs-ganesha-entrypoint.sh /usr/local/sbin/nfs-ganesha-entrypoint

RUN mkdir -p /run/ganesha \
 && chmod 0755 /usr/local/sbin/nfs-ganesha-entrypoint

EXPOSE 2049/tcp
ENTRYPOINT ["/usr/local/sbin/nfs-ganesha-entrypoint"]
