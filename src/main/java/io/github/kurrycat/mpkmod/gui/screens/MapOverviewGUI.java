package io.github.kurrycat.mpkmod.gui.screens;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.*;
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
        ScrollableList<TestItem> test = new ScrollableList<>(
                getDisplayPos().add(120, 20),
                new Vector2D(200, 200)
        );
        test.addItem(
                new TestItem(test)
        );
        test.addItem(
                new TestItem(test)
        );
        test.addItem(
                new TestItem(test)
        );
        test.addItem(
                new TestItem(test)
        );
        test.addItem(
                new TestItem(test)
        );
        test.addItem(
                new TestItem(test)
        );
        components.add(test);
    }

    public static class TestItem extends ScrollableListItem<TestItem> {
        public TestItem(ScrollableList<TestItem> parent) {
            super(parent);
        }

        public void render(Vector2D pos, Vector2D size, Vector2D mouse) {
            Renderer2D.drawRectWithEdge(pos, size, 1, Color.GRAY, new Color(68, 86, 152, 128));
            FontRenderer.drawCenteredString("TEST", pos.add(size.div(2)), Color.WHITE, false);
        }
    }
}
