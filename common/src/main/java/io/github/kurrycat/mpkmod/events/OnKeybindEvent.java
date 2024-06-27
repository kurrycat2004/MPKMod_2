package io.github.kurrycat.mpkmod.events;

public class OnKeybindEvent extends Event {
    public String id;

    public OnKeybindEvent(String id) {
        super(EventType.KEYBIND);
        this.id = id;
    }
}
