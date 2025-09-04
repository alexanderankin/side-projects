#!/bin/bash
# standard bash script prefix
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -euxo pipefail  # Debug mode, exit on first error, print commands
full="$(readlink -f "$BASH_SOURCE")"; dir=${full%\/*}; file=${full##*/};


# https://ubuntu.com/tutorials/how-to-launch-an-instantly-functional-linux-desktop-vm-with-lxd#2-initiate-an-ubuntu-desktop-vm

lxd --version || {
  sudo snap install lxd
  sudo lxd init --minimal
}

groups | grep lxd -q || {
  sudo usermod -aG lxd $USER
  echo "need to relog to become part of lxd group"
  exit 1
}

lxc launch images:ubuntu/22.04/desktop ubuntu --vm -c limits.cpu=4 -c limits.memory=4GiB --console=vga
