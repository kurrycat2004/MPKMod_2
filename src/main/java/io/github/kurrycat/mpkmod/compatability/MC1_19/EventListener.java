package io.github.kurrycat.mpkmod.compatability.MC1_19;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class EventListener {
    @SubscribeEvent
    public void onEvent(InputEvent.Key event) {
        if (event.getAction() == InputConstants.PRESS) {
            FunctionCompatibility.pressedButtons.add(InputConstants.getKey(event.getKey(), event.getScanCode()).getName());
        } else if (event.getAction() == InputConstants.RELEASE) {
            FunctionCompatibility.pressedButtons.remove(InputConstants.getKey(event.getKey(), event.getScanCode()).getName());
        }

        API.Events.onKeyInput(event.getKey(), InputConstants.getKey(event.getKey(), event.getScanCode()).getName(), event.getAction() == InputConstants.PRESS);

        MPKMod_1_19.keyBindingMap.forEach((id, keyBinding) -> {
            boolean keyBindingPressed = keyBinding.consumeClick();
            if (keyBindingPressed && API.guiScreenMap.containsKey(id)) {
                Minecraft.getInstance().setScreen(
                        new MPKGuiScreen_1_19(API.guiScreenMap.get(id))
                );
            }

            if (keyBindingPressed && API.keyBindingMap.containsKey(id)) {
                API.keyBindingMap.get(id).run();
            }
        });
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer mcPlayer = mc.player;

        if (e.type != TickEvent.Type.CLIENT) return;
        if (e.side != LogicalSide.CLIENT) return;

        if (mcPlayer != null && e.phase == TickEvent.Phase.START) {
            new Player()
                    .setPos(new Vector3D(mcPlayer.getX(), mcPlayer.getY(), mcPlayer.getZ()))
                    .setLastPos(new Vector3D(mcPlayer.xOld, mcPlayer.yOld, mcPlayer.zOld))
                    .setMotion(new Vector3D(mcPlayer.getDeltaMovement().x, mcPlayer.getDeltaMovement().y, mcPlayer.getDeltaMovement().z))
                    .setRotation(mcPlayer.getYRot(), mcPlayer.getXRot())
                    .setOnGround(mcPlayer.isOnGround())
                    .constructKeyInput()
                    .buildAndSave();
        }

        if (e.phase == TickEvent.Phase.START)
            API.Events.onTickStart();
        else if (e.phase == TickEvent.Phase.END)
            API.Events.onTickEnd();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderLevelStageEvent e) {
        if (e.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER)
            API.Events.onRenderWorldOverlay(e.getPartialTick());

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
