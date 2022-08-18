package io.github.kurrycat.mpkmod.discord;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import io.github.kurrycat.mpkmod.compatability.API;

import java.io.File;

public class DiscordRPC {
    public static final long CLIENT_ID = 773933401296076800L;
    public static Core core = null;

    public static void init() {
        File discordLibrary = DiscordNativeLibrary.getNativeLibrary();
        if (discordLibrary == null) {
            System.out.println("Discord library not found.");
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
        updateActivity("Testing MPKMod 2 Discord RPC");

        startCallbackThread();
    }

    public static void updateActivity(String details) {
        try (Activity activity = new Activity()) {
            activity.setDetails(details);

            activity.timestamps().setStart(API.gameStartedInstant);

            activity.assets().setLargeImage("mpkmod_logo");
            core.activityManager().updateActivity(activity);
        }
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
