package io.github.kurrycat.mpkmod.api.module;

import io.github.kurrycat.mpkmod.api.log.ILogger;

public interface IModule {
    /**
     * Called once when the module is loaded. <br>
     * Note that on reload this will be called on a fresh instance of the module <br>
     * Initialization logic should go here. <br>
     *
     * @param entry  Your module entry in the modules.toml, prefer grabbing values from here instead of hardcoding them.
     * @param logger The logger for this module, use this to log messages.
     */
    void onLoad(IModuleEntry entry, ILogger logger);

    /**
     * Called when the module is being unloaded.
     * Cleanup logic should go here.
     */
    void onUnload();
}
