package io.github.kurrycat.mpkmod.stonecutter.vintage_forge;

import io.github.kurrycat.mpkmod.api.minecraft.IFileEnv;

import java.nio.file.Path;
import java.util.List;

public final class FileEnvImpl implements IFileEnv {
    public static final FileEnvImpl INSTANCE = new FileEnvImpl();

    private FileEnvImpl() {
    }

    private Path gamePath;
    private Path gameConfigPath;
    private List<Path> rootPaths;

    @Override
    public Path getGamePath() {
        return gamePath;
    }

    void setGamePath(Path gamePath) {
        this.gamePath = gamePath;
    }

    @Override
    public Path getGameConfigPath() {
        return gameConfigPath;
    }

    void setGameConfigPath(Path gameConfigPath) {
        this.gameConfigPath = gameConfigPath;
    }

    @Override
    public List<Path> getRootPaths() {
        return rootPaths;
    }

    void setRootPaths(List<Path> rootPaths) {
        this.rootPaths = rootPaths;
    }
}
