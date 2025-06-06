package io.github.kurrycat.mpkmod.api;

import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.log.LogManager;
import io.github.kurrycat.mpkmod.api.minecraft.IFileEnv;
import io.github.kurrycat.mpkmod.api.minecraft.IModInfo;
import io.github.kurrycat.mpkmod.api.module.ModuleRegistry;
import io.github.kurrycat.mpkmod.api.service.ServiceManager;

public interface ModPlatform {
    static ModPlatform instance() {
        return ServiceManager.instance().get(ModPlatform.class);
    }

    ILogger LOGGER = LogManager.instance().createLogger(instance().modInfo().modId());

    static void init() {
        LOGGER.info("Initializing {} ModPlatform for loader \"{}\"",
                instance().modInfo().modName(), instance().modInfo().modLoader());
        ModuleRegistry.instance().loadAllModules();
    }

    IModInfo modInfo();

    IFileEnv fileEnv();
}
