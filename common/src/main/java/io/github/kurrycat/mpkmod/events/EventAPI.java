package io.github.kurrycat.mpkmod.events;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Profiler;
import io.github.kurrycat.mpkmod.util.Debug;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Custom event API that provides minecraft version independent events
 */
@SuppressWarnings("unused")
public class EventAPI {
    private static final EventListenerMap listeners = new EventListenerMap();
    private static String currentModuleID = null;

    /**
     * Listeners should be added in {@link io.github.kurrycat.mpkmod.compatibility.API#init API.init}<br>
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
     * <p>
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
        if (currentModuleID == null) {
            Debug.stacktrace("You can't add event listeners outside of MPKModule.loaded()");
            return;
        }
        listeners.addListener(currentModuleID, listener);
    }

    public static void loading(String moduleID) {
        currentModuleID = moduleID;
        listeners.forEach((type, map) -> {
                if(map.containsKey(currentModuleID))
                    map.get(currentModuleID).clear();
        });
    }

    public static void finishLoading() {
        currentModuleID = null;
    }

    public static void postEvent(Event event) {
        if (Player.getLatest() != null)
            listeners.postEvent(event);
    }

    public static void init() {
    }

    public static class EventListenerMap extends EnumMap<Event.EventType, HashMap<String, ArrayList<EventListener<?>>>> {
        public EventListenerMap() {
            super(Event.EventType.class);
            for (Event.EventType eventType : Event.EventType.values()) {
                HashMap<String, ArrayList<EventListener<?>>> map = new HashMap<>();
                put(eventType, map);
            }
        }

        public void addListener(String moduleID, EventListener<?> listener) {
            HashMap<String, ArrayList<EventListener<?>>> map = get(listener.getType());
            if (!map.containsKey(moduleID)) map.put(moduleID, new ArrayList<>());

            map.get(moduleID).add(listener);
        }

        public void postEvent(Event event) {
            Profiler.startSection("mpk_event_" + event.getType().name());
            get(event.getType()).forEach((moduleID, listeners) ->
                    listeners.forEach(listener -> {
                        try {
                            listener.run(event);
                        } catch (Exception e) {
                            API.LOGGER.info("Caught exception from module: " + moduleID + " during Event: " + event.getType().name(), e);
                        }
                    })
            );
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
