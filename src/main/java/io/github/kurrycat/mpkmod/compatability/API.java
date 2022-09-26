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
import io.github.kurrycat.mpkmod.gui.screens.MapOverviewGUI;
import io.github.kurrycat.mpkmod.landingblock.LandingBlock;
import io.github.kurrycat.mpkmod.save.Serializer;
import io.github.kurrycat.mpkmod.util.JSONConfig;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.time.Instant;

public class API {
    public static final String MODID = "mpkmod";
    public static final String NAME = "MPK Mod";
    public static final String VERSION = "2.0";
    public static final String KEYBINDING_CATEGORY = NAME;
    public static Instant gameStartedInstant;
    private static MPKGuiScreen guiScreen;

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

        EventAPI.init();

        DiscordRPC.init();

        EventAPI.addListener(
                EventAPI.EventListener.onRenderOverlay(
                        e -> {
                            for (Component c : ((MainGuiScreen) getGuiScreen()).movableComponents) {
                                c.render(new Vector2D(-1, -1));
                            }
                        }
                )
        );

        EventAPI.addListener(
                new EventAPI.EventListener<OnRenderWorldOverlayEvent>(
                        e -> {
                            MapOverviewGUI.bbs.forEach(bb ->
                                    Renderer3D.drawBox(
                                            bb,
                                            new Color(255, 68, 68, 157),
                                            e.partialTicks
                                    )
                            );
                        },
                        Event.EventType.RENDER_WORLD_OVERLAY
                )
        );

        EventAPI.addListener(
                EventAPI.EventListener.onTickEnd(
                        e -> {
                            if (Player.getLatest() == null) return;
                            MapOverviewGUI.bbs.stream()
                                    .filter(LandingBlock::isTryingToLandOn)
                                    .peek(System.out::println)
                                    .map(bb -> bb.distanceTo(Player.getLatest().getBB()))
                                    .peek(System.out::println)
                                    .filter(vec -> vec.lengthSqr() < 0.3 * 0.3)
                                    .forEach(System.out::println);
                        }
                )
        );
    }

    public static class Events {
        public static void onTickStart() {
            EventAPI.postEvent(new OnTickStartEvent());
        }

        public static void onTickEnd() {
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
