package io.github.kurrycat.mpkmod;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.*;
import io.github.kurrycat.mpkmod.discord.DiscordRPC;
import io.github.kurrycat.mpkmod.events.Event;
import io.github.kurrycat.mpkmod.events.EventAPI;
import io.github.kurrycat.mpkmod.events.OnKeyInputEvent;
import io.github.kurrycat.mpkmod.events.OnRenderWorldOverlayEvent;
import io.github.kurrycat.mpkmod.gui.TickThread;
import io.github.kurrycat.mpkmod.gui.components.Component;
import io.github.kurrycat.mpkmod.gui.screens.LandingBlockGuiScreen;
import io.github.kurrycat.mpkmod.gui.screens.main_gui.LabelConfiguration;
import io.github.kurrycat.mpkmod.modules.MPKModule;
import io.github.kurrycat.mpkmod.modules.ModuleManager;
import io.github.kurrycat.mpkmod.util.ArrayListUtil;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

import static io.github.kurrycat.mpkmod.compatibility.API.mainGUI;

public class Main implements MPKModule {
    public static boolean discordRpcInitialized = false;

    @Override
    public void init() {
        LabelConfiguration.init();
        EventAPI.init();

        API.LOGGER.info(API.DISCORD_RPC_MARKER, "Starting DiscordRPC...");
        try {
            DiscordRPC.init();
            discordRpcInitialized = true;
        } catch (Exception e) {
            API.LOGGER.error(API.DISCORD_RPC_MARKER, "Unexpected exception while initializing DiscordRPC:");
            e.printStackTrace();
            discordRpcInitialized = false;
        }
        TickThread.startThread();

        EventAPI.addListener(
                new EventAPI.EventListener<OnKeyInputEvent>(event -> {
                    if (event.keyCode == InputConstants.KEY_M && Keyboard.getPressedButtons().contains(InputConstants.KEY_F3)) {
                        API.LOGGER.info("Reloading mpkmodules...");
                        ModuleManager.reloadAllModules();
                    }
                }, Event.EventType.KEY_INPUT));

        EventAPI.addListener(EventAPI.EventListener.onTickStart(e -> API.tickTime++));
        EventAPI.addListener(EventAPI.EventListener.onTickStart(e -> {
            TickThread.setTickables(
                    ArrayListUtil.getAllOfType(TickThread.Tickable.class, mainGUI.movableComponents)
            );
        }));

        EventAPI.addListener(
                EventAPI.EventListener.onRenderOverlay(
                        e -> {
                            Profiler.startSection("components");
                            if (mainGUI != null) {
                                mainGUI.setSize(Renderer2D.getScaledSize());
                                for (Component c : mainGUI.movableComponents) {
                                    Profiler.startSection(c.getClass().getSimpleName());
                                    c.render(new Vector2D(-1, -1));
                                    Profiler.endSection();
                                }
                            }
                            Profiler.endSection();
                        }
                )
        );

        EventAPI.addListener(
                new EventAPI.EventListener<OnRenderWorldOverlayEvent>(
                        e -> {
                            Profiler.startSection("renderLBOverlays");
                            LandingBlockGuiScreen.lbs.forEach(lb -> {
                                        if (lb.enabled || lb.highlight && lb.boundingBox != null)
                                            Renderer3D.drawBox(
                                                    lb.boundingBox.expand(0.005D),
                                                    lb.highlight ?
                                                            new Color(98, 255, 74, 157) :
                                                            new Color(255, 68, 68, 157),
                                                    e.partialTicks
                                            );
                                    }
                            );
                            Profiler.endSection();
                        },
                        Event.EventType.RENDER_WORLD_OVERLAY
                )
        );

        EventAPI.addListener(
                EventAPI.EventListener.onTickEnd(
                        e -> {
                            Profiler.startSection("calculateLBOffsets");
                            LandingBlockGuiScreen.calculateLBOffsets()
                                    .forEach(offset -> {
                                        if (mainGUI != null)
                                            mainGUI.postMessage(
                                                    "offset",
                                                    MathUtil.formatDecimals(offset.getX(), 5, false) +
                                                            ", " + MathUtil.formatDecimals(offset.getZ(), 5, false),
                                                    offset.getX() > 0 && offset.getZ() > 0
                                            );
                                    });
                            Profiler.endSection();
                        }
                )
        );

        /*EventAPI.addListener(
                EventAPI.EventListener.onTickStart(
                        e -> {
                            if (metronome == 0)
                                SoundManager.playButtonSound();

                            if (metronome == 11) {
                                metronome = 0;
                            } else metronome++;
                        }
                )
        );*/
    }

    @Override
    public void loaded() {

    }
}
