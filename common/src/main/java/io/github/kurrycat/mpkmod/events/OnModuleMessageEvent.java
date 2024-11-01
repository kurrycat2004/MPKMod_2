package io.github.kurrycat.mpkmod.events;

import io.github.kurrycat.mpknetapi.common.network.packet.MPKPacket;
import io.github.kurrycat.mpknetapi.common.network.packet.impl.shared.MPKPacketModuleMessage;

public class OnModuleMessageEvent extends Event {
    private final MPKPacketModuleMessage packet;

    public OnModuleMessageEvent(MPKPacketModuleMessage packet) {
        super(EventType.PLUGIN_MESSAGE);
        this.packet = packet;
    }

    public MPKPacket getPacket() {
        return packet;
    }
}