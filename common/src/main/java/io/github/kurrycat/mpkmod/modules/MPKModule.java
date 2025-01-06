package io.github.kurrycat.mpkmod.modules;

public interface MPKModule {

    /**
     * Gets called once in {@link io.github.kurrycat.mpkmod.compatibility.API#preInit(Class) API.preInit}.<br>
     * Registering should be done here. <br>
     * Reloading the module will not call this again.
     */
    void init();

    /**
     * Gets called once in {@link io.github.kurrycat.mpkmod.compatibility.API#init(String) API.init}.<br>
     * Reloading will call this again. <br>
     */
    void loaded();

    /**
     * Gets called in {@link io.github.kurrycat.mpkmod.modules.ModuleManager#unloadModule(MPKModuleImpl) ModuleManager.unregisterModule}.<br>
     * This is done when the player joins a server which doesn't allow this module.<br>
     * The module does not need to unregister any {@link io.github.kurrycat.mpkmod.events.EventAPI EventAPI} hooks it's made.
     */
    default void unloaded() {
    }
}
