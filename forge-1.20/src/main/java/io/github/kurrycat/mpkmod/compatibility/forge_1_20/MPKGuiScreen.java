package io.github.kurrycat.mpkmod.compatibility.forge_1_20;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Profiler;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Vector2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class MPKGuiScreen extends Screen {
    public io.github.kurrycat.mpkmod.gui.MPKGuiScreen eventReceiver;

    public MPKGuiScreen(io.github.kurrycat.mpkmod.gui.MPKGuiScreen screen) {
        super(Component.translatable(API.MODID + ".gui.title"));
        eventReceiver = screen;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.pose().pushPose();
        API.<FunctionCompatibility>getFunctionHolder().guiGraphics = guiGraphics;
        Profiler.startSection(eventReceiver.getID() == null ? "mpk_gui" : eventReceiver.getID());
        try {
            eventReceiver.drawScreen(new Vector2D(mouseX, mouseY), partialTicks);
        } catch (Exception e) {
            API.LOGGER.warn("Error in drawScreen with id: " + eventReceiver.getID(), e);
        }
        Profiler.endSection();
        guiGraphics.pose().popPose();
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        eventReceiver.onKeyEvent(keyCode, scanCode, modifiers, false);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void onClose() {
        super.onClose();
        eventReceiver.onGuiClosed();
    }

    public void init() {
        //Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
        eventReceiver.onInit();
    }

    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void resize(@NotNull Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        eventReceiver.onResize(width, height);
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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        eventReceiver.onMouseScroll(
                new Vector2D(mouseX, mouseY),
                (int) (MathUtil.constrain(delta, -1, 1) * 7)
        );
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        eventReceiver.onKeyEvent(c, 0, modifiers, true);
        return super.charTyped(c, modifiers);
    }
}
