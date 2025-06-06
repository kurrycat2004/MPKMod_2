package io.github.kurrycat.mpkmod.api.minecraft;

import io.github.kurrycat.mpkmod.api.ModPlatform;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public interface IFileEnv {
    /**
     * Get the path to the game directory.
     *
     * @return the path to the game directory
     */
    Path gamePath();

    /**
     * Get the path to the suggested mod's config directory.
     * <p> The returned path is not guaranteed to exist!
     *
     * @return the path to the mod's config directory
     */
    default Path modConfigPath() {
        return gameConfigPath().resolve(ModPlatform.instance().modInfo().modId() + ".cfg");
    }

    /**
     * Get the path to the game's config directory.
     *
     * @return the path to the game's config directory
     */
    Path gameConfigPath();

    /**
     * Find a file in the mod's root paths.
     * <p> This method will search through all root paths of the mod and return the first path that exists.
     *
     * @param file the file to find
     * @return the path to the file, or null if not found
     */
    default Path findPath(String file) {
        for (Path root : rootPaths()) {
            Path path = root.resolve(file.replace("/", root.getFileSystem().getSeparator()));
            if (Files.exists(path)) {
                return path;
            }
        }
        return null;
    }

    /**
     * Get the root paths of the mod. <br>
     * <strong>Note:</strong> This should return the root path of the mod jar, not its path
     *
     * @return the root path of the mod
     * @see io.github.kurrycat.mpkmod.api.util.FileUtil#getRootPath(Path) FileUtil.getRootPath(Path)
     */
    List<Path> rootPaths();
}
