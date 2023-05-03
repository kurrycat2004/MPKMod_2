package io.github.kurrycat.mpkmod.discord;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.GameSDKException;
import de.jcm.discordgamesdk.activity.Activity;
import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft;
import io.github.kurrycat.mpkmod.gui.screens.options_gui.Option;

import java.io.File;

public class DiscordRPC {
    public static final long CLIENT_ID = 773933401296076800L;
    public static Core core = null;
    public static boolean LIBRARY_LOADED = false;

    @Option.Field
    public static boolean discordRPCEnabled = true;

    public static void init() {
        File discordLibrary = DiscordNativeLibrary.getNativeLibrary();
        if (discordLibrary == null) {
            API.LOGGER.info(API.DISCORD_RPC_MARKER, "Discord library not found.");
            return;
        }
        // Initialize the Core
        Core.init(discordLibrary);
        API.LOGGER.info(API.DISCORD_RPC_MARKER, "DiscordRPC Core initialized.");
        LIBRARY_LOADED = true;

        onEnabledStatusChanged();

        startCallbackThread();
        API.LOGGER.info(API.DISCORD_RPC_MARKER, "Started DiscordRPC callback thread");
    }

    public static void createCore() {
        try (CreateParams params = new CreateParams()) {
            params.setClientID(CLIENT_ID);
            params.setFlags(CreateParams.getDefaultFlags());

            try {
                core = new Core(params);
            } catch (GameSDKException e) {
                API.LOGGER.info(API.DISCORD_RPC_MARKER, "DiscordRPC Core creation failed: ");
                e.printStackTrace();
            }
        }
    }

    @Option.ChangeListener(field = "discordRPCEnabled")
    public static void onEnabledStatusChanged() {
        if (!LIBRARY_LOADED) {
            API.LOGGER.info(API.DISCORD_RPC_MARKER, "DiscordRPC library not loaded correctly, unable to update rich presence");
            return;
        }

        if (discordRPCEnabled) {
            createCore();
            updateWorldAndPlayState();
            API.LOGGER.info(API.DISCORD_RPC_MARKER, "DiscordRPC started");
        } else {
            if (core != null)
                core.close();
            core = null;
            API.LOGGER.info(API.DISCORD_RPC_MARKER, "DiscordRPC disabled in options, turn on to activate");
        }
    }

    public static void updateActivity(String details, String state) {
        if (!LIBRARY_LOADED) return;
        if (!API.discordRpcInitialized) return;
        if (!discordRPCEnabled) return;

        try (Activity activity = new Activity()) {
            activity.setDetails(details);
            if (state != null)
                activity.setState(state);

            activity.timestamps().setStart(API.gameStartedInstant);

            activity.assets().setLargeImage("mpkmod_logo");
            if (core != null && core.isOpen())
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
            state = (Minecraft.playState == Minecraft.PlayState.AFK ? "AFK on " : "Playing on ") + Minecraft.getIp();
        }

        updateActivity(details, state);
    }

    public static void startCallbackThread() {
        Thread t = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (core != null && core.isOpen()) core.runCallbacks();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    if (core != null)
                        core.close();
                    break;
                }
            }
            if (core != null)
                core.close();
        }, "Discord_RPC_Callback_Handler");
        t.start();
    }
}
