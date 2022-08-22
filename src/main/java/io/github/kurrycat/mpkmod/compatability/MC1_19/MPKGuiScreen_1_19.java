package io.github.kurrycat.mpkmod.compatability.MC1_19;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.gui.MPKGuiScreen;
import io.github.kurrycat.mpkmod.util.Vector2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class MPKGuiScreen_1_19 extends Screen {
    public MPKGuiScreen eventReceiver;

    public MPKGuiScreen_1_19(MPKGuiScreen screen) {
        super(Component.translatable(API.MODID + ".gui.title"));
        eventReceiver = screen;
    }

    public void init() {
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
        eventReceiver.onGuiInit();
    }

    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        if (eventReceiver != null)
            eventReceiver.drawScreen(new Vector2D(mouseX, mouseY), partialTicks);
    }

    public void onClose() {
        super.onClose();
        eventReceiver.onGuiClosed();
    }

    public boolean isPauseScreen() {
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int state) {
        eventReceiver.onMouseClicked(new Vector2D(mouseX, mouseY), state);
        return super.mouseClicked(mouseX, mouseY, state);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        eventReceiver.onMouseReleased(new Vector2D(mouseX, mouseY), state);
        return super.mouseReleased(mouseX, mouseY, state);
    }

    public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int clickedMouseButton, double mouseX, double mouseY) {
        eventReceiver.onMouseClickMove(new Vector2D(mouseX, mouseY), clickedMouseButton, 0);
        return super.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, clickedMouseButton, mouseX, mouseY);
    }

    public boolean keyPressed(int key, int scanCode, int modifiers) {
        eventReceiver.onKeyEvent(key, InputConstants.getKey(key, scanCode).getName(), true);
        return super.keyPressed(key, scanCode, modifiers);
    }

    public boolean keyReleased(int key, int scanCode, int modifiers) {
        eventReceiver.onKeyEvent(key, InputConstants.getKey(key, scanCode).getName(), true);
        return super.keyReleased(key, scanCode, modifiers);
    }
}
