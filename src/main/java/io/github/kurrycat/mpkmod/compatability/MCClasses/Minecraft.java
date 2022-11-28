package io.github.kurrycat.mpkmod.compatability.MCClasses;

import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.events.Event;
import io.github.kurrycat.mpkmod.gui.MPKGuiScreen;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;

@SuppressWarnings("unused")
public class Minecraft {
    public static String version;
    public static WorldState worldState = WorldState.MENU;
    public static PlayState playState = PlayState.ACTIVE;

    public static String getIP() {
        if (isSingleplayer()) return "Singleplayer";
        return Interface.get().map(Interface::getIP).orElseGet(() -> {
            API.LOGGER.info(API.COMPATIBILITY_MARKER, "Failed to get IP, are you playing on an unsupported minecraft version?");
            return "Failed getting IP";
        });
    }

    public static String getFPS() {
        return Interface.get().map(Interface::getFPS).orElseGet(() -> {
            API.LOGGER.info(API.COMPATIBILITY_MARKER, "Failed to get FPS, are you playing on an unsupported minecraft version?");
            return "Error";
        });
    }

    public static String getTime() {
        return new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
    }

    public static String getDate() {
        return new SimpleDateFormat("dd/MM/yy").format(Calendar.getInstance().getTime());
    }

    public static boolean isSingleplayer() {
        return worldState == WorldState.SINGLE_PLAYER;
    }

    public static void updateWorldState(Event.EventType type, boolean isLocal) {
        if (type == Event.EventType.SERVER_CONNECT) {
            if (isLocal) worldState = WorldState.SINGLE_PLAYER;
            else worldState = WorldState.MULTI_PLAYER;
        } else worldState = WorldState.MENU;
    }

    public static void displayGuiScreen(MPKGuiScreen screen) {
        Interface.get().ifPresent(i -> i.displayGuiScreen(screen));
    }

    public enum WorldState {
        MENU,
        SINGLE_PLAYER,
        MULTI_PLAYER;
    }


    public enum PlayState {
        ACTIVE,
        AFK;
    }

    public interface Interface extends FunctionHolder {
        static Optional<Interface> get() {
            return API.getFunctionHolder(Interface.class);
        }

        String getIP();

        String getFPS();

        void displayGuiScreen(MPKGuiScreen screen);
    }
}
