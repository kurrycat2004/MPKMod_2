package io.github.kurrycat.mpkmod.compatability.MC1_8;

import io.github.kurrycat.mpkmod.gui.MPKGuiScreen;
import io.github.kurrycat.mpkmod.util.Vector2D;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class MPKGuiScreen_1_8 extends GuiScreen {
    public boolean repeatEventsEnabled;
    public MPKGuiScreen eventReceiver;

    public MPKGuiScreen_1_8(MPKGuiScreen screen) {
        super();
        eventReceiver = screen;
    }

    @Override
    public void initGui() {
        repeatEventsEnabled = Keyboard.areRepeatEventsEnabled();
        Keyboard.enableRepeatEvents(true);
        super.initGui();
        eventReceiver.onGuiInit();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        eventReceiver.drawScreen(new Vector2D(mouseX, mouseY), partialTicks);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(repeatEventsEnabled);
        super.onGuiClosed();
        eventReceiver.onGuiClosed();
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        int keyCode = Keyboard.getEventKey();
        String key = Character.toString(Keyboard.getEventCharacter());
        boolean pressed = Keyboard.getEventKeyState();

        eventReceiver.onKeyEvent(keyCode, key, pressed);

        super.handleKeyboardInput();
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
    public boolean doesGuiPauseGame() {
        return false;
    }
}
