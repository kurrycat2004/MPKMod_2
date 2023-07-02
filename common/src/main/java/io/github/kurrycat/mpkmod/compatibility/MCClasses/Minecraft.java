package io.github.kurrycat.mpkmod.compatibility.MCClasses;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.events.Event;
import io.github.kurrycat.mpkmod.gui.MPKGuiScreen;
import io.github.kurrycat.mpkmod.gui.infovars.InfoString;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;

@SuppressWarnings("unused")
@InfoString.AccessInstance
public class Minecraft {
    public static String version;
    public static WorldState worldState = WorldState.MENU;
    public static PlayState playState = PlayState.ACTIVE;
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH : mm : ss");
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");

    @InfoString.Getter
    public static String getIp() {
        if (isSingleplayer()) return "Singleplayer";
        return Interface.get().map(Interface::getIP).orElseGet(() -> {
            API.LOGGER.info(API.COMPATIBILITY_MARKER, "Failed to get IP, are you playing on an unsupported minecraft version?");
            return "Failed getting IP";
        });
    }

    @InfoString.Getter
    public static String getFps() {
        return Interface.get().map(Interface::getFPS).orElseGet(() -> {
            API.LOGGER.info(API.COMPATIBILITY_MARKER, "Failed to get FPS, are you playing on an unsupported minecraft version?");
            return "Error";
        });
    }

    @InfoString.Getter
    public static String getMcVersion() {
        return version;
    }

    @InfoString.Getter
    public static String getUsername() {
        if(!Interface.get().isPresent()) {
            API.LOGGER.info(API.COMPATIBILITY_MARKER, "Failed to get username, are you playing on an unsupported minecraft version?");
            return "Error";
        } else {
            return Interface.get().get().getUserName();
        }
    }

    public static String getCurrentGuiScreen() {
        if(!Interface.get().isPresent()) {
            API.LOGGER.info(API.COMPATIBILITY_MARKER, "Failed to get current screen name, are you playing on an unsupported minecraft version?");
            return "Error";
        } else {
            return Interface.get().get().getCurrentGuiScreen();
        }
    }

    @InfoString.Getter
    public static String getTime() {
        return Minecraft.timeFormat.format(Calendar.getInstance().getTime());
    }

    @InfoString.Getter
    public static String getDate() {
        return Minecraft.dateFormat.format(Calendar.getInstance().getTime());
    }

    @InfoString.Getter
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

        String getCurrentGuiScreen();

        String getUserName();
    }
}
