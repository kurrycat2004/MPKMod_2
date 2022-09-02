package io.github.kurrycat.mpkmod.compatability.MCClasses;

import io.github.kurrycat.mpkmod.compatability.functions.GetIPFunction;
import io.github.kurrycat.mpkmod.events.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@SuppressWarnings("unused")
public class Minecraft {
    public static String version;
    public static WorldState worldState = WorldState.MENU;
    public static PlayState playState = PlayState.ACTIVE;

    private static GetIPFunction getIPFunction;

    public static void registerGetIPFunction(GetIPFunction f) {
        getIPFunction = f;
    }

    public static String getIP() {
        if (isSingleplayer()) return "Singleplayer";
        return getIPFunction.apply();
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

    public enum WorldState {
        MENU,
        SINGLE_PLAYER,
        MULTI_PLAYER;
    }

    public enum PlayState {
        ACTIVE,
        AFK;
    }
}
