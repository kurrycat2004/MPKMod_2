package io.github.kurrycat.mpkmod.gui.screens.options_gui;

import io.github.kurrycat.mpkmod.gui.components.CheckButton;
import io.github.kurrycat.mpkmod.gui.components.ScrollableList;
import io.github.kurrycat.mpkmod.util.Vector2D;

public class OptionListItemBoolean extends OptionListItem {
    private final CheckButton checkButton;

    public OptionListItemBoolean(ScrollableList<OptionListItem> parent, Option option) {
        super(parent, option);

        checkButton = new CheckButton(Vector2D.OFFSCREEN, checked -> value = String.valueOf(checked));
        checkButton.enabled = option.getType() == Option.ValueType.BOOLEAN;
        if (checkButton.enabled) {
            checkButton.setChecked(option.getBoolean());
            updateComponents.add(checkButton);
        }
    }

    protected void updateDisplayValue() {
        if (checkButton.enabled) {
            checkButton.setChecked(Boolean.parseBoolean(value));
        }
    }

    protected void renderTypeSpecific(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
        checkButton.pos = pos.add(size.getX() - 40 - checkButton.getDisplayedSize().getX(), size.getY() / 2 - 5);
        checkButton.render(mouse);
    }
}
