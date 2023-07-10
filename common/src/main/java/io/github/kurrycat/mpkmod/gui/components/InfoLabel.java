package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.Main;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.gui.Theme;
import io.github.kurrycat.mpkmod.gui.TickThread;
import io.github.kurrycat.mpkmod.gui.infovars.InfoString;
import io.github.kurrycat.mpkmod.gui.infovars.InfoVar;
import io.github.kurrycat.mpkmod.gui.interfaces.MouseInputListener;
import io.github.kurrycat.mpkmod.gui.screens.main_gui.MainGuiScreen;
import io.github.kurrycat.mpkmod.util.ArrayListUtil;
import io.github.kurrycat.mpkmod.util.Colors;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class InfoLabel extends Label implements TickThread.Tickable {
    private InfoString infoString;
    private volatile String formattedText = "";

    /**
     * @param text the text to be displayed<br><br>
     *             <code>"{COLOR}"</code> with COLOR being any <code>{@link Colors}.name</code> will be replaced by that color's code<br><br>
     *             <code>"{VARNAME}"</code> with VARNAME being any key in {@link Main#infoTree}<br>
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

    @Override
    public Vector2D getDisplayedSize() {
        return FontRenderer.getStringSize(getFormattedText());
    }

    @JsonIgnore
    public Vector2D getSizeForJson() {
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

    private static class InfoVarList extends ScrollableList<InfoVarListItem> {
        private final List<InfoVarListItem> allItems;
        private final InputField inputField;

        public InfoVarList(InputField inputField, Vector2D pos, Vector2D size) {
            this.setPos(pos);
            this.setSize(size);
            this.setTitle("Variables");
            this.inputField = inputField;
            allItems = Main.infoTree.getEntries().stream()
                    .map(entry ->
                            new InfoVarListItem(this, entry.getValue())
                    ).collect(Collectors.toList());
            updateSearchFilter("");
        }

        public void updateSearchFilter(String searchString) {
            items = allItems.stream()
                    .filter(i -> i.containsSearchString(searchString))
                    .collect(Collectors.toList());
        }

        @Override
        public void render(Vector2D mouse) {
            super.render(mouse);
            renderComponents(mouse);
        }
    }

    private static class InfoVarListItem extends ScrollableListItem<InfoVarListItem> {
        public static final int HEIGHT = 18;
        private final InfoVarComponent infoVarComponent;

        public InfoVarListItem(InfoVarList parent, InfoVar infoVar) {
            super(parent);
            this.setHeight(18);
            this.infoVarComponent = new InfoVarComponent(infoVar, parent.inputField);
            infoVarComponent.setSize(new Vector2D(1, -HEIGHT));
            passPositionTo(infoVarComponent, PERCENT.SIZE_X);
        }

        @Override
        public int getHeight() {
            return infoVarComponent.getHeight();
        }

        @Override
        public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
            infoVarComponent.render(mouse);
        }

        @Override
        public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
            return infoVarComponent.handleMouseInput(state, mousePos, button) ||
                    super.handleMouseInput(state, mousePos, button);
        }

        public boolean containsSearchString(String searchString) {
            return infoVarComponent.searchAndOpenString(searchString);
        }
    }

    private static class InfoVarComponent extends Component implements MouseInputListener {
        private final InfoVar infoVar;
        private final InputField inputField;
        private final ArrayList<InfoVarComponent> children = new ArrayList<>();
        private final Button collapseButton;
        private final Button addButton;
        private final boolean showCollapseButton;
        public boolean collapsed = true;
        private InfoVarComponent parent = null;

        public InfoVarComponent(InfoVar infoVar, InputField inputField) {
            this.infoVar = infoVar;
            this.inputField = inputField;
            double[] i = {InfoVarListItem.HEIGHT};
            infoVar.getEntries().stream()
                    .map(entry -> new InfoVarComponent(entry.getValue(), inputField))
                    .forEach(c -> {
                        c.parent = this;
                        c.setSize(new Vector2D(-4, InfoVarListItem.HEIGHT));
                        c.setPos(new Vector2D(2, i[0] + 2));
                        i[0] += c.getHeight();
                        passPositionTo(c, PERCENT.NONE, Anchor.TOP_RIGHT);
                        children.add(c);
                    });

            showCollapseButton = children.size() > 0;

            TextRectangle text = new TextRectangle(
                    Vector2D.ZERO,
                    new Vector2D(1, InfoVarListItem.HEIGHT),
                    infoVar.getName(),
                    new Color(0, 0, 0, 0),
                    Color.WHITE
            );
            text.edgeColor = ScrollableListItem.defaultEdgeColor;
            text.leftAligned = true;
            addChild(text, PERCENT.SIZE_X);

            Div buttonHolder = new Div();
            buttonHolder.setSize(new Vector2D(1, InfoVarListItem.HEIGHT));
            passPositionTo(buttonHolder, PERCENT.SIZE_X);

            collapseButton = new Button("v", new Vector2D(13, 0), new Vector2D(11, 11));
            collapseButton.setButtonCallback(mouseButton -> {
                if (mouseButton != Mouse.Button.LEFT) return;
                setCollapsed(!collapsed);
            });
            buttonHolder.passPositionTo(collapseButton, PERCENT.NONE, Anchor.CENTER_RIGHT);
            components.add(collapseButton);

            addButton = new Button("+", new Vector2D(1, 0), new Vector2D(11, 11));
            addButton.setButtonCallback(mouseButton -> {
                if (mouseButton != Mouse.Button.LEFT) return;
                inputField.typeContentAtCursor("{" + infoVar.getFullName() + "}");
                inputField.focus();
            });
            buttonHolder.passPositionTo(addButton, PERCENT.NONE, Anchor.CENTER_RIGHT);
            components.add(addButton);
        }

        private void setCollapsed(boolean collapsed) {
            if (collapsed == this.collapsed) return;
            this.collapsed = collapsed;
            collapseButton.setText(collapsed ? "v" : "^");
            collapseButton.textOffset = collapsed ? Vector2D.ZERO : new Vector2D(0, 3);

            if (parent != null) parent.updateChildPositions();

            if (collapsed)
                for (InfoVarComponent c : children) c.setCollapsed(true);
        }

        public boolean searchAndOpenString(String searchString) {
            if (Objects.equals(searchString, "")) setCollapsed(true);
            if (infoVar.getName().toLowerCase().contains(searchString.toLowerCase()))
                return true;

            boolean result = false;
            for (InfoVarComponent c : children) {
                if (c.searchAndOpenString(searchString)) result = true;
            }
            setCollapsed(!result);
            return result;
        }

        private void updateChildPositions() {
            double[] i = {InfoVarListItem.HEIGHT};
            children.forEach(c -> {
                c.setPos(new Vector2D(2, i[0] + 2));
                i[0] += c.getHeight();
            });
            if (parent != null) parent.updateChildPositions();
        }

        @Override
        public void render(Vector2D mouse) {
            for (Component c : components) {
                if (!showCollapseButton && c == collapseButton) continue;
                c.render(mouse);
            }

            if (!collapsed) {
                children.forEach(c -> c.render(mouse));
            }
        }

        public int getHeight() {
            return InfoVarListItem.HEIGHT +
                    (collapsed ? 0 : children.stream()
                            .map(InfoVarComponent::getHeight)
                            .reduce(0, Integer::sum) + 4);
        }

        @Override
        public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
            return addButton.handleMouseInput(state, mousePos, button) ||
                    showCollapseButton && collapseButton.handleMouseInput(state, mousePos, button) ||
                    !collapsed && ArrayListUtil.orMap(children, c -> c.handleMouseInput(state, mousePos, button));
        }
    }

    private static class ColorList extends ScrollableList<ColorItem> {
        public ColorList(InputField inputField) {
            super();
            setTitle("Colors");
            setPos(new Vector2D(5, 5));
            double width = Arrays.stream(Colors.values())
                    .map(c -> FontRenderer.getStringSize(c.getName()).getX())
                    .max(Comparator.naturalOrder()).orElse(50D);
            setSize(new Vector2D(width + 15, -10));
            for (Colors c : Colors.values()) {
                addItem(new ColorItem(this, inputField, c));
            }
        }
    }

    private static class ColorItem extends ScrollableListItem<ColorItem> {
        public ColorItem(ScrollableList<ColorItem> parent, InputField inputField, Colors color) {
            super(parent);
            setHeight(12);
            Button button = new Button(
                    color.getCode() + color.getName(),
                    new Vector2D(0, 0), new Vector2D(1, 1),
                    mouseButton -> {
                        inputField.typeContentAtCursor("{" + color.getName() + "}");
                        inputField.focus();
                    }
            );
            button.normalColor = Theme.NONE;
            addChild(button, PERCENT.SIZE);
        }

        @Override
        public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
            renderComponents(mouse);
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

            ColorList cl = new ColorList(inputField);
            cl.setAbsolute(true);
            addChild(cl);

            InfoVarList vl = new InfoVarList(
                    inputField,
                    new Vector2D(5, 5),
                    new Vector2D(2 / 9D, -10)
            );
            vl.setAbsolute(true);
            addChild(vl, PERCENT.SIZE_X, Anchor.TOP_RIGHT);

            vl.bottomCover.setHeight(35, false);
            vl.bottomCover.addChild(
                    new TextRectangle(
                            new Vector2D(0, 5),
                            new Vector2D(1, 14),
                            "Filter",
                            null,
                            Color.WHITE
                    ), PERCENT.SIZE_X, Anchor.TOP_CENTER);
            vl.bottomCover.addChild(
                    new InputField(new Vector2D(0, 5), 0.9D)
                            .setOnContentChange(c -> vl.updateSearchFilter(c.getContent())),
                    PERCENT.SIZE_X, Anchor.BOTTOM_CENTER);
        }

        @Override
        public void render(Vector2D mousePos) {
            this.label.setText(getFormattedText());
            super.render(mousePos);
        }
    }
}
