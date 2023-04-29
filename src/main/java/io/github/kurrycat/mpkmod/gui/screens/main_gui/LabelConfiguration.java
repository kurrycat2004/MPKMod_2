package io.github.kurrycat.mpkmod.gui.screens.main_gui;

import io.github.kurrycat.mpkmod.gui.components.Component;
import io.github.kurrycat.mpkmod.save.Serializer;
import io.github.kurrycat.mpkmod.util.Copyable;
import io.github.kurrycat.mpkmod.util.FileUtil;
import io.github.kurrycat.mpkmod.util.JSONConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LabelConfiguration implements Copyable<LabelConfiguration> {
    public static final HashMap<String, LabelConfiguration> presets = new HashMap<>();
    public static final HashMap<String, LabelConfiguration> savedConfigs = new HashMap<>();
    public static final String presetsFolderName = "/assets/mpkmod/presets/";
    public static final String[] presetsFileNames = {"default"};
    public static final String savedConfigsFolderName = "config/mpk/config/saved_configs/";
    public static final String customConfigurationFileName = "mpk-config.json";
    public static LabelConfiguration currentConfig;
    public static File customConfigurationFile;

    public ArrayList<Component> components;
    public boolean isMutable = false;

    public LabelConfiguration(ArrayList<Component> components) {
        this.components = components;
    }

    public LabelConfiguration() {
        this(new ArrayList<>());
    }

    public static void init() {
        File savedConfigsFolder = new File(savedConfigsFolderName);
        if (!savedConfigsFolder.exists()) savedConfigsFolder.mkdir();

        for (String fileName : presetsFileNames) {
            InputStream in = FileUtil.getResource(presetsFolderName + fileName + ".json");
            if (in == null) continue;
            Component[] components = Serializer.deserialize(in, Component[].class);
            if (components == null) continue;
            presets.put(fileName, new LabelConfiguration(new ArrayList<>(Arrays.asList(components))));
        }

        List<File> files = FileUtil.getJSONFiles(savedConfigsFolderName);
        if (files != null) {
            for (File file : files) {
                Component[] components = Serializer.deserialize(file, Component[].class);
                if (components == null) continue;
                savedConfigs.put(FileUtil.getName(file), new LabelConfiguration(new ArrayList<>(Arrays.asList(components))));
            }
        }

        customConfigurationFile = new File(JSONConfig.configFolderPath + customConfigurationFileName);
        Component[] components = Serializer.deserialize(customConfigurationFile, Component[].class);
        if (components == null) currentConfig = presets.getOrDefault("default", new LabelConfiguration()).copy();
        else currentConfig = new LabelConfiguration(new ArrayList<>(Arrays.asList(components)));
        currentConfig.isMutable = true;
    }

    public static void delete(String name) {
        File file = new File(savedConfigsFolderName + name + ".json");
        if(!file.exists()) return;
        if(file.delete()) {
            savedConfigs.remove(name);
        }
    }

    public boolean save(String name) {
        File file = new File(savedConfigsFolderName + name + ".json");
        try {
            if (!file.createNewFile()) return false;
            Serializer.serialize(file, components);
            savedConfigs.put(name, copy());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void saveInCustom() {
        Serializer.serialize(customConfigurationFile, components);
    }

    @Override
    public String toString() {
        return "LabelConfiguration{" +
                "components=" + components +
                '}';
    }

    public LabelConfiguration copy() {
        if (components.isEmpty()) return new LabelConfiguration();

        String components = Serializer.serializeAsString(this.components);
        Component[] copy = Serializer.deserializeString(components, Component[].class);
        if (copy == null) return new LabelConfiguration();

        return new LabelConfiguration(new ArrayList<>(Arrays.asList(copy)));
    }
}
