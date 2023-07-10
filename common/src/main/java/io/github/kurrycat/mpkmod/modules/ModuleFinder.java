package io.github.kurrycat.mpkmod.modules;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.save.Serializer;
import io.github.kurrycat.mpkmod.util.ClassUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ModuleFinder {
    private static final String MODULE_FOLDER_NAME = "mpkmodules";
    private static File modDir;

    public static void init() {
        modDir = getModDir();
        if (!modDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            modDir.mkdir();
        }
    }

    private static File getModDir() {
        return new File("./mods");
    }

    public static Map<MPKModuleConfig, File> findAllModules() {
        HashMap<MPKModuleConfig, File> modules = new HashMap<>();
        registerModulesInFolder(modules, modDir);
        File moduleDir = new File(modDir, MODULE_FOLDER_NAME);
        if (moduleDir.exists())
            registerModulesInFolder(modules, moduleDir);

        return modules;
    }

    private static void registerModulesInFolder(Map<MPKModuleConfig, File> modules, File folder) {
        File[] files = folder.listFiles();
        if (files == null) {
            API.LOGGER.info("Failed to search folder: " + folder.getPath() + " for modules");
            return;
        }

        fileLoop:
        for (File file : files) {
            if (!file.getName().endsWith(".jar")) continue;
            MPKModuleConfig config = getConfigFromModule(file);
            if (config == null) continue;

            for (Map.Entry<MPKModuleConfig, File> e : modules.entrySet()) {
                if (e.getKey().moduleName.equals(config.moduleName)) {
                    API.LOGGER.info(
                            "Found duplicate module " + config.moduleName + ": " +
                                    e.getValue().getName() + " and " + file.getName() +
                                    " have the same module name"
                    );
                    continue fileLoop;
                }
            }

            modules.put(config, file);
        }
    }

    private static MPKModuleConfig getConfigFromModule(File modJar) {
        MPKModuleConfig config = null;
        try (JarFile jarFile = new JarFile(modJar)) {
            ZipEntry entry = jarFile.getJarEntry("mpkmodule.config.json");
            if (entry == null) return null;
            try (InputStream stream = jarFile.getInputStream(entry)) {
                config = Serializer.deserialize(stream, MPKModuleConfig.class);
            }
        } catch (IOException ignored) {
        }
        return config;
    }

    public static MPKModuleImpl getAsImpl(MPKModuleConfig config, File modJar) throws Exception {
        MPKModule module;

        URL[] jars = {modJar.toURI().toURL()};
        CustomClassLoader loader = new CustomClassLoader(config.mainClass, jars, ClassUtil.ModClass.getClassLoader());
        Class<?> moduleClass = loader.loadClass(config.mainClass);
        module = (MPKModule) moduleClass.newInstance();

        return new MPKModuleImpl(config.moduleName, module, loader);
    }

    public static class CustomClassLoader extends URLClassLoader {
        private final String packageName;

        public CustomClassLoader(String module, URL[] urls, ClassLoader parent) {
            super(urls, parent);
            packageName = module.substring(0, module.lastIndexOf("."));
        }

        protected Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException {
            if (!name.startsWith(this.packageName)) return super.loadClass(name, resolve);

            synchronized (getClassLoadingLock(name)) {
                Class<?> c = findClass(name);
                if (resolve) resolveClass(c);
                return c;
            }
        }
    }
}
