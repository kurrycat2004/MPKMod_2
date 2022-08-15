package io.github.kurrycat.mpkmod.events;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;

public abstract class Event {
    private final Player player;
    private final EventType type;


    public Event(Player player, EventType type) {
        this.player = player;
        this.type = type;
    }

    public Player getPlayer() {
        return player;
    }

    public EventType getType() {
        return type;
    }

    public enum EventType {
        TICK_START(OnTickStartEvent.class),
        TICK_END(OnTickEndEvent.class),
        RENDER_OVERLAY(OnRenderOverlayEvent.class);

        public final Class<? extends Event> eventClass;
        EventType(Class<? extends Event> eventClass) {
            this.eventClass = eventClass;
        }
    }
}
