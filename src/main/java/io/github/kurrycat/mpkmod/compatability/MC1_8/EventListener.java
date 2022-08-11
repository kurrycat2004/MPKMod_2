package io.github.kurrycat.mpkmod.compatability.MC1_8;

import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EventListener {
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP mcPlayer = mc.thePlayer;

        if (e.type != TickEvent.Type.CLIENT) return;
        if (e.side != Side.CLIENT) return;

        Player player;
        if (mcPlayer == null)
            player = null;
        else {
            player = new Player()
                    .setPos(new Vector3D(mcPlayer.posX, mcPlayer.posY, mcPlayer.posZ))
                    .setLastPos(new Vector3D(mcPlayer.lastTickPosX, mcPlayer.lastTickPosY, mcPlayer.lastTickPosZ))
                    .setMotion(new Vector3D(mcPlayer.motionX, mcPlayer.motionY, mcPlayer.motionZ))
                    .setYaw(mcPlayer.rotationYaw)
                    .setPitch(mcPlayer.rotationPitch);
        }

        if (e.phase == TickEvent.Phase.START)
            API.onTickStart(player);
        else if (e.phase == TickEvent.Phase.END)
            API.onTickEnd(player);
    }
}
