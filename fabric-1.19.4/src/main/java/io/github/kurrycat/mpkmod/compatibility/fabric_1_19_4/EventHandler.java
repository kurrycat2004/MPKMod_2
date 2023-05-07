package io.github.kurrycat.mpkmod.compatibility.fabric_1_19_4;


import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;

public class EventHandler {
    /**
     * @param key The GLFW key code. See {@link net.minecraft.client.util.InputUtil}.
     * @param scanCode
     * @param action The action, where 0 = unpressed, 1 = pressed, 2 = held.
     */
    public void onKey(int key, int scanCode, int action) {
        if (action == 1) {
            FunctionCompatibility.pressedButtons.add(InputUtil.fromKeyCode(key, scanCode).getLocalizedText().getString());
        } else if (action == 0) {
            FunctionCompatibility.pressedButtons.remove(InputUtil.fromKeyCode(key, scanCode).getLocalizedText().getString());
        }

        API.Events.onKeyInput(key, InputUtil.fromKeyCode(key, scanCode).getLocalizedText().getString(), action == 1);

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

    public void onInGameOverlayRender(MatrixStack matrices, float tickDelta) {
        MPKMod.INSTANCE.matrixStack = matrices;
        API.Events.onRenderOverlay();
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
                    .buildAndSave();
        }

        API.Events.onTickEnd();
    }
}
