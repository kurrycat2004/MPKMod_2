package io.github.kurrycat.mpkmod.api;

import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.log.LogManager;
import io.github.kurrycat.mpkmod.api.minecraft.IFileEnv;
import io.github.kurrycat.mpkmod.api.minecraft.IGraphics;
import io.github.kurrycat.mpkmod.api.minecraft.IModInfo;
import io.github.kurrycat.mpkmod.api.module.ModuleRegistry;

import java.util.ServiceLoader;

public interface ModPlatform {
    ModPlatform INSTANCE = ServiceLoader.load(ModPlatform.class).stream()
            .map(ServiceLoader.Provider::get)
            .filter(ModPlatform::isActive)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No active ModPlatform found"));
    ILogger LOGGER = LogManager.INSTANCE.getLogger(INSTANCE.modInfo().modId());

    static void init() {
        LOGGER.info("Initializing {} ModPlatform for loader \"{}\"",
                INSTANCE.modInfo().modName(), INSTANCE.modInfo().modLoader());
        ModuleRegistry.INSTANCE.loadAllModules();
    }

    boolean isActive();

    IModInfo modInfo();

    IGraphics graphics();

    IFileEnv fileEnv();
}
