package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.SoundManager;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class NumberSlider extends Component implements MouseInputListener, MouseScrollListener {
    private final SliderCallback sliderCallback;
    private final double from, to, step;
    private final Button button;
    public Color backgroundColor = new Color(31, 31, 31, 150);
    public Color buttonColor = new Color(150, 150, 150, 150);
    public Color buttonHoverColor = new Color(190, 190, 190, 150);
    public Color buttonPressedColor = new Color(255, 255, 255, 95);
    private double value;
    private boolean isSliding = false;

    public NumberSlider(double from, double to, double step, double value, Vector2D pos, Vector2D size, SliderCallback sliderCallback) {
        this.setPos(pos);
        this.setSize(size);
        this.from = from;
        this.to = to;
        this.step = step;
        this.value = value;
        this.sliderCallback = sliderCallback;

        this.button = new Button("",
                new Vector2D(getRelativeXPosFromValue(), 1),
                new Vector2D(getSliderWidth(), -2)
        );
        passPositionTo(this.button, PERCENT.NONE, Anchor.TOP_LEFT);
        this.button.hoverColor = buttonHoverColor;
        this.button.normalColor = buttonColor;
        this.button.pressedColor = buttonPressedColor;
    }

    private double getRelativeXPosFromValue() {
        return MathUtil.map(value, from, to, 1, getDisplayedSize().getX() - 1 - getSliderWidth());
    }

    private double getSliderWidth() {
        return Math.max(5, (step / (to - from)) * getDisplayedSize().getX() - 2);
    }

    public double getValue() {
        return this.value;
    }

    public NumberSlider setValue(double value) {
        if(this.value == value) return this;

        this.value = value;
        this.button.pos.setX(getRelativeXPosFromValue());
        this.button.updatePosAndSize();
        return this;
    }

    public void render(Vector2D mouse) {
        Renderer2D.drawRect(getDisplayedPos(), getDisplayedSize(), backgroundColor);

        FontRenderer.drawCenteredString(
                MathUtil.formatDecimals(value, 5, false),
                getDisplayedPos().add(getDisplayedSize().div(2)).add(new Vector2D(0, 1)),
                Color.WHITE,
                false
        );

        this.setValue(value);
        this.button.render(mouse);
    }

    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        if (contains(mousePos)) {
            this.button.handleMouseInput(state, mousePos, button);

            if (state == Mouse.State.DOWN) {
                isSliding = true;
                if (!this.button.isPressed())
                    SoundManager.playButtonSound();
                this.button.setPressed(true);

                double beforeValue = this.value;
                setValue(getValueFromPos(mousePos));
                if (beforeValue != this.value)
                    sliderCallback.apply(value);

                return true;
            }
        }
        switch (state) {
            case UP:
                if (isSliding) sliderCallback.apply(value);
                isSliding = false;
                return contains(mousePos);
            case DRAG:
                if (isSliding) {
                    double beforeValue = this.value;
                    setValue(getValueFromPos(mousePos));
                    if (beforeValue != this.value)
                        sliderCallback.apply(value);

                    return true;
                }
                return false;
        }
        return contains(mousePos);
    }

    private double getValueFromPos(Vector2D pos) {
        double v = MathUtil.strictMap(
                pos.getX() - getDisplayedPos().getX(),
                1 + getSliderWidth() / 2, getDisplayedSize().getX() - 1 - getSliderWidth() / 2,
                from, to
        );
        if (step != 0) {
            v = MathUtil.constrain(MathUtil.roundToStep(v, step), from, to);
        }
        return v;
    }

    @Override
    public boolean handleMouseScroll(Vector2D mousePos, int delta) {
        if (contains(mousePos)) {
            setValue(this.value - (delta < 0 ? -1 : 1 * step));
            sliderCallback.apply(this.value);
            return true;
        }
        return false;
    }

    @FunctionalInterface
    public interface SliderCallback {
        void apply(double newValue);
    }
}
