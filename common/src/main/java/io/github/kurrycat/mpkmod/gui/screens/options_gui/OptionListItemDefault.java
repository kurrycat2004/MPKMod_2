package io.github.kurrycat.mpkmod.gui.screens.options_gui;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.gui.components.ScrollableList;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class OptionListItemDefault extends OptionListItem {
    public OptionListItemDefault(ScrollableList<OptionListItem> parent, Option option) {
        super(parent, option);
    }

    protected void updateDisplayValue() {
    }

    protected void renderTypeSpecific(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
        FontRenderer.drawRightCenteredString(
                value,
                pos.add(size.getX() - 40, size.getY() / 2),
                Color.WHITE,
                false
        );
    }
}
