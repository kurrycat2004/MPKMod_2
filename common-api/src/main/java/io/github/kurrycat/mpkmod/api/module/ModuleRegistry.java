package io.github.kurrycat.mpkmod.api.module;

import java.util.ServiceLoader;

public interface ModuleRegistry {
    ModuleRegistry INSTANCE = ServiceLoader.load(ModuleRegistry.class).findFirst().orElseThrow();

    boolean isModuleLoaded(String moduleId);

    void loadAllModules();
}
