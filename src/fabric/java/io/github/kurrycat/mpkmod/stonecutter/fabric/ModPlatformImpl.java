package io.github.kurrycat.mpkmod.stonecutter.fabric;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.ModPlatform;
import io.github.kurrycat.mpkmod.api.minecraft.IFileEnv;
import io.github.kurrycat.mpkmod.api.minecraft.IModInfo;
import io.github.kurrycat.mpkmod.service.DefaultServiceProvider;
import io.github.kurrycat.mpkmod.service.ServiceProvider;
import io.github.kurrycat.mpkmod.stonecutter.shared.ModInfoImpl;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Optional;

public class ModPlatformImpl implements ModPlatform {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends DefaultServiceProvider<ModPlatform> {
        public Provider() {
            super(ModPlatformImpl::new, ModPlatform.class);
        }

        @Override
        public Optional<String> invalidReason() {
            if (!isClassLoaded("net.fabricmc.loader.api.FabricLoader")) {
                return Optional.of("FabricLoader not found");
            }
            return super.invalidReason();
        }
    }

    private IModInfo modInfo = null;

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
    public IFileEnv fileEnv() {
        return FileEnvImpl.INSTANCE;
    }
}
