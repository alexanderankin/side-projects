#!/bin/bash
# standard bash script prefix
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -euxo pipefail  # Debug mode, exit on first error, print commands
full="$(readlink -f "$BASH_SOURCE")"; dir=${full%\/*}; file=${full##*/};


sudo snap install lxd-imagebuilder --classic

# https://www.microsoft.com/software-download/windows11 # multi edition iso
cd Downloads/
ls WindowsIsoImage.iso

sudo lxd-imagebuilder repack-windows WindowsIsoImage.iso win11.lxd.iso

# sudo apt-get install -y --no-install-recommends genisoimage libwin-hivex-perl rsync wimtools # unverified

ls -lh win11.lxd.iso

# lxc delete win11

lxc init win11 --vm --empty
lxc config device override win11 root size=60GiB
lxc config set win11 limits.cpu=4 limits.memory=8GiB
lxc config device add win11 vtpm tpm path=/dev/tpm0
lxc config device add win11 install disk source=$HOME/Downloads/win11.lxd.iso boot.priority=10
lxc start win11 --console=vga

sudo apt-get install -y --no-install-recommends virt-viewer spice-client-gtk

lxc console win11 --type=vga # on every reboot

lxc config device remove win11 install

# Shift+F10 + OOBE\BYPASSNRO # https://superuser.com/a/1882089
# start ms-cxh:localonly # https://www.reddit.com/r/sysadmin/comments/1jp5vln/an_alternative_to_bypass_microsoft_account/
