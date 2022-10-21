package io.github.kurrycat.mpkmod.compatability;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FunctionHolder;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Minecraft;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer3D;
import io.github.kurrycat.mpkmod.compatability.MCClasses.WorldInteraction;
import io.github.kurrycat.mpkmod.discord.DiscordRPC;
import io.github.kurrycat.mpkmod.events.Event;
import io.github.kurrycat.mpkmod.events.*;
import io.github.kurrycat.mpkmod.gui.MPKGuiScreen;
import io.github.kurrycat.mpkmod.gui.components.Component;
import io.github.kurrycat.mpkmod.gui.screens.LandingBlockGuiScreen;
import io.github.kurrycat.mpkmod.gui.screens.main_gui.MainGuiScreen;
import io.github.kurrycat.mpkmod.landingblock.LandingBlock;
import io.github.kurrycat.mpkmod.save.Serializer;
import io.github.kurrycat.mpkmod.util.JSONConfig;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Procedure;
import io.github.kurrycat.mpkmod.util.Vector2D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class API {
    public static final String MODID = "mpkmod";

    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final Marker DISCORD_RPC_MARKER = MarkerManager.getMarker("DISCORD_RPC");
    public static final Marker COMPATIBILITY_MARKER = MarkerManager.getMarker("COMPATIBILITY");

    public static final String NAME = "MPK Mod";
    public static final String VERSION = "2.0";
    public static final String KEYBINDING_CATEGORY = NAME;
    public static Instant gameStartedInstant;
    public static MainGuiScreen mainGUI;
    public static Map<String, MPKGuiScreen> guiScreenMap = new HashMap<>();
    public static Map<String, Procedure> keyBindingMap = new HashMap<>();
    private static FunctionHolder functionHolder;

    /**
     * Gets called at the beginning of mod init<br>
     * Register GUIs here using {@link #registerGUIScreen(String, MPKGuiScreen) registerGuiScreen}
     */
    public static void preInit() {
        mainGUI = new MainGuiScreen();
        registerGUIScreen("main_gui", mainGUI);

        registerGUIScreen("lb_gui", new LandingBlockGuiScreen());
        registerKeyBinding("lb_set",
                () -> {
                    List<LandingBlock> lbs = LandingBlock.asLandingBlocks(WorldInteraction.getLookingAtCollisionBoundingBoxes());
                    lbs.forEach(lb -> {
                        if (LandingBlockGuiScreen.lbs.contains(lb))
                            LandingBlockGuiScreen.lbs.remove(lb);
                        else LandingBlockGuiScreen.lbs.add(lb);
                    });
                }
        );
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

        API.LOGGER.info(API.DISCORD_RPC_MARKER, "Starting DiscordRPC...");
        DiscordRPC.init();
        API.LOGGER.info(API.DISCORD_RPC_MARKER, "DiscordRPC started");

        EventAPI.addListener(
                EventAPI.EventListener.onRenderOverlay(
                        e -> {
                            if (mainGUI != null)
                                for (Component c : mainGUI.movableComponents) {
                                    c.render(new Vector2D(-1, -1));
                                }
                        }
                )
        );

        EventAPI.addListener(
                new EventAPI.EventListener<OnRenderWorldOverlayEvent>(
                        e -> {
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
                        },
                        Event.EventType.RENDER_WORLD_OVERLAY
                )
        );

        EventAPI.addListener(
                EventAPI.EventListener.onTickEnd(
                        e -> LandingBlockGuiScreen.lbs.stream()
                                .filter(lb -> lb.enabled)
                                .filter(LandingBlock::isTryingToLandOn)
                                .filter(lb -> lb.landingMode.getPlayerBB() != null)
                                .map(lb -> lb.boundingBox.distanceTo(lb.landingMode.getPlayerBB()).mult(-1D))
                                .filter(vec -> vec.getX() > -0.3 && vec.getZ() > -0.3)
                                .forEach(offset -> {
                                    if (mainGUI != null)
                                        mainGUI.postMessage(
                                                "offset",
                                                MathUtil.formatDecimals(offset.getX(), 5, false) +
                                                        ", " + MathUtil.formatDecimals(offset.getZ(), 5, false)
                                        );
                                })
                )
        );

        /*EventAPI.addListener(
                new EventAPI.EventListener<OnKeyInputEvent>(
                        e -> {
                            System.out.println(
                                    "KeyCode: " + e.keyCode +
                                            " Key: " + e.key +
                                            " Pressed: " + e.pressed
                            );

                            System.out.println(
                                    Keyboard.getPressedButtons()
                            );
                        },
                        Event.EventType.KEY_INPUT
                )
        );*/
    }

    /**
     * Should be called in {@link #preInit()}
     *
     * @param guiID  ID used to display the GUI and localize the GUI key bind ({@link API#MODID} + ".key." + guiID + ".desc")
     * @param screen {@link MPKGuiScreen} instance to be registered
     */
    public static void registerGUIScreen(String guiID, MPKGuiScreen screen) {
        guiScreenMap.put(guiID, screen);
    }

    /**
     * Should be called in {@link #preInit()}
     *
     * @param id        ID used to localize the key bind ({@link API#MODID} + ".key." + guiID + ".desc")
     * @param procedure procedure to be called when key event is received
     */
    public static void registerKeyBinding(String id, Procedure procedure) {
        keyBindingMap.put(id, procedure);
    }

    /**
     * Used to link mc version specific functions to their static counterpart in one of the {@link FunctionHolder}s in {@link io.github.kurrycat.mpkmod.compatability.MCClasses MCClasses}
     *
     * @param holder an interface of a mc compatibility class that extends {@link FunctionHolder}<br>
     *               e.g. {@link io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D.Interface Renderer2D.Interface}
     */
    public static void registerFunctionHolder(FunctionHolder holder) {
        functionHolder = holder;
    }

    public static <T extends FunctionHolder> Optional<T> getFunctionHolder(Class<T> subClass) {
        return Optional.ofNullable(subClass.isInstance(functionHolder) ? subClass.cast(functionHolder) : null);
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
            guiScreenMap.forEach((id, guiScreen) -> {
                guiScreen.onGuiInit();
            });
        }

        public static void onServerConnect(boolean isLocal) {
            Minecraft.updateWorldState(Event.EventType.SERVER_CONNECT, isLocal);
            DiscordRPC.updateWorldAndPlayState();
        }

        public static void onServerDisconnect() {
            Minecraft.updateWorldState(Event.EventType.SERVER_DISCONNECT, false);
            DiscordRPC.updateWorldAndPlayState();
        }

        public static void onKeyInput(int keyCode, String key, boolean pressed) {
            EventAPI.postEvent(new OnKeyInputEvent(keyCode, key, pressed));
        }
    }
}
