package io.github.kurrycat.mpkmod.gui.screens;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.compatability.MCClasses.WorldInteraction;
import io.github.kurrycat.mpkmod.gui.ComponentScreen;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.*;
import io.github.kurrycat.mpkmod.landingblock.LandingBlock;
import io.github.kurrycat.mpkmod.util.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LandingBlockGuiScreen extends ComponentScreen {
    public static List<LandingBlock> lbs = new ArrayList<>();
    public static Color lbListColorItemEdge = new Color(255, 255, 255, 95);
    public static Color lbListColorBg = new Color(31, 31, 31, 150);
    public static Color hoverColor = new Color(70, 70, 70, 150);

    private LBList lbList;

    @Override
    public boolean shouldCreateKeyBind() {
        return true;
    }

    @Override
    public void onGuiInit() {
        super.onGuiInit();

        Vector2D windowSize = Renderer2D.getScaledSize();
        lbList = new LBList(
                new Vector2D(windowSize.getX() / 4D, 20),
                new Vector2D(windowSize.getX() / 2D, windowSize.getY() - 40)
        );

        components.add(
                new Button(
                        "TEST",
                        new Vector2D(10, 50),
                        new Vector2D(50, 20),
                        mouseButton -> {
                            lbs = LandingBlock.asLandingBlocks(WorldInteraction.getCollisionBoundingBoxes(new Vector3D(0, 10, 0)));
                            lbList.updateList();
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

        components.add(lbList);
    }

    public void drawScreen(Vector2D mouse, float partialTicks) {
        super.drawScreen(mouse, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        for (int i = 0; i < lbList.getItemCount(); i++) {
            lbList.getItem(i).landingBlock.highlight = false;
        }
    }

    public static class LBList extends ScrollableList<LBListItem> {

        public LBList(Vector2D pos, Vector2D size) {
            super(pos, size);
            updateList();
        }

        public void updateList() {
            items = lbs.stream().map(lb -> new LBListItem(this, lb)).collect(Collectors.toCollection(ArrayList<LBListItem>::new));
        }

        @Override
        public void render(Vector2D mouse) {
            super.render(mouse);
            for (int i = 0; i < getItemCount(); i++) {
                getItem(i).landingBlock.highlight = false;
            }
            Pair<LBListItem, Vector2D> p = getItemAndRelMousePosUnderMouse(mouse);
            if (p != null)
                p.first.landingBlock.highlight = true;
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
        public CheckButton shouldRender;
        public LandingBlock landingBlock;

        public LBListItem(ScrollableList<LBListItem> parent, LandingBlock landingBlock) {
            super(parent);
            this.landingBlock = landingBlock;
            shouldRender = new CheckButton(Vector2D.ZERO, checked -> {
                landingBlock.shouldRender = checked;
            });
            shouldRender.setChecked(landingBlock.shouldRender);
        }

        public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
            shouldRender.pos = pos.add(size.getX() / 5 - 11, size.getY() / 2 - 5.5);

            Renderer2D.drawRectWithEdge(pos, size, 1, lbListColorBg, lbListColorItemEdge);
            FontRenderer.drawCenteredString(
                    "minX: " + landingBlock.boundingBox.getMin().getX(),
                    pos.add(size.getX() / 5 * 2,
                            size.getY() / 4),
                    Color.WHITE, false
            );
            FontRenderer.drawCenteredString(
                    "minY: " + landingBlock.boundingBox.getMin().getY(),
                    pos.add(size.getX() / 5 * 2,
                            size.getY() / 2),
                    Color.WHITE, false
            );
            FontRenderer.drawCenteredString(
                    "minZ: " + landingBlock.boundingBox.getMin().getZ(),
                    pos.add(size.getX() / 5 * 2,
                            size.getY() / 4 * 3),
                    Color.WHITE, false
            );

            FontRenderer.drawCenteredString(
                    "maxX: " + landingBlock.boundingBox.getMax().getX(),
                    pos.add(size.getX() / 5 * 4,
                            size.getY() / 4),
                    Color.WHITE, false
            );
            FontRenderer.drawCenteredString(
                    "maxY: " + landingBlock.boundingBox.getMax().getY(),
                    pos.add(size.getX() / 5 * 4,
                            size.getY() / 2),
                    Color.WHITE, false
            );
            FontRenderer.drawCenteredString(
                    "maxZ: " + landingBlock.boundingBox.getMax().getZ(),
                    pos.add(size.getX() / 5 * 4,
                            size.getY() / 4 * 3),
                    Color.WHITE, false
            );

            shouldRender.render(mouse);
        }

        public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
            return shouldRender.handleMouseInput(state, mousePos, button);
        }
    }
}
