package io.github.kurrycat.mpkmod.modules;

public class MPKModuleImpl {
    private final String name;
    private final MPKModule module;

    public MPKModuleImpl(String name, MPKModule module) {
        this.name = name;
        this.module = module;
    }

    public String getName() {
        return name;
    }

    public MPKModule getModule() {
        return module;
    }
}
