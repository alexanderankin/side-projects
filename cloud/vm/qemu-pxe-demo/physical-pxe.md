# DHCP + TFTP + HTTP on a physical LAN

`single-host.sh:31` implements DHCP/TFTP inside QEMU's user network. For physical
clients, run dnsmasq on the server's LAN interface and nginx for HTTP downloads;
the clients boot from their NIC firmware, so there is no replacement QEMU command.

The supplied `dnsmasq-pxe.conf` and `nginx-pxe.conf` assume:

- Server statically configured as `192.168.50.2/24` on its provisioning LAN.
- Router and existing DNS resolver `192.168.50.1`.
- DHCP pool `192.168.50.100`–`192.168.50.199`.
- Clients and server on the same LAN/VLAN, with dnsmasq the sole DHCP allocator.

Replace these values in both configs. `listen-address` with `bind-interfaces`
selects the server's LAN address without requiring an interface name. The DHCP
pool must match that LAN's subnet.

`port=0` disables dnsmasq's DNS service;
clients use the explicitly advertised resolver. On an isolated network without a
router, use `dhcp-option=option:router` to suppress the default gateway option and
advertise a DNS resolver only if one is reachable. Disable the router's DHCP for
this LAN or use a dedicated provisioning VLAN before starting this configuration.
If another DHCP allocator must remain, use a separate proxy-DHCP setup instead;
do not run this address pool alongside it.

From this directory, prepare the legacy BIOS assets without starting QEMU or the
demo's Python HTTP server:

```sh
make ipxe_pxe ipxe_scripts ubuntu
sudo install -d -m 0755 /srv/pxe
sudo cp -a build/. /srv/pxe/
sudo find /srv/pxe -type d -exec chmod 0755 {} +
sudo find /srv/pxe -type f -exec chmod 0644 {} +
```

Copying `build/` also copies any VM disks present there. Remove those copies from
`/srv/pxe`; publish only the intended boot scripts, kernels, initrds, and images.
Always copy the source `boot.ipxe` and `arch.ipxe` over any scripts left by an
offline QEMU run:

```sh
make ipxe_scripts
sudo install -m 0644 build/autoexec.ipxe build/boot.ipxe build/arch.ipxe /srv/pxe/
```

`make ipxe_pxe` builds both `build/ipxe.pxe` for legacy BIOS and
`build/ipxe.efi` for x86-64 UEFI, pinned to commit
`e6d0a97c05d238c17eeae5116cb6e9c0fc9fdb56`, embedding `ipxe-embedded/embed.ipxe`.
`make ipxe_scripts` copies `ipxe-embedded/embed.ipxe` to `build/autoexec.ipxe`
alongside the menu scripts; publish this bootstrap alongside both binaries.
Updating the embedded copy requires rebuilding the binaries; the external copy
can also be used by the official signed UEFI release without recompilation.
The copy above installs all three. `ipxe.pxe` cannot boot UEFI firmware. This example assumes Secure Boot is disabled; enabling it
requires a trusted, signed boot chain. Other firmware architectures need their
own binaries and DHCP architecture matches.

Install the edited configs and validate before restarting:

```sh
sudo install -m 0644 dnsmasq-pxe.conf /etc/dnsmasq.d/pxe.conf
sudo install -m 0644 nginx-pxe.conf /etc/nginx/conf.d/pxe.conf
sudo dnsmasq --test
sudo nginx -t
sudo systemctl restart dnsmasq
sudo systemctl reload nginx
```

Ensure `/etc/dnsmasq.conf` actually includes `/etc/dnsmasq.d/` and has no conflicting
DHCP/TFTP settings. Stop any demo Python server occupying port 8000. If nginx is
not running yet, start it instead of reloading. Distro AppArmor/SELinux policies
may need to permit dnsmasq and nginx to read `/srv/pxe`.

Permit incoming UDP 67 (DHCP), UDP 69 (TFTP), UDP 50000–50100 (TFTP transfers), and
TCP 8000 (HTTP) from the provisioning LAN. DHCP replies must reach client UDP 68.
Check `curl -f http://192.168.50.2:8000/boot.ipxe` from another LAN host and monitor
`journalctl -u dnsmasq -f` while a client performs a network boot.

Boot flow: firmware obtains a lease and downloads `ipxe.pxe` or `ipxe.efi` over
TFTP. The local binaries have the DHCP/TFTP bootstrap embedded; UEFI also probes
for external `autoexec.ipxe`. Both copies run DHCP and chain
`tftp://${next-server}/boot.ipxe`. The explicit next-server IP supports the
existing TFTP script/kernel URLs, while nginx serves the image/ISO downloads on
port 8000. QEMU continues to download `ipxe.pxe` and use its embedded bootstrap.

## Reusing autoexec with Secure Boot later

For now, leave Secure Boot disabled and publish the locally built binaries and
external script:

```sh
make ipxe_pxe ipxe_scripts
sudo install -m 0644 build/ipxe.pxe build/ipxe.efi \
  build/autoexec.ipxe build/boot.ipxe build/arch.ipxe /srv/pxe/
```

When switching to the official signed release, extract its `ipxeboot.tar.gz`
archive into `/srv/pxe`, preserving the shim and binary directory structure.
Copy the same external script alongside the signed x86-64 binaries:

```sh
make ipxe_scripts
sudo install -m 0644 build/autoexec.ipxe /srv/pxe/ipxeboot/x86_64-sb/autoexec.ipxe
```

Replace the UEFI firmware boot line in dnsmasq with:

```ini
dhcp-boot=tag:!ipxe,tag:efi64,ipxeboot/x86_64-sb/ipxe-shim.efi,,192.168.50.2
```

Keep the BIOS line and the `tag:ipxe,autoexec.ipxe` line. The bootstrap continues
to load the root-level `boot.ipxe`, so the menu and asset paths stay the same.
The script is reusable, but the OS boot chain still needs the distribution's
signed shim and a signed kernel before enabling Secure Boot. See the
[iPXE Secure Boot instructions](https://ipxe.org/secboot), including the Linux
`shim` command. The local builds are unsigned; a source commit pin does not
provide a firmware-trusted signature.

## Adapt the installer before deploying physical disks

This config provides boot services; the demo's default `ubuntu` menu entry still
assumes `/dev/vda` and `/dev/vda1` in `deploy-initrd/deploy-image` and `boot.ipxe`.
Set the intended physical disk and root partition in both places, rebuild
`build/deploy-overlay.cpio.gz`, and copy the updated overlay/menu into `/srv/pxe`.
Confirm the image's partition layout and include the physical NIC/storage drivers
in the initrd. The deployment script overwrites the selected disk when the image
version differs.

For the ISO installer menu entry, also build and publish
`build/installer-vmlinuz`, `build/installer-initrd`,
`build/ubuntu-26.04-live-server-amd64.iso`, `build/autoinstall/user-data`, and
`build/autoinstall/meta-data`. Review autoinstall storage selection and replace
the demo credentials before deployment; this path performs an unattended install.

References: [dnsmasq manual](https://thekelleys.org.uk/dnsmasq/docs/dnsmasq-man.html)
and [iPXE chainloading](https://ipxe.org/howto/chainloading).
