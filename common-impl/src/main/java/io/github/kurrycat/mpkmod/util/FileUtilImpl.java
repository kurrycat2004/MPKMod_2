package io.github.kurrycat.mpkmod.util;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.util.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipError;

public final class FileUtilImpl implements FileUtil {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<FileUtil> {
        public Provider() {
            super(FileUtilImpl::new, FileUtil.class);
        }
    }

    public static String getFileNameWithoutExtension(Path path) {
        String filename = path.getFileName().toString();
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex < 0 ? filename : filename.substring(0, dotIndex);
    }

    public static Path resolve(Path base, String relativePath) {
        return base.resolve(relativePath.replace("/", base.getFileSystem().getSeparator()));
    }

    public static Path resolve(Path base, Path relativePath) {
        return resolve(base, relativePath.toString());
    }

    @Override
    public Path getRootPath(Path path) throws IOException {
        if (Files.isDirectory(path)) return path;
        return getOrCreateJarFileSystem(path).getRootDirectories().iterator().next();
    }

    @Override
    public FileSystem getOrCreateJarFileSystem(Path path) throws IOException {
        URI jarUri = URI.create("jar:" + path.toUri());

        FileSystem fileSystem;
        try {
            fileSystem = FileSystems.getFileSystem(jarUri);
        } catch (FileSystemNotFoundException ignored) {
            try {
                fileSystem = FileSystems.newFileSystem(jarUri, Collections.emptyMap());
            } catch (FileSystemAlreadyExistsException ignored2) {
                fileSystem = FileSystems.getFileSystem(jarUri);
            } catch (IOException | ZipError e) {
                throw new IOException("Error accessing " + jarUri + ": " + e, e);
            }
        }
        return fileSystem;
    }

    public static FileSystem getJarFileSystem(Path path) {
        try {
            return FileSystems.getFileSystem(URI.create("jar:" + path.toUri()));
        } catch (FileSystemNotFoundException e) {
            return null;
        }
    }

    public static void tryCloseJar(Path path) {
        FileSystem fileSystem = FileUtilImpl.getJarFileSystem(path);
        if (fileSystem != null && fileSystem.isOpen()) {
            try {
                fileSystem.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static String getOrCreateSha256Sum(Path file) throws IOException {
        Path shaFile = file.resolveSibling(file.getFileName() + ".sha256");
        if (Files.exists(shaFile)) {
            List<String> lines = Files.readAllLines(shaFile, StandardCharsets.UTF_8);
            for (String line : lines) {
                String[] parts = line.trim().split("\\s+\\*", 2); // match "<hash> *filename"
                if (parts.length == 2 && parts[1].equals(file.getFileName().toString())) {
                    return parts[0];
                }
            }
        }
        return createSha256Sum(file);
    }

    public static String createSha256Sum(Path file) throws IOException {
        String hash = computeHash(file);
        String line = hash + " *" + file.getFileName().toString() + "\n";
        Path shaFile = file.resolveSibling(file.getFileName() + ".sha256");
        Files.writeString(shaFile, line, StandardCharsets.UTF_8);
        return hash;
    }

    /**
     * Compute the SHA-256 hash of a file or directory.
     *
     * @param path the path to the file or directory
     * @return the SHA-256 hash as a hex string
     * @throws IOException if an I/O error occurs
     */
    public static String computeHash(Path path) throws IOException {
        try {
            byte[] buffer = new byte[8192];
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            if (Files.isDirectory(path)) {
                try (Stream<Path> stream = Files.walk(path)) {
                    List<Path> files = stream.filter(Files::isRegularFile)
                            .sorted(Comparator.comparing(Path::toString))
                            .toList();

                    for (Path file : files) {
                        Path relative = path.relativize(file);
                        digest.update(relative.toString().getBytes(StandardCharsets.UTF_8));

                        updateDigest(digest, buffer, file);
                    }
                }
            } else {
                updateDigest(digest, buffer, path);
            }

            byte[] hashBytes = digest.digest();
            return toHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    private static void updateDigest(MessageDigest digest, byte[] buffer, Path file) throws IOException {
        int read;
        try (InputStream inputStream = Files.newInputStream(file)) {
            while ((read = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    public static Path createHiddenDir(Path dirPath) {
        if (!dirPath.getFileName().toString().startsWith(".")) {
            dirPath = dirPath.getParent().resolve("." + dirPath.getFileName());
        }
        try {
            if (Files.isDirectory(dirPath)) return dirPath;

            Files.createDirectories(dirPath);
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                Files.setAttribute(dirPath, "dos:hidden", true);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return dirPath;
    }

    public static Path getOrCreateDir(Path dirPath) {
        if (Files.isDirectory(dirPath)) return dirPath;

        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return dirPath;
    }
}
