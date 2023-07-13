package io.github.kurrycat.mpkmod.gui.screens.main_gui;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.*;
import io.github.kurrycat.mpkmod.util.Colors;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.WorldToFile;

import java.awt.*;

public class OptionsPane extends Pane<MainGuiScreen> {
    public OptionsPane(Vector2D pos, Vector2D size) {
        super(pos, size);
        this.backgroundColor = new Color(16, 16, 16, 70);
        addTitle("Options");
        initComponents();
    }

    private void initComponents() {
        TextRectangle pkcFileRadiusLabel = new TextRectangle(
                new Vector2D(20, 50),
                new Vector2D(50, 20),
                "Radius:",
                new Color(0, 0, 0, 0),
                Color.WHITE
        );
        NumberSlider pkcFileRadius = new NumberSlider(
                1, 20, 1, 5,
                new Vector2D(70, 50),
                new Vector2D(50, 20),
                v -> {
                }
        );
        addChild(
                new Button("Save as PKC File",
                        new Vector2D(20, 30),
                        new Vector2D(100, 20),
                        mouseButton -> WorldToFile.parseWorld((int) pkcFileRadius.getValue())),
                PERCENT.NONE, Anchor.TOP_LEFT
        );
        addChild(pkcFileRadiusLabel, PERCENT.NONE, Anchor.TOP_LEFT);
        addChild(pkcFileRadius, PERCENT.NONE, Anchor.TOP_LEFT);

        TestList list = new TestList();
        list.setPos(new Vector2D(10, 0.1));
        list.setSize(new Vector2D(0.4, 0.8));
        for (int i = 0; i < 20; i++)
            list.addItem(new TestItem(list));
        addChild(list, PERCENT.POS_Y | PERCENT.SIZE, Anchor.TOP_RIGHT);
    }

    @Override
    public void render(Vector2D mousePos) {
        super.render(mousePos);
    }

    private static class TestList extends ScrollableList<TestItem> {

    }

    private static class TestItem extends ScrollableListItem<TestItem> {
        public TestItem(ScrollableList<TestItem> parent) {
            super(parent);
            setHeight(20);
        }

        @Override
        public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
            Renderer2D.drawRect(pos, size, new Color((index * 62325) % 256, (index * 221434) % 256, (index * 151464) % 256));
            FontRenderer.drawCenteredString("Test", pos.add(size.div(2)), Color.WHITE, false);
        }
    }
}
