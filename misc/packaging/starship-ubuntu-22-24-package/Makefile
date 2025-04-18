SHELL := /bin/bash

.PHONY: all
all: build/deb.zip

# make build directory
build/.exists:
	mkdir -p ./build && touch ./build/.exists

# download helper program
build/simple-deb-4j-all.jar: build/.exists
	cd build && wget -N https://github.com/alexanderankin/simple-deb-4j/releases/download/v0.0.4/simple-deb-4j-all.jar

# download release information
build/releases: build/.exists
	cd build && wget -N https://api.github.com/repos/starship/starship/releases

# parse latest release
build/latest_release: build/releases
	cat build/releases | jq -r .[0].name | tr -d 'v' > $@

# build config file
build/config.yaml: config.yaml build/.exists
	cat config.yaml | yaml2json | jq '.meta.version = "'$(shell cat ./build/latest_release)'"' | json2yaml > $@

# download latest release
build/starship-x86_64-unknown-linux-gnu.tar.gz: build/latest_release
	# http://archive.ubuntu.com/ubuntu/pool/universe/s/starship/starship_1.22.1-2_amd64.deb
	cd build && wget -N https://github.com/starship/starship/releases/download/v$(shell cat ./build/latest_release)/starship-x86_64-unknown-linux-gnu.tar.gz

# repackage release
build/deb.zip: build/simple-deb-4j-all.jar build/config.yaml
	[[ -f ~/.sdkman/bin/sdkman-init.sh ]] && . ~/.sdkman/bin/sdkman-init.sh; java -jar build/simple-deb-4j-all.jar b -c build/config.yaml -o build/
