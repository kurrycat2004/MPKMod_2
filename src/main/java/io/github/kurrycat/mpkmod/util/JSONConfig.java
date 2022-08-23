package io.github.kurrycat.mpkmod.util;

import java.io.File;

public class JSONConfig {

    public static final String fileName = "mpk-config.json";
    public static final String configPath = "mods/mpk/config/";
    public static final String configFilePath = configPath + fileName;

    public static File configFile;

    public static void setupFile() {
        File dir = new File(configPath);
        if (!dir.exists()) dir.mkdirs();
        configFile = new File(configFilePath);
    }

}
