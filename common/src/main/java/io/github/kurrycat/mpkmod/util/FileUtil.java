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

    public static void createFile(String filePath, String defaultContent) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    API.LOGGER.info(API.CONFIG_MARKER, "Created new file: {}", filePath);
                } else {
                    API.LOGGER.warn(API.CONFIG_MARKER, "Failed to create file: {}", filePath);
                }
            } catch (Exception e) {
                API.LOGGER.error(API.CONFIG_MARKER, "Error creating file: {}", filePath, e);
            }
        }
        // Optionally write default content to the file
        if (defaultContent != null && !defaultContent.isEmpty()) {
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                writer.write(defaultContent);
            } catch (Exception e) {
                API.LOGGER.error(API.CONFIG_MARKER, "Error writing default content to file: {}", filePath, e);
            }
        }
    }
}
