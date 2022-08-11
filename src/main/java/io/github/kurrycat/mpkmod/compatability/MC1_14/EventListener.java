package io.github.kurrycat.mpkmod.compatability.MC1_14;

import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
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
                    .setYaw(mcPlayer.rotationYaw)
                    .setPitch(mcPlayer.rotationPitch);
        }

        if (e.phase == TickEvent.Phase.START)
            API.onTickStart(player);
        else if (e.phase == TickEvent.Phase.END)
            API.onTickEnd(player);
    }
}
