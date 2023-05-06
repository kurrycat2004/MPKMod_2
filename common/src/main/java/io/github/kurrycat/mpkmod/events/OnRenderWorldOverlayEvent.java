package io.github.kurrycat.mpkmod.events;

/**
 * Gets called at the very end of a render cycle<br>
 * <small>But before rendering the player hand in forge</small>
 */
public class OnRenderWorldOverlayEvent extends Event {
    public float partialTicks;

    public OnRenderWorldOverlayEvent(float partialTicks) {
        super(EventType.RENDER_WORLD_OVERLAY);
        this.partialTicks = partialTicks;
    }
}
