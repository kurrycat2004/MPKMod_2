package io.github.kurrycat.mpkmod.module;

import io.github.kurrycat.mpkmod.api.module.IModule;

import java.nio.file.Path;

public record LoadedModule(
        Path root,
        String sourceHash,
        ModuleEntry entry,
        ClassLoader classLoader,
        IModule moduleInstance
) {
    public boolean matchesExactly(DiscoveredModule module) {
        return !module.isError() && sourceHash.equals(module.sourceHash());
    }
}
