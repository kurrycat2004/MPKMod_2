package io.github.kurrycat.mpkmod.compatability.MC1_19;

import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class EventListener {
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer mcPlayer = mc.player;

        if (e.type != TickEvent.Type.CLIENT) return;
        if (e.side != LogicalSide.CLIENT) return;

        Player player;
        if (mcPlayer == null)
            player = null;
        else {
            player = new Player()
                    .setPos(new Vector3D(mcPlayer.getX(), mcPlayer.getY(), mcPlayer.getZ()))
                    .setLastPos(new Vector3D(mcPlayer.xOld, mcPlayer.yOld, mcPlayer.zOld))
                    .setMotion(new Vector3D(mcPlayer.getDeltaMovement().x, mcPlayer.getDeltaMovement().y, mcPlayer.getDeltaMovement().z))
                    .setTrueYaw(mcPlayer.getXRot())
                    .setTruePitch(mcPlayer.getYRot());
        }

        if (e.phase == TickEvent.Phase.START)
            API.Events.onTickStart(player);
        else if (e.phase == TickEvent.Phase.END)
            API.Events.onTickEnd(player);
    }



    @SubscribeEvent
    public void onServerConnect(ClientPlayerNetworkEvent.LoggingIn e) {
        Connection nm = e.getConnection();
        API.Events.onServerConnect(nm.isMemoryConnection());
    }

    @SubscribeEvent
    public void onServerDisconnect(ClientPlayerNetworkEvent.LoggingOut e) {
        API.Events.onServerDisconnect();
    }
}
