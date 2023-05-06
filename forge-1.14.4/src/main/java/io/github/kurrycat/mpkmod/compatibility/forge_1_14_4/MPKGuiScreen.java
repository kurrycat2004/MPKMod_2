package io.github.kurrycat.mpkmod.compatibility.forge_1_14_4;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Profiler;
import io.github.kurrycat.mpkmod.util.Vector2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;

public class MPKGuiScreen extends Screen {
    public io.github.kurrycat.mpkmod.gui.MPKGuiScreen eventReceiver;

    public MPKGuiScreen(io.github.kurrycat.mpkmod.gui.MPKGuiScreen screen) {
        super(new TranslationTextComponent(API.MODID + ".gui.title"));
        eventReceiver = screen;
    }

    public void init(Minecraft mc, int width, int height) {
        mc.keyboardListener.enableRepeatEvents(true);
        super.init(mc, width, height);
        if (!eventReceiver.isInitialized() || eventReceiver.resetOnOpen())
            eventReceiver.onInit();
    }

    @Override
    public void resize(@Nonnull Minecraft mcIn, int width, int height) {
        super.resize(mcIn, width, height);
        eventReceiver.onResize(width, height);
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        Profiler.startSection(eventReceiver.getID() == null ? "unknown" : eventReceiver.getID());
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

    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double mouseXOff, double mouseYOff) {
        eventReceiver.onMouseClickMove(new Vector2D(mouseX, mouseY), clickedMouseButton, 0);
        return super.mouseDragged(mouseX, mouseY, clickedMouseButton, mouseXOff, mouseYOff);
    }

    public boolean keyPressed(int key, int scanCode, int modifiers) {
        eventReceiver.onKeyEvent(key, scanCode, modifiers, true);
        return super.keyPressed(key, scanCode, modifiers);
    }

    public boolean keyReleased(int key, int scanCode, int modifiers) {
        eventReceiver.onKeyEvent(key, scanCode, modifiers, false);
        return super.keyReleased(key, scanCode, modifiers);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        eventReceiver.onMouseScroll(new Vector2D(mouseX, mouseY), (int) delta);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}
