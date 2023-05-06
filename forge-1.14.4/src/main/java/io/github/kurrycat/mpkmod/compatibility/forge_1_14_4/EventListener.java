package io.github.kurrycat.mpkmod.compatibility.forge_1_14_4;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.ControlsScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class EventListener {
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onEvent(InputEvent.KeyInputEvent event) {
        int keyCode = event.getKey();
        //getName from scanCode
        String key = InputMappings.func_216502_b(event.getScanCode());
        boolean pressed = event.getAction() != GLFW_RELEASE;

        API.Events.onKeyInput(keyCode, key, pressed);

        MPKMod.keyBindingMap.forEach((id, keyBinding) -> {
            boolean keyBindingPressed = keyBinding.isPressed();
            if (keyBindingPressed && API.guiScreenMap.containsKey(id)) {
                Minecraft.getInstance().displayGuiScreen(
                        new MPKGuiScreen(API.guiScreenMap.get(id))
                );
            }

            if(keyBindingPressed && API.keyBindingMap.containsKey(id)) {
                API.keyBindingMap.get(id).run();
            }
        });
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity mcPlayer = mc.player;

        if (e.type != TickEvent.Type.CLIENT) return;
        if (e.side != LogicalSide.CLIENT) return;


        if (mcPlayer != null && e.phase == TickEvent.Phase.START) {
            new Player()
                    .setPos(new Vector3D(mcPlayer.posX, mcPlayer.posY, mcPlayer.posZ))
                    .setLastPos(new Vector3D(mcPlayer.lastTickPosX, mcPlayer.lastTickPosY, mcPlayer.lastTickPosZ))
                    .setMotion(new Vector3D(mcPlayer.getMotion().x, mcPlayer.getMotion().y, mcPlayer.getMotion().z))
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

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent e) {
        if (e.getType() == RenderGameOverlayEvent.ElementType.TEXT && e instanceof RenderGameOverlayEvent.Text)
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
