package io.github.kurrycat.mpkmod.events;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;

public class OnServerConnect extends Event {
    public OnServerConnect(Player player) {
        super(player, EventType.SERVER_CONNECT);
    }
}
