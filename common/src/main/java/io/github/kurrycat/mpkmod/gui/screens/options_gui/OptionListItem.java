package io.github.kurrycat.mpkmod.gui.screens.options_gui;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.*;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public abstract class OptionListItem extends ScrollableListItem<OptionListItem> {
    private static final Color optionListColorItemEdge = new Color(255, 255, 255, 95);
    private static final Color optionListColorBg = new Color(31, 31, 31, 150);
    private final Button resetButton;
    public Option option;
    protected String value;
    protected Option.ValueType type;
    protected Div hoverText;
    protected TextRectangle helpHover;

    public OptionListItem(ScrollableList<OptionListItem> parent, Option option) {
        super(parent);
        this.option = option;
        this.value = option.getValue();
        this.type = option.getType();

        resetButton = new Button("Reset", new Vector2D(15, 0), new Vector2D(30, 11), mouseButton -> {
            if (mouseButton == Mouse.Button.LEFT) {
                loadDefaultValue();
            }
        });
        addChild(resetButton, PERCENT.NONE, Anchor.CENTER_RIGHT);

        helpHover = new TextRectangle(
                new Vector2D(0, 0),
                new Vector2D(11, 11),
                "?", null, Color.WHITE);
        addChild(helpHover, PERCENT.NONE, Anchor.CENTER_RIGHT);

        hoverText = new Div();
        passPositionTo(hoverText, PERCENT.SIZE_X);
        hoverText.backgroundColor = optionListColorBg;
        hoverText.borderColor = optionListColorItemEdge;
        hoverText.setText(
                option.getDescription().isEmpty() ?
                        option.getName() : option.getDescription()
        );
        hoverText.setMaxWidth(0.5);
    }

    public void loadDefaultValue() {
        value = option.getDefaultValue();
        updateDisplayValue();
    }

    protected abstract void updateDisplayValue();

    public void update() {
        option.setValue(value);
    }

    public int getHeight() {
        return 21;
    }

    @Override
    public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
        Renderer2D.drawRectWithEdge(pos, size, 1, optionListColorBg, optionListColorItemEdge);

        helpHover.render(mouse);

        FontRenderer.drawLeftCenteredString(
                option.getDisplayName().isEmpty() ? option.getName() : option.getDisplayName(),
                pos.add(5, size.getY() / 2),
                Color.WHITE,
                false
        );

        renderTypeSpecific(index, pos, size, mouse);

        if (resetButton != null) {
            resetButton.enabled = !option.getDefaultValue().equals(value);
            resetButton.render(mouse);
        }
    }

    public void renderHover(Vector2D mouse) {
        if (mouse.isInRectBetweenPS(helpHover.getDisplayedPos(), helpHover.getDisplayedSize())) {
            hoverText.setCPos(
                    helpHover.getDisplayedPos().sub(hoverText.getDisplayedSize())
                            .constrain(getRoot().getDisplayedPos().add(1), getRoot().getDisplayedSize().sub(1))
            );
            hoverText.render(mouse);
        }
    }

    protected abstract void renderTypeSpecific(int index, Vector2D pos, Vector2D size, Vector2D mouse);
}
