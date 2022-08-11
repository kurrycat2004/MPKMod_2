package io.github.kurrycat.mpkmod.compatability.MC1_8;

import io.github.kurrycat.mpkmod.compatability.MPKGuiScreen;
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

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(repeatEventsEnabled);
        super.onGuiClosed();
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        int keyCode = Keyboard.getEventKey();
        char key = Keyboard.getEventCharacter();
        boolean repeatKeyEvent = Keyboard.isRepeatEvent();
        boolean pressed = Keyboard.getEventKeyState();

        super.handleKeyboardInput();
    }


    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
