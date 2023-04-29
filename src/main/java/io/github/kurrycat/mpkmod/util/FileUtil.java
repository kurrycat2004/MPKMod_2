package io.github.kurrycat.mpkmod.util;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    public static InputStream getResource(String path) {
        return ClassUtil.ModClass.getResourceAsStream(path);
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
        if(n.indexOf('.') != -1)
            n = n.substring(0, n.lastIndexOf('.'));
        return n;
    }
}
