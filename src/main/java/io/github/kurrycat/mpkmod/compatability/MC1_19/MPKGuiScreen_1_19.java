package io.github.kurrycat.mpkmod.compatability.MC1_19;

import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.compatability.MPKGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.TranslationTextComponent;

public class MPKGuiScreen_1_19 extends Screen {
    public MPKGuiScreen eventReceiver;

    public MPKGuiScreen_1_19(MPKGuiScreen screen) {
        super(new TranslationTextComponent(API.MODID + ".gui.title"));
        eventReceiver = screen;
    }

    public void init(Minecraft mc, int width, int height) {
        mc.keyboardListener.enableRepeatEvents(true);
        super.init(mc, width, height);
        eventReceiver.onGuiInit();
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        if (eventReceiver != null)
            eventReceiver.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void onClose() {
        super.onClose();
        eventReceiver.onGuiClosed();
    }

    public boolean isPauseScreen() {
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int state) {
        eventReceiver.onMouseReleased((int) mouseX, (int) mouseY, state);
        return super.mouseClicked(mouseX, mouseY, state);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        eventReceiver.onMouseReleased((int) mouseX, (int) mouseY, state);
        return super.mouseReleased(mouseX, mouseY, state);
    }

    public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int clickedMouseButton, double mouseX, double mouseY) {
        eventReceiver.onMouseClickMove((int) mouseX, (int) mouseY, clickedMouseButton, 0);
        return super.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, clickedMouseButton, mouseX, mouseY);
    }

    public boolean keyPressed(int key, int scanCode, int modifiers) {
        eventReceiver.onKeyEvent(key, InputMappings.getInputByCode(key, scanCode).getTranslationKey(), true);
        return super.keyPressed(key, scanCode, modifiers);
    }

    public boolean keyReleased(int key, int scanCode, int modifiers) {
        eventReceiver.onKeyEvent(key, InputMappings.getInputByCode(key, scanCode).getTranslationKey(), true);
        return super.keyReleased(key, scanCode, modifiers);
    }
}
