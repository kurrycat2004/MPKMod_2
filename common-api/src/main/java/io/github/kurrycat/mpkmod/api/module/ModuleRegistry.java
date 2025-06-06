package io.github.kurrycat.mpkmod.api.module;

import io.github.kurrycat.mpkmod.api.service.ServiceManager;

public interface ModuleRegistry {
    static ModuleRegistry instance() {
        return ServiceManager.instance().get(ModuleRegistry.class);
    }

    boolean isModuleLoaded(String moduleId);

    void loadAllModules();
}
