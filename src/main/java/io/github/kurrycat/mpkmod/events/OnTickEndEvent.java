package io.github.kurrycat.mpkmod.events;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;

public class OnTickEndEvent extends Event {
    public OnTickEndEvent(Player player) {
        super(player, EventType.TICK_END);
    }
}
