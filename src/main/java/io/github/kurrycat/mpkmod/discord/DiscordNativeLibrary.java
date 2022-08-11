package io.github.kurrycat.mpkmod.discord;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

// https://github.com/JnCrMx/discord-game-sdk4j

public class DiscordNativeLibrary {
    public static final String DISCORD_GAME_SDK_URL = "https://dl-game-sdk.discordapp.net/2.5.6/discord_game_sdk.zip";
    public static final String DISCORD_FILE_NAME = "discord_game_sdk";
    private static String getLibPath() throws IOException {
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        if (arch.equals("amd64")) arch = "x86_64";

        return arch + "/" + DISCORD_FILE_NAME + getSuffix();
    }

    private static String getSuffix() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        String suffix;
        if (osName.contains("windows")) {
            suffix = ".dll";
        } else if (osName.contains("linux")) {
            suffix = ".so";
        } else if (osName.contains("mac os")) {
            suffix = ".dylib";
        } else {
            throw new RuntimeException("cannot determine OS type: " + osName);
        }
        return suffix;
    }

    public static File getNativeLibrary() {
        File lib = null;
        try {
            lib = openNativeLibraryFromResources();
        } catch (IOException e) {
            System.out.println("Could not load discord library from resources. Attempting to download...");
        }

        try {
            lib = downloadNativeLibrary();
        } catch (IOException e) {
            System.out.println("Could not download discord library");
        }

        return lib;
    }
    public static File openNativeLibraryFromResources() throws IOException {
        return new File(getLibPath());
    }
    public static File downloadNativeLibrary() throws IOException {
        // Path of Discord's library inside the ZIP
        String zipPath = "lib/" + getLibPath();

        // Open the URL as a ZipInputStream
        URL downloadUrl = new URL(DISCORD_GAME_SDK_URL);
        HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
        connection.setRequestProperty("User-Agent", "discord-game-sdk4j");
        ZipInputStream zin = new ZipInputStream(connection.getInputStream());

        // Search for the right file inside the ZIP
        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null) {
            if (entry.getName().equals(zipPath)) {
                // Create a new temporary directory
                // We need to do this, because we may not change the filename on Windows
                File tempDir = new File(System.getProperty("java.io.tmpdir"), "java-" + DISCORD_FILE_NAME + System.nanoTime());
                if (!tempDir.mkdir())
                    throw new IOException("Cannot create temporary directory");
                tempDir.deleteOnExit();

                // Create a temporary file inside our directory (with a "normal" name)
                File temp = new File(tempDir, DISCORD_FILE_NAME + getSuffix());
                temp.deleteOnExit();

                // Copy the file in the ZIP to our temporary file
                Files.copy(zin, temp.toPath());

                // We are done, so close the input stream
                zin.close();

                // Return our temporary file
                return temp;
            }
            // next entry
            zin.closeEntry();
        }
        zin.close();
        // We couldn't find the library inside the ZIP
        return null;
    }
}
