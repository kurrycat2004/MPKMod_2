package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Colors;
import io.github.kurrycat.mpkmod.util.InfoString;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                    if (Mouse.Button.LEFT.equals(mouseButton)) {
                        menu.paneHolder.openPane(editPane);
                        menu.paneHolder.closePane(menu);
                    }
                })
        );
        menu.addComponent(
                new Button("Delete", Vector2D.OFFSCREEN, new Vector2D(30, 11), mouseButton -> {
                    if (Mouse.Button.LEFT.equals(mouseButton)) {
                        menu.paneHolder.removeComponent(this);
                        menu.paneHolder.closePane(menu);
                    }
                })
        );
        return menu;
    }

    private static class InfoLabelVariableList extends ScrollableList<InfoLabelVariableListItem> {
        private final List<InfoLabelVariableListItem> allItems;

        public InfoLabelVariableList(Vector2D pos, Vector2D size) {
            super(pos, size);
            allItems = API.infoVars.stream().map(s -> new InfoLabelVariableListItem(this, s)).collect(Collectors.toList());
            updateSearchFilter("");
        }

        public void updateSearchFilter(String searchString) {
            items = allItems.stream().filter(i -> i.varName.toLowerCase().contains(searchString.toLowerCase())).collect(Collectors.toList());
        }
    }

    private static class InfoLabelVariableListItem extends ScrollableListItem<InfoLabelVariableListItem> {
        private final String varName;

        public InfoLabelVariableListItem(ScrollableList<InfoLabelVariableListItem> parent, String varName) {
            super(parent);
            this.varName = varName;
            height = 18;
        }

        @Override
        public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
            renderDefaultBorder(pos, size);
            FontRenderer.drawLeftCenteredString(varName,
                    new Vector2D(pos.getX() + 5, pos.getY() + size.getY() / 2D),
                    Color.WHITE,
                    false
            );
        }
    }

    private class EditPane extends Pane {
        private final Label label;

        public EditPane(Vector2D pos, Vector2D size) {
            super(pos, size);
            this.backgroundColor = new Color(31, 31, 31, 50);
            this.label = new Label("", new Vector2D(0.5, 5));
            addChild(this.label, true, false, false, false, Anchor.TOP_LEFT);
            InputField inputField = new InputField(text, new Vector2D(0.5, 5), 1)
                    .setOnContentChange(content -> {
                        text = content.getContent();
                    });
            addChild(inputField, true, false, true, false, Anchor.BOTTOM_LEFT);

            ArrayList<Component> colors = new ArrayList<>();
            int currY = 3;
            double maxWidth = 0;
            for (Colors c : Colors.values()) {
                ColorLabel l = new ColorLabel(c, new Vector2D(3, currY));
                colors.add(l);
                currY += l.getDisplayedSize().getY() + 1;
                maxWidth = Math.max(maxWidth, l.getDisplayedSize().getX());
            }
            Div d = new Div(new Vector2D(2, 2), new Vector2D(maxWidth + 2, currY - 2));
            colors.forEach(d::addChild);
            d.backgroundColor = new Color(31, 31, 31, 150);
            d.setAbsolute(true);
            addChild(d);

            InfoLabelVariableList vl = new InfoLabelVariableList(new Vector2D(5, 20), new Vector2D(1 / 5D, -50));
            vl.title = "Variables";
            vl.setAbsolute(true);
            addChild(vl, false, false, true, false, Anchor.TOP_RIGHT);

            Div searchFieldDiv = new Div(new Vector2D(5, 0), new Vector2D(1 / 5D, 30));

            TextRectangle searchText = new TextRectangle(
                    new Vector2D(0.5, 0),
                    new Vector2D(1, 14),
                    "Filter",
                    null,
                    Color.WHITE
            );
            searchFieldDiv.addChild(searchText, true, false, true, false, Anchor.TOP_LEFT);

            InputField searchField = new InputField(new Vector2D(0.5D, 5), 0.9D);
            searchField.setOnContentChange(c -> vl.updateSearchFilter(c.getContent()));
            searchFieldDiv.addChild(searchField, true, false, true, false, Anchor.BOTTOM_LEFT);

            searchFieldDiv.setAbsolute(true);
            addChild(searchFieldDiv, false, false, true, false, Anchor.BOTTOM_RIGHT);
        }

        @Override
        public void render(Vector2D mousePos) {
            this.label.setText(getFormattedText());
            super.render(mousePos);
        }

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
    }
}
