package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.KeyBinding;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class KeyBindingLabel extends ResizableComponent {
    @JsonProperty("name")
    private String name;
    @JsonProperty("displayName")
    private String displayName;
    private KeyBinding keyBinding;

    @JsonProperty
    public Color keyDownColor = new Color(255, 255, 255, 95);
    @JsonProperty
    public Color keyUpColor = new Color(31, 31, 31, 47);
    @JsonProperty
    public Color selectedColor = new Color(255, 170, 0, 100);

    public KeyBindingLabel(@JsonProperty("pos") Vector2D pos, @JsonProperty("size") Vector2D size, @JsonProperty("name") String name) {
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
        if (highlighted) Renderer2D.drawDottedRect(getDisplayPos(), getSize(), 1, 1, 1, Color.BLACK);
        Renderer2D.drawRect(getDisplayPos(), getSize(), c);

        FontRenderer.drawCenteredString(
                displayName,
                getDisplayPos().add(getSize().div(2)).add(new Vector2D(0, 1)),
                keyDown ? Color.BLACK : Color.WHITE,
                false
        );
        renderHoverEdges(mouse);
    }

    @JsonProperty("name")
    public String getName() {
        return this.name;
    }

    @Override
    public PopupMenu getPopupMenu() {
        PopupMenu menu = new PopupMenu();

        Vector2D windowSize = Renderer2D.getScaledSize();
        KeyBindingLabel.EditPane editPane = new KeyBindingLabel.EditPane(
                new Vector2D(windowSize.getX() * 0.3, windowSize.getY() / 2 - 20),
                new Vector2D(windowSize.getX() * 0.4, 50)
        );

        PopupMenu keyUpColorMenu = new PopupMenu();
        keyUpColorMenu.addComponent(new ColorSelector(keyUpColor, Vector2D.OFFSCREEN, color -> keyUpColor = color));
        PopupMenu keyDownColorMenu = new PopupMenu();
        keyDownColorMenu.addComponent(new ColorSelector(keyDownColor, Vector2D.OFFSCREEN, color -> keyDownColor = color));
       /* PopupMenu selectedColorMenu = new PopupMenu();
        keyUpColorMenu.addComponent(new ColorSelector(selectedColor, Vector2D.OFFSCREEN, color -> selectedColor = color));*/

        menu.addSubMenu(new Button("Up Color", Vector2D.OFFSCREEN, new Vector2D(64, 11)), keyUpColorMenu);
        menu.addSubMenu(new Button("Down Color", Vector2D.OFFSCREEN, new Vector2D(64, 11)), keyDownColorMenu);
        menu.addComponent(new Button("Edit", Vector2D.OFFSCREEN, new Vector2D(64, 11), mouseButton -> {
            if (Mouse.Button.LEFT.equals(mouseButton)) {
                menu.parent.openPane(editPane);
                menu.parent.closePane(menu);
            }
        }));
        menu.addComponent(new Button("Delete", Vector2D.OFFSCREEN, new Vector2D(64, 11), mouseButton -> {
            if (Mouse.Button.LEFT.equals(mouseButton)) {
                menu.parent.removeComponent(this);
                menu.parent.closePane(menu);
            }
        }));
        return menu;
    }

    private class EditPane extends Pane {
        private final TextRectangle downKey;
        private final TextRectangle upKey;
        private final InputField keyNameField;
        private final InputField displayNameField;

        public EditPane(Vector2D pos, Vector2D size) {
            super(pos, size);
            this.backgroundColor = new Color(31, 31, 31, 50);
            this.downKey = new TextRectangle(Vector2D.OFFSCREEN, KeyBindingLabel.this.getSize(), displayName, keyDownColor, Color.BLACK);
            this.components.add(this.downKey);
            this.upKey = new TextRectangle(Vector2D.OFFSCREEN, KeyBindingLabel.this.getSize(), displayName, keyUpColor, Color.WHITE);
            this.components.add(this.upKey);
            this.keyNameField = new InputField(name, Vector2D.OFFSCREEN, getSize().getX())
                    .setOnContentChange(content -> {
                        name = content.getContent();
                        keyBinding = KeyBinding.getByName(name);
                    });
            this.components.add(keyNameField);
            this.displayNameField = new InputField(displayName, Vector2D.OFFSCREEN, getSize().getX())
                    .setOnContentChange(content -> {
                        displayName = content.getContent();
                        this.downKey.setText(displayName);
                        this.upKey.setText(displayName);
                    });
            this.components.add(displayNameField);

            int currY = 3;
            double maxWidthLeft = 0;
            double maxWidthRight = 0;
            SortedSet<String> keyMap = new TreeSet<>(KeyBinding.getKeyMap().keySet());
            Vector2D windowSize = Renderer2D.getScaledSize();
            boolean overflowed = false;
            for (String keyName : keyMap) {
                Label l = new Label(keyName, new Vector2D(3, currY));
                currY += l.getSize().getY() + 1;
                if (overflowed) {
                    l.setPos(new Vector2D(windowSize.getX() - 3 - l.getSize().getX(), l.getPos().getY()));
                    this.components.add(l);
                    maxWidthRight = Math.max(maxWidthRight, l.getSize().getX());
                } else {
                    maxWidthLeft = Math.max(maxWidthLeft, l.getSize().getX());
                    this.components.add(l);
                }
                if (currY + 3 + l.getSize().getY() > windowSize.getY() && !overflowed) {
                    this.components.add(0, new Rectangle(new Vector2D(2, 2), new Vector2D(maxWidthLeft + 2, currY - 2), new Color(31, 31, 31, 150)));
                    overflowed = true;
                    currY = 3;
                }
            }
            this.components.add(0, new Rectangle(new Vector2D(windowSize.getX() - 4 - maxWidthRight, 2), new Vector2D(maxWidthRight + 2, currY - 2), new Color(31, 31, 31, 150)));
        }

        @Override
        public void render(Vector2D mousePos) {
            this.upKey.pos = new Vector2D(
                    getDisplayPos().getX() + getSize().getX() / 2 - this.upKey.getSize().getX() * 1.2,
                    getDisplayPos().getY() + 3
            );
            this.downKey.pos = new Vector2D(
                    getDisplayPos().getX() + getSize().getX() / 2 + this.downKey.getSize().getX() * 0.2,
                    getDisplayPos().getY() + 3
            );
            this.keyNameField.pos = new Vector2D(
                    getDisplayPos().getX() + getSize().getX() / 2 - this.keyNameField.getSize().getX() / 2,
                    getDisplayPos().getY() + getSize().getY() - 2 * this.keyNameField.getSize().getY() - 2
            );
            this.displayNameField.pos = new Vector2D(
                    getDisplayPos().getX() + getSize().getX() / 2 - this.displayNameField.getSize().getX() / 2,
                    getDisplayPos().getY() + getSize().getY() - this.displayNameField.getSize().getY() - 1
            );
            super.render(mousePos);
        }
    }
}
