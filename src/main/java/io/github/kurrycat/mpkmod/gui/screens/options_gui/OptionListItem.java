package io.github.kurrycat.mpkmod.gui.screens.options_gui;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.components.*;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.Component;
import io.github.kurrycat.mpkmod.util.ArrayListUtil;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.ArrayList;

public abstract class OptionListItem extends ScrollableListItem<OptionListItem> {

    private static final Color optionListColorItemEdge = new Color(255, 255, 255, 95);
    private static final Color optionListColorBg = new Color(31, 31, 31, 150);
    private final Button resetButton;
    protected final ArrayList<Component> updateComponents = new ArrayList<>();
    public Option option;
    protected String value;
    protected Option.ValueType type;

    public OptionListItem(ScrollableList<OptionListItem> parent, Option option) {
        super(parent);
        this.option = option;
        this.value = option.getValue();
        this.type = option.getType();

        resetButton = new Button("Reset", Vector2D.OFFSCREEN, new Vector2D(30, 11), mouseButton -> {
            if (mouseButton == Mouse.Button.LEFT) {
                loadDefaultValue();
            }
        });
        updateComponents.add(resetButton);
    }

    public void loadDefaultValue() {
        value = option.getDefaultValue();
        updateDisplayValue();
    }
    protected abstract void updateDisplayValue();

    public void update() {
        option.setValue(value);
    }

    @Override
    public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
        Renderer2D.drawRectWithEdge(pos, size, 1, optionListColorBg, optionListColorItemEdge);

        FontRenderer.drawLeftCenteredString(
                option.getName(),
                pos.add(5, size.getY() / 2),
                Color.WHITE,
                false
        );

        renderTypeSpecific(index, pos, size, mouse);

        resetButton.enabled = !option.getDefaultValue().equals(value);
        resetButton.pos = pos.add(size.getX() - 35, size.getY() / 2 - 5);
        resetButton.render(mouse);
    }

    protected abstract void renderTypeSpecific(int index, Vector2D pos, Vector2D size, Vector2D mouse);

    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        return ArrayListUtil.orMapAll(
                ArrayListUtil.getAllOfType(MouseInputListener.class, updateComponents),
                ele -> ele.handleMouseInput(state, mousePos, button)
        );
    }

    public boolean handleKeyInput(char keyCode, String key, boolean pressed) {
        return ArrayListUtil.orMapAll(
                ArrayListUtil.getAllOfType(KeyInputListener.class, updateComponents),
                ele -> ele.handleKeyInput(keyCode, key, pressed)
        );
    }

    public int getHeight() {
        return 21;
    }
}
