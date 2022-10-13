package io.github.kurrycat.mpkmod.gui.screens;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.compatability.MCClasses.WorldInteraction;
import io.github.kurrycat.mpkmod.gui.ComponentScreen;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.NumberSlider;
import io.github.kurrycat.mpkmod.gui.components.ScrollableList;
import io.github.kurrycat.mpkmod.gui.components.ScrollableListItem;
import io.github.kurrycat.mpkmod.landingblock.LandingBlock;
import io.github.kurrycat.mpkmod.util.Colors;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.Vector3D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LandingBlockGuiScreen extends ComponentScreen {
    public static List<LandingBlock> lbs = new ArrayList<>();
    public static Color lbListColorItemEdge = new Color(255, 255, 255, 95);
    public static Color lbListColorBg = new Color(31, 31, 31, 150);
    public static Color hoverColor = new Color(70, 70, 70, 150);

    @Override
    public boolean shouldCreateKeyBind() {
        return true;
    }

    @Override
    public void onGuiInit() {
        super.onGuiInit();

        Vector2D windowSize = Renderer2D.getScaledSize();

        components.add(
                new Button(
                        "TEST",
                        new Vector2D(10, 50),
                        new Vector2D(50, 20),
                        mouseButton -> {
                            lbs = LandingBlock.asLandingBlocks(WorldInteraction.getCollisionBoundingBoxes(new Vector3D(0, 10, 0)));
                        }
                )
        );

        components.add(
                new NumberSlider(
                        0, 5, 1, 3,
                        new Vector2D(10, 150),
                        new Vector2D(100, 20),
                        System.out::println
                )
        );

        components.add(
                new LBList(
                        new Vector2D(windowSize.getX() / 4D, 20),
                        new Vector2D(windowSize.getX() / 2D, windowSize.getY() - 40)
                )
        );
    }

    public void drawScreen(Vector2D mouse, float partialTicks) {
        super.drawScreen(mouse, partialTicks);
    }

    public static class LBList extends ScrollableList<LBListItem> {
        public LBListItem itemInstance;
        public LBList(Vector2D pos, Vector2D size) {
            super(pos, size);
            itemInstance = new LBListItem(this);
        }

        @Override
        public LBListItem getItem(int index) {
            return itemInstance;
        }

        @Override
        public int getItemCount() {
            return lbs.size();
        }

        @Override
        public void drawTopCover(Vector2D pos, Vector2D size) {
            super.drawTopCover(pos, size);
            FontRenderer.drawCenteredString(Colors.UNDERLINE + "Landing Blocks", pos.add(size.div(2)), Color.WHITE, false);
        }

        @Override
        public void drawBottomCover(Vector2D pos, Vector2D size) {
            super.drawBottomCover(pos, size);

        }
    }

    public static class LBListItem extends ScrollableListItem<LBListItem> {
        public LBListItem(ScrollableList<LBListItem> parent) {
            super(parent);
        }

        public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
            if(index > lbs.size()) return;
            LandingBlock lb = lbs.get(index);
            if(lb == null) return;

            Renderer2D.drawRectWithEdge(pos, size, 1, lbListColorBg, lbListColorItemEdge);
            FontRenderer.drawCenteredString("minX: " + lb.boundingBox.getMin().getX(), pos.add(size.getX() / 4, size.getY() / 4), Color.WHITE, false);
            FontRenderer.drawCenteredString("minY: " + lb.boundingBox.getMin().getY(), pos.add(size.getX() / 4, size.getY() / 2), Color.WHITE, false);
            FontRenderer.drawCenteredString("minZ: " + lb.boundingBox.getMin().getZ(), pos.add(size.getX() / 4, size.getY() / 4 * 3), Color.WHITE, false);

            FontRenderer.drawCenteredString("maxX: " + lb.boundingBox.getMax().getX(), pos.add(size.getX() / 4 * 3, size.getY() / 4), Color.WHITE, false);
            FontRenderer.drawCenteredString("maxY: " + lb.boundingBox.getMax().getY(), pos.add(size.getX() / 4 * 3, size.getY() / 2), Color.WHITE, false);
            FontRenderer.drawCenteredString("maxZ: " + lb.boundingBox.getMax().getZ(), pos.add(size.getX() / 4 * 3, size.getY() / 4 * 3), Color.WHITE, false);
        }
    }
}
