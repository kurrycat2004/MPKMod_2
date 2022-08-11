package io.github.kurrycat.mpkmod.compatability;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.discord.DiscordRPC;

import java.time.Instant;

public class API {
    public static Instant gameStartedInstant;
    public static String mcVersion;
    public static final String MODID = "assets/mpkmod";
    public static final String NAME = "MPK Mod";
    public static final String VERSION = "2.0";
    public static final String KEYBINDING_CATEGORY = NAME;

    public static MPKGuiScreen guiScreen;
    public static void init(String mcVersion) {
        API.mcVersion = mcVersion;
        gameStartedInstant = Instant.now();

        guiScreen = new MainGuiScreen();

        System.out.println("Loaded\n\n\n\n\n\n\n\n\n\n\n\n");

        DiscordRPC.init();
    }

    public static void onTickStart(Player player) {

    }

    public static void onTickEnd(Player player) {

    }
}
