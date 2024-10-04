set_project("test")
set_version("1.0.0")

add_rules("mode.debug", "mode.release")

target("nativelib")
    set_kind("shared")
    add_files("nativelib.cc")

