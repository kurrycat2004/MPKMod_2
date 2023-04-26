package io.github.kurrycat.mpkmod.compatability;

import io.github.kurrycat.mpkmod.compatability.MCClasses.*;
import io.github.kurrycat.mpkmod.discord.DiscordRPC;
import io.github.kurrycat.mpkmod.events.Event;
import io.github.kurrycat.mpkmod.events.*;
import io.github.kurrycat.mpkmod.gui.MPKGuiScreen;
import io.github.kurrycat.mpkmod.gui.components.Component;
import io.github.kurrycat.mpkmod.gui.components.InfoLabel;
import io.github.kurrycat.mpkmod.gui.screens.LandingBlockGuiScreen;
import io.github.kurrycat.mpkmod.gui.screens.main_gui.MainGuiScreen;
import io.github.kurrycat.mpkmod.gui.screens.options_gui.Option;
import io.github.kurrycat.mpkmod.gui.screens.options_gui.OptionsGuiScreen;
import io.github.kurrycat.mpkmod.landingblock.LandingBlock;
import io.github.kurrycat.mpkmod.save.Serializer;
import io.github.kurrycat.mpkmod.ticks.TimingStorage;
import io.github.kurrycat.mpkmod.util.*;
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
    public static final Marker CONFIG_MARKER = MarkerManager.getMarker("CONFIG");

    public static final String NAME = "MPK Mod";
    public static final String VERSION = "2.0";
    public static final String KEYBINDING_CATEGORY = NAME;
    public static final String packageName = "io.github.kurrycat.mpkmod";
    public static Instant gameStartedInstant;
    /**
     * Ticks passed since {@link #init(String)}
     */
    public static long tickTime = 0;

    public static MainGuiScreen mainGUI;
    public static Map<String, MPKGuiScreen> guiScreenMap = new HashMap<>();
    public static Map<String, Procedure> keyBindingMap = new HashMap<>();

    public static boolean discordRpcInitialized = false;
    public static HashMap<String, Option> optionsMap;
    private static FunctionHolder functionHolder;
    /*@Option.Field
    public static String testOption = "String Option";*/

    /*public static int metronome = 0;*/

    /**
     * Gets called at the beginning of mod init<br>
     * Register GUIs here using {@link #registerGUIScreen(String, MPKGuiScreen) registerGuiScreen}
     */
    public static void preInit(Class<?> callerClass) {
        ClassUtil.setModClass(callerClass);

        JSONConfig.setupFiles();
        Serializer.registerSerializer();

        optionsMap = Option.createOptionMap();
        Option.updateOptionMapFromJSON(true);

        //InputPatternStorage.init();
        TimingStorage.init();

        mainGUI = new MainGuiScreen();
        registerGUIScreen("main_gui", mainGUI);

        registerGUIScreen("lb_gui", new LandingBlockGuiScreen());
        registerKeyBinding("lb_set",
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

        registerGUIScreen("options_gui", new OptionsGuiScreen());
    }

    /**
     * Gets called once at the end of the mod loader initialization event
     *
     * @param mcVersion String containing the current minecraft version (e.g. "1.8.9")
     */
    public static void init(String mcVersion) {
        Minecraft.version = mcVersion;

        gameStartedInstant = Instant.now();

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

        EventAPI.addListener(EventAPI.EventListener.onTickStart(e -> tickTime++));

        EventAPI.addListener(
                EventAPI.EventListener.onRenderOverlay(
                        e -> {
                            Profiler.startSection("labels");
                            if (mainGUI != null)
                                for (Component c : mainGUI.movableComponents) {
                                    try {
                                        c.render(new Vector2D(-1, -1));
                                    } catch (ClassCastException err) {
                                        if (c instanceof InfoLabel) {
                                            ((InfoLabel) c).infoString.updateProviders();
                                        }
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
                EventAPI.EventListener.onTickEnd(
                        e -> {
                            Player p = Player.getLatest();
                            if (p != null)
                                System.out.println(p.tickInput);
                        }
                )
        );*/

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
     * Should be called in {@link #preInit(Class)}
     *
     * @param guiID  ID used to display the GUI and localize the GUI key bind ({@link API#MODID} + ".key." + guiID + ".desc")
     * @param screen {@link MPKGuiScreen} instance to be registered
     */
    public static void registerGUIScreen(String guiID, MPKGuiScreen screen) {
        screen.setID(guiID);
        guiScreenMap.put(guiID, screen);
    }

    /**
     * Should be called in {@link #preInit(Class)}
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
            if (discordRpcInitialized) DiscordRPC.updateWorldAndPlayState();
        }

        public static void onServerDisconnect() {
            Minecraft.updateWorldState(Event.EventType.SERVER_DISCONNECT, false);
            if (discordRpcInitialized) DiscordRPC.updateWorldAndPlayState();
        }

        public static void onKeyInput(int keyCode, String key, boolean pressed) {
            EventAPI.postEvent(new OnKeyInputEvent(keyCode, key, pressed));
        }
    }
}
