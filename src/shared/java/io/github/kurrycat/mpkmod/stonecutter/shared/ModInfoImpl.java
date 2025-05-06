package io.github.kurrycat.mpkmod.stonecutter.shared;

import io.github.kurrycat.mpkmod.Tags;
import io.github.kurrycat.mpkmod.api.minecraft.IModInfo;

public record ModInfoImpl(String mcVersion, String modLoader) implements IModInfo {
    @Override
    public String modId() {
        return Tags.MOD_ID;
    }

    @Override
    public String modName() {
        return Tags.MOD_NAME;
    }

    @Override
    public String modVersion() {
        return Tags.MOD_VERSION;
    }
}
