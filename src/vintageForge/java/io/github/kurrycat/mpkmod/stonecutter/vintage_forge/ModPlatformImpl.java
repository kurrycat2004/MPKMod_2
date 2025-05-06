package io.github.kurrycat.mpkmod.stonecutter.vintage_forge;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.ModPlatform;
import io.github.kurrycat.mpkmod.api.minecraft.IFileEnv;
import io.github.kurrycat.mpkmod.api.minecraft.IGraphics;
import io.github.kurrycat.mpkmod.api.minecraft.IModInfo;
import io.github.kurrycat.mpkmod.stonecutter.shared.AbstractModPlatform;
import io.github.kurrycat.mpkmod.stonecutter.shared.ModInfoImpl;
import net.minecraftforge.common.ForgeVersion;

@AutoService(ModPlatform.class)
public class ModPlatformImpl extends AbstractModPlatform {
    private ModInfoImpl modInfo = null;

    public ModPlatformImpl() {
        super("net.minecraftforge.fml.common.Loader");
    }

    @Override
    public IModInfo modInfo() {
        if (modInfo == null) {
            modInfo = new ModInfoImpl(ForgeVersion.mcVersion, "forge");
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
