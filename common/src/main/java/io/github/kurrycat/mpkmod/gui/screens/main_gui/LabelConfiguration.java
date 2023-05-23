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

    private File saveFile;

    public LabelConfiguration() {
        this(new ArrayList<>());
    }

    public LabelConfiguration(ArrayList<Component> components) {
        this(null, components);
    }

    public LabelConfiguration(File saveFile, ArrayList<Component> components) {
        this.saveFile = saveFile;
        this.components = components;
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
                LabelConfiguration c = LabelConfiguration.fromFile(file);
                if (c == null) continue;
                savedConfigs.put(FileUtil.getName(file), c);
            }
        }

        customConfigurationFile = new File(JSONConfig.configFolderPath + customConfigurationFileName);
        currentConfig = LabelConfiguration.fromFile(customConfigurationFile);
        if (currentConfig == null) {
            currentConfig = presets.getOrDefault("default", new LabelConfiguration()).copy();
            currentConfig.saveFile = customConfigurationFile;
        }
        currentConfig.isMutable = true;
    }

    public static LabelConfiguration fromFile(File file) {
        ArrayList<Component> components = loadComponentsFromFile(file);
        if (components == null) return null;
        return new LabelConfiguration(file, components);
    }

    public LabelConfiguration copy() {
        if (components.isEmpty()) return new LabelConfiguration();

        String components = Serializer.serializeAsString(this.components);
        Component[] copy = Serializer.deserializeString(components, Component[].class);
        if (copy == null) return new LabelConfiguration();

        return new LabelConfiguration(new ArrayList<>(Arrays.asList(copy)));
    }

    private static ArrayList<Component> loadComponentsFromFile(File file) {
        Component[] components = Serializer.deserialize(file, Component[].class);
        if (components == null) return null;
        return new ArrayList<>(Arrays.asList(components));
    }

    public void selectAsCurrent() {
        reloadFromFile();
        currentConfig = copy();
        currentConfig.saveFile = customConfigurationFile;
    }

    public LabelConfiguration reloadFromFile() {
        if (saveFile == null) return this;
        ArrayList<Component> components = loadComponentsFromFile(saveFile);
        if (components == null) return this;
        this.components = components;
        return this;
    }

    public static void delete(String name) {
        File file = new File(savedConfigsFolderName + name + ".json");
        if (!file.exists()) return;
        if (file.delete()) {
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
}
