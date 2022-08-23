package io.github.kurrycat.mpkmod.compatability;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Minecraft;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer3D;
import io.github.kurrycat.mpkmod.discord.DiscordRPC;
import io.github.kurrycat.mpkmod.events.Event;
import io.github.kurrycat.mpkmod.events.*;
import io.github.kurrycat.mpkmod.gui.MPKGuiScreen;
import io.github.kurrycat.mpkmod.gui.MainGuiScreen;
import io.github.kurrycat.mpkmod.gui.components.Component;
import io.github.kurrycat.mpkmod.save.Deserializer;
import io.github.kurrycat.mpkmod.save.Serializer;
import io.github.kurrycat.mpkmod.util.BoundingBox;
import io.github.kurrycat.mpkmod.util.JSONConfig;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.Vector3D;

import java.awt.*;
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

    /**
     * Gets called once at the end of the mod loader initialization event
     *
     * @param mcVersion String containing the current minecraft version (e.g. "1.8.9")
     */
    public static void init(String mcVersion) {
        Minecraft.version = mcVersion;

        gameStartedInstant = Instant.now();

        JSONConfig.setupFile();
        Serializer.registerSerializer();
        Deserializer.registerDeserializer();

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

        /*EventAPI.addListener(
                new EventAPI.EventListener<OnRenderWorldOverlayEvent>(
                        e -> Renderer3D.drawBox(
                                new BoundingBox(
                                        new Vector3D(
                                                0, 10, 0
                                        ),
                                        new Vector3D(
                                                1, 11, 1
                                        )
                                ),
                                new Color(255, 68, 68, 157),
                                getLastPlayer(),
                                e.partialTicks
                        ),
                        Event.EventType.RENDER_WORLD_OVERLAY
                )
        );*/
    }

    public static class Events {
        public static void onTickStart(Player player) {
            lastPlayer = player;
            EventAPI.postEvent(new OnTickStartEvent());
        }

        public static void onTickEnd(Player player) {
            lastPlayer = player;
            EventAPI.postEvent(new OnTickEndEvent());
        }

        public static void onRenderOverlay() {
            EventAPI.postEvent(new OnRenderOverlayEvent());
        }

        public static void onRenderWorldOverlay(float partialTicks) {
            EventAPI.postEvent(new OnRenderWorldOverlayEvent(partialTicks));
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
