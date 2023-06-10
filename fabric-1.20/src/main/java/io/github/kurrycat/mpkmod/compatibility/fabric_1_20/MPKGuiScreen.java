package io.github.kurrycat.mpkmod.compatibility.fabric_1_20;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Profiler;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Vector2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class MPKGuiScreen extends Screen {
    public io.github.kurrycat.mpkmod.gui.MPKGuiScreen eventReceiver;

    public MPKGuiScreen(io.github.kurrycat.mpkmod.gui.MPKGuiScreen screen) {
        super(Text.translatable(API.MODID + ".gui.title"));
        eventReceiver = screen;
    }

    public void init() {
        eventReceiver.onInit();
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        eventReceiver.onResize(width, height);
    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.getMatrices().push();
        API.<FunctionCompatibility>getFunctionHolder().drawContext = drawContext;
        Profiler.startSection(eventReceiver.getID() == null ? "mpk_gui" : eventReceiver.getID());
        try {
            eventReceiver.drawScreen(new Vector2D(mouseX, mouseY), delta);
        } catch (Exception e) {
            API.LOGGER.warn("Error in drawScreen with id: " + eventReceiver.getID(), e);
        }
        Profiler.endSection();
        drawContext.getMatrices().pop();
    }

    public void close() {
        super.close();
        eventReceiver.onGuiClosed();
    }

    public boolean shouldPause() {
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

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        eventReceiver.onKeyEvent(keyCode, scanCode, modifiers, false);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        eventReceiver.onKeyEvent(c, 0, modifiers, true);
        return super.charTyped(c, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        eventReceiver.onMouseScroll(
                new Vector2D(mouseX, mouseY),
                (int) (MathUtil.constrain(delta, -1, 1) * 7)
        );
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}
