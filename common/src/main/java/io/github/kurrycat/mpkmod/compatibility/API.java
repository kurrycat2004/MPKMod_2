package io.github.kurrycat.mpkmod.compatibility;

import io.github.kurrycat.mpkmod.Main;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.FunctionHolder;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft;
import io.github.kurrycat.mpkmod.discord.DiscordRPC;
import io.github.kurrycat.mpkmod.events.*;
import io.github.kurrycat.mpkmod.gui.MPKGuiScreen;
import io.github.kurrycat.mpkmod.gui.screens.options_gui.Option;
import io.github.kurrycat.mpkmod.modules.MPKModule;
import io.github.kurrycat.mpkmod.modules.MPKModuleImpl;
import io.github.kurrycat.mpkmod.modules.ModuleFinder;
import io.github.kurrycat.mpkmod.modules.ModuleManager;
import io.github.kurrycat.mpkmod.network.impl.MPKPacketListenerClientImpl;
import io.github.kurrycat.mpkmod.save.Serializer;
import io.github.kurrycat.mpkmod.util.ClassUtil;
import io.github.kurrycat.mpkmod.util.JSONConfig;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Procedure;
import io.github.kurrycat.mpknetapi.common.network.packet.MPKPacket;
import io.github.kurrycat.mpknetapi.common.network.packet.impl.clientbound.MPKPacketListenerClient;
import io.github.kurrycat.mpknetapi.common.network.packet.impl.serverbound.MPKPacketRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.time.Instant;
import java.util.*;

public class API {
    public static final String MODID = "mpkmod";

    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final Marker DISCORD_RPC_MARKER = MarkerManager.getMarker("DISCORD_RPC");
    public static final Marker COMPATIBILITY_MARKER = MarkerManager.getMarker("COMPATIBILITY");
    public static final Marker CONFIG_MARKER = MarkerManager.getMarker("CONFIG");
    public static final MPKPacketListenerClient PACKET_LISTENER_CLIENT = new MPKPacketListenerClientImpl();

    public static final String NAME = "MPK Mod";
    public static final String VERSION = "2.0";
    public static final String KEYBINDING_CATEGORY = NAME;
    public static final String packageName = "io.github.kurrycat.mpkmod";
    public static Instant gameStartedInstant;
    /**
     * Ticks passed since {@link #init(String)}
     */
    public static long tickTime = 0;

    public static Map<String, MPKGuiScreen> guiScreenMap = new HashMap<>();
    public static Map<String, Procedure> keyBindingMap = new HashMap<>();

    public static HashMap<String, Option> optionsMap;
    private static FunctionHolder functionHolder;

    /*@Option.Field
    public static String testOption = "String Option";*/

    /*public static int metronome = 0;*/

    /**
     * Gets called at the beginning of mod init<br>
     * Register GUIs here using {@link #registerGUIScreen(String, MPKGuiScreen) registerGuiScreen}
     *
     * @param callerClass class that calls this
     */
    public static void preInit(Class<?> callerClass) {
        ClassUtil.setModClass(callerClass);

        JSONConfig.setupFiles();
        Serializer.registerSerializer();

        optionsMap = Option.createOptionMap();
        Option.updateOptionMapFromJSON(true);

        MPKModule mainModule = new Main();
        ModuleManager.moduleMap.put("main", new MPKModuleImpl("main", mainModule, null));
        mainModule.init();

        ModuleFinder.init();
        ModuleManager.initAllModules();
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
        ModuleManager.loadAllModules();
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
            List<String> modules = new ArrayList<>();
            ModuleManager.moduleMap.forEach((id, module) -> modules.add(id));
            Minecraft.Interface.get().ifPresent(i -> i.sendPacket(new MPKPacketRegister(API.VERSION, modules)));
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

        public static void onKeybind(String id) {
            MPKGuiScreen guiScreen = guiScreenMap.get(id);
            if (guiScreen != null) guiScreen.onKeybindPressed();

            Procedure keyBinding = keyBindingMap.get(id);
            if (keyBinding != null) keyBinding.run();

            EventAPI.postEvent(new OnKeybindEvent(id));
        }

        public static void onPluginMessage(MPKPacket packet) {
            packet.process(PACKET_LISTENER_CLIENT);
            EventAPI.postEvent(new OnPluginMessageEvent(packet));
        }
    }
}
