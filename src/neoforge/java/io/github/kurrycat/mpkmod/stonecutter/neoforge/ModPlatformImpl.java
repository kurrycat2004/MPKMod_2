package io.github.kurrycat.mpkmod.stonecutter.neoforge;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.ModPlatform;
import io.github.kurrycat.mpkmod.api.minecraft.IFileEnv;
import io.github.kurrycat.mpkmod.api.minecraft.IGraphics;
import io.github.kurrycat.mpkmod.api.minecraft.IModInfo;
import io.github.kurrycat.mpkmod.stonecutter.shared.AbstractModPlatform;
import io.github.kurrycat.mpkmod.stonecutter.shared.ModInfoImpl;
import net.neoforged.fml.ModList;

@AutoService(ModPlatform.class)
public class ModPlatformImpl extends AbstractModPlatform {
    private IModInfo modInfo = null;

    public ModPlatformImpl() {
        super("net.neoforged.fml.ModLoader");
    }

    @Override
    public IModInfo modInfo() {
        if (modInfo == null) {
            modInfo = new ModInfoImpl(
                    ModList.get().getModContainerById("minecraft")
                            .orElseThrow(() -> new IllegalStateException("Minecraft not found (?)"))
                            .getModInfo().getVersion().toString(),
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
