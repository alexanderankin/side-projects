## running ubuntu VMs with QEmu (and cloud-init)

## todo

* list of apt packages
* remove package checks (for cmd checks), figure out how to communicate packages
* homebrew paths for riscv64, arm (32 bit)

## ubuntu

```shell
apt install ...
```

## macos

```shell
brew install qemu openssl@3

echo -e 'FROM ubuntu:24.04\nRUN apt update && apt install -y cloud-image-utils' | docker build - -t cit
echo -e '#!/usr/bin/env bash\ndocker run --rm -it -w /work -v $PWD:/work cit cloud-localds "$@"' > ~/.local/bin/cloud-localds
chmod +x ~/.local/bin/cloud-localds
```

## isolated nested PXE lab

The lab uses an outer Linux VM for management and an inner, diskless x86 VM as
the PXE client.  The inner VM is attached to a TAP device on a private bridge;
it does **not** use QEMU's `user` network backend, so QEMU does not provide DHCP
on the PXE network.

Start the outer VM, wait for SSH to become available, provision QEMU and the
network tools, then enter it:

```shell
make start_pxe_lab
make provision_pxe_lab
make copy_pxe_server
make ssh_pxe_lab
```

Inside the outer VM, create the isolated network:

```shell
sudo ./pxe-lab-network.sh up "$USER"
```

The resulting isolated topology is:

```text
custom DHCP/TFTP server                 nested QEMU PXE client
network namespace pxe-server            e1000 PXE ROM
192.168.50.1/24                          no disk
          pxens0 -- pxes0 -- pxe-br -- pxe-tap
```

Run the custom DHCP server in the namespace so UDP broadcasts can only use the
private PXE network. Its defaults offer `192.168.50.100`, advertise
`192.168.50.1` as the next server, and advertise `pxelinux.0` as the TFTP boot
file:

```shell
sudo ./pxe-lab-network.sh exec /home/ubuntu/pxe-stuff/bin/pxe-stuff
```

The defaults can be changed with `PXE_SERVER_ADDRESS`, `PXE_OFFER_ADDRESS`,
`PXE_SUBNET_MASK`, `PXE_BOOT_FILE`, and `PXE_LEASE_SECONDS` in the command's
environment.

Run TFTP in a second shell. For example, the scratch TFTP server in this repo
can listen on the real PXE port when run inside the namespace:

```shell
sudo ./pxe-lab-network.sh exec \
  python3 /path/to/tftp.py server --host 192.168.50.1 --port 69 --root /path/to/tftp-root
```

Launch the nested client in a third shell:

```shell
./pxe-client.sh
```

`PXE_CLIENT_DISPLAY=none` disables the curses console. If the outer VM exposes
`/dev/kvm`, `PXE_CLIENT_ACCELERATION=kvm` enables nested acceleration; otherwise
the default `tcg` works without nested-virtualization support. To inspect the
actual exchange independently of either implementation:

```shell
sudo tcpdump -eni pxe-br 'udp port 67 or udp port 68 or udp port 69'
```

Clean up only the lab-owned namespace and interfaces with:

```shell
sudo ./pxe-lab-network.sh down
```
