package io.github.kurrycat.mpkmod.compatibility.forge_1_8.network;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpknetapi.common.MPKNetworking;
import io.github.kurrycat.mpknetapi.common.network.packet.MPKPacket;
import io.github.kurrycat.mpknetapi.common.network.packet.impl.serverbound.MPKPacketRegister;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MPKForgeNetworking {
    private FMLEventChannel channel;

    public void init() {
        this.channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(MPKNetworking.MESSENGER_CHANNEL);
        this.channel.register(this);
    }

    public void sendPacket(MPKPacket packet) {
        Executors.newSingleThreadScheduledExecutor().schedule(
                () -> this.channel.sendToServer(new FMLProxyPacket(new PacketBuffer(Unpooled.copiedBuffer(packet.getData())), MPKNetworking.MESSENGER_CHANNEL)),
                packet instanceof MPKPacketRegister ? 500 : 0, TimeUnit.MILLISECONDS
        );
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        MPKPacket packet = MPKPacket.handle(API.PACKET_LISTENER_CLIENT, event.packet.payload().array(), null);
        if (packet != null) {
            API.Events.onPluginMessage(packet);
        }
    }
}