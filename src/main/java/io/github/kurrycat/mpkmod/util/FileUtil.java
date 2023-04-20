package io.github.kurrycat.mpkmod.util;

import java.net.URL;

public class FileUtil {
    public static URL getResource(String path) {
        return ClassUtil.ModClass.getResource(path);
    }
}
