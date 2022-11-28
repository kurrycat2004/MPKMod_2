package io.github.kurrycat.mpkmod.events;

public class OnKeyInputEvent extends Event {
    public int keyCode;
    public String key;
    public boolean pressed;

    public OnKeyInputEvent(int keyCode, String key, boolean pressed) {
        super(EventType.KEY_INPUT);
        this.keyCode = keyCode;
        this.key = key;
        this.pressed = pressed;
    }
}
