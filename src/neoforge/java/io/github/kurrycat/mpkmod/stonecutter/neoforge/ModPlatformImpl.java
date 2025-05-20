package io.github.kurrycat.mpkmod.stonecutter.neoforge;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.ModPlatform;
import io.github.kurrycat.mpkmod.api.minecraft.IFileEnv;
import io.github.kurrycat.mpkmod.api.minecraft.IModInfo;
import io.github.kurrycat.mpkmod.service.DefaultServiceProvider;
import io.github.kurrycat.mpkmod.service.ServiceProvider;
import io.github.kurrycat.mpkmod.stonecutter.shared.ModInfoImpl;
import net.neoforged.fml.ModList;

import java.util.Optional;

public class ModPlatformImpl implements ModPlatform {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends DefaultServiceProvider<ModPlatform> {
        public Provider() {
            super(ModPlatformImpl::new, ModPlatform.class);
        }

        @Override
        public Optional<String> invalidReason() {
            if (!isClassLoaded("net.neoforged.fml.ModLoader")) {
                return Optional.of("NeoForge not found");
            }
            return super.invalidReason();
        }
    }

    private IModInfo modInfo = null;

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
    public IFileEnv fileEnv() {
        return FileEnvImpl.INSTANCE;
    }
}
