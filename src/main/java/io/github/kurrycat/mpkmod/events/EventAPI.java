package io.github.kurrycat.mpkmod.events;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Profiler;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.function.Consumer;

/**
 * Custom event API that provides minecraft version independent events
 */
@SuppressWarnings("unused")
public class EventAPI {
    private static final EventListenerMap listeners = new EventListenerMap();

    /**
     * Listeners should be added in {@link io.github.kurrycat.mpkmod.compatability.API#init API.init}<br>
     * Usage example:
     * <pre>{@code
     *      EventAPI.addListener(
     *          EventAPI.EventListener.onTickEnd(
     *              e -> {
     *                  System.out.println("I will print a message at the end of every tick!")
     *              }
     *          )
     *      )
     * }</pre>
     *
     * or more general
     * <pre>{@code
     *      EventAPI.addListener(
     *          new EventAPI.EventListener<OnTickEndEvent>(
     *              e -> {
     *                  System.out.println("I will print a message at the end of every tick!")
     *              },
     *              OnTickEndEvent.class
     *          )
     *      )
     * }</pre>
     *
     * @param listener the listener to be added
     */
    public static void addListener(EventListener<?> listener) {
        listeners.addListener(listener);
    }

    public static void postEvent(Event event) {
        if (Player.getLatest() != null)
            listeners.postEvent(event);
    }

    public static void init() {
    }

    public static class EventListenerMap extends EnumMap<Event.EventType, ArrayList<EventListener<?>>> {
        public EventListenerMap() {
            super(Event.EventType.class);
            for (Event.EventType eventType : Event.EventType.values()) {
                put(eventType, new ArrayList<>());
            }
        }

        public void addListener(EventListener<?> listener) {
            get(listener.getType()).add(listener);
        }

        public void postEvent(Event event) {
            Profiler.startSection("event_" + event.getType().name());
            get(event.getType()).forEach(listener -> {
                try {
                    listener.run(event);
                } catch (Exception e) {
                    System.err.println("Error during Event: " + event.getType().name());
                    e.printStackTrace();
                }
            });
            Profiler.endSection();
        }
    }

    public static class EventListener<T extends Event> {
        private final Consumer<T> runnable;
        private final Event.EventType type;

        public EventListener(Consumer<T> runnable, Event.EventType type) {
            this.runnable = runnable;
            this.type = type;
        }

        public static EventListener<OnTickStartEvent> onTickStart(Consumer<OnTickStartEvent> runnable) {
            return new EventListener<>(runnable, Event.EventType.TICK_START);
        }

        public static EventListener<OnTickEndEvent> onTickEnd(Consumer<OnTickEndEvent> runnable) {
            return new EventListener<>(runnable, Event.EventType.TICK_END);
        }

        public static EventListener<OnRenderOverlayEvent> onRenderOverlay(Consumer<OnRenderOverlayEvent> runnable) {
            return new EventListener<>(runnable, Event.EventType.RENDER_OVERLAY);
        }

        public Event.EventType getType() {
            return type;
        }

        @SuppressWarnings("unchecked")
        public void run(Event event) {
            runnable.accept((T) event);
        }

    }
}
