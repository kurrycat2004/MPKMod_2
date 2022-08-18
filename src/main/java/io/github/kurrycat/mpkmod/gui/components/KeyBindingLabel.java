package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.KeyBinding;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class KeyBindingLabel extends Component {
    private final String name;
    private final KeyBinding keyBinding;

    public KeyBindingLabel(Vector2D pos, String name) {
        super(pos);
        this.name = name;
        this.keyBinding = KeyBinding.getByName(name);
    }

    public void render() {
        String displayName = keyBinding == null ? name : keyBinding.getDisplayName();
        boolean keyDown = keyBinding != null && keyBinding.isKeyDown();

        if (keyDown) {
            FontRenderer.drawString(displayName, pos.getXF() + 1, pos.getYF() + 1, Color.WHITE, false);
        } else {
            FontRenderer.drawString(displayName, pos.getXF(), pos.getYF(), Color.WHITE, true);
        }
    }
}
