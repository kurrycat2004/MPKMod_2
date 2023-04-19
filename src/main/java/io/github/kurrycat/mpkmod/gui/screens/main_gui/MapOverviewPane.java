package io.github.kurrycat.mpkmod.gui.screens.main_gui;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.compatability.MCClasses.WorldInteraction;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.*;
import io.github.kurrycat.mpkmod.util.*;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

public class MapOverviewPane extends Pane {
    public MapOverviewPane(Vector2D pos, Vector2D size) {
        super(pos, size);
        this.backgroundColor = Color.DARK_GRAY;
        initComponents();
    }

    @Override
    public void render(Vector2D mousePos) {
        super.render(mousePos);
    }

    private void initComponents() {
        int padding = 10;

        components.add(new TextLabel("Test Label", this.getDisplayedPos().add(50)));
        components.add(new Button("TEST", getDisplayedPos().add(50, 100), new Vector2D(50, 20), mouseButton -> WorldToFile.parseWorld()));

        components.add(new CheckButton(getDisplayedPos().add(110, 100), System.out::println));

        components.add(new NumberSlider(0, 5, 1, 3, getDisplayedPos().add(50, 150), new Vector2D(100, 20), System.out::println));

        double sizeX = getDisplayedSize().getX() - getDisplayedSize().getX() / 2 - padding * 2;
        double sizeY = getDisplayedSize().getY() - padding * 2;

        ScrollableList<MapItem> mapItemList = new ScrollableList<>(getDisplayedPos().add(getDisplayedSize().getX() - sizeX - padding, padding), new Vector2D(sizeX, sizeY));

        mapItemList.addItem(new MapItem(mapItemList));
        mapItemList.addItem(new MapItem(mapItemList));
        mapItemList.addItem(new MapItem(mapItemList));
        mapItemList.addItem(new MapItem(mapItemList));
        mapItemList.addItem(new MapItem(mapItemList));
        mapItemList.addItem(new MapItem(mapItemList));
        mapItemList.addItem(new MapItem(mapItemList));
        components.add(mapItemList);
    }

    public static class MapItem extends ScrollableListItem<MapItem> {
        public MapItem(ScrollableList<MapItem> parent) {
            super(parent);
        }

        public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
            Renderer2D.drawRectWithEdge(pos, size, 1, Color.GRAY, new Color(68, 86, 152, 128));
            FontRenderer.drawCenteredString("TEST", pos.add(size.div(2)), Color.WHITE, false);
        }
    }
}
