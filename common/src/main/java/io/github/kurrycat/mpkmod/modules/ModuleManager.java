package io.github.kurrycat.mpkmod.modules;

import io.github.kurrycat.mpkmod.compatibility.API;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ModuleManager {
    public static final HashMap<String, MPKModuleImpl> moduleMap = new HashMap<>();

    public static void reloadAllModules() {
        Map<MPKModuleConfig, File> modules = ModuleFinder.findAllModules();
        for (Map.Entry<MPKModuleConfig, File> module : modules.entrySet()) {
            try {
                registerModule(ModuleFinder.getAsImpl(module.getKey(), module.getValue()));
            } catch (Exception e) {
                API.LOGGER.info("Failed to register module " +
                        module.getKey().moduleName + " in: " +
                        module.getValue().getName());
                API.LOGGER.info("Reason: " + e.getMessage());
            }
        }
    }

    public static void registerModule(MPKModuleImpl module) {
        boolean alreadyLoaded = moduleMap.containsKey(module.getName());
        API.LOGGER.info(
                (alreadyLoaded ? "Reloaded" : "Loaded") +
                        " module " + module.getName()
        );
        moduleMap.put(module.getName(), module);

        if (!alreadyLoaded)
            module.getModule().init();
        module.getModule().loaded();
    }
}
