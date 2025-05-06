package io.github.kurrycat.mpkmod.stonecutter.neoforge;

import io.github.kurrycat.mpkmod.Tags;
import io.github.kurrycat.mpkmod.api.minecraft.IFileEnv;
import io.github.kurrycat.mpkmod.api.util.FileUtil;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.locating.IModFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class FileEnvImpl implements IFileEnv {
    public static final FileEnvImpl INSTANCE = new FileEnvImpl();

    private FileEnvImpl() {
    }

    private Path filePath;

    @Override
    public Path getGamePath() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public Path getGameConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public List<Path> getRootPaths() {
        if (filePath == null) {
            IModFile modFile = ModList.get().getModFileById(Tags.MOD_ID).getFile();
            filePath = modFile.getFilePath();
        }
        Path rootPath;
        try {
            rootPath = FileUtil.INSTANCE.getRootPath(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to get root path for source file", e);
        }
        return Collections.singletonList(rootPath);
    }
}
