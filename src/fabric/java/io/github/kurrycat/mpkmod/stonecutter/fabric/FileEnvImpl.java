package io.github.kurrycat.mpkmod.stonecutter.fabric;

import io.github.kurrycat.mpkmod.Tags;
import io.github.kurrycat.mpkmod.api.minecraft.IFileEnv;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Path;
import java.util.List;

public class FileEnvImpl implements IFileEnv {
    public static final FileEnvImpl INSTANCE = new FileEnvImpl();

    private FileEnvImpl() {
    }

    private ModContainer modContainer;

    @Override
    public Path getGamePath() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public Path getGameConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public List<Path> getRootPaths() {
        if (modContainer == null) {
            modContainer = FabricLoader.getInstance().getModContainer(Tags.MOD_ID)
                    .orElseThrow(() -> new IllegalStateException("Mod not found: " + Tags.MOD_ID));
        }
        return modContainer.getRootPaths();
    }
}
