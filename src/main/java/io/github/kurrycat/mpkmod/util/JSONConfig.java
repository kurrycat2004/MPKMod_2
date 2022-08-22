package io.github.kurrycat.mpkmod.util;

import java.io.File;

public class JSONConfig {

    public static final String configPath = "config/";
    public static final String configFilePath = "config/mpk-config.json";

    public static File configFile;

    public static void setupFile() {
        File dir = new File(configPath);
        if (!dir.exists()) dir.mkdir();
        configFile = new File(configFilePath);
    }

}
