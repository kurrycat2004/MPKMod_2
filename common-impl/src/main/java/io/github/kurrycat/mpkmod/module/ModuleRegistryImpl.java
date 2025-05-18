package io.github.kurrycat.mpkmod.module;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.Tags;
import io.github.kurrycat.mpkmod.api.ModPlatform;
import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.log.LogManager;
import io.github.kurrycat.mpkmod.api.module.IModule;
import io.github.kurrycat.mpkmod.api.module.IVersionConstraint;
import io.github.kurrycat.mpkmod.api.module.ModuleRegistry;
import io.github.kurrycat.mpkmod.util.FileUtilImpl;
import io.github.kurrycat.mpkmod.util.MultiParentClassLoader;
import io.github.kurrycat.mpkmod.util.StringUtil;
import io.github.kurrycat.mpkmod.util.TarjanSCC;
import xyz.wagyourtail.jvmdg.ClassDowngrader;
import xyz.wagyourtail.jvmdg.classloader.DowngradingClassLoader;

import java.io.Closeable;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AutoService(ModuleRegistry.class)
public class ModuleRegistryImpl implements ModuleRegistry {
    private final Set<String> disabledModuleIds = new HashSet<>();
    private final Map<Path, DiscoveredModule> errorModules = new HashMap<>();
    private final Map<String, DiscoveredModule> disabledModules = new HashMap<>();
    private final Map<String, LoadedModule> loadedModules = new HashMap<>();
    private final Map<String, Set<String>> moduleToDependants = new HashMap<>();
    private final Map<String, Set<String>> moduleToDependencies = new HashMap<>();

    private static <T> T removeOrDefault(Map<String, T> map, String key, T defaultValue) {
        T value = map.remove(key);
        return value == null ? defaultValue : value;
    }

    private void unloadRecursive(String moduleId) {
        Set<String> dependants = removeOrDefault(moduleToDependants, moduleId, Set.of());
        for (String dependantId : dependants) {
            unloadRecursive(dependantId);
        }

        Set<String> dependencies = removeOrDefault(moduleToDependencies, moduleId, Set.of());
        for (String dep : dependencies) {
            moduleToDependants.get(dep).remove(moduleId);
        }

        LoadedModule module = loadedModules.remove(moduleId);
        module.moduleInstance().onUnload();
        if (module.classLoader() instanceof Closeable cl) {
            try {
                cl.close();
            } catch (Exception e) {
                ModPlatform.LOGGER.warn("Failed to close classloader for module {}", moduleId, e);
            }
        }
    }

    public void loadModules() {
        errorModules.clear();

        List<DiscoveredModule> discoveredModules = new ArrayList<>();
        ModuleCache.extractInternalModules();
        ModuleDiscoverer.discoverModulesFromDir(
                ModuleCache.getInternalModuleDir(),
                discoveredModules
        );
        ModuleDiscoverer.discoverModulesFromDir(
                ModuleCache.getModulesDir(),
                discoveredModules
        );

        // filter errored + disabled modules
        Map<String, DiscoveredModule> toLoad = new HashMap<>();
        for (DiscoveredModule module : discoveredModules) {
            if (module.isError()) {
                errorModules.put(module.source(), module);
            } else if (!disabledModuleIds.contains(module.entry().id())) {
                toLoad.put(module.entry().id(), module);
            } else {
                disabledModules.put(module.entry().id(), module);
            }
        }

        // load trivially loadable modules
        boolean modifiedToLoad = true;
        while (modifiedToLoad) {
            modifiedToLoad = false;
            Iterator<Map.Entry<String, DiscoveredModule>> iterator = toLoad.entrySet().iterator();
            currentPass:
            while (iterator.hasNext()) {
                Map.Entry<String, DiscoveredModule> entry = iterator.next();
                DiscoveredModule module = entry.getValue();

                for (Map.Entry<String, IVersionConstraint> depEntry : module.entry().dependencies().entrySet()) {
                    String depId = depEntry.getKey();
                    IVersionConstraint depVersion = depEntry.getValue();
                    LoadedModule loaded = loadedModules.get(depId);

                    if (loaded == null) {
                        continue currentPass;
                    }

                    if (!loaded.entry().version().satisfies(depVersion)) {
                        modifiedToLoad = true;
                        iterator.remove();
                        errorModules.put(module.source(), module.withError(
                                new ModuleLoadException.Builder("Unsatisfied version constraint")
                                        .addError("Requires the " + depId + " version to match " + depVersion +
                                                  ", but found version " + loaded.entry().version())
                                        .build()
                        ));
                        continue currentPass;
                    }
                }

                modifiedToLoad = true;
                iterator.remove();
                try {
                    LoadedModule loadedModule = loadModule(loadedModules, module);
                    loadedModules.put(loadedModule.entry().id(), loadedModule);
                } catch (ModuleLoadException e) {
                    errorModules.put(module.source(), module.withError(e));
                }
            }
        }

        // handle missing dependencies
        Iterator<DiscoveredModule> it = toLoad.values().iterator();
        while (it.hasNext()) {
            DiscoveredModule mod = it.next();
            List<String> missing = mod.entry().dependencies().keySet().stream()
                    .filter(d -> !loadedModules.containsKey(d) && !toLoad.containsKey(d))
                    .toList();
            if (!missing.isEmpty()) {
                it.remove();
                errorModules.put(mod.source(), mod.withError(
                        new ModuleLoadException.Builder("Missing dependency")
                                .addError("Module " + mod.entry().id() +
                                          " requires missing module(s): " + String.join(", ", missing))
                                .build()
                ));
            }
        }

        // build graph of remaining modules
        Set<String> nodeIds = toLoad.keySet();
        Map<String, List<String>> graph = new HashMap<>();
        for (var e : toLoad.entrySet()) {
            graph.put(e.getKey(),
                    e.getValue().entry().dependencies().keySet().stream()
                            .filter(nodeIds::contains)
                            .toList());
        }

        // handle cycles
        TarjanSCC<String> scc = new TarjanSCC<>(graph);
        for (Set<String> comp : scc.getSCCs()) {
            String start = comp.iterator().next();
            boolean isCycle = comp.size() > 1 || (comp.size() == 1 && graph.get(start).contains(start));
            if (!isCycle) continue;

            for (String modId : comp) {
                DiscoveredModule mod = toLoad.remove(modId);
                errorModules.put(mod.source(), mod.withError(
                        new ModuleLoadException.Builder("Circular dependency")
                                .addError("Cycle: " + StringUtil.joinCycle(comp, start, " -> "))
                                .build()
                ));
            }
        }
    }

