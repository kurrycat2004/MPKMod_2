package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Minecraft;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Colors;
import io.github.kurrycat.mpkmod.util.InfoString;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class InfoLabel extends Label {
    /**
     * @param text the text to be displayed<br><br>
     *             <code>"{COLOR}"</code> with COLOR being any <code>{@link Colors}.name</code> will be replaced by that color's code<br><br>
     *             <code>"{VARNAME}"</code> with VARNAME being any field that exists or has an according getter in {@link Minecraft} or
     *             {@link Player} will be replaced by the field's value<br>
     * @param pos  top left position of the text
     */
    @JsonCreator
    public InfoLabel(@JsonProperty("text") String text, @JsonProperty("pos") Vector2D pos) {
        super(text, pos);
    }

    public String getFormattedText() {
        return InfoString.getFormattedText(this.text);
    }

    public void render(Vector2D mouse) {
        drawDefaultSelectedBackground();
        FontRenderer.drawString(getFormattedText(), getDisplayedPos(), color, true);
        //CUSTOM FONT - FontManager.testArialFont.drawStringWithShadow(getFormattedText(), getDisplayedPos().getX(), getDisplayedPos().getY(), color.getRGB());
    }

    @JsonIgnore
    public Vector2D getSize() {
        return FontRenderer.getStringSize(getFormattedText());
    }

    @Override
    public PopupMenu getPopupMenu() {
        Vector2D windowSize = Renderer2D.getScaledSize();
        EditPane editPane = new EditPane(
                new Vector2D(windowSize.getX() / 4, windowSize.getY() / 2 - 20),
                new Vector2D(windowSize.getX() / 2, 40)
        );


        PopupMenu menu = new PopupMenu();
        menu.addComponent(
                new Button("Edit", Vector2D.OFFSCREEN, new Vector2D(30, 11), mouseButton -> {
                    if(Mouse.Button.LEFT.equals(mouseButton)) {
                        menu.paneHolder.openPane(editPane);
                        menu.paneHolder.closePane(menu);
                    }
                })
        );
        menu.addComponent(
                new Button("Delete", Vector2D.OFFSCREEN, new Vector2D(30, 11), mouseButton -> {
                    if(Mouse.Button.LEFT.equals(mouseButton)) {
                        menu.paneHolder.removeComponent(this);
                        menu.paneHolder.closePane(menu);
                    }
                })
        );
        return menu;
    }

    private class EditPane extends Pane {
        private final Label label;
        private final InputField inputField;

        private class ColorLabel extends Label {
            private final Colors color;
            public ColorLabel(Colors color, Vector2D pos) {
                super(color.getCode() + color.getName(), pos);
                this.color = color;
            }

            @Override
            public void render(Vector2D mouse) {
                this.text = contains(mouse) ? color.getName() : color.getCode() + color.getName();
                super.render(mouse);
            }
        }

        public EditPane(Vector2D pos, Vector2D size) {
            super(pos, size);
            this.backgroundColor = new Color(31, 31, 31, 50);
            this.label = new Label("", Vector2D.OFFSCREEN);
            this.components.add(label);
            this.inputField = new InputField(text, Vector2D.OFFSCREEN, getDisplayedSize().getX())
                    .setOnContentChange(content -> {
                        text = content.getContent();
                    });
            this.components.add(inputField);

            int currY = 3;
            double maxWidth = 0;
            for(Colors c : Colors.values()) {
                ColorLabel l = new ColorLabel(c, new Vector2D(3, currY));
                this.components.add(l);
                currY += l.getDisplayedSize().getY() + 1;
                maxWidth = Math.max(maxWidth, l.getDisplayedSize().getX());
            }

            this.components.add(0, new Rectangle(new Vector2D(2, 2), new Vector2D(maxWidth + 2, currY - 2), new Color(31, 31, 31, 150)));
        }

        @Override
        public void render(Vector2D mousePos) {
            this.label.setText(getFormattedText());
            this.label.pos = new Vector2D(
                    getDisplayedPos().getX() + getDisplayedSize().getX() / 2 - this.label.getDisplayedSize().getX() / 2,
                    getDisplayedPos().getY() + 3
            );
            this.inputField.pos = new Vector2D(
                    getDisplayedPos().getX() + getDisplayedSize().getX() / 2 - this.inputField.getDisplayedSize().getX() / 2,
                    getDisplayedPos().getY() + getDisplayedSize().getY() - this.inputField.getDisplayedSize().getY() - 1
            );
            super.render(mousePos);
        }
    }
}
