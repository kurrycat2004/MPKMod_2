package io.github.kurrycat.mpkmod.compatability.MC1_8;

import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

public class EventListener {
    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onEvent(InputEvent.KeyInputEvent event) {
        int keyCode = Keyboard.getEventKey();
        String key = Keyboard.getKeyName(keyCode);
        boolean pressed = Keyboard.getEventKeyState();

        API.Events.onKeyInput(keyCode, key, pressed);

        MPKMod_1_8.keyBindingMap.forEach((id, keyBinding) -> {
            boolean keyBindingPressed = keyBinding.isPressed();
            if (keyBindingPressed && API.guiScreenMap.containsKey(id)) {
                Minecraft.getMinecraft().displayGuiScreen(
                        new MPKGuiScreen_1_8(API.guiScreenMap.get(id))
                );
            }

            if(keyBindingPressed && API.keyBindingMap.containsKey(id)) {
                API.keyBindingMap.get(id).run();
            }
        });
    }


    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP mcPlayer = mc.thePlayer;

        if (e.type != TickEvent.Type.CLIENT) return;
        if (e.side != Side.CLIENT) return;

        if (mcPlayer != null && e.phase == TickEvent.Phase.START) {
            new Player()
                    .setPos(new Vector3D(mcPlayer.posX, mcPlayer.posY, mcPlayer.posZ))
                    .setLastPos(new Vector3D(mcPlayer.lastTickPosX, mcPlayer.lastTickPosY, mcPlayer.lastTickPosZ))
                    .setMotion(new Vector3D(mcPlayer.motionX, mcPlayer.motionY, mcPlayer.motionZ))
                    .setRotation(mcPlayer.rotationYaw, mcPlayer.rotationPitch)
                    .setOnGround(mcPlayer.onGround)
                    .setSprinting(mcPlayer.isSprinting())
                    .constructKeyInput()
                    .buildAndSave();
        }
        if (e.phase == TickEvent.Phase.START) {
            API.Events.onTickStart();
        } else if (e.phase == TickEvent.Phase.END)
            API.Events.onTickEnd();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent e) {
        if (e.type == RenderGameOverlayEvent.ElementType.TEXT)
            API.Events.onRenderOverlay();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onRenderWorld(RenderWorldLastEvent e) {
        API.Events.onRenderWorldOverlay(e.partialTicks);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onServerConnect(FMLNetworkEvent.ClientConnectedToServerEvent e) {
        API.Events.onServerConnect(e.isLocal);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onServerDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
        API.Events.onServerDisconnect();
    }
}
