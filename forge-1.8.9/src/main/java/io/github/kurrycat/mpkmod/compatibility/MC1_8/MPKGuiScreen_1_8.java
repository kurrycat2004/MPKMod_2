package io.github.kurrycat.mpkmod.compatibility.MC1_8;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.InputConstants;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Profiler;
import io.github.kurrycat.mpkmod.gui.MPKGuiScreen;
import io.github.kurrycat.mpkmod.util.Vector2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class MPKGuiScreen_1_8 extends GuiScreen {
    public boolean repeatEventsEnabled;
    public MPKGuiScreen eventReceiver;

    public MPKGuiScreen_1_8(MPKGuiScreen screen) {
        super();
        eventReceiver = screen;
    }

    public static int createModifiers() {
        int i = 0;
        if (GuiScreen.isShiftKeyDown()) i |= 1;
        if (GuiScreen.isCtrlKeyDown()) i |= 2;
        if (GuiScreen.isAltKeyDown()) i |= 4;
        return i;
    }

    @Override
    public void initGui() {
        repeatEventsEnabled = Keyboard.areRepeatEventsEnabled();
        Keyboard.enableRepeatEvents(true);
        super.initGui();
        if (!eventReceiver.isInitialized() || eventReceiver.resetOnOpen())
            eventReceiver.onInit();
    }

    @Override
    public void onResize(Minecraft mcIn, int width, int height) {
        super.onResize(mcIn, width, height);
        eventReceiver.onResize(width, height);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Profiler.startSection(eventReceiver.getID() == null ? "unknown" : eventReceiver.getID());
        try {
            eventReceiver.drawScreen(new Vector2D(mouseX, mouseY), partialTicks);
        } catch (Exception e) {
            System.err.println("Error in drawScreen with id: " + eventReceiver.getID());
            e.printStackTrace();
        }
        Profiler.endSection();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(repeatEventsEnabled);
        super.onGuiClosed();
        eventReceiver.onGuiClosed();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        char c = Keyboard.getEventCharacter();

        eventReceiver.onKeyEvent(InputConstants.convert(Keyboard.getEventKey()), 0, createModifiers(), false);
        if (c >= 32 && c != 127)
            eventReceiver.onKeyEvent(c, 0, createModifiers(), true);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        eventReceiver.onMouseClicked(new Vector2D(mouseX, mouseY), mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        eventReceiver.onMouseClickMove(new Vector2D(mouseX, mouseY), clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        eventReceiver.onMouseReleased(new Vector2D(mouseX, mouseY), state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        if (Mouse.getEventDWheel() != 0) {
            ScaledResolution scaledresolution = new ScaledResolution(this.mc);
            eventReceiver.onMouseScroll(
                    new Vector2D(
                            (double) (Mouse.getX() * scaledresolution.getScaledWidth() / this.mc.displayWidth),
                            (double) (scaledresolution.getScaledHeight() - Mouse.getY() * scaledresolution.getScaledHeight() / this.mc.displayHeight - 1)
                    ),
                    Mouse.getEventDWheel() / 40
            );
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
