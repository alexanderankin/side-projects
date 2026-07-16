cmake_minimum_required(VERSION 3.20)

project(
    choose_zstd
    VERSION 1.0.0
    LANGUAGES CXX
)

find_package(PkgConfig REQUIRED)
pkg_check_modules(ZSTD REQUIRED IMPORTED_TARGET libzstd)

include(FetchContent)

FetchContent_Declare(
    cpp_httplib
    GIT_REPOSITORY https://github.com/yhirose/cpp-httplib.git
    GIT_TAG v0.18.3
)

FetchContent_Declare(
    nlohmann_json
    GIT_REPOSITORY https://github.com/nlohmann/json.git
    GIT_TAG v3.11.3
)

FetchContent_MakeAvailable(cpp_httplib nlohmann_json)

add_executable(choose_zstd
    choose_zstd.cpp
)

target_compile_features(choose_zstd PRIVATE cxx_std_17)

target_link_libraries(choose_zstd
    PRIVATE
        PkgConfig::ZSTD
        httplib::httplib
        nlohmann_json::nlohmann_json
)

if(APPLE)
    find_library(COREFOUNDATION_FRAMEWORK CoreFoundation REQUIRED)

    target_link_libraries(
        choose_zstd
        PRIVATE
            ${COREFOUNDATION_FRAMEWORK}
    )
endif()

if(MSVC)
    target_compile_options(choose_zstd PRIVATE /W4)
else()
    target_compile_options(choose_zstd PRIVATE
        -Wall
        -Wextra
        -Wpedantic
    )
endif()
