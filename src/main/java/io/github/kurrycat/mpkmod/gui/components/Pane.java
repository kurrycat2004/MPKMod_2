package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.ArrayList;

public class Pane extends Component {

    public ArrayList<Component> components = new ArrayList<>();
    public ArrayList<Button> buttons = new ArrayList<>();
    public Color backgroundColor = new Color(255, 255, 255, 255);

    public Vector2D size;

    public Pane(Vector2D pos, Vector2D size) {
        super(pos);
        this.size = size;
    }

    @Override
    public void render(Vector2D mouse) {
        Renderer2D.drawRect(getDisplayPos(), getSize(), backgroundColor);
    }

    @Override
    public Vector2D getSize() {
        return this.size;
    }

}