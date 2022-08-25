package io.github.kurrycat.mpkmod.gui.screens;

import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.Pane;
import io.github.kurrycat.mpkmod.gui.components.ScrollableList;
import io.github.kurrycat.mpkmod.gui.components.TextLabel;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class MapOverviewGUI extends Pane {
    public MapOverviewGUI(Vector2D pos, Vector2D size) {
        super(pos, size);
        this.backgroundColor = Color.DARK_GRAY;
        initComponents();
    }

    @Override
    public void render(Vector2D mousePos) {
        super.render(mousePos);
    }

    private void initComponents() {
        components.add(new TextLabel("Test Label", this.getDisplayPos().add(50)));
        components.add(
                new Button(
                        "TEST",
                        getDisplayPos().add(50, 100),
                        new Vector2D(50, 20),
                        mouseButton -> {

                        }
                )
        );
        components.add(
                new ScrollableList(
                        getDisplayPos().add(100, 50),
                        new Vector2D(100, 200)
                )
        );
    }
}
