package io.github.kurrycat.examplemodule;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.events.EventAPI;
import io.github.kurrycat.mpkmod.modules.MPKModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExampleModule implements MPKModule {
    // Create your own logger to be able to differentiate messages from mpkmod messages
    public static final String MODULE_NAME = "examplemodule";
    public static final Logger LOGGER = LogManager.getLogger(MODULE_NAME);

    /**
     * This gets called ONLY the first time the mod is loaded.
     * Reloads will not call this so restart the game if you change this
     * (or pause game in debug mode and run "ModuleManager.reloadAllModules();ModuleManager.moduleMap.get(MODULE_NAME).init()")
     */
    public void init() {
        LOGGER.info("Module " + MODULE_NAME + " initialized");
        EventAPI.addListener(
                EventAPI.EventListener.onTickStart(
                        e -> {
                            if (API.tickTime % 2 == 0)
                                LOGGER.info("Tick");
                            else LOGGER.info("Tack");
                        }
                )
        );
    }

    /**
     * This gets called every time the module is reloaded, including the first time
     */
    public void loaded() {
        LOGGER.info("Module " + MODULE_NAME + " reloaded");
    }
}
