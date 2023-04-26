package io.github.kurrycat.mpkmod.util;

import java.io.File;
import java.net.URL;

public class JSONConfig {
    public static final String configFileName = "mpk-config.json";
    public static final String optionsFileName = "mpk-options.json";
    public static final String configFolderPath = "config/mpk/config/";

    public static final String defaultConfigFileName = "/assets/mpkmod/presets/default.json";

    public static File configFile;
    public static File optionsFile;
    public static URL defaultConfigURL;

    public static void setupFiles() {
        File dir = new File(configFolderPath);
        if (!dir.exists()) dir.mkdirs();
        configFile = new File(configFolderPath + configFileName);
        optionsFile = new File(configFolderPath + optionsFileName);

        defaultConfigURL = FileUtil.getResource(defaultConfigFileName);
    }
}
