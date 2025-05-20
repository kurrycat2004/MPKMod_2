package io.github.kurrycat.mpkmod.api.util;

import io.github.kurrycat.mpkmod.service.TypedServiceProvider;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

public interface FileUtil {
    FileUtil INSTANCE = TypedServiceProvider.loadOrThrow(FileUtil.class);

    /**
     * If path is a directory, return the path itself. <br>
     * Otherwise interpret the path as a jar file and return the root path of the jar file.
     *
     * @param path the path to check
     * @return the root path of the jar file or the path itself
     * @see #getOrCreateJarFileSystem(Path)
     */
    Path getRootPath(Path path) throws IOException;

    /**
     * Get the file system of a jar file.
     *
     * <p>Note: This method will create a new file system if it does not exist and <strong>will not close it</strong>.</p>
     *
     * @param path the path to the jar file
     * @return the file system of the jar file
     * @throws IOException if an I/O error occurs
     */
    FileSystem getOrCreateJarFileSystem(Path path) throws IOException;
}
