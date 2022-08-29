package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.BoundingBox2D;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.ArrayList;

public class ScrollableList<I extends ScrollableListItem<I>> extends Component implements MouseInputListener, MouseScrollListener {
    public Color backgroundColor = Color.DARK_GRAY;
    public ScrollBar<I> scrollBar;
    public ArrayList<I> items = new ArrayList<>();

    public ScrollableList(Vector2D pos, Vector2D size) {
        super(pos);
        this.setSize(size);
        scrollBar = new ScrollBar<>(this);
    }

    public void addItem(I item) {
        this.items.add(item);
    }

    public void render(Vector2D mouse) {
        Renderer2D.drawRectWithEdge(getDisplayPos(), getSize(), 1, backgroundColor, Color.BLACK);
        scrollBar.render(mouse);
        int h = 1;
        for (I item : items) {
            if (h - scrollBar.scrollAmount > -item.getHeight() && h - scrollBar.scrollAmount < getSize().getY())
                item.render(
                        new Vector2D(getDisplayPos().getX() + 1, getDisplayPos().getY() + h - scrollBar.scrollAmount),
                        new Vector2D(getSize().getX() - 1 - scrollBar.barWidth, item.getHeight()),
                        mouse
                );
            h += item.getHeight() + 1;
        }
    }

    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        scrollBar.handleMouseInput(state, mousePos, button);
        return contains(mousePos);
    }

    public boolean handleMouseScroll(Vector2D mousePos, int delta) {
        scrollBar.scrollBy(-delta);
        return contains(mousePos);
    }

    public int totalHeight() {
        return items.stream().mapToInt(ScrollableListItem::getHeight).sum();
    }

    public static class ScrollBar<I extends ScrollableListItem<I>> extends Component implements MouseInputListener {
        private final ScrollableList<I> parent;
        public double barWidth = 10;
        public Color backgroundColor = Color.DARK_GRAY;
        public Color hoverColor = new Color(180, 180, 180);
        public Color clickedColor = new Color(101, 101, 101);
        private int scrollAmount = 0;

        private int clickedYOffset = -1;

        public ScrollBar(ScrollableList<I> parent) {
            super(null);
            this.pos = parent.getDisplayPos().add(parent.getSize().getX() - barWidth, 0);
            this.setSize(new Vector2D(barWidth, parent.getSize().getY()));
            this.parent = parent;
        }

        @Override
        public void render(Vector2D mouse) {
            Renderer2D.drawRectWithEdge(getDisplayPos(), getSize(), 1, backgroundColor, Color.BLACK);
            BoundingBox2D scrollButtonBB = getScrollButtonBB();

            Renderer2D.drawRect(
                    scrollButtonBB.getMin(),
                    scrollButtonBB.getSize(),
                    clickedYOffset != -1 ? clickedColor : contains(mouse) ? hoverColor : Color.WHITE
            );
        }

        public BoundingBox2D getScrollButtonBB() {
            return BoundingBox2D.fromPosSize(
                    new Vector2D(
                            getDisplayPos().getX() + 1,
                            getDisplayPos().getY() + mapScrollAmountToScrollButtonPos()
                    ),
                    new Vector2D(barWidth - 2, getScrollButtonHeight())
            );
        }

        public int mapScrollAmountToScrollButtonPos() {
            return MathUtil.map(
                    scrollAmount,
                    0, parent.totalHeight() - parent.getSize().getYI() - 2,
                    1, getSize().getYI() - getScrollButtonHeight() - 1
            );
        }

        public int mapScrollButtonPosToScrollAmount(Vector2D pos) {
            return MathUtil.map(
                    pos.getYI() - clickedYOffset - getDisplayPos().getYI(),
                    1, getSize().getYI() - getScrollButtonHeight() - 1,
                    0, parent.totalHeight() - parent.getSize().getYI() - 2
            );
        }

        @Override
        public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
            switch (state) {
                case DOWN:
                    if (getScrollButtonBB().contains(mousePos))
                        clickedYOffset = mousePos.getYI() - getScrollButtonBB().getMin().getYI();
                    break;
                case DRAG:
                    if (clickedYOffset != -1) {
                        scrollAmount = mapScrollButtonPosToScrollAmount(mousePos);
                        constrainScrollAmountToScreen();
                    }
                    break;
                case UP:
                    if (clickedYOffset != -1) {
                        scrollAmount = mapScrollButtonPosToScrollAmount(mousePos);
                        constrainScrollAmountToScreen();
                    }
                    clickedYOffset = -1;
                    break;
            }

            return getScrollButtonBB().contains(mousePos);
        }

        public void scrollBy(int delta) {
            scrollAmount += delta;
            constrainScrollAmountToScreen();
        }

        public int getScrollButtonHeight() {
            return MathUtil.sqr(getSize().getYI() - 2) / parent.totalHeight();
        }

        public void constrainScrollAmountToScreen() {
            scrollAmount = MathUtil.constrain(scrollAmount, 0, parent.totalHeight() - parent.getSize().getYI() - 2);
        }
    }
}
