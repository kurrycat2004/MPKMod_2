package io.github.kurrycat.mpkmod.modules.main;

import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.module.IModule;
import io.github.kurrycat.mpkmod.api.module.IModuleEntry;

public class MainModule implements IModule {
    public static ILogger LOGGER;

    @Override
    public void onLoad(IModuleEntry entry, ILogger logger) {
        LOGGER = logger;
        LOGGER.info("Loading Main Module");
    }

    @Override
    public void onUnload() {

    }
}
