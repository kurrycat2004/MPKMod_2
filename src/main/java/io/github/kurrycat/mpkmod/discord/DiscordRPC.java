package io.github.kurrycat.mpkmod.discord;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Minecraft;

import java.io.File;

public class DiscordRPC {
    public static final long CLIENT_ID = 773933401296076800L;
    public static Core core = null;

    public static void init() {
        File discordLibrary = DiscordNativeLibrary.getNativeLibrary();
        if (discordLibrary == null) {
            API.LOGGER.info(API.DISCORD_RPC_MARKER, "Discord library not found.");
            return;
        }
        // Initialize the Core
        Core.init(discordLibrary);

        // Set parameters for the Core
        try (CreateParams params = new CreateParams()) {
            params.setClientID(CLIENT_ID);
            params.setFlags(CreateParams.getDefaultFlags());

            core = new Core(params);
        }
        // Create the Activity
        updateWorldAndPlayState();

        startCallbackThread();
    }

    public static void updateActivity(String details, String state) {
        try (Activity activity = new Activity()) {
            activity.setDetails(details);
            if (state != null)
                activity.setState(state);

            activity.timestamps().setStart(API.gameStartedInstant);

            activity.assets().setLargeImage("mpkmod_logo");
            core.activityManager().updateActivity(activity);
        }
    }

    public static void updateWorldAndPlayState() {
        String details = "Minecraft " + Minecraft.version;
        String state = null;


        if (Minecraft.worldState == Minecraft.WorldState.MENU) {
            state = Minecraft.playState == Minecraft.PlayState.AFK ? "AFK in Menu" : "In Menu";
        } else if (Minecraft.worldState == Minecraft.WorldState.SINGLE_PLAYER) {
            state = Minecraft.playState == Minecraft.PlayState.AFK ? "AFK in Singleplayer" : "Playing Singleplayer";
        } else if (Minecraft.worldState == Minecraft.WorldState.MULTI_PLAYER) {
            state = (Minecraft.playState == Minecraft.PlayState.AFK ? "AFK on " : "Playing on ") + Minecraft.getIP();
        }

        updateActivity(details, state);
    }

    public static void startCallbackThread() {
        Thread t = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (core.isOpen()) core.runCallbacks();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    core.close();
                    break;
                }
            }
            core.close();
        }, "Discord_RPC_Callback_Handler");
        t.start();
    }
}
