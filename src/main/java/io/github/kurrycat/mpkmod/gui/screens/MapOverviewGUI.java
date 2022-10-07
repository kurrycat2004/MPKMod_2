package io.github.kurrycat.mpkmod.gui.screens;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.compatability.MCClasses.WorldInteraction;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.*;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.Vector3D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MapOverviewGUI extends Pane {
    public static List<BoundingBox3D> bbs = new ArrayList<>();

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
        int padding = 10;

        components.add(new TextLabel("Test Label", this.getDisplayPos().add(50)));
        components.add(
                new Button(
                        "TEST",
                        getDisplayPos().add(50, 100),
                        new Vector2D(50, 20),
                        mouseButton -> {
                            bbs = WorldInteraction.getCollisionBoundingBoxes(new Vector3D(0, 10, 0));
                        }
                )
        );

        double sizeX = getSize().getX() - getSize().getX() / 2 - padding * 2;
        double sizeY = getSize().getY() - padding * 2;

        ScrollableList<MapItem> mapItemList = new ScrollableList<>(
                getDisplayPos().add(getSize().getX() - sizeX - padding, padding),
                new Vector2D(sizeX, sizeY)
        );

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

        public void render(Vector2D pos, Vector2D size, Vector2D mouse) {
            Renderer2D.drawRectWithEdge(pos, size, 1, Color.GRAY, new Color(68, 86, 152, 128));
            FontRenderer.drawCenteredString("TEST", pos.add(size.div(2)), Color.WHITE, false);
        }
    }
}
