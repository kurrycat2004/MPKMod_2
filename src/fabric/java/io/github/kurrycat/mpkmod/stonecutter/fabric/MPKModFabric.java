package io.github.kurrycat.mpkmod.stonecutter.fabric;

import io.github.kurrycat.mpkmod.api.ModPlatform;
import net.fabricmc.api.ModInitializer;

public class MPKModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModPlatform.init();
    }
}
