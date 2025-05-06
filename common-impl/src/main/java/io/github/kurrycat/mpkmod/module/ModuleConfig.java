package io.github.kurrycat.mpkmod.module;

import io.github.kurrycat.mpkmod.api.module.IVersion;
import io.github.kurrycat.mpkmod.api.module.IVersionConstraint;
import io.github.kurrycat.mpkmod.api.module.InvalidVersionConstraintException;
import io.github.kurrycat.mpkmod.util.FileUtilImpl;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class ModuleConfig {
    private static final String MODULES_DIR = "META-INF/mpkmodules";
    private static final Pattern VALID_ID_PATTERN = Pattern.compile("[a-z0-9_]+");

    private ModuleConfig() {
    }

    public static List<ModuleEntry> load(Path root) throws ModuleLoadException {
        Path modulesDir = FileUtilImpl.resolve(root, MODULES_DIR);
        if (!Files.isDirectory(modulesDir)) return List.of();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(modulesDir)) {
            List<ModuleEntry> modules = new ArrayList<>();
            for (Path path : stream) {
                if (!Files.isRegularFile(path) || !path.toString().endsWith(".toml")) {
                    continue;
                }
                modules.add(loadFromFile(root, path));
            }
            return modules;
        } catch (IOException e) {
            throw new ModuleLoadException("Failed to read module directory", e);
        }
    }

    private static ModuleEntry loadFromFile(Path root, Path configFile) throws ModuleLoadException {
        TomlParseResult result;
        try {
            result = Toml.parse(configFile);
        } catch (Exception e) {
            throw new ModuleLoadException("Failed to parse module .toml", e);
        }

        if (result.hasErrors()) {
            ModuleLoadException.Builder builder = new ModuleLoadException.Builder("Syntax errors while parsing modules.toml");
            result.errors().forEach(error -> builder.addError(
                    "At line " + error.position().line() + ", column " + error.position().column() + ": " + error.getMessage()
            ));
            throw builder.build();
        }

        ModuleLoadException.Builder errors =
                new ModuleLoadException.Builder("Invalid module definition: " + configFile);

        Long formatVersion = getLong(result, "format_version", errors, true);
        if (formatVersion == null || formatVersion != 1) {
            throw errors.build();
        }

        return parseModuleEntry(root, result, errors);
    }

    private static ModuleEntry parseModuleEntry(Path root, TomlTable table, ModuleLoadException.Builder errors) throws ModuleLoadException {
        String id = getString(table, "id", errors, true);
        if (id != null && !VALID_ID_PATTERN.matcher(id).matches()) {
            errors.addError("Invalid module displayName: '" + id + "'. Must match [a-z0-9_]+.");
        }

        String versionString = getString(table, "version", errors, true);
        IVersion version = null;
        if (versionString != null) {
            try {
                version = SemVer.parse(versionString);
            } catch (SemVer.InvalidVersionFormatException e) {
                errors.addError(e);
            }
        }

        String entrypoint = getString(table, "entrypoint", errors, true);
        String name = getString(table, "name", errors, true);
        String description = getString(table, "description", errors, false);
        List<String> authors = getStringList(table, "authors", errors, false);
        String source = getString(table, "source", errors, false);
        String license = getString(table, "license", errors, false);

        String iconString = getString(table, "icon", errors, false);
        Path icon = null;
        if (iconString != null) icon = FileUtilImpl.resolve(root, iconString);

        Map<String, String> dependencyStrings = getStringMap(table, "dependencies", errors, false);
        Map<String, IVersionConstraint> dependencies = new HashMap<>();
        for (Map.Entry<String, String> entry : dependencyStrings.entrySet()) {
            String depId = entry.getKey();
            if (!VALID_ID_PATTERN.matcher(depId).matches()) {
                errors.addError("Dependency module id '" + depId + "' is invalid. Must match [a-z0-9_]+.");
            }

            try {
                IVersionConstraint constraint = SemVer.ConstraintSet.parse(entry.getValue());
                dependencies.put(depId, constraint);
            } catch (InvalidVersionConstraintException e) {
                errors.addError(e);
            }
        }

        if (errors.hasErrors()) {
            throw errors.build();
        }

        return new ModuleEntry(id, version, entrypoint, name, description,
                authors, source, license, icon, dependencies);
    }

    private static Long getLong(TomlTable table, String key, ModuleLoadException.Builder errors, boolean required) {
        return getValue(table, key, Long.class, errors, required);
    }

    private static String getString(TomlTable table, String key, ModuleLoadException.Builder errors, boolean required) {
        return getValue(table, key, String.class, errors, required);
    }

    private static List<String> getStringList(TomlTable table, String key, ModuleLoadException.Builder errors, boolean required) {
        return getList(table, key, String.class, errors, required);
    }

    private static Map<String, String> getStringMap(TomlTable table, String key, ModuleLoadException.Builder errors, boolean required) {
        return getMap(table, key, String.class, errors, required);
    }

    private static <T> T getValue(TomlTable table, String key, Class<T> type, ModuleLoadException.Builder errors, boolean required) {
        Object value = table.get(Collections.singletonList(key));
        if (value == null) {
            if (required) errors.addError("Missing required field: '" + key + "'");
            return null;
        }
        if (type.isInstance(value)) {
            return type.cast(value);
        } else {
            errors.addError("Field '" + key + "' is not of type " + type.getSimpleName());
            return null;
        }
    }

    private static <T> List<T> getList(TomlTable table, String key, Class<T> elementType, ModuleLoadException.Builder errors, boolean required) {
        TomlArray array = getValue(table, key, TomlArray.class, errors, required);
        if (array == null) return List.of();
        List<T> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            Object item = array.get(i);
            if (elementType.isInstance(item)) {
                list.add(elementType.cast(item));
            } else {
                errors.addError("Field '" + key + "' array item " + i + " is not of type " + elementType.getSimpleName());
            }
        }
        return list;
    }

    private static <T> Map<String, T> getMap(TomlTable table, String key, Class<T> valueType, ModuleLoadException.Builder errors, boolean required) {
        TomlTable tomlTable = getValue(table, key, TomlTable.class, errors, required);
        if (tomlTable == null) return Map.of();
        Map<String, T> map = new HashMap<>();
        for (String tableKey : tomlTable.keySet()) {
            Object version = tomlTable.get(tableKey);
            if (valueType.isInstance(version)) {
                map.put(tableKey, valueType.cast(version));
            } else {
                errors.addError("Map value for key '" + tableKey + "' is not of type " + valueType.getSimpleName());
            }
        }
        return map;
    }
}