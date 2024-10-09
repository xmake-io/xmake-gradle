-- imports
import("core.base.option")
import("core.project.config")
import("core.project.project")
import("core.tool.toolchain")
import("target.action.install")
import("target.action.uninstall")

local options = {
    {'o', "installdir",  "kv", nil,  "Set the install directory"},
    {'a', "arch",        "kv",  nil, "Set the installed target architecture"},
    {'c', "clean",       "k", false, "Clean artifacts"},
    {nil, "targets",     "vs", nil,  "Set the targets"}
}

-- get targets
function _get_targets(targetNames)
    local targets = {}
    targetNames = targetNames or {}
    if #targetNames > 0 then
        for _, targetName in ipairs(targetNames) do
            local target = project.target(targetName)
            if target:get("enabled") ~= false and target:is_shared() then
                table.insert(targets, target)
            else
                raise("invalid target(%s)!", targetName)
            end
        end
    else
        for _, target in ipairs(project.ordertargets()) do
            local default = target:get("default")
            if (default == nil or default == true) and target:is_shared() then
                table.insert(targets, target)
            end
        end
    end
    return targets
end

-- install artifacts
function _install_artifacts(targets, opt)
    local arch = opt.arch
    local installdir = opt.installdir
    assert(xmake.version():satisfies("> 2.9.5"), "please update xmake to > 2.9.5")
    for _, target in ipairs(targets) do
        install(target, {installdir = installdir, libdir = arch})
    end
end

-- install cxxstl for ndk >= r25
function _install_cxxstl_newer_ndk(opt)
    local arch = opt.arch
    local installdir = path.join(opt.installdir, arch)
    local ndk = get_config("ndk")
    local ndk_cxxstl = get_config("runtimes") or get_config("ndk_cxxstl")
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
function _install_cxxstl(opt)
    local arch = opt.arch
    local installdir = path.join(opt.installdir, arch)
    local ndk = get_config("ndk")
    local ndk_cxxstl = get_config("runtimes") or get_config("ndk_cxxstl")
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
function _clean_artifacts(targets, opt)
    local arch = opt.arch
    local installdir = opt.installdir
    assert(xmake.version():satisfies("> 2.9.5"), "please update xmake to > 2.9.5")
    for _, target in ipairs(targets) do
        uninstall(target, {installdir = installdir, libdir = arch})
    end

    -- clean cxxstl
    installdir = path.join(installdir, arch)
    local ndk_cxxstl = get_config("ndk_cxxstl")
    if ndk_cxxstl then
        local cxxstl_filename
        if ndk_cxxstl == "c++_shared" then
            cxxstl_filename = "libc++_shared.so"
        elseif ndk_cxxstl == "gnustl_shared" then
            cxxstl_filename = "libgnustl_shared.so"
        elseif ndk_cxxstl == "stlport_shared" then
            cxxstl_filename = "libstlport_shared.so"
        end
        os.tryrm(installdir)
    end

    if os.emptydir(installdir) then
        os.rmdir(installdir)
    end
end

function main(...)
    local argv = table.pack(...)
    local opt = option.parse(argv, options, "Install the target artifacts."
                                           , ""
                                           , "Usage: xmake l install_artifacts.lua [options]")
    assert(opt.installdir)

    -- load config
    config.load()

    -- do install or clean
    local targets = _get_targets(opt.targets)
    assert(targets and #targets > 0, "no targets provided, make sure to have at least one shared target in your xmake.lua or to provide one")

    opt.arch = opt.arch or get_config("arch")
    if not opt.clean then
        _install_artifacts(targets, opt)
        if get_config("ndkver") >= 25 then
            _install_cxxstl_newer_ndk(opt)
        else
            _install_cxxstl(opt)
        end
    else
        _clean_artifacts(targets, opt)
    end
end

