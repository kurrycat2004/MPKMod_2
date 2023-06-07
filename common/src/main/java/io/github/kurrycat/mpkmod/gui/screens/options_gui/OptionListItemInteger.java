package io.github.kurrycat.mpkmod.gui.screens.options_gui;

import io.github.kurrycat.mpkmod.gui.components.InputField;
import io.github.kurrycat.mpkmod.gui.components.ScrollableList;
import io.github.kurrycat.mpkmod.util.Vector2D;

public class OptionListItemInteger extends OptionListItem {
    private final InputField inputField;

    public OptionListItemInteger(ScrollableList<OptionListItem> parent, Option option) {
        super(parent, option);

        inputField = new InputField(option.getValue(), Vector2D.OFFSCREEN, 64, true);
        inputField.setOnContentChange(content -> {
            this.value = content.getContent();
        });

        updateComponents.add(inputField);
    }

    protected void updateDisplayValue() {
        inputField.content = this.value;
    }

    protected void renderTypeSpecific(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
        inputField.setSize(
                new Vector2D(
                        size.getX() / 2.1,
                        inputField.getDisplayedSize().getY()
                )
        );
        inputField.setPos(pos.add(size.getX() - 40 - inputField.getDisplayedSize().getX(), size.getY() / 2 - 6));
        inputField.render(mouse);
    }
}
