package io.github.kurrycat.mpkmod.compatibility.forge_1_19_4;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.ticks.ButtonMS;
import io.github.kurrycat.mpkmod.ticks.ButtonMSList;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class EventListener {
    private static final ButtonMSList timeQueue = new ButtonMSList();

    @SubscribeEvent
    public void onEvent(InputEvent.Key event) {
        Options options = Minecraft.getInstance().options;
        long eventNanos = Util.getNanos();

        int[] keys = {
                options.keyUp.getKey().getValue(),
                options.keyLeft.getKey().getValue(),
                options.keyDown.getKey().getValue(),
                options.keyRight.getKey().getValue(),
                options.keySprint.getKey().getValue(),
                options.keyShift.getKey().getValue(),
                options.keyJump.getKey().getValue()
        };

        for (int i = 0; i < keys.length; i++)
            if (event.getKey() == keys[i])
                timeQueue.add(ButtonMS.of(ButtonMS.Button.values()[i], eventNanos, event.getAction() == InputConstants.PRESS));


        if (event.getAction() == InputConstants.PRESS) {
            FunctionCompatibility.pressedButtons.add(InputConstants.getKey(event.getKey(), event.getScanCode()).getValue());
        } else if (event.getAction() == InputConstants.RELEASE) {
            FunctionCompatibility.pressedButtons.remove(InputConstants.getKey(event.getKey(), event.getScanCode()).getValue());
        }

        API.Events.onKeyInput(event.getKey(), InputConstants.getKey(event.getKey(), event.getScanCode()).getName(), event.getAction() == InputConstants.PRESS);

        MPKMod.keyBindingMap.forEach((id, keyBinding) -> {
            boolean keyBindingPressed = keyBinding.consumeClick();
            if (keyBindingPressed && API.guiScreenMap.containsKey(id)) {
                Minecraft.getInstance().setScreen(
                        new MPKGuiScreen(API.guiScreenMap.get(id))
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

        if (mcPlayer != null && e.phase == TickEvent.Phase.END) {
            new Player()
                    .setPos(new Vector3D(mcPlayer.getX(), mcPlayer.getY(), mcPlayer.getZ()))
                    .setLastPos(new Vector3D(mcPlayer.xOld, mcPlayer.yOld, mcPlayer.zOld))
                    .setMotion(new Vector3D(mcPlayer.getDeltaMovement().x, mcPlayer.getDeltaMovement().y, mcPlayer.getDeltaMovement().z))
                    .setRotation(mcPlayer.getYRot(), mcPlayer.getXRot())
                    .setOnGround(mcPlayer.isOnGround())
                    .setSprinting(mcPlayer.isSprinting())
                    .constructKeyInput()
                    .setKeyMSList(timeQueue)
                    .buildAndSave();
            timeQueue.clear();
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
