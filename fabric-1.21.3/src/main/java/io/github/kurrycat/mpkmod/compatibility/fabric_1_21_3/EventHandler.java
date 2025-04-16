package io.github.kurrycat.mpkmod.compatibility.fabric_1_21_3;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.compatibility.fabric_1_21_3.mixin.KeyBindingAccessor;
import io.github.kurrycat.mpkmod.ticks.ButtonMS;
import io.github.kurrycat.mpkmod.ticks.ButtonMSList;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class EventHandler {
    private static final ButtonMSList timeQueue = new ButtonMSList();

    /**
     * @param key      The GLFW key code. See {@link InputUtil}.
     * @param scanCode
     * @param action   The action, where 0 = unpressed, 1 = pressed, 2 = held.
     */
    public void onKey(int key, int scanCode, int action) {
        GameOptions options = MinecraftClient.getInstance().options;
        long eventNanos = Util.getMeasuringTimeNano();

        InputUtil.Key inputKey = InputUtil.fromKeyCode(key, scanCode);

        int[] keys = {
                ((KeyBindingAccessor) options.forwardKey).getBoundKey().getCode(),
                ((KeyBindingAccessor) options.leftKey).getBoundKey().getCode(),
                ((KeyBindingAccessor) options.backKey).getBoundKey().getCode(),
                ((KeyBindingAccessor) options.rightKey).getBoundKey().getCode(),
                ((KeyBindingAccessor) options.sprintKey).getBoundKey().getCode(),
                ((KeyBindingAccessor) options.sneakKey).getBoundKey().getCode(),
                ((KeyBindingAccessor) options.jumpKey).getBoundKey().getCode()
        };

        for (int i = 0; i < keys.length; i++) {
            if (key == keys[i]) {
                timeQueue.add(ButtonMS.of(ButtonMS.Button.values()[i], eventNanos, action == 1));
            }
        }

        if (action == 1) {
            FunctionCompatibility.pressedButtons.add(inputKey.getCode());
        } else if (action == 0) {
            FunctionCompatibility.pressedButtons.remove(inputKey.getCode());
        }

        API.Events.onKeyInput(key, inputKey.getLocalizedText().getString(), action == 1);

        MPKMod.keyBindingMap.forEach((id, keyBinding) -> {
            if (keyBinding.isPressed()) {
                API.Events.onKeybind(id);
            }
        });
    }

    public void onInGameOverlayRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        drawContext.getMatrices().push();
        API.<FunctionCompatibility>getFunctionHolder().drawContext = drawContext;
        API.Events.onRenderOverlay();
        drawContext.getMatrices().pop();
    }

    public void onRenderWorldOverlay(MatrixStack matrixStack, float tickDelta) {
        MPKMod.INSTANCE.matrixStack = matrixStack;
        matrixStack.push();
        Vec3d pos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        MPKMod.INSTANCE.matrixStack.translate(-pos.x, -pos.y, -pos.z);
        API.Events.onRenderWorldOverlay(tickDelta);
        matrixStack.pop();
    }

    public void onClientTickStart(MinecraftClient mc) {
        if (mc.isPaused() || mc.world == null) return;
        API.Events.onTickStart();
    }

    public void onClientTickEnd(MinecraftClient mc) {
        if (mc.isPaused() || mc.world == null) return;
        ClientPlayerEntity mcPlayer = mc.player;

        if (mcPlayer != null) {
            Box playerBB = mcPlayer.getBoundingBox();
            new Player()
                    .setPos(new Vector3D(mcPlayer.getX(), mcPlayer.getY(), mcPlayer.getZ()))
                    .setLastPos(new Vector3D(mcPlayer.prevX, mcPlayer.prevY, mcPlayer.prevZ))
                    .setMotion(new Vector3D(mcPlayer.getVelocity().x, mcPlayer.getVelocity().y, mcPlayer.getVelocity().z))
                    .setRotation(mcPlayer.getRotationClient().y, mcPlayer.getRotationClient().x)
                    .setOnGround(mcPlayer.isOnGround())
                    .setSprinting(mcPlayer.isSprinting())
                    .setBoundingBox(new BoundingBox3D(
                            new Vector3D(playerBB.minX, playerBB.minY, playerBB.minZ),
                            new Vector3D(playerBB.maxX, playerBB.maxY, playerBB.maxZ)
                    ))
                    .constructKeyInput()
                    .setKeyMSList(timeQueue)
                    .buildAndSave();
            timeQueue.clear();
        }

        API.Events.onTickEnd();
    }


    public void onServerConnect(ClientPlayNetworkHandler clientPlayNetworkHandler, PacketSender packetSender, MinecraftClient minecraftClient) {
        API.Events.onServerConnect(clientPlayNetworkHandler.getConnection().isLocal());
    }

    public void onServerDisconnect(ClientPlayNetworkHandler clientPlayNetworkHandler, MinecraftClient minecraftClient) {
        API.Events.onServerDisconnect();
    }
}
