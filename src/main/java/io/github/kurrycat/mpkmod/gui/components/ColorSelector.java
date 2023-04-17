package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.ArrayListUtil;
import io.github.kurrycat.mpkmod.util.ColorUtil;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.Arrays;
import java.util.function.Consumer;

public class ColorSelector extends Component implements KeyInputListener, MouseInputListener, MouseScrollListener {
    private final NumberSlider red, green, blue, alpha;
    private final InputField color;
    private final Consumer<Color> onChange;
    public Color backgroundColor = new Color(31, 31, 31, 150);
    private Color value;

    public ColorSelector(Color value, Vector2D pos, Consumer<Color> onChange) {
        super(pos);
        this.value = value;
        this.onChange = onChange;

        double width = 62;

        double currY = 1;
        this.red = new NumberSlider(0, 255, 1, value.getRed(),
                new Vector2D(1, currY),
                new Vector2D(width, 11),
                newValue -> sliderValueChanged(new Color((int) newValue, this.value.getGreen(), this.value.getBlue(), this.value.getAlpha()))
        );
        currY += this.red.getDisplayedSize().getY() + 1;
        this.red.setParent(this);

        this.green = new NumberSlider(0, 255, 1, value.getGreen(),
                new Vector2D(1, currY),
                new Vector2D(width, 11),
                newValue -> sliderValueChanged(new Color(this.value.getRed(), (int) newValue, this.value.getBlue(), this.value.getAlpha()))
        );
        currY += this.green.getDisplayedSize().getY() + 1;
        this.green.setParent(this);

        this.blue = new NumberSlider(0, 255, 1, value.getBlue(),
                new Vector2D(1, currY),
                new Vector2D(width, 11),
                newValue -> sliderValueChanged(new Color(this.value.getRed(), this.value.getGreen(), (int) newValue, this.value.getAlpha()))
        );
        currY += this.blue.getDisplayedSize().getY() + 1;
        this.blue.setParent(this);

        this.alpha = new NumberSlider(0, 255, 1, value.getAlpha(),
                new Vector2D(1, currY),
                new Vector2D(width, 11),
                newValue -> sliderValueChanged(new Color(this.value.getRed(), this.value.getGreen(), this.value.getBlue(), (int) newValue))
        );
        currY += this.alpha.getDisplayedSize().getY() + 1;
        this.alpha.setParent(this);

        this.color = new InputField(ColorUtil.colorToHex(this.value), new Vector2D(1, currY + 3), width)
                .setOnContentChange(content -> inputValueChanged(content.getContent()))
                .setFilter(InputField.FILTER_HEX);
        this.color.setParent(this);

        this.setSize(
                new Vector2D(
                        this.color.getDisplayedSize().getX() + 2,
                        1 + this.red.getDisplayedSize().getY() + 1 +
                                this.green.getDisplayedSize().getY() + 1 +
                                this.blue.getDisplayedSize().getY() + 1 +
                                this.alpha.getDisplayedSize().getY() + 4 +
                                this.color.getDisplayedSize().getY() + 1
                )
        );
    }

    private void sliderValueChanged(Color value) {
        this.value = value;
        this.color.content = ColorUtil.colorToHex(value);
        this.onChange.accept(this.value);
    }

    private void inputValueChanged(String value) {
        Color c = ColorUtil.hexToColor(value);
        if (c != null) {
            this.value = c;
            this.red.setValue(c.getRed());
            this.green.setValue(c.getGreen());
            this.blue.setValue(c.getBlue());
            this.alpha.setValue(c.getAlpha());
            this.onChange.accept(this.value);
        }
    }

    @Override
    public void render(Vector2D mouse) {
        Renderer2D.drawRectWithEdge(getDisplayedPos(), getDisplayedSize(), 1, backgroundColor, backgroundColor);

        this.red.render(mouse);
        this.green.render(mouse);
        this.blue.render(mouse);
        this.alpha.render(mouse);

        this.color.render(mouse);
    }

    @Override
    public boolean handleKeyInput(int keyCode, int scanCode, int modifiers, boolean isCharTyped) {
        return this.color.handleKeyInput(keyCode, scanCode, modifiers, isCharTyped);
    }

    @Override
    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        return ArrayListUtil.orMapAll(ArrayListUtil.getAllOfType(MouseInputListener.class, this.red, this.green, this.blue, this.alpha, this.color),
                listener -> listener.handleMouseInput(state, mousePos, button));
    }

    @Override
    public boolean handleMouseScroll(Vector2D mousePos, int delta) {
        return ArrayListUtil.orMapAll(Arrays.asList(this.red, this.green, this.blue, this.alpha),
                slider -> slider.handleMouseScroll(mousePos, delta));
    }
}
