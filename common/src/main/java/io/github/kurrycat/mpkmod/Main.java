package io.github.kurrycat.mpkmod;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.*;
import io.github.kurrycat.mpkmod.discord.DiscordRPC;
import io.github.kurrycat.mpkmod.events.Event;
import io.github.kurrycat.mpkmod.events.*;
import io.github.kurrycat.mpkmod.gui.TickThread;
import io.github.kurrycat.mpkmod.gui.components.Component;
import io.github.kurrycat.mpkmod.gui.components.InputHistory;
import io.github.kurrycat.mpkmod.gui.infovars.InfoString;
import io.github.kurrycat.mpkmod.gui.infovars.InfoTree;
import io.github.kurrycat.mpkmod.gui.screens.LandingBlockGuiScreen;
import io.github.kurrycat.mpkmod.gui.screens.main_gui.LabelConfiguration;
import io.github.kurrycat.mpkmod.gui.screens.main_gui.MainGuiScreen;
import io.github.kurrycat.mpkmod.gui.screens.options_gui.Option;
import io.github.kurrycat.mpkmod.gui.screens.options_gui.OptionsGuiScreen;
import io.github.kurrycat.mpkmod.landingblock.LandingBlock;
import io.github.kurrycat.mpkmod.modules.MPKModule;
import io.github.kurrycat.mpkmod.modules.ModuleManager;
import io.github.kurrycat.mpkmod.ticks.TimingStorage;
import io.github.kurrycat.mpkmod.util.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Main implements MPKModule {
    public static boolean discordRpcInitialized = false;

    public static List<Vector2D> mouseMovements = new ArrayList<>();
    public static MainGuiScreen mainGUI;
    public static InfoTree infoTree;

    @Option.Field(
            category = "labels",
            displayName = "Display Overlay",
            description = "Whether to show all the components on the overlay while playing"
    )
    public static boolean displayOverlay = true;

    @Option.Field(
            category = "landingblocks",
            displayName = "Highlight Landing Blocks",
            description = "Whether to highlight all enabled landing blocks"
    )
    public static boolean highlightLandingBlocks = true;

    @Override
    public void init() {
        infoTree = InfoString.createInfoTree();
        API.LOGGER.info("{} infoVars registered", infoTree.getSize());

        TimingStorage.init();

        mainGUI = new MainGuiScreen();
        API.registerGUIScreen("main_gui", mainGUI);

        API.registerGUIScreen("lb_gui", new LandingBlockGuiScreen());
        API.registerKeyBinding("lb_set",
                () -> {
                    List<BoundingBox3D> boundingBox3DList = WorldInteraction.getLookingAtCollisionBoundingBoxes();
                    List<LandingBlock> lbs = LandingBlock.asLandingBlocks(boundingBox3DList);
                    lbs.forEach(lb -> {
                        if (LandingBlockGuiScreen.lbs.contains(lb))
                            LandingBlockGuiScreen.lbs.remove(lb);
                        else LandingBlockGuiScreen.lbs.add(lb);
                    });
                }
        );

        API.registerGUIScreen("options_gui", new OptionsGuiScreen());
    }

    @Override
    public void loaded() {
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
                    if (Keyboard.getPressedButtons().contains(InputConstants.KEY_F3)) {
                        if (event.keyCode == InputConstants.KEY_M) {
                            if (Keyboard.getPressedButtons().contains(InputConstants.KEY_LSHIFT)) {
                                API.LOGGER.info("Closing all mpkmodules...");
                                ModuleManager.closeAllModules();
                            } else {
                                API.LOGGER.info("Reloading mpkmodules...");
                                ModuleManager.reloadAllModules();
                            }
                        } else if (event.keyCode == InputConstants.KEY_C) {
                            if (Player.getLatest() == null) return;
                            Player p = Player.getLatest();
                            Minecraft.copyToClipboard(
                                    p.pos.getX() + " " +
                                            p.pos.getY() + " " +
                                            p.pos.getZ() + " " +
                                            p.trueYaw + " " +
                                            p.truePitch
                            );
                        }
                    }
                }, Event.EventType.KEY_INPUT));

        EventAPI.addListener(EventAPI.EventListener.onTickStart(e -> API.tickTime++));
        EventAPI.addListener(EventAPI.EventListener.onTickStart(e -> {
            TickThread.setTickables(
                    ItrUtil.getAllOfType(TickThread.Tickable.class, mainGUI.movableComponents)
            );
        }));

        EventAPI.addListener(
                new EventAPI.EventListener<OnMouseInputEvent>(e -> {
                    if (e.dx != 0 || e.dy != 0)
                        mouseMovements.add(new Vector2D(e.dx, e.dy));
                }, Event.EventType.MOUSE_INPUT)
        );

        EventAPI.addListener(
                EventAPI.EventListener.onRenderOverlay(
                        e -> {
                            if (!displayOverlay) return;
                            if (Minecraft.isF3Enabled()) return;

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
                            if (!highlightLandingBlocks) return;

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
                            Profiler.startSection("tickInputHistories");
                            for (Component component : mainGUI.movableComponents) {
                                if (!(component instanceof InputHistory)) continue;
                                ((InputHistory) component).onTick();
                            }
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
    public void unloaded() {
        throw new UnsupportedOperationException();
    }
}
