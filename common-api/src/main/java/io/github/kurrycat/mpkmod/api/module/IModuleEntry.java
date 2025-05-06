package io.github.kurrycat.mpkmod.api.module;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface IModuleEntry {
    String id();

    IVersion version();

    String entrypoint();

    String name();

    String description();

    List<String> authors();

    String sources();

    String license();

    Path icon();

    Map<String, IVersionConstraint> dependencies();
}
