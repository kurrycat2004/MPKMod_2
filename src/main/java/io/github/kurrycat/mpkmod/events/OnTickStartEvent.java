package io.github.kurrycat.mpkmod.events;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;

public class OnTickStartEvent extends Event {
    public OnTickStartEvent(Player player) {
        super(player, EventType.TICK_START);
    }
}
