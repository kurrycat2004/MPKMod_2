package io.github.kurrycat.mpkmod.gui.screens.main_gui;

import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.Component;
import io.github.kurrycat.mpkmod.gui.components.*;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.WorldToFile;

import java.awt.*;
import java.util.ArrayList;

public class OptionsPane extends Pane<MainGuiScreen> {
    public OptionsPane(Vector2D pos, Vector2D size) {
        super(pos, size);
        this.backgroundColor = new Color(16, 16, 16, 70);
        addTitle("Options");
        initComponents();
    }

    @Override
    public void render(Vector2D mousePos) {
        super.render(mousePos);
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
                false, false, Anchor.TOP_LEFT
        );
        addChild(pkcFileRadiusLabel, false, false, Anchor.TOP_LEFT);
        addChild(pkcFileRadius, false, false, Anchor.TOP_LEFT);

        addChild(
                new Button("Reload From File",
                        new Vector2D(20, 80),
                        new Vector2D(100, 20),
                        mouseButton -> {
                            paneHolder.cachedElements = paneHolder.loadJSONComponents();
                            paneHolder.movableComponents = new ArrayList<>(paneHolder.cachedElements);
                        }
                ),
                false, false, Anchor.TOP_LEFT
        );
    }
}
