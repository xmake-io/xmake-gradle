# project
cmake_minimum_required(VERSION 3.6.0)

# target
add_library(nativelib SHARED "")
target_compile_options(nativelib PRIVATE -Os)
target_sources(nativelib PRIVATE
    nativelib.cc
)

