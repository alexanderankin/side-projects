SHELL := /bin/bash

.PHONY: all
all: build/deb.zip

# make build directory
build/.exists:
	mkdir -p ./build && touch ./build/.exists

# download helper program
build/simple-deb-4j-all.jar: build/.exists
	cd build && wget -N https://github.com/alexanderankin/simple-deb-4j/releases/download/v0.0.4/simple-deb-4j-all.jar && cd .. && touch $@

# download original package
build/original.zip: build/.exists
	cd build && mkdir -p original && docker run --rm -it -v $(shell pwd)/build/original:/w -w /w ubuntu:25.04 sh -c 'apt update && apt download starship && chmod 777 *.deb' && zip -r original.zip original/

build/original_version.txt: build/original.zip
	zipinfo -1 build/original.zip | grep .deb | sed -e 's.original/..' | cut -d_ -f2 > $@

# build config file
build/config.yaml: config.yaml build/.exists build/original_version.txt
	cat config.yaml | yaml2json | jq '.meta.version = "'$(shell cat ./build/original_version.txt)'"' | json2yaml > $@

build/files: build/original.zip
	cd build && mkdir -p data-files && unzip -o -d data-files original.zip && cd data-files/original && ar xf *.deb && tar xaf data.* && cd ../../../ && touch $@

# repackage release
build/deb.zip: build/simple-deb-4j-all.jar build/config.yaml build/files
	cd build; if [[ -f ~/.sdkman/bin/sdkman-init.sh ]] then . ~/.sdkman/bin/sdkman-init.sh; fi; java -jar ./simple-deb-4j-all.jar b -c ./config.yaml -o ./ && zip deb.zip ./*.deb

.PHONY: clean
clean:
	rm -rf build
