package io.github.kurrycat.mpkmod.events;

public class OnRenderWorldOverlayEvent extends Event {
    public float partialTicks;

    public OnRenderWorldOverlayEvent(float partialTicks) {
        super(EventType.RENDER_WORLD_OVERLAY);
        this.partialTicks = partialTicks;
    }
}
