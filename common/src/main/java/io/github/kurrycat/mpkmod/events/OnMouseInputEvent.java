package io.github.kurrycat.mpkmod.events;

import io.github.kurrycat.mpkmod.util.Mouse;

public class OnMouseInputEvent extends Event {
    public Mouse.Button button;
    public Mouse.State state;
    public int x, y;
    public int dx, dy;
    public int dwheel;
    public long nanos;

    public OnMouseInputEvent(Mouse.Button button, Mouse.State state, int x, int y, int dx, int dy, int dwheel, long nanos) {
        super(EventType.MOUSE_INPUT);
        this.button = button;
        this.state = state;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.dwheel = dwheel;
        this.nanos = nanos;
    }
}
