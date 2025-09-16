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
