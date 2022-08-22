package io.github.kurrycat.mpkmod.events;

public abstract class Event {
    private final EventType type;

    public Event(EventType type) {
        this.type = type;
    }

    public EventType getType() {
        return type;
    }

    public enum EventType {
        TICK_START(OnTickStartEvent.class),
        TICK_END(OnTickEndEvent.class),
        RENDER_OVERLAY(OnRenderOverlayEvent.class),
        RENDER_WORLD_OVERLAY(OnRenderWorldOverlayEvent.class),
        SERVER_CONNECT(OnServerConnect.class),
        SERVER_DISCONNECT(OnServerDisconnect.class);

        public final Class<? extends Event> eventClass;

        EventType(Class<? extends Event> eventClass) {
            this.eventClass = eventClass;
        }
    }
}
