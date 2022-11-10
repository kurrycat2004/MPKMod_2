package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.KeyBinding;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Colors;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class KeyBindingLabel extends ResizableComponent {
    @JsonProperty
    public Color keyDownColor = new Color(255, 255, 255, 95);
    @JsonProperty
    public Color keyUpColor = new Color(31, 31, 31, 47);
    @JsonProperty
    public Color selectedColor = new Color(255, 170, 0, 100);
    @JsonProperty("name")
    private String name;
    @JsonProperty("displayName")
    private String displayName;
    private KeyBinding keyBinding;

    public KeyBindingLabel(Vector2D pos, Vector2D size, String name) {
        super(pos, size);
        this.name = name;
        this.keyBinding = KeyBinding.getByName(name);
        this.displayName = keyBinding == null ? name : keyBinding.getDisplayName();
    }

    @JsonCreator
    public KeyBindingLabel(@JsonProperty("pos") Vector2D pos, @JsonProperty("size") Vector2D size, @JsonProperty("name") String name, @JsonProperty("displayName") String displayName) {
        super(pos, size);
        this.name = name;
        this.keyBinding = KeyBinding.getByName(name);
        this.displayName = displayName == null ? (keyBinding == null ? name : keyBinding.getDisplayName()) : displayName;
    }

    public void render(Vector2D mouse) {
        String displayName = this.displayName;
        boolean keyDown = keyBinding != null && keyBinding.isKeyDown();
        Color c = selected ? selectedColor : keyDown ? keyDownColor : keyUpColor;
        if (highlighted) Renderer2D.drawDottedRect(getDisplayedPos(), getDisplayedSize(), 1, 1, 1, Color.BLACK);
        Renderer2D.drawRect(getDisplayedPos(), getDisplayedSize(), c);

        FontRenderer.drawCenteredString(
                displayName,
                getDisplayedPos().add(getDisplayedSize().div(2)).add(new Vector2D(0, 1)),
                keyDown ? Color.BLACK : Color.WHITE,
                false
        );
        renderHoverEdges(mouse);
    }

    @JsonGetter("name")
    public String getName() {
        return this.name;
    }

    @Override
    public PopupMenu getPopupMenu() {
        PopupMenu menu = new PopupMenu();

        KeyBindingLabel.EditPane editPane = new KeyBindingLabel.EditPane(
                new Vector2D(0.65, 0.5),
                new Vector2D(0.4, 0.5)
        );
        editPane.setParent(API.mainGUI, true, true, true, true);

        menu.addComponent(new Button("Edit", Vector2D.OFFSCREEN, new Vector2D(64, 11), mouseButton -> {
            if (Mouse.Button.LEFT.equals(mouseButton)) {
                menu.paneHolder.openPane(editPane);
                menu.paneHolder.closePane(menu);
            }
        }));
        menu.addComponent(new Button("Delete", Vector2D.OFFSCREEN, new Vector2D(64, 11), mouseButton -> {
            if (Mouse.Button.LEFT.equals(mouseButton)) {
                menu.paneHolder.removeComponent(this);
                menu.paneHolder.closePane(menu);
            }
        }));
        return menu;
    }

    private class EditPane extends Pane {
        private final TextRectangle downKey;
        private final TextRectangle upKey;

        public EditPane(Vector2D pos, Vector2D size) {
            super(pos, size);
            this.backgroundColor = new Color(31, 31, 31, 50);
            this.upKey = new TextRectangle(new Vector2D(0.27, 2), KeyBindingLabel.this.getDisplayedSize(), displayName, keyUpColor, Color.WHITE);
            this.addChild(upKey, true, false, false, false, Anchor.TOP_LEFT);
            this.downKey = new TextRectangle(new Vector2D(0.27, 2), KeyBindingLabel.this.getDisplayedSize(), displayName, keyDownColor, Color.BLACK);
            this.addChild(downKey, true, false, false, false, Anchor.TOP_RIGHT);

            this.addChild(
                    new ColorSelector(keyUpColor, new Vector2D(0.2, 0.4), color -> keyUpColor = color),
                    true, true, false, false, Anchor.TOP_LEFT
            );
            this.addChild(
                    new ColorSelector(keyDownColor, new Vector2D(0.2, 0.4), color -> keyDownColor = color),
                    true, true, false, false, Anchor.TOP_RIGHT
            );


            this.addChild(
                    new InputField(name, new Vector2D(0.5, InputField.HEIGHT + 2), 1)
                            .setOnContentChange(content -> {
                                name = content.getContent();
                                keyBinding = KeyBinding.getByName(name);
                            }),
                    true, false, true, false, Anchor.BOTTOM_LEFT
            );
            this.addChild(
                    new InputField(displayName, new Vector2D(0.5, 1), 1)
                            .setOnContentChange(content -> {
                                displayName = content.getContent();
                                this.downKey.setText(displayName);
                                this.upKey.setText(displayName);
                            }),
                    true, false, true, false, Anchor.BOTTOM_LEFT
            );

            KeySetList list = new KeySetList(new Vector2D(1, 16), new Vector2D(0.3, -20));
            components.add(list);
            list.setParent(API.mainGUI, false, false, true, false);
            SortedSet<String> keyMap = new TreeSet<>(KeyBinding.getKeyMap().keySet());
            HashMap<String, ArrayList<String>> modMap = new HashMap<>();
            for (String s : keyMap) {
                String modName = s.substring(0, !s.contains(".") ? s.length() : s.indexOf("."));
                if (modName.equals("key")) modName = "Minecraft";

                if (!modMap.containsKey(modName)) modMap.put(modName, new ArrayList<>());
                modMap.get(modName).add(s);
            }
            list.addItem(new KeySetListItem(list, "Minecraft (key.)", modMap.get("Minecraft")));
            modMap.remove("Minecraft");

            for (String modName : new TreeSet<>(modMap.keySet())) {
                String keyPrefix = "No Prefix found";

                String exampleKey = modMap.get(modName).isEmpty() ? null : modMap.get(modName).get(0);
                if (exampleKey != null) {
                    if (exampleKey.contains(".key.")) {
                        keyPrefix = exampleKey.substring(0, exampleKey.indexOf(".key.") + 5);
                    } else if (exampleKey.contains(".")) {
                        keyPrefix = exampleKey.substring(0, exampleKey.indexOf(".") + 1);
                    }
                }

                list.addItem(
                        new KeySetListItem(
                                list,
                                modName + " (" + keyPrefix + ")",
                                modMap.get(modName)
                        )
                );
            }

            /*int currY = 3;
            double maxWidthLeft = 0;
            double maxWidthRight = 0;
            Vector2D windowSize = Renderer2D.getScaledSize();
            boolean overflowed = false;
            for (String keyName : keyMap) {
                Label l = new Label(keyName, new Vector2D(3, currY));
                currY += l.getDisplayedSize().getY() + 1;
                if (overflowed) {
                    l.setWrapPos(new Vector2D(windowSize.getX() - 3 - l.getDisplayedSize().getX(), l.getPos().getY()));
                    this.components.add(l);
                    maxWidthRight = Math.max(maxWidthRight, l.getDisplayedSize().getX());
                } else {
                    maxWidthLeft = Math.max(maxWidthLeft, l.getDisplayedSize().getX());
                    this.components.add(l);
                }
                if (currY + 3 + l.getDisplayedSize().getY() > windowSize.getY() && !overflowed) {
                    this.components.add(0, new Rectangle(new Vector2D(2, 2), new Vector2D(maxWidthLeft + 2, currY - 2), new Color(31, 31, 31, 150)));
                    overflowed = true;
                    currY = 3;
                }
            }
            this.components.add(0, new Rectangle(new Vector2D(windowSize.getX() - 4 - maxWidthRight, 2), new Vector2D(maxWidthRight + 2, currY - 2), new Color(31, 31, 31, 150)));*/
        }

        @Override
        public void render(Vector2D mousePos) {
            upKey.setColor(keyUpColor);
            downKey.setColor(keyDownColor);
            super.render(mousePos);
        }

        private class KeySetList extends ScrollableList<KeySetListItem> {
            public KeySetList(Vector2D pos, Vector2D size) {
                super(pos, size);
            }

            @Override
            public void drawTopCover(Vector2D mouse, Vector2D pos, Vector2D size) {
                super.drawTopCover(mouse, pos, size);
                FontRenderer.drawCenteredString(Colors.UNDERLINE + "Keybinding Names", pos.add(size.div(2)), Color.WHITE, false);
            }
        }

        private class KeySetListItem extends ScrollableListItem<KeySetListItem> {
            private final String modName;
            private final ArrayList<String> keyNames;
            private final Button collapseButton;
            private boolean collapsed = true;

            public KeySetListItem(ScrollableList<KeySetListItem> parent, String modName, ArrayList<String> keyNames) {
                super(parent);
                this.modName = modName;
                this.keyNames = keyNames;
                this.height = 13;
                collapseButton = new Button("v", Vector2D.OFFSCREEN, new Vector2D(11, 11), mouseButton -> {
                    if (mouseButton == Mouse.Button.LEFT) collapsed = !collapsed;
                });
            }

            @Override
            public int getHeight() {
                return collapsed ? 13 : 12 * (keyNames.size() + 1) + 1;
            }

            @Override
            public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
                if (collapsed) {
                    Renderer2D.drawRectWithEdge(pos, size, 1, new Color(31, 31, 31, 150), new Color(31, 31, 31, 150));
                    FontRenderer.drawCenteredString(modName, pos.add(size.div(2)), Color.WHITE, false);
                } else {
                    Renderer2D.drawRectWithEdge(pos, new Vector2D(size.getX(), this.height), 1, new Color(31, 31, 31, 150), new Color(31, 31, 31, 150));
                    Renderer2D.drawRect(pos.add(0, this.height), size.sub(0, this.height), new Color(31, 31, 31, 150));
                    FontRenderer.drawCenteredString(modName, pos.add(size.getX() / 2, this.height / 2D), Color.WHITE, false);

                    for (int i = 0; i < keyNames.size(); i++) {
                        FontRenderer.drawCenteredString(
                                keyNames.get(i),
                                pos.add(size.getX() / 2, this.height * 1.5D + 12 * i),
                                Color.WHITE, false
                        );
                    }
                }

                collapseButton.setText(collapsed ? "v" : "^");
                collapseButton.textOffset = collapsed ? Vector2D.ZERO : new Vector2D(0, 3);
                collapseButton.pos = pos.add(size.getX() - 12, 1).round();

                collapseButton.render(mouse);
            }

            @Override
            public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
                return collapseButton.handleMouseInput(state, mousePos, button);
            }
        }
    }
}
