package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.SoundManager;
import io.github.kurrycat.mpkmod.gui.Theme;
import io.github.kurrycat.mpkmod.gui.interfaces.MouseInputListener;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class CheckButton extends Component implements MouseInputListener {
    public CheckButtonCallback checkButtonCallback;
    @SuppressWarnings("unused")
    public Color checkedColor = Theme.lightBackground;
    public Color normalColor = Theme.darkBackground;
    public Color disabledColor = Theme.disabledBackground;
    public Color textColor = Theme.defaultText;
    public Color disabledTextColor = Theme.disabledText;
    private boolean isChecked;

    public boolean enabled = true;

    public CheckButton(Vector2D pos, CheckButtonCallback checkButtonCallback) {
        this(pos, false, checkButtonCallback);
    }

    public CheckButton(Vector2D pos, boolean checked, CheckButtonCallback checkButtonCallback) {
        this.setPos(pos);
        this.setSize(new Vector2D(11, 11));
        this.isChecked = checked;
        this.checkButtonCallback = checkButtonCallback;
    }

    @SuppressWarnings("unused")
    public CheckButton(Vector2D pos) {
        this(pos, checked -> {});
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public void render(Vector2D mouse) {
        Color color = enabled ? normalColor : disabledColor;
        Renderer2D.drawRectWithEdge(getDisplayedPos(), getDisplayedSize(), 1, color, color);

        if (isChecked())
            FontRenderer.drawString(
                    "x",
                    getDisplayedPos().add(new Vector2D(3, 1)),
                    enabled ? textColor : disabledTextColor,
                    false
            );
    }

    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        if (state == Mouse.State.DOWN) {
            if (contains(mousePos)) {
                isChecked = !isChecked;
                SoundManager.playButtonSound();
                checkButtonCallback.apply(isChecked);
                return true;
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface CheckButtonCallback {
        void apply(boolean checked);
    }
}
