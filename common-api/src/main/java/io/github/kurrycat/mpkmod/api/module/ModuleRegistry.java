package io.github.kurrycat.mpkmod.api.module;

import io.github.kurrycat.mpkmod.service.TypedServiceProvider;

public interface ModuleRegistry {
    ModuleRegistry INSTANCE = TypedServiceProvider.loadOrThrow(ModuleRegistry.class);

    boolean isModuleLoaded(String moduleId);

    void loadAllModules();
}
