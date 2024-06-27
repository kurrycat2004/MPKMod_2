package io.github.kurrycat.mpkmod.modules;

import java.io.IOException;

public class MPKModuleImpl {
    private final String name;
    private final MPKModule module;
    private ModuleFinder.CustomClassLoader loader;

    public MPKModuleImpl(String name, MPKModule module, ModuleFinder.CustomClassLoader loader) {
        this.name = name;
        this.module = module;
        this.loader = loader;
    }

    public void closeLoader() {
        if (loader == null) return;
        try {
            loader.close();
        } catch (IOException ignored) {
        } finally {
            loader = null;
        }
    }

    public String getName() {
        return name;
    }

    public MPKModule getModule() {
        return module;
    }
}
