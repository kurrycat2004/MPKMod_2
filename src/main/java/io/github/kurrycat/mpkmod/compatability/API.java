package io.github.kurrycat.mpkmod.compatability;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Minecraft;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.discord.DiscordRPC;
import io.github.kurrycat.mpkmod.events.*;
import io.github.kurrycat.mpkmod.gui.MPKGuiScreen;
import io.github.kurrycat.mpkmod.gui.MainGuiScreen;
import io.github.kurrycat.mpkmod.gui.components.Component;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.time.Instant;

public class API {
    public static final String MODID = "mpkmod";
    public static final String NAME = "MPK Mod";
    public static final String VERSION = "2.0";
    public static final String KEYBINDING_CATEGORY = NAME;
    public static Instant gameStartedInstant;
    private static MPKGuiScreen guiScreen;

    private static Player lastPlayer = null;

    public static Player getLastPlayer() {
        return lastPlayer;
    }

    public static MPKGuiScreen getGuiScreen() {
        if (guiScreen == null) {
            guiScreen = new MainGuiScreen();
        }
        return guiScreen;
    }

    public static void init(String mcVersion) {
        Minecraft.version = mcVersion;

        gameStartedInstant = Instant.now();

        EventAPI.init();

        DiscordRPC.init();

        EventAPI.addListener(
                EventAPI.EventListener.onRenderOverlay(
                        e -> {
                            for (Component c : ((MainGuiScreen) getGuiScreen()).components) {
                                c.render(new Vector2D(-1, -1));
                            }
                        }
                )
        );
    }

    public static class Events {
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

        public static void onLoadComplete() {
            getGuiScreen().onGuiInit();
        }

        public static void onServerConnect(boolean isLocal) {
            Minecraft.updateWorldState(Event.EventType.SERVER_CONNECT, isLocal);
            DiscordRPC.updateWorldAndPlayState();
        }

        public static void onServerDisconnect() {
            Minecraft.updateWorldState(Event.EventType.SERVER_DISCONNECT, false);
            DiscordRPC.updateWorldAndPlayState();
        }
    }
}
