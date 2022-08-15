package io.github.kurrycat.mpkmod.events;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.function.Consumer;

public class EventAPI {
    private static final EventListenerMap listeners = new EventListenerMap();

    public static void addListener(EventListener listener) {
        listeners.addListener(listener);
    }

    public static void postEvent(Event event) {
        listeners.postEvent(event);
    }

    public static void init() {
    }

    public static class EventListenerMap extends EnumMap<Event.EventType, ArrayList<EventListener>> {
        public EventListenerMap() {
            super(Event.EventType.class);
            for (Event.EventType eventType : Event.EventType.values()) {
                put(eventType, new ArrayList<>());
            }
        }

        public void addListener(EventListener listener) {
            get(listener.getType()).add(listener);
        }

        public void postEvent(Event event) {
            get(event.getType()).forEach(listener -> listener.run(event));
        }
    }

    public static class EventListener {
        private final Consumer<Event> runnable;
        private final Event.EventType type;

        public EventListener(Consumer<Event> runnable, Event.EventType type) {
            this.runnable = runnable;
            this.type = type;
        }

        public static EventListener onTickStart(Consumer<Event> runnable) {
            return new EventListener(runnable, Event.EventType.TICK_START);
        }

        public static EventListener onTickEnd(Consumer<Event> runnable) {
            return new EventListener(runnable, Event.EventType.TICK_END);
        }

        public static EventListener onRenderOverlay(Consumer<Event> runnable) {
            return new EventListener(runnable, Event.EventType.RENDER_OVERLAY);
        }

        public Event.EventType getType() {
            return type;
        }

        public void run(Event event) {
            runnable.accept(event);
        }

    }
}
