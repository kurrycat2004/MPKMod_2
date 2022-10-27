package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.KeyBinding;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class KeyBindingLabel extends ResizableComponent {
    @JsonProperty("name")
    private final String name;
    @JsonProperty("displayName")
    private final String displayName;
    private final KeyBinding keyBinding;

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

        PopupMenu keyUpColorMenu = new PopupMenu();
        keyUpColorMenu.addComponent(new ColorSelector(keyUpColor, Vector2D.OFFSCREEN, color -> keyUpColor = color));
        PopupMenu keyDownColorMenu = new PopupMenu();
        keyDownColorMenu.addComponent(new ColorSelector(keyDownColor, Vector2D.OFFSCREEN, color -> keyDownColor = color));
       /* PopupMenu selectedColorMenu = new PopupMenu();
        keyUpColorMenu.addComponent(new ColorSelector(selectedColor, Vector2D.OFFSCREEN, color -> selectedColor = color));*/

        menu.addSubMenu(new Button("Up Color", Vector2D.OFFSCREEN, new Vector2D(64, 11)), keyUpColorMenu);
        menu.addSubMenu(new Button("Down Color", Vector2D.OFFSCREEN, new Vector2D(64, 11)), keyDownColorMenu);
        return menu;
    }
}
