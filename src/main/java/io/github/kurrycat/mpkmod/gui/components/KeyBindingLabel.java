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
    @JsonProperty("name")
    private final String name;
    private final KeyBinding keyBinding;

    @JsonProperty
    public Color keyDownColor = new Color(255, 255, 255, 95);
    @JsonProperty
    public Color keyUpColor = new Color(31, 31, 31, 47);
    @JsonProperty
    public Color selectedColor = new Color(255, 170, 0, 100);

    @JsonCreator
    public KeyBindingLabel(@JsonProperty("pos") Vector2D pos, @JsonProperty("name") String name) {
        super(pos);
        this.name = name;
        this.keyBinding = KeyBinding.getByName(name);
        this.setSize(new Vector2D(15, 15));
    }

    public void render(Vector2D mouse) {
        String displayName = keyBinding == null ? name : keyBinding.getDisplayName();
        boolean keyDown = keyBinding != null && keyBinding.isKeyDown();
        Color c = selected ? selectedColor : keyDown ? keyDownColor : keyUpColor;

        Renderer2D.drawRect(getDisplayPos(), getSize(), c);

        FontRenderer.drawCenteredString(
                displayName,
                getDisplayPos().add(getSize().div(2)).add(new Vector2D(0, 1)),
                keyDown ? Color.BLACK : Color.WHITE,
                false
        );
    }

    @JsonProperty("name")
    public String getName() {
        return this.name;
    }
}
