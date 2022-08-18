package io.github.kurrycat.mpkmod.events;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;

public class OnServerDisconnect extends Event {
    public OnServerDisconnect(Player player) {
        super(player, EventType.SERVER_DISCONNECT);
    }
}
