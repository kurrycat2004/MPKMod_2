package io.github.kurrycat.mpkmod.stonecutter.fabric;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.ModPlatform;
import io.github.kurrycat.mpkmod.api.minecraft.IFileEnv;
import io.github.kurrycat.mpkmod.api.minecraft.IGraphics;
import io.github.kurrycat.mpkmod.api.minecraft.IModInfo;
import io.github.kurrycat.mpkmod.stonecutter.shared.AbstractModPlatform;
import io.github.kurrycat.mpkmod.stonecutter.shared.ModInfoImpl;
import net.fabricmc.loader.api.FabricLoader;

@AutoService(ModPlatform.class)
public class ModPlatformImpl extends AbstractModPlatform {
    private IModInfo modInfo = null;

    public ModPlatformImpl() {
        super("net.fabricmc.loader.api.FabricLoader");
    }

    @Override
    public IModInfo modInfo() {
        if (modInfo == null) {
            modInfo = new ModInfoImpl(
                    FabricLoader.getInstance().getModContainer("minecraft")
                            .orElseThrow(() -> new IllegalStateException("Minecraft not found (?)"))
                            .getMetadata().getVersion().getFriendlyString(),
                    "fabric"
            );
        }
        return modInfo;
    }

    @Override
    public IGraphics graphics() {
        return GraphicsImpl.INSTANCE;
    }

    @Override
    public IFileEnv fileEnv() {
        return FileEnvImpl.INSTANCE;
    }
}
