package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class Button extends Component {
    private final ButtonCallback buttonCallback;
    private final Vector2D size;
    public Color pressedColor = new Color(255, 255, 255, 95);
    public Color normalColor = new Color(31, 31, 31, 150);
    public Color hoverColor = new Color(70, 70, 70, 150);
    private String text;
    private boolean isBeingPressed = false;

    public Button(String text, Vector2D pos, Vector2D size, ButtonCallback buttonCallback) {
        super(pos);
        this.text = text;
        this.buttonCallback = buttonCallback;
        this.size = size;
    }

    public String getText() {
        return text;
    }

    public Button setText(String text) {
        this.text = text;
        return this;
    }

    public void render(Vector2D mouse) {
        Color bg = isBeingPressed ? pressedColor : contains(mouse) ? hoverColor : normalColor;
        Renderer2D.drawRect(getDisplayPos(), size, bg);

        FontRenderer.drawCenteredString(
                this.getText(),
                getDisplayPos().add(size.div(2)).add(new Vector2D(0, 1)),
                isBeingPressed ? Color.BLACK : Color.WHITE,
                false
        );
    }

    public Vector2D getSize() {
        return this.size;
    }

    public void handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        if (contains(mousePos)) {
            switch (state) {
                case DOWN:
                    isBeingPressed = true;
                    break;
                case UP:
                    isBeingPressed = false;
                    buttonCallback.apply(button);
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
    }

    @FunctionalInterface
    public interface ButtonCallback {
        void apply(Mouse.Button mouseButton);
    }
}
