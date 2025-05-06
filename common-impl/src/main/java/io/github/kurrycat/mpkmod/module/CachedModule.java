package io.github.kurrycat.mpkmod.module;

import java.nio.file.Path;

public record CachedModule(
        Path source,
        String sourceHash,
        ModuleEntry entry
) {
}
