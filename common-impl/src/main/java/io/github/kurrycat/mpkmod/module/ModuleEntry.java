package io.github.kurrycat.mpkmod.module;

import io.github.kurrycat.mpkmod.api.module.IModuleEntry;
import io.github.kurrycat.mpkmod.api.module.IVersion;
import io.github.kurrycat.mpkmod.api.module.IVersionConstraint;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ModuleEntry(
        String id,
        IVersion version,
        String entrypoint,
        String name,
        String description,
        List<String> authors,
        String sources,
        String license,
        Path icon,
        Map<String, IVersionConstraint> dependencies
) implements IModuleEntry {
    public ModuleEntry {
        Objects.requireNonNull(id, "displayName cannot be null");
        Objects.requireNonNull(version, "version cannot be null");
        Objects.requireNonNull(entrypoint, "entrypoint cannot be null");

        if (authors == null) authors = List.of();
        else authors = Collections.unmodifiableList(authors);
        if (dependencies == null) dependencies = Map.of();
        else dependencies = Collections.unmodifiableMap(dependencies);
    }
}
