package io.github.kurrycat.mpkmod.events;

import io.github.kurrycat.mpknetapi.common.network.packet.MPKPacket;

public class OnPluginMessageEvent extends Event {
    private final MPKPacket packet;

    public OnPluginMessageEvent(MPKPacket packet) {
        super(EventType.PLUGIN_MESSAGE);
        this.packet = packet;
    }

    public MPKPacket getPacket() {
        return packet;
    }
}