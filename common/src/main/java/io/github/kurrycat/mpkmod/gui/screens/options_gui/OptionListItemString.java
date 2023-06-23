package io.github.kurrycat.mpkmod.gui.screens.options_gui;

import io.github.kurrycat.mpkmod.gui.components.Anchor;
import io.github.kurrycat.mpkmod.gui.components.InputField;
import io.github.kurrycat.mpkmod.gui.components.ScrollableList;
import io.github.kurrycat.mpkmod.util.Vector2D;

public class OptionListItemString extends OptionListItem {
    private final InputField inputField;

    public OptionListItemString(ScrollableList<OptionListItem> parent, Option option) {
        super(parent, option);

        inputField = new InputField(option.getValue(), new Vector2D(50, 0), 1 / 2.1);
        inputField.setOnContentChange(content -> {
            this.value = content.getContent();
        });

        addChild(inputField, PERCENT.SIZE_X, Anchor.CENTER_RIGHT);
    }

    protected void updateDisplayValue() {
        inputField.content = this.value;
    }

    protected void renderTypeSpecific(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
        inputField.render(mouse);
    }
}
