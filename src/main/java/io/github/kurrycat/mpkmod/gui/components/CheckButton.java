package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.compatability.MCClasses.SoundManager;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class CheckButton extends Component implements MouseInputListener {
    private final CheckButtonCallback checkButtonCallback;
    public Color checkedColor = new Color(255, 255, 255, 95);
    public Color normalColor = new Color(31, 31, 31, 150);
    private boolean isChecked = false;

    public CheckButton(Vector2D pos, CheckButtonCallback checkButtonCallback) {
        super(pos);
        this.checkButtonCallback = checkButtonCallback;
        this.setSize(new Vector2D(11, 11));
    }

    public CheckButton(Vector2D pos) {
        super(pos);
        this.checkButtonCallback = null;
        this.setSize(new Vector2D(11, 11));
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public void render(Vector2D mouse) {
        Renderer2D.drawRectWithEdge(getDisplayPos(), getSize(), 1, normalColor, normalColor);

        if (isChecked)
            FontRenderer.drawCenteredString(
                    "x",
                    getDisplayPos().add(getSize().div(2)).add(new Vector2D(0.5, 0)),
                    Color.WHITE,
                    false
            );
    }

    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        if (contains(mousePos)) {
            if (state == Mouse.State.DOWN) {
                isChecked = !isChecked;
                SoundManager.playButtonSound();
                checkButtonCallback.apply(isChecked);
            }
            return true;
        }
        return false;
    }

    @FunctionalInterface
    public interface CheckButtonCallback {
        void apply(boolean checked);
    }
}
