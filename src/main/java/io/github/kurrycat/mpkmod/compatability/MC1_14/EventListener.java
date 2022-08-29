package io.github.kurrycat.mpkmod.compatability.MC1_14;

import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class EventListener {
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity mcPlayer = mc.player;

        if (e.type != TickEvent.Type.CLIENT) return;
        if (e.side != LogicalSide.CLIENT) return;

        Player player;
        if (mcPlayer == null)
            player = null;
        else {
            player = new Player()
                    .setPos(new Vector3D(mcPlayer.posX, mcPlayer.posY, mcPlayer.posZ))
                    .setLastPos(new Vector3D(mcPlayer.lastTickPosX, mcPlayer.lastTickPosY, mcPlayer.lastTickPosZ))
                    .setMotion(new Vector3D(mcPlayer.getMotion().x, mcPlayer.getMotion().y, mcPlayer.getMotion().z))
                    .setTrueYaw(mcPlayer.rotationYaw)
                    .setTruePitch(mcPlayer.rotationPitch);
        }

        if (e.phase == TickEvent.Phase.START)
            API.Events.onTickStart(player);
        else if (e.phase == TickEvent.Phase.END)
            API.Events.onTickEnd(player);
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent e) {
        if (e.getType() == RenderGameOverlayEvent.ElementType.TEXT)
            API.Events.onRenderOverlay();
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent e) {
        API.Events.onRenderWorldOverlay(e.getPartialTicks());
    }

    @SubscribeEvent
    public void onServerConnect(ClientPlayerNetworkEvent.LoggedInEvent e) {
        NetworkManager nm = e.getNetworkManager();
        API.Events.onServerConnect(nm == null || nm.isLocalChannel());
    }

    @SubscribeEvent
    public void onServerDisconnect(ClientPlayerNetworkEvent.LoggedOutEvent e) {
        API.Events.onServerDisconnect();
    }
}
