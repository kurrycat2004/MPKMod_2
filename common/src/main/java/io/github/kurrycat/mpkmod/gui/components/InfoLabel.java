package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.gui.TickThread;
import io.github.kurrycat.mpkmod.gui.screens.main_gui.MainGuiScreen;
import io.github.kurrycat.mpkmod.util.Colors;
import io.github.kurrycat.mpkmod.util.InfoString;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class InfoLabel extends Label implements TickThread.Tickable {
    private InfoString infoString;
    private volatile String formattedText = "";

    /**
     * @param text the text to be displayed<br><br>
     *             <code>"{COLOR}"</code> with COLOR being any <code>{@link Colors}.name</code> will be replaced by that color's code<br><br>
     *             <code>"{VARNAME}"</code> with VARNAME being any key in {@link API#infoMap}<br>
     */
    @JsonCreator
    public InfoLabel(@JsonProperty("text") String text) {
        super(text);
        this.infoString = new InfoString(text);
    }

    public void updateText(String text) {
        this.text = text;
        infoString = new InfoString(text);
    }

    public void render(Vector2D mouse) {
        drawDefaultSelectedBackground();
        FontRenderer.drawString(getFormattedText(), getDisplayedPos(), color, true);
        //CUSTOM FONT - FontManager.testArialFont.drawStringWithShadow(getFormattedText(), getDisplayedPos().getX(), getDisplayedPos().getY(), color.getRGB());
    }

    public String getFormattedText() {
        return formattedText;
    }

    @JsonIgnore
    public Vector2D getSizeForJson() {
        return FontRenderer.getStringSize(getFormattedText());
    }

    @Override
    public Vector2D getDisplayedSize() {
        return FontRenderer.getStringSize(getFormattedText());
    }

    public void tick() {
        formattedText = infoString.get();
    }

    @Override
    public PopupMenu getPopupMenu() {
        PopupMenu menu = new PopupMenu();
        EditPane editPane = new EditPane();

        menu.addComponent(new Button("Edit", mouseButton -> {
            if (mouseButton != Mouse.Button.LEFT) return;
            menu.paneHolder.passPositionTo(editPane, PERCENT.SIZE_X, Anchor.CENTER);
            menu.paneHolder.openPane(editPane);
            menu.close();
        }));
        menu.addComponent(new Button("Delete", mouseButton -> {
            if (mouseButton != Mouse.Button.LEFT) return;
            menu.paneHolder.removeComponent(this);
            menu.close();
        }));
        return menu;
    }

    private static class InfoLabelVariableList extends ScrollableList<InfoLabelVariableListItem> {
        private final List<InfoLabelVariableListItem> allItems;

        public InfoLabelVariableList(Vector2D pos, Vector2D size) {
            this.setPos(pos);
            this.setSize(size);
            title = "Variables";
            allItems = API.infoVars.stream().map(s -> new InfoLabelVariableListItem(this, s)).collect(Collectors.toList());
            updateSearchFilter("");
        }

        @Override
        public void render(Vector2D mouse) {
            super.render(mouse);
            renderComponents(mouse);
        }

        public void updateSearchFilter(String searchString) {
            items = allItems.stream().filter(i -> i.varName.toLowerCase().contains(searchString.toLowerCase())).collect(Collectors.toList());
        }
    }

    private static class InfoLabelVariableListItem extends ScrollableListItem<InfoLabelVariableListItem> {
        private final String varName;

        public InfoLabelVariableListItem(ScrollableList<InfoLabelVariableListItem> parent, String varName) {
            super(parent);
            this.setHeight(18);
            this.varName = varName;
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

    public class EditPane extends Pane<MainGuiScreen> {
        private final TextRectangle label;

        public EditPane() {
            super(Vector2D.ZERO, new Vector2D(0.5, 40));
            this.label = new TextRectangle(
                    new Vector2D(0, 1),
                    new Vector2D(1, 20),
                    "",
                    new Color(0, 0, 0, 0),
                    Color.WHITE
            );
            addChild(this.label, PERCENT.SIZE_X, Anchor.TOP_CENTER);

            InputField inputField = new InputField(text, new Vector2D(0, 5), 0.9D)
                    .setOnContentChange(content -> updateText(content.getContent()));

            addChild(inputField, PERCENT.SIZE_X, Anchor.BOTTOM_CENTER);

            Div colors = new Div();
            for (Colors c : Colors.values()) {
                colors.addChildBelow(new ColorLabel(c));
            }
            colors.backgroundColor = new Color(31, 31, 31, 150);
            colors.setAbsolute(true);
            addChild(colors);


            InfoLabelVariableList vl = new InfoLabelVariableList(new Vector2D(0, 20), new Vector2D(2 / 9D, -50));
            vl.setAbsolute(true);
            addChild(vl, PERCENT.SIZE_X, Anchor.TOP_RIGHT);

            Div searchFieldDiv = new Div(Vector2D.ZERO, new Vector2D(1, 30));

            TextRectangle searchText = new TextRectangle(
                    Vector2D.ZERO,
                    new Vector2D(1, 14),
                    "Filter",
                    null,
                    Color.WHITE
            );
            searchFieldDiv.addChild(searchText, PERCENT.SIZE_X, Anchor.TOP_CENTER);

            InputField searchField = new InputField(new Vector2D(0, 5), 0.9D);
            searchField.setOnContentChange(c -> vl.updateSearchFilter(c.getContent()));
            searchFieldDiv.addChild(searchField, PERCENT.SIZE_X, Anchor.BOTTOM_CENTER);

            vl.bottomCover.addChild(searchFieldDiv, PERCENT.SIZE_X, Anchor.CENTER);
        }

        @Override
        public void render(Vector2D mousePos) {
            this.label.setText(getFormattedText());
            super.render(mousePos);
        }
    }
}
