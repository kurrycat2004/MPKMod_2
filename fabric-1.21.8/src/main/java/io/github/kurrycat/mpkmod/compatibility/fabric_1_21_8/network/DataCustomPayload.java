package io.github.kurrycat.mpkmod.compatibility.fabric_1_21_8.network;

import io.github.kurrycat.mpknetapi.common.MPKNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record DataCustomPayload(byte[] data) implements CustomPayload {
    public static final Id<DataCustomPayload> MPK_ID = new Id<>(Identifier.of(MPKNetworking.CHANNEL_NAMESPACE, MPKNetworking.CHANNEL_PATH));

    public static final PacketCodec<PacketByteBuf, DataCustomPayload> CODEC = PacketCodec.of(
        (payload, buf) -> buf.writeBytes(payload.data()),
        buf -> {
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            return new DataCustomPayload(data);
        }
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return MPK_ID;
    }

    public static Id<DataCustomPayload> registerClientboundPayload() {
        PayloadTypeRegistry.playS2C().register(MPK_ID, DataCustomPayload.CODEC);
        return MPK_ID;
    }

    public static Id<DataCustomPayload> registerServerboundPayload() {
        PayloadTypeRegistry.playC2S().register(MPK_ID, DataCustomPayload.CODEC);
        return MPK_ID;
    }
}