package io.github.kurrycat.mpkmod.stonecutter.vintage_forge;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.ModPlatform;
import io.github.kurrycat.mpkmod.api.minecraft.IFileEnv;
import io.github.kurrycat.mpkmod.api.minecraft.IModInfo;
import io.github.kurrycat.mpkmod.api.service.DefaultServiceProvider;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.stonecutter.shared.ModInfoImpl;
import net.minecraftforge.common.ForgeVersion;

import java.util.Optional;

public final class ModPlatformImpl implements ModPlatform {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends DefaultServiceProvider<ModPlatform> {
        public Provider() {
            super(ModPlatformImpl::new, ModPlatform.class);
        }

        @Override
        public Optional<String> invalidReason() {
            if (!isClassLoaded("net.minecraftforge.fml.common.Loader")) {
                return Optional.of("Forge is not loaded");
            }
            return Optional.empty();
        }
    }

    private ModInfoImpl modInfo = null;

    @Override
    public IModInfo modInfo() {
        if (modInfo == null) {
            modInfo = new ModInfoImpl(ForgeVersion.mcVersion, "forge");
        }
        return modInfo;
    }

    @Override
    public IFileEnv fileEnv() {
        return FileEnvImpl.INSTANCE;
    }
}
