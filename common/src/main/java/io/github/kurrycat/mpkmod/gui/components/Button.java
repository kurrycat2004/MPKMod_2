package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.SoundManager;
import io.github.kurrycat.mpkmod.gui.Theme;
import io.github.kurrycat.mpkmod.gui.interfaces.MouseInputListener;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class Button extends Component implements MouseInputListener {
    public Color pressedColor = Theme.lightBackground;
    public Color normalColor = Theme.darkBackground;
    public Color hoverColor = Theme.hoverBackground;
    public Color textColor = Theme.defaultText;
    public Color disabledColor = Theme.disabledBackground;
    public Color disabledTextColor = Theme.disabledText;
    public Color pressedTextColor = Theme.darkText;
    public Vector2D textOffset = Vector2D.ZERO;
    public boolean enabled = true;
    private ButtonCallback buttonCallback;
    private String text;
    private boolean isBeingPressed = false;

    public Button(String text) {
        this(text, null);
    }

    public Button(String text, ButtonCallback buttonCallback) {
        this.setSize(FontRenderer.getStringSize(text).add(2, 2));
        this.text = text;
        this.buttonCallback = buttonCallback;
    }

    public Button(String text, Vector2D pos, Vector2D size) {
        this(text, pos, size, null);
    }

    public Button(String text, Vector2D pos, Vector2D size, ButtonCallback buttonCallback) {
        this.setPos(pos);
        this.setSize(size);
        this.text = text;
        this.buttonCallback = buttonCallback;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Button setButtonCallback(ButtonCallback buttonCallback) {
        this.buttonCallback = buttonCallback;
        return this;
    }

    public boolean isPressed() {
        return isBeingPressed;
    }

    public void setPressed(boolean pressed) {
        isBeingPressed = pressed;
    }

    public void render(Vector2D mouse) {
        Color bg = !enabled ? disabledColor :
                isBeingPressed ? pressedColor :
                        contains(mouse) ? hoverColor : normalColor;
        Renderer2D.drawRect(getDisplayedPos(), getDisplayedSize(), bg);

        String text = this.getText();
        FontRenderer.drawCenteredString(
                text,
                getDisplayedPos()
                        .add(getDisplayedSize().div(2))
                        .add(new Vector2D(0.5D, text.toLowerCase().equals(text) ? 0 : 1))
                        .add(textOffset),
                !enabled ? disabledTextColor :
                        isBeingPressed ? pressedTextColor :
                                textColor,
                false
        );
    }

    public String getText() {
        return text;
    }

    public Button setText(String text) {
        this.text = text;
        return this;
    }

    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        if (!enabled) return false;
        if (contains(mousePos)) {
            switch (state) {
                case DOWN:
                    isBeingPressed = true;
                    SoundManager.playButtonSound();
                    return true;
                case UP:
                    if (isBeingPressed) {
                        isBeingPressed = false;
                        if (buttonCallback != null)
                            buttonCallback.apply(button);
                        return true;
                    }
                    break;
            }
        } else {
            switch (state) {
                case UP:
                case DRAG:
                    isBeingPressed = false;
                    break;
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface ButtonCallback {
        void apply(Mouse.Button mouseButton);
    }
}
