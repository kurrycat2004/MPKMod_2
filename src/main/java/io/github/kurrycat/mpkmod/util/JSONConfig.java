package io.github.kurrycat.mpkmod.util;

import java.io.File;

public class JSONConfig {

    public static final String configFileName = "mpk-config.json";
    public static final String optionsFileName = "mpk-options.json";
    public static final String configFolderPath = "config/mpk/config/";

    public static File configFile;
    public static File optionsFile;

    public static void setupFiles() {
        File dir = new File(configFolderPath);
        if (!dir.exists()) dir.mkdirs();
        configFile = new File(configFolderPath + configFileName);
        optionsFile = new File(configFolderPath + optionsFileName);
    }

}
