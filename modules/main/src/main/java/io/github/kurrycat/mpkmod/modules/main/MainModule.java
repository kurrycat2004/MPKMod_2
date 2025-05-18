package io.github.kurrycat.mpkmod.modules.main;

import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.module.IModule;
import io.github.kurrycat.mpkmod.api.module.IModuleEntry;

import java.util.List;

public class MainModule implements IModule {
    public static ILogger LOGGER;

    @Override
    public void onLoad(IModuleEntry entry, ILogger logger) {
        LOGGER = logger;
        LOGGER.info("Loading Main Module");
        /*Test test = new Test(1, 2);
        LOGGER.info("Test: " + test);*/
        //LOGGER.info("RuntimeVersion: {}", Runtime.version());
        List.of("a", "b", "c").forEach(
                s -> LOGGER.info("Test: {}", s)
        );
    }

    /*record Test(int a, int b) {
        public Test(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }*/

    @Override
    public void onUnload() {

    }
}
