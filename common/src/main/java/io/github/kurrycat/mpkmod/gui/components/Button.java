package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.SoundManager;
import io.github.kurrycat.mpkmod.gui.interfaces.MouseInputListener;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class Button extends Component implements MouseInputListener {
    public Color pressedColor = new Color(255, 255, 255, 95);
    public Color normalColor = new Color(31, 31, 31, 150);
    public Color hoverColor = new Color(70, 70, 70, 150);
    public Color textColor = Color.WHITE;
    public Color disabledColor = new Color(11, 11, 11, 150);
    public Color disabledTextColor = new Color(70, 70, 70, 150);
    public Color pressedTextColor = Color.BLACK;
    public Vector2D textOffset = Vector2D.ZERO;
    public boolean enabled = true;
    private ButtonCallback buttonCallback;
    private String text;
    private boolean isBeingPressed = false;

    public Button(String text) {
        this(text, null);
    }

    public Button(String text, ButtonCallback buttonCallback) {
        this.setSize(FontRenderer.getStringSize(text).add(2,2));
        this.text = text;
        this.buttonCallback = buttonCallback;
    }

    public Button(String text, Vector2D pos, Vector2D size, ButtonCallback buttonCallback) {
        this.setPos(pos);
        this.setSize(size);
        this.text = text;
        this.buttonCallback = buttonCallback;
    }

    public Button(String text, Vector2D pos, Vector2D size) {
        this(text, pos, size, null);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Button setButtonCallback(ButtonCallback buttonCallback) {
        this.buttonCallback = buttonCallback;
        return this;
    }

    public String getText() {
        return text;
    }

    public Button setText(String text) {
        this.text = text;
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

        FontRenderer.drawCenteredString(
                this.getText(),
                getDisplayedPos()
                        .add(getDisplayedSize().div(2))
                        .add(new Vector2D(0.5, this.getText().toLowerCase().equals(this.getText()) ? 0 : 1))
                        .add(textOffset),
                !enabled ? disabledTextColor :
                        isBeingPressed ? pressedTextColor :
                                textColor,
                false
        );
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
