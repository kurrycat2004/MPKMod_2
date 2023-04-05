package io.github.kurrycat.mpkmod.compatability.MC1_19;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Profiler;
import io.github.kurrycat.mpkmod.gui.MPKGuiScreen;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
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
        eventReceiver.onInit();
    }

    @Override
    public void resize(@NotNull Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        eventReceiver.onResize(width, height);
    }

    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        Profiler.startSection(eventReceiver.getID() == null ? "mpk_gui" : eventReceiver.getID());
        try {
            eventReceiver.drawScreen(new Vector2D(mouseX, mouseY), partialTicks);
        } catch (Exception e) {
            System.err.println("Error in drawScreen with id: " + eventReceiver.getID());
            e.printStackTrace();
        }
        Profiler.endSection();
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

    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double moveX, double moveY) {
        eventReceiver.onMouseClickMove(new Vector2D(mouseX, mouseY), clickedMouseButton, 0);
        return super.mouseDragged(mouseX, mouseY, clickedMouseButton, moveX, moveY);
    }

    public boolean keyPressed(int key, int scanCode, int modifiers) {
        eventReceiver.onKeyEvent((char) InputConstants.getKey(key, scanCode).getValue(), InputConstants.getKey(key, scanCode).getName(), true);
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char key, int p_94684_) {
        eventReceiver.onKeyEvent(key, "KEY_" + key, true);
        return super.charTyped(key, p_94684_);
    }

    public boolean keyReleased(int key, int scanCode, int modifiers) {
        eventReceiver.onKeyEvent((char) InputConstants.getKey(key, scanCode).getValue(), InputConstants.getKey(key, scanCode).getName(), false);
        return super.keyReleased(key, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        eventReceiver.onMouseScroll(
                new Vector2D(mouseX, mouseY),
                (int) (delta / 40)
        );
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}
