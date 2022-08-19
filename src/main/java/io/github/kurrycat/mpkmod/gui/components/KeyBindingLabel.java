package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.KeyBinding;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class KeyBindingLabel extends Component {
    private final String name;
    private final KeyBinding keyBinding;
    private final Vector2D size;

    public KeyBindingLabel(Vector2D pos, String name) {
        super(pos);
        this.name = name;
        this.keyBinding = KeyBinding.getByName(name);
        this.size = new Vector2D(15, 15);
    }

    public void render() {
        Vector2D pos = this.pos.asInRange(new Vector2D(0, 0), Renderer2D.getScaledSize());

        String displayName = keyBinding == null ? name : keyBinding.getDisplayName();
        boolean keyDown = keyBinding != null && keyBinding.isKeyDown();

        if (keyDown) {
            Renderer2D.drawRect(pos, size, new Color(255, 255, 255, 95));
        } else {
            Renderer2D.drawRect(pos, size, new Color(31, 31, 31, 47));
        }

        FontRenderer.drawCenteredString(
                displayName,
                pos.add(size.div(2)).add(new Vector2D(0, 1)),
                keyDown ? Color.BLACK : Color.WHITE,
                false
        );
    }

    public Vector2D getSize() {
        return this.size;
    }

    public KeyBindingLabel setSize(Vector2D size) {
        this.size.set(size);
        return this;
    }
}
