## running ubuntu VMs with QEmu (and cloud-init)

## ubuntu

```shell
apt install ...
```

## macos

```shell
echo -e 'FROM ubuntu:24.04\nRUN apt update && apt install -y cloud-image-utils' | docker build - -t cit
echo -e '#!/usr/bin/env bash\ndocker run --rm -it -w /work -v $PWD:/work cit cloud-localds "$@"' > ~/.local/bin/cloud-localds
chmod +x ~/.local/bin/cloud-localds
```
