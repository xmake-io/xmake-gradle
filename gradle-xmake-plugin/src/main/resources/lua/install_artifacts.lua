-- imports
import("core.project.config")
import("core.project.project")
import("core.tool.toolchain")

-- get targets
function _get_targets(...)
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
    return targets
end

-- install artifacts
function _install_artifacts(libsdir, installdir, targets, arch)

    -- append arch sub-directory
    libsdir = path.join(libsdir, arch, "lib")
    installdir = path.join(installdir, arch)

    -- install targets
    if not os.isdir(installdir) then
        os.mkdir(installdir)
    end
    for _, target in ipairs(targets) do
        os.vcp(path.join(libsdir, path.filename(target:targetfile())), installdir)
    end
end

-- install cxxstl for ndk >= r25
function _install_cxxstl_newer_ndk(installdir, arch)
    -- append arch sub-directory
    installdir = path.join(installdir, arch)

    local ndk = get_config("ndk")
    local ndk_cxxstl = get_config("ndk_cxxstl")

    if ndk and ndk_cxxstl and ndk_cxxstl:endswith("_shared") and arch then
       
        -- get the toolchains arch
        local toolchains_archs = {
            ["armeabi-v7a"] = "arm-linux-androideabi",
            ["arm64-v8a"] = "aarch64-linux-android",
            ["x86"] = "i686-linux-android",
            ["x86_64"] = "x86_64-linux-android"
        }
    
        -- get stl library
        local cxxstl_filename
        if ndk_cxxstl == "c++_shared" then
             cxxstl_filename = "libc++_shared.so"
        end

        if toolchains_archs[arch] ~= nil and cxxstl_filename then
            local ndk_toolchain = toolchain.load("ndk", {plat = config.plat(), arch = config.arch()})
            local ndk_sysroot = ndk_toolchain:config("ndk_sysroot")
            local cxxstl_sdkdir_llvmstl = path.translate(format("%s/usr/lib/%s", ndk_sysroot, toolchains_archs[arch]))
            
            os.vcp(path.join(cxxstl_sdkdir_llvmstl, cxxstl_filename), path.join(installdir, cxxstl_filename))
        end

    end
end

-- install c++ stl shared library
function _install_cxxstl(installdir, arch)

    -- append arch sub-directory
    installdir = path.join(installdir, arch)

    -- install stl shared library
    local ndk = get_config("ndk")
    local ndk_cxxstl = get_config("ndk_cxxstl")
    arch = arch or get_config("arch")
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
        if ndk_cxxstl:startswith("c++") then
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
        if ndk_cxxstl == "c++_shared" then
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

-- clean artifacts
function _clean_artifacts(installdir, targets, arch)

    -- append arch sub-directory
    installdir = path.join(installdir, arch)

    -- clean targets artifacts in the install directory
    for _, target in ipairs(targets) do
        os.tryrm(path.join(installdir, path.filename(target:targetfile())))
        if os.emptydir(installdir) then
            os.tryrm(installdir)
        end
    end
end

-- main entry
function main(libsdir, installdir, archs, ...)

    -- check
    assert(libsdir and installdir and archs)

    -- load config
    config.load()

    -- get abi filters
    local abi_filters = {}
    for _, arch in ipairs(archs:split(',', {plain = true})) do
        abi_filters[arch] = true
    end

    -- do install or clean
    local targets = _get_targets(...)
    if targets and #targets > 0 then
        for _, arch in ipairs({"armeabi", "armeabi-v7a", "arm64-v8a", "x86", "x86_64"}) do
            if abi_filters[arch] then
                _install_artifacts(libsdir, installdir, targets, arch)
        
                if get_config("ndkver") >= 25 then
                    _install_cxxstl_newer_ndk(installdir, arch)
                else
                    _install_cxxstl(installdir, arch)
                end
            else
                _clean_artifacts(installdir, targets, arch)
            end
        end
    end
end
