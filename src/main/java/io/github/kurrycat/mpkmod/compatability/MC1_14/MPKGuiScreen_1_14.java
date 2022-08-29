package io.github.kurrycat.mpkmod.compatability.MC1_14;

import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.gui.MPKGuiScreen;
import io.github.kurrycat.mpkmod.util.Vector2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ControlsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.TranslationTextComponent;

public class MPKGuiScreen_1_14 extends Screen {
    public MPKGuiScreen eventReceiver;

    public MPKGuiScreen_1_14(MPKGuiScreen screen) {
        super(new TranslationTextComponent(API.MODID + ".gui.title"));
        eventReceiver = screen;
    }

    public void init(Minecraft mc, int width, int height) {
        mc.keyboardListener.enableRepeatEvents(true);
        super.init(mc, width, height);
        eventReceiver.onGuiInit();
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
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

    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double mouseXOff, double mouseYOff) {
        eventReceiver.onMouseClickMove(new Vector2D(mouseX, mouseY), clickedMouseButton, 0);
        return super.mouseDragged(mouseX, mouseY, clickedMouseButton, mouseXOff, mouseYOff);
    }

    public boolean keyPressed(int key, int scanCode, int modifiers) {
        String keyString = InputMappings.getInputByCode(key, scanCode).getTranslationKey();
        if(keyString.startsWith("key.keyboard.")) keyString = keyString.substring(13).toUpperCase();
        eventReceiver.onKeyEvent(key, keyString, true);
        return super.keyPressed(key, scanCode, modifiers);
    }

    public boolean keyReleased(int key, int scanCode, int modifiers) {
        String keyString = InputMappings.getInputByCode(key, scanCode).getTranslationKey();
        if(keyString.startsWith("key.keyboard.")) keyString = keyString.substring(13).toUpperCase();
        eventReceiver.onKeyEvent(key, keyString, false);
        return super.keyReleased(key, scanCode, modifiers);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        eventReceiver.onMouseScroll(new Vector2D(mouseX, mouseY), (int) delta);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}
