package io.github.kurrycat.mpkmod.compatability;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.compatability.functions.DrawStringFunction;
import io.github.kurrycat.mpkmod.discord.DiscordRPC;
import io.github.kurrycat.mpkmod.events.EventAPI;
import io.github.kurrycat.mpkmod.events.OnRenderOverlayEvent;
import io.github.kurrycat.mpkmod.events.OnTickEndEvent;
import io.github.kurrycat.mpkmod.events.OnTickStartEvent;

import java.awt.*;
import java.time.Instant;

public class API {
    public static final String MODID = "mpkmod";
    public static final String NAME = "MPK Mod";
    public static final String VERSION = "2.0";
    public static final String KEYBINDING_CATEGORY = NAME;
    public static Instant gameStartedInstant;
    public static String mcVersion;
    private static MPKGuiScreen guiScreen;

    private static Player lastPlayer = null;

    public static MPKGuiScreen getGuiScreen() {
        if (guiScreen == null) guiScreen = new MainGuiScreen();
        return guiScreen;
    }

    public static void init(String mcVersion) {
        API.mcVersion = mcVersion;
        gameStartedInstant = Instant.now();

        EventAPI.init();

        DiscordRPC.init();

        EventAPI.addListener(EventAPI.EventListener.onRenderOverlay(e -> FontRenderer.drawString("TEST", 50, 50, Color.RED, true)));
    }

    public static void registerDrawString(DrawStringFunction function) {
        FontRenderer.registerDrawString(function);
    }

    public static void onTickStart(Player player) {
        lastPlayer = player;
        EventAPI.postEvent(new OnTickStartEvent(player));
    }

    public static void onTickEnd(Player player) {
        lastPlayer = player;
        EventAPI.postEvent(new OnTickEndEvent(player));
    }

    public static void onRenderOverlay() {
        EventAPI.postEvent(new OnRenderOverlayEvent(lastPlayer));
    }

}
