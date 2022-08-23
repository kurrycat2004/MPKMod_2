package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.KeyBinding;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class KeyBindingLabel extends Component {
    private final String name;
    @JsonIgnore
    private final KeyBinding keyBinding;
    private final Vector2D size;

    public Color keyDownColor = new Color(255, 255, 255, 95);
    public Color keyUpColor = new Color(31, 31, 31, 47);
    public Color selectedColor = new Color(255, 170, 0, 100);

    @JsonCreator
    public KeyBindingLabel(@JsonProperty("pos") Vector2D pos, @JsonProperty("name") String name) {
        super(pos);
        this.name = name;
        this.keyBinding = KeyBinding.getByName(name);
        this.size = new Vector2D(15, 15);
    }

    public void render(Vector2D mouse) {
        String displayName = keyBinding == null ? name : keyBinding.getDisplayName();
        boolean keyDown = keyBinding != null && keyBinding.isKeyDown();
        Color c = selected ? selectedColor : keyDown ? keyDownColor : keyUpColor;

        Renderer2D.drawRect(getDisplayPos(), size, c);

        FontRenderer.drawCenteredString(
                displayName,
                getDisplayPos().add(size.div(2)).add(new Vector2D(0, 1)),
                keyDown ? Color.BLACK : Color.WHITE,
                false
        );
    }

    @JsonProperty("size")
    public Vector2D getSize() {
        return this.size;
    }

    @JsonProperty("size")
    public KeyBindingLabel setSize(Vector2D size) {
        this.size.set(size);
        return this;
    }

    @JsonProperty("name")
    public String getName() {
        return this.name;
    }
}
