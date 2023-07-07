package io.github.kurrycat.mpkmod.compatibility;

import io.github.kurrycat.mpkmod.Main;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.*;
import io.github.kurrycat.mpkmod.discord.DiscordRPC;
import io.github.kurrycat.mpkmod.events.Event;
import io.github.kurrycat.mpkmod.events.*;
import io.github.kurrycat.mpkmod.gui.MPKGuiScreen;
import io.github.kurrycat.mpkmod.gui.infovars.InfoString;
import io.github.kurrycat.mpkmod.gui.infovars.InfoTree;
import io.github.kurrycat.mpkmod.gui.screens.LandingBlockGuiScreen;
import io.github.kurrycat.mpkmod.gui.screens.main_gui.MainGuiScreen;
import io.github.kurrycat.mpkmod.gui.screens.options_gui.Option;
import io.github.kurrycat.mpkmod.gui.screens.options_gui.OptionsGuiScreen;
import io.github.kurrycat.mpkmod.landingblock.LandingBlock;
import io.github.kurrycat.mpkmod.modules.MPKModule;
import io.github.kurrycat.mpkmod.modules.ModuleFinder;
import io.github.kurrycat.mpkmod.modules.ModuleManager;
import io.github.kurrycat.mpkmod.save.Serializer;
import io.github.kurrycat.mpkmod.ticks.TimingStorage;
import io.github.kurrycat.mpkmod.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

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

    public static HashMap<String, Option> optionsMap;
    public static InfoTree infoTree;
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

        infoTree = InfoString.createInfoTree();
        API.LOGGER.info("{} infoVars registered", infoTree.getSize());

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
     * Gets called once at the end of the mod loader initialization event
     *
     * @param mcVersion String containing the current minecraft version (e.g. "1.8.9")
     */
    public static void init(String mcVersion) {
        Minecraft.version = mcVersion;

        gameStartedInstant = Instant.now();

        MPKModule mainModule = new Main();
        mainModule.init();

        ModuleFinder.init();
        ModuleManager.reloadAllModules();
    }

    /**
     * Used to link mc version specific functions to their static counterpart in one of the {@link FunctionHolder}s in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses MCClasses}
     *
     * @param holder an interface of a mc compatibility class that extends {@link FunctionHolder}<br>
     *               e.g. {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D.Interface Renderer2D.Interface}
     */
    public static void registerFunctionHolder(FunctionHolder holder) {
        functionHolder = holder;
    }

    @SuppressWarnings("unchecked")
    public static <T extends FunctionHolder> T getFunctionHolder() {
        return (T) functionHolder;
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
                guiScreen.onInit();
            });
        }

        public static void onServerConnect(boolean isLocal) {
            Minecraft.updateWorldState(Event.EventType.SERVER_CONNECT, isLocal);
            if (Main.discordRpcInitialized) DiscordRPC.updateWorldAndPlayState();
        }

        public static void onServerDisconnect() {
            Minecraft.updateWorldState(Event.EventType.SERVER_DISCONNECT, false);
            if (Main.discordRpcInitialized) DiscordRPC.updateWorldAndPlayState();
        }

        public static void onKeyInput(int keyCode, String key, boolean pressed) {
            EventAPI.postEvent(new OnKeyInputEvent(keyCode, key, pressed));
        }

        public static void onMouseInput(Mouse.Button button, Mouse.State state, int x, int y, int dx, int dy, int dwheel, long nanos) {
            EventAPI.postEvent(new OnMouseInputEvent(button, state, x, y, dx, dy, dwheel, nanos));
        }
    }
}
