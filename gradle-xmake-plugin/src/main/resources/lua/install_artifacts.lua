-- imports
import("core.project.config")
import("core.project.project")

-- main entry
function main(installdir, ...)

    -- check
    assert(installdir, "install directory is empty!")

    -- load config
    config.load()

    -- get targets
    local targets = {}
    local targetNames = table.pack(...)
    if targetNames.n > 0 then
        for _, targetName in ipairs(targetNames) do
            local target = project.target(targetName)
            if target:get("enabled") ~= false and target:targetkind() == "shared" then
                table.insert(targets, target)
            else
                raise("invalid target(%s)!", targetName)
            end
        end
    else
        for _, target in pairs(project.targets()) do
            local default = target:get("default")
            if (default == nil or default == true) and target:targetkind() == "shared" then
                table.insert(targets, target)
            end
        end
    end

    -- install targets
    if not os.isdir(installdir) then
        os.mkdir(installdir)
    end
    for _, target in ipairs(targets) do
        os.vcp(target:targetfile(), installdir)
    end

    -- install stl shared library
    local ndk = get_config("ndk")
    local ndk_cxxstl = get_config("ndk_cxxstl")
    local arch = get_config("arch")
    if ndk and ndk_cxxstl and ndk_cxxstl:endswith("_shared") and arch then

        -- get llvm c++ stl sdk directory
        local cxxstl_sdkdir_llvmstl = path.translate(format("%s/sources/cxx-stl/llvm-libc++", ndk))

        -- get gnu c++ stl sdk directory
        local cxxstl_sdkdir_gnustl = nil
        if get_config("ndk_toolchains_ver") then
            cxxstl_sdkdir_gnustl = path.translate(format("%s/sources/cxx-stl/gnu-libstdc++/%s", ndk, get_config("ndk_toolchains_ver")))
        end

        -- get stlport c++ sdk directory
        local cxxstl_sdkdir_stlport = path.translate(format("%s/sources/cxx-stl/stlport", ndk))

        -- get c++ sdk directory
        local cxxstl_sdkdir
        if ndk_cxxstl:startswith("llvmstl") then
            cxxstl_sdkdir = cxxstl_sdkdir_llvmstl
        elseif ndk_cxxstl:startswith("gnustl") then
            cxxstl_sdkdir = cxxstl_sdkdir_gnustl
        elseif ndk_cxxstl:startswith("stlport") then
            cxxstl_sdkdir = cxxstl_sdkdir_stlport
        end

        -- get the toolchains arch
        local toolchains_archs =
        {
            ["armv5te"]     = "armeabi"         -- deprecated
        ,   ["armv7-a"]     = "armeabi-v7a"     -- deprecated
        ,   ["armeabi"]     = "armeabi"         -- removed in ndk r17
        ,   ["armeabi-v7a"] = "armeabi-v7a"
        ,   ["arm64-v8a"]   = "arm64-v8a"
        ,   i386            = "x86"             -- deprecated
        ,   x86             = "x86"
        ,   x86_64          = "x86_64"
        ,   mips            = "mips"            -- removed in ndk r17
        ,   mips64          = "mips64"          -- removed in ndk r17
        }
        local toolchains_arch = toolchains_archs[arch]

        -- get stl library
        local cxxstl_filename
        if ndk_cxxstl == "llvmstl_shared" then
            cxxstl_filename = "libc++_shared.so"
        elseif ndk_cxxstl == "gnustl_shared" then
            cxxstl_filename = "libgnustl_shared.so"
        elseif ndk_cxxstl == "stlport_shared" then
            cxxstl_filename = "libstlport_shared.so"
        end

        -- do copy
        if cxxstl_sdkdir and toolchains_arch and cxxstl_filename then
            os.vcp(path.join(cxxstl_sdkdir, "libs", toolchains_arch, cxxstl_filename), path.join(installdir, cxxstl_filename))
        end
    end
end
