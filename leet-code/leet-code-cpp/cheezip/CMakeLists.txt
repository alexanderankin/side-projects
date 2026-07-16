cmake_minimum_required(VERSION 3.20)

project(
    choose_zstd
    VERSION 1.0.0
    LANGUAGES CXX
)

add_executable(choose_zstd
    choose_zstd.cpp
)

target_compile_features(choose_zstd PRIVATE cxx_std_17)

find_package(PkgConfig REQUIRED)
pkg_check_modules(ZSTD REQUIRED IMPORTED_TARGET libzstd)

target_link_libraries(choose_zstd
    PRIVATE
        PkgConfig::ZSTD
)

if(MSVC)
    target_compile_options(choose_zstd PRIVATE /W4)
else()
    target_compile_options(choose_zstd PRIVATE
        -Wall
        -Wextra
        -Wpedantic
    )
endif()
