package io.github.kurrycat.mpkmod.modules;

import io.github.kurrycat.mpkmod.compatibility.API;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ModuleManager {
    public static final HashMap<String, MPKModuleImpl> moduleMap = new HashMap<>();

    public static void reloadAllModules() {
        loadModules(false);
    }

    private static void loadModules(boolean init) {
        Map<MPKModuleConfig, File> modules = ModuleFinder.findAllModules();
        for (Map.Entry<MPKModuleConfig, File> module : modules.entrySet()) {
            try {
                registerModule(ModuleFinder.getAsImpl(module.getKey(), module.getValue()), init);
            } catch (Exception e) {
                API.LOGGER.info("Failed to register module " +
                        module.getKey().moduleName + " in: " +
                        module.getValue().getName());
                API.LOGGER.info("Reason: " + e.getMessage());
            }
        }
    }

    public static void registerModule(MPKModuleImpl module, boolean init) {
        boolean alreadyLoaded = moduleMap.containsKey(module.getName());
        API.LOGGER.info(
                (alreadyLoaded ? "Reloaded" : "Loaded") +
                        " module " + module.getName()
        );
        moduleMap.put(module.getName(), module);

        if (init)
            module.getModule().init();
        else
            module.getModule().loaded();
    }

    public static void initAllModules() {
        loadModules(true);
    }

    public static void loadAllModules() {
        for (Map.Entry<String, MPKModuleImpl> entry : moduleMap.entrySet()) {
            entry.getValue().getModule().loaded();
        }
    }
}
