package io.github.kurrycat.mpkmod.api;

import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.log.LogManager;
import io.github.kurrycat.mpkmod.api.minecraft.IFileEnv;
import io.github.kurrycat.mpkmod.api.minecraft.IModInfo;
import io.github.kurrycat.mpkmod.api.module.ModuleRegistry;
import io.github.kurrycat.mpkmod.api.service.TypedServiceProvider;

public interface ModPlatform {
    ModPlatform INSTANCE = TypedServiceProvider.loadOrThrow(ModPlatform.class);
    ILogger LOGGER = LogManager.INSTANCE.getLogger(INSTANCE.modInfo().modId());

    static void init() {
        LOGGER.info("Initializing {} ModPlatform for loader \"{}\"",
                INSTANCE.modInfo().modName(), INSTANCE.modInfo().modLoader());
        ModuleRegistry.INSTANCE.loadAllModules();
    }

    IModInfo modInfo();

    IFileEnv fileEnv();
}
