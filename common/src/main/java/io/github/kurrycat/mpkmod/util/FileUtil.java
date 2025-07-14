package io.github.kurrycat.mpkmod.util;

import io.github.kurrycat.mpkmod.compatibility.API;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    public static InputStream getResource(String path) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (is == null)
            is = ClassUtil.ModClass.getResourceAsStream(path);
        return is;
    }

    public static void copyResource(String path, File file) {
        InputStream is = getResource(path);
        if (is == null) {
            API.LOGGER.error(API.CONFIG_MARKER, "Resource not found: {}", path);
            return;
        }
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            java.nio.file.Files.copy(is, file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            API.LOGGER.error(API.CONFIG_MARKER, "Failed to copy resource {} to file {}", path, file.getAbsolutePath(), e);
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                API.LOGGER.error(API.CONFIG_MARKER, "Failed to close InputStream for resource {}", path, e);
            }
        }
    }

    public static List<File> getJSONFiles(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) return null;
        if (!folder.isDirectory()) return null;


        File[] files = folder.listFiles();
        if (files == null) return null;

        List<File> result = new ArrayList<>();
        for (File file : files) {
            if (file.getName().endsWith(".json"))
                result.add(file);
        }
        return result;
    }

    public static String getName(File file) {
        String n = file.getName();
        if (n.indexOf('.') != -1)
            n = n.substring(0, n.lastIndexOf('.'));
        return n;
    }
}
