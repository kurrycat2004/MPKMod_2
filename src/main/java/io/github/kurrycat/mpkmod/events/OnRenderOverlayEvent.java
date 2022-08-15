package io.github.kurrycat.mpkmod.events;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;

public class OnRenderOverlayEvent extends Event {
    public OnRenderOverlayEvent(Player player) {
        super(player, EventType.RENDER_OVERLAY);
    }
}