    private static LoadedModule loadModule(Map<String, LoadedModule> loadedModules, DiscoveredModule module) throws ModuleLoadException {
        CachedModule cachedModule = ModuleCache.getOrCreateCachedModule(module);
        FileUtilImpl.tryCloseJar(module.source());

        try {
            List<ClassLoader> dependencyClassLoaders = new ArrayList<>();
            for (String depId : cachedModule.entry().dependencies().keySet()) {
                LoadedModule depModule = loadedModules.get(depId);
                dependencyClassLoaders.add(depModule.classLoader());
            }

            ClassLoader parent = buildClassLoaderHierarchy(dependencyClassLoaders);
            DowngradingClassLoader loader = new DowngradingClassLoader(ClassDowngrader.getCurrentVersionDowngrader(), parent);
            loader.addDelegate(new URL[]{cachedModule.source().toUri().toURL()});

            Class<?> entrypointClass;
            try {
                entrypointClass = loader.loadClass(cachedModule.entry().entrypoint());
            } catch (ClassNotFoundException e) {
                throw new ModuleLoadException.Builder("Invalid entrypoint class for module " + cachedModule.entry().id())
                        .addError(e)
                        .build();
            }

            if (!IModule.class.isAssignableFrom(entrypointClass)) {
                throw new ModuleLoadException.Builder("Invalid entrypoint class for module " + cachedModule.entry().id())
                        .addError("Entrypoint class: " + entrypointClass.getName() + " does not implement IModule")
                        .build();
            }

            @SuppressWarnings("unchecked")
            Class<? extends IModule> moduleClass = (Class<? extends IModule>) entrypointClass;

            IModule moduleInstance;
            try {
                moduleInstance = moduleClass.getDeclaredConstructor().newInstance();
            } catch (NoSuchMethodException e) {
                throw new ModuleLoadException.Builder("Invalid entrypoint class for module " + cachedModule.entry().id())
                        .addError("Entrypoint class: " + entrypointClass.getName() + " does not have a no-arg constructor")
                        .build();
            }

            ILogger logger = LogManager.INSTANCE.getLogger(Tags.MOD_ID + "/" + cachedModule.entry().id());
            try {
                moduleInstance.onLoad(cachedModule.entry(), logger);
            } catch (Throwable e) {
                throw new ModuleLoadException.Builder("Invalid entrypoint class for module " + cachedModule.entry().id())
                        .addError("onLoad() threw an error", e)
                        .build();
            }

            return new LoadedModule(
                    cachedModule.source(),
                    cachedModule.sourceHash(),
                    cachedModule.entry(),
                    loader,
                    moduleInstance
            );
        } catch (Exception e) {
            throw new ModuleLoadException.Builder("Unexpected error while trying to load module: " + cachedModule.entry().id())
                    .addError(e)
                    .build();
        }
    }

    private static ClassLoader buildClassLoaderHierarchy(List<ClassLoader> parents) {
        if (parents.isEmpty()) return ModuleRegistryImpl.class.getClassLoader();
        else if (parents.size() == 1) return parents.getFirst();
        else return new MultiParentClassLoader(parents);
    }

    @Override
    public boolean isModuleLoaded(String moduleId) {
        return loadedModules.containsKey(moduleId);
    }

    @Override
    public void loadAllModules() {
        loadModules();
        ModPlatform.LOGGER.info("enable stacktrace: {}", ModuleLoadException.ENABLE_STACKTRACE);
        ModPlatform.LOGGER.info("Loaded modules: {}", loadedModules.keySet());
        ModPlatform.LOGGER.info("Disabled modules: {}", disabledModules.keySet());
        ModPlatform.LOGGER.info("Errored modules:");
        for (var e : errorModules.values()) {
            ModPlatform.LOGGER.info("", e.error());
        }
    }
}
