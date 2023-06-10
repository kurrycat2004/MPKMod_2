package io.github.kurrycat.mpkmod.compatibility.fabric_1_19_4;


import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.ticks.ButtonMS;
import io.github.kurrycat.mpkmod.ticks.ButtonMSList;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;

public class EventHandler {
    private static final ButtonMSList timeQueue = new ButtonMSList();

    /**
     * @param key      The GLFW key code. See {@link net.minecraft.client.util.InputUtil}.
     * @param scanCode
     * @param action   The action, where 0 = unpressed, 1 = pressed, 2 = held.
     */
    public void onKey(int key, int scanCode, int action) {
        GameOptions options = MinecraftClient.getInstance().options;
        long eventNanos = Util.getMeasuringTimeNano();

        InputUtil.Key inputKey = InputUtil.fromKeyCode(key, scanCode);

        int[] keys = {
                options.forwardKey.boundKey.getCode(),
                options.leftKey.boundKey.getCode(),
                options.backKey.boundKey.getCode(),
                options.rightKey.boundKey.getCode(),
                options.sprintKey.boundKey.getCode(),
                options.sneakKey.boundKey.getCode(),
                options.jumpKey.boundKey.getCode()
        };

        for (int i = 0; i < keys.length; i++) {
            if (key == keys[i]) {
                timeQueue.add(ButtonMS.of(ButtonMS.Button.values()[i], eventNanos, action == 1));
            }
        }

        if (action == 1) {
            FunctionCompatibility.pressedButtons.add(inputKey.getLocalizedText().getString());
        } else if (action == 0) {
            FunctionCompatibility.pressedButtons.remove(inputKey.getLocalizedText().getString());
        }

        API.Events.onKeyInput(key, inputKey.getLocalizedText().getString(), action == 1);

        MPKMod.keyBindingMap.forEach((id, keyBinding) -> {
            boolean keyBindingPressed = keyBinding.isPressed();
            if (keyBindingPressed && API.guiScreenMap.containsKey(id)) {
                MinecraftClient.getInstance().setScreen(
                        new MPKGuiScreen(API.guiScreenMap.get(id))
                );
            }

            if (keyBindingPressed && API.keyBindingMap.containsKey(id)) {
                API.keyBindingMap.get(id).run();
            }
        });
    }

    public void onInGameOverlayRender(MatrixStack matrixStack, float tickDelta) {
        matrixStack.push();
        API.<FunctionCompatibility>getFunctionHolder().matrixStack = matrixStack;
        API.Events.onRenderOverlay();
        matrixStack.pop();
    }

    public void onRenderWorldOverlay(MatrixStack matrixStack, float tickDelta) {
        matrixStack.push();
        API.<FunctionCompatibility>getFunctionHolder().matrixStack = matrixStack;
        Vec3d pos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        matrixStack.translate(-pos.x, -pos.y, -pos.z);
        API.Events.onRenderWorldOverlay(tickDelta);
        matrixStack.pop();
    }

    public void onClientTickStart(MinecraftClient mc) {
        API.Events.onTickStart();
    }

    public void onClientTickEnd(MinecraftClient mc) {
        ClientPlayerEntity mcPlayer = mc.player;

        if (mcPlayer != null) {
            new Player()
                    .setPos(new Vector3D(mcPlayer.getX(), mcPlayer.getY(), mcPlayer.getZ()))
                    .setLastPos(new Vector3D(mcPlayer.prevX, mcPlayer.prevY, mcPlayer.prevZ))
                    .setMotion(new Vector3D(mcPlayer.getVelocity().x, mcPlayer.getVelocity().y, mcPlayer.getVelocity().z))
                    .setRotation(mcPlayer.getRotationClient().y, mcPlayer.getRotationClient().x)
                    .setOnGround(mcPlayer.isOnGround())
                    .setSprinting(mcPlayer.isSprinting())
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
