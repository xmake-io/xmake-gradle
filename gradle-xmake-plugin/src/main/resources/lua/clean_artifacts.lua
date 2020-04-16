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

    -- clean targets artifacts in the install directory
    for _, target in ipairs(targets) do
        for _, arch in ipairs({"armeabi", "armeabi-v7a", "arm64-v8a", "x86", "x86_64"}) do
            local libdir = path.join(installdir, arch)
            os.tryrm(path.join(libdir, path.filename(target:targetfile())))
            if os.emptydir(libdir) then
                os.tryrm(libdir)
            end
        end
    end
end
