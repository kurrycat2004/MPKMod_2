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
    public boolean resetOnOpen() {
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

        components.add(
                new InputField(
                        new Vector2D(10, 180),
                        100
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

        public InputField minX, minY, minZ, maxX, maxY, maxZ;
        public InputField[] fields;

        public LBListItem(ScrollableList<LBListItem> parent, LandingBlock landingBlock) {
            super(parent);
            this.landingBlock = landingBlock;
            shouldRender = new CheckButton(Vector2D.ZERO, checked -> {
                landingBlock.shouldRender = checked;
            });
            shouldRender.setChecked(landingBlock.shouldRender);
            minX = new InputField("" + landingBlock.boundingBox.getMin().getX(), Vector2D.OFFSCREEN, 25).setName("minX: ");
            minY = new InputField("" + landingBlock.boundingBox.getMin().getY(), Vector2D.OFFSCREEN, 25).setName("minY: ");
            minZ = new InputField("" + landingBlock.boundingBox.getMin().getZ(), Vector2D.OFFSCREEN, 25).setName("minZ: ");
            maxX = new InputField("" + landingBlock.boundingBox.getMax().getX(), Vector2D.OFFSCREEN, 25).setName("maxX: ");
            maxY = new InputField("" + landingBlock.boundingBox.getMax().getY(), Vector2D.OFFSCREEN, 25).setName("maxY: ");
            maxZ = new InputField("" + landingBlock.boundingBox.getMax().getZ(), Vector2D.OFFSCREEN, 25).setName("maxZ: ");

            fields = new InputField[]{minX, minY, minZ, maxX, maxY, maxZ};
        }

        public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
            shouldRender.pos = pos.add(size.getX() / 16 - 4, size.getY() / 2 - 5.5);

            Renderer2D.drawRectWithEdge(pos, size, 1, lbListColorBg, lbListColorItemEdge);

            for (int i = 0; i < fields.length; i++) {
                fields[i].pos = pos.add(
                        size.getX() / 7 * (1 + ((int) (i / 3) * 3)),
                        size.getY() / 4 * (1 + (i % 3)) - fields[i].getSize().getY() / 2
                );
                fields[i].setWidth(size.getX() / 3);
                fields[i].render(mouse);
            }

            shouldRender.render(mouse);
        }

        public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
            return ArrayListUtil.orMapAll(
                    ArrayListUtil.getAllOfType(MouseInputListener.class, minX, minY, minZ, maxX, maxY, maxZ, shouldRender),
                    ele -> ele.handleMouseInput(state, mousePos, button)
            );
        }

        public boolean handleKeyInput(int keyCode, String key, boolean pressed) {
            return ArrayListUtil.orMapAll(
                    ArrayListUtil.getAllOfType(KeyInputListener.class, minX, minY, minZ, maxX, maxY, maxZ),
                    ele -> ele.handleKeyInput(keyCode, key, pressed)
            );
        }
    }
}
