package io.github.kurrycat.mpkmod.module;

import io.github.kurrycat.mpkmod.api.util.FileUtil;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ModuleDiscoverer {
    public static void discoverModulesFromDir(Path dir, List<DiscoveredModule> modules) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path source : stream) {
                if (!Files.isDirectory(source) && !source.toString().endsWith(".jar")) continue;
                discoverModules(source, modules);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read module directory: " + dir, e);
        }
    }

    public static void discoverModules(Path source, List<DiscoveredModule> modules) {
        try {
            Path root = FileUtil.INSTANCE.getRootPath(source);
            List<ModuleEntry> entries = ModuleConfig.load(root);
            for (ModuleEntry entry : entries) {
                modules.add(DiscoveredModule.fromEntry(source, entry));
            }
        } catch (ModuleLoadException e) {
            modules.add(DiscoveredModule.fromError(source, e));
        } catch (Exception e) {
            modules.add(DiscoveredModule.fromError(source,
                    new ModuleLoadException("Unexpected exception trying to read module at " + source, e)
            ));
        }
    }
}