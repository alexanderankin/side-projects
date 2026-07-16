make: build/choose_zstd

build/choose_zstd: choose_zstd.cpp CMakeLists.txt
	mkdir -p build
	cmake -S . -B build
	cmake --build build -j
