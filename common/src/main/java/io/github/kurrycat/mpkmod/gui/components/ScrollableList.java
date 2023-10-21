package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.Theme;
import io.github.kurrycat.mpkmod.gui.interfaces.HoverComponent;
import io.github.kurrycat.mpkmod.gui.interfaces.KeyInputListener;
import io.github.kurrycat.mpkmod.gui.interfaces.MouseInputListener;
import io.github.kurrycat.mpkmod.gui.interfaces.MouseScrollListener;
import io.github.kurrycat.mpkmod.util.*;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ScrollableList<I extends ScrollableListItem<I>> extends Component implements MouseInputListener, MouseScrollListener, KeyInputListener, HoverComponent {
    public final List<I> items = new CopyOnWriteArrayList<>();
    private final TextRectangle titleComponent;
    public Color backgroundColor = Theme.darkBackground;
    public Color edgeColor = Theme.darkEdge;
    public ScrollBar<I> scrollBar;
    public Div topCover;
    public Div bottomCover;
    public Div content;

    public ScrollableList() {
        topCover = new Div(new Vector2D(0, 0), new Vector2D(1, 0));
        topCover.backgroundColor = backgroundColor;
        passPositionTo(topCover, PERCENT.SIZE_X, Anchor.TOP_LEFT);

        titleComponent = new TextRectangle(
                new Vector2D(0, 0),
                new Vector2D(1, 1),
                "", null, Color.WHITE
        );
        topCover.addChild(titleComponent, PERCENT.SIZE);

        bottomCover = new Div(new Vector2D(0, 0), new Vector2D(1, 0));
        bottomCover.backgroundColor = backgroundColor;
        passPositionTo(bottomCover, PERCENT.SIZE_X, Anchor.BOTTOM_LEFT);

        content = new Div(new Vector2D(0, 0), new Vector2D(1, 1));
        passPositionTo(content, PERCENT.SIZE);
        stretchYBetween(content, topCover, bottomCover);

        scrollBar = new ScrollBar<>(this);
        content.passPositionTo(scrollBar, PERCENT.SIZE_Y, Anchor.TOP_RIGHT);
        scrollBar.setSize(new Vector2D(scrollBar.barWidth, 1));
    }

    public void addItem(I item) {
        this.items.add(item);
    }

    public void setTitle(String title) {
        this.titleComponent.setText(Colors.UNDERLINE + title);
        if (topCover.getDisplayedSize().getY() < 20)
            topCover.setSize(new Vector2D(topCover.size.getX(), 20));
    }

    public void renderComponents(Vector2D mouse) {
        components.forEach(c -> c.render(mouse));
        topCover.components.forEach(c -> c.render(mouse));
        bottomCover.components.forEach(c -> c.render(mouse));
    }

    public void render(Vector2D mouse) {
        scrollBar.constrainScrollAmountToScreen();

        int relItemYPos = 1;

        double itemWidth = getDisplayedSize().getX() - 2;
        if (shouldRenderScrollbar()) itemWidth -= scrollBar.barWidth - 1;

        Renderer2D.enableScissor(
                content.getDisplayedPos().getX(),
                content.getDisplayedPos().getY(),
                content.getDisplayedSize().getX(),
                content.getDisplayedSize().getY() -
                        (bottomCover.getDisplayedSize().getY() > 0 ? 0 : 1));

        int i = 0;
        for (I item : getItems()) {
            double absItemYPos = relItemYPos - scrollBar.scrollAmount;
            item.setParent(content);
            item.setPos(new Vector2D(1, absItemYPos));
            item.setSize(new Vector2D(itemWidth, item.getHeight()));
            if (absItemYPos >= -item.getHeight() && absItemYPos <= content.getDisplayedSize().getY()) {
                item.render(
                        i,
                        item.getDisplayedPos(),
                        item.getDisplayedSize(),
                        mouse
                );
            }
            relItemYPos += item.getHeight() + 1;
            i++;
        }

        Renderer2D.disableScissor();

        Renderer2D.drawHollowRect(getDisplayedPos().add(1), getDisplayedSize().sub(2), 1, edgeColor);
        if (topCover.getDisplayedSize().getY() > 0)
            Renderer2D.drawRect(topCover.getDisplayedPos().add(0, topCover.getDisplayedSize().getY()),
                    new Vector2D(topCover.getDisplayedSize().getX(), 1), edgeColor);
        if (bottomCover.getDisplayedSize().getY() > 0)
            Renderer2D.drawRect(bottomCover.getDisplayedPos(), new Vector2D(bottomCover.getDisplayedSize().getX(), 1), edgeColor);
        if (shouldRenderScrollbar())
            scrollBar.render(mouse);

        topCover.render(mouse);
        bottomCover.render(mouse);
    }

    private boolean shouldRenderScrollbar() {
        return totalHeight() > content.getDisplayedSize().getY() - 2;
    }

    /**
     * Override this if you don't want to use the default ArrayList implementation
     *
     * @return an iterable containing the items
     */
    public Iterable<I> getItems() {
        return items;
    }

    public int totalHeight() {
        int sum = 0;
        for (I item : getItems()) sum += item.getHeight() + 1;
        if (sum != 0) sum += 3;

        return sum;
    }

    public Pair<I, Vector2D> getItemAndRelMousePosUnderMouse(Vector2D mouse) {
        double itemWidth = content.getDisplayedSize().getX() - 2;
        if (shouldRenderScrollbar()) itemWidth -= scrollBar.barWidth - 1;
        if (mouse.getX() < content.getDisplayedPos().getX() + 1 || mouse.getX() > content.getDisplayedPos().getX() + itemWidth + 1)
            return null;

        double currY = mouse.getY() - 1 - content.getDisplayedPos().getY() + scrollBar.scrollAmount;
        for (I item : getItems()) {
            if (currY >= 0 && currY <= item.getHeight()) {
                return new Pair<>(item, new Vector2D(mouse.getX() - content.getDisplayedPos().getX() - 1, currY));
            }
            currY -= item.getHeight() + 1;
        }
        return null;
    }

    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        if (shouldRenderScrollbar() && scrollBar.handleMouseInput(state, mousePos, button))
            return true;

        boolean itemClicked = false;
        if (mousePos.getY() > content.getDisplayedPos().getY() &&
                mousePos.getY() < content.getDisplayedPos().getY() + content.getDisplayedSize().getY())
            for (I item : getItems()) {
                if (item.isVisible()) {
                    itemClicked = itemClicked || item.handleMouseInput(state, mousePos, button);
                }
            }

        return itemClicked ||
                ItrUtil.orMapAll(
                        ItrUtil.getAllOfType(MouseInputListener.class, components, topCover.components, bottomCover.components),
                        e -> e.handleMouseInput(state, mousePos, button)
                ) || contains(mousePos);
    }

    public boolean handleMouseScroll(Vector2D mousePos, int delta) {
        if (!contains(mousePos)) return false;

        boolean itemClicked = false;
        if (mousePos.getY() > content.getDisplayedPos().getY() &&
                mousePos.getY() < content.getDisplayedPos().getY() + content.getDisplayedSize().getY())
            for (I item : getItems()) {
                if (item.isVisible()) {
                    itemClicked = itemClicked || item.handleMouseScroll(mousePos, delta);
                }
            }

        if (itemClicked ||
                ItrUtil.orMapAll(
                        ItrUtil.getAllOfType(MouseScrollListener.class, components, topCover.components, bottomCover.components),
                        e -> e.handleMouseScroll(mousePos, delta)
                )
        ) return true;

        if (shouldRenderScrollbar())
            scrollBar.scrollBy(-delta);
        return contains(mousePos);
    }

    public boolean handleKeyInput(int keyCode, int scanCode, int modifiers, boolean isCharTyped) {
        boolean itemClicked = false;
        for (I item : getItems()) {
            if (item.isVisible()) {
                itemClicked = itemClicked || item.handleKeyInput(keyCode, scanCode, modifiers, isCharTyped);
            }
        }
        return itemClicked ||
                ItrUtil.orMapAll(
                        ItrUtil.getAllOfType(KeyInputListener.class, components, topCover.components, bottomCover.components),
                        e -> e.handleKeyInput(keyCode, scanCode, modifiers, isCharTyped)
                );
    }

    @Override
    public void renderHover(Vector2D mouse) {
        getItems().forEach(i -> i.renderHover(mouse));
        ItrUtil.getAllOfType(HoverComponent.class, components, topCover.components, bottomCover.components)
                .forEach(i -> i.renderHover(mouse));
    }

    public static class ScrollBar<I extends ScrollableListItem<I>> extends Component implements MouseInputListener {
        private final ScrollableList<I> parentList;
        public double barWidth = 11;
        public Color backgroundColor = Color.DARK_GRAY;
        public Color hoverColor = new Color(180, 180, 180);
        public Color clickedColor = new Color(101, 101, 101);
        private int scrollAmount = 0;

        private int clickedYOffset = -1;

        public ScrollBar(ScrollableList<I> parentList) {
            this.parentList = parentList;
        }

        @Override
        public void render(Vector2D mouse) {
            Renderer2D.drawRectWithEdge(getDisplayedPos(), getDisplayedSize(), 1, backgroundColor, Color.BLACK);
            BoundingBox2D scrollButtonBB = getScrollButtonBB();

            Renderer2D.drawRect(
                    scrollButtonBB.getMin().add(1),
                    scrollButtonBB.getSize().sub(2),
                    clickedYOffset != -1 ? clickedColor : contains(mouse) ? hoverColor : Color.WHITE
            );
        }

        public BoundingBox2D getScrollButtonBB() {
            return BoundingBox2D.fromPosSize(
                    new Vector2D(
                            getDisplayedPos().getX() + 1,
                            getDisplayedPos().getY() + mapScrollAmountToScrollButtonPos()
                    ),
                    new Vector2D(barWidth - 2, getScrollButtonHeight())
            );
        }

        public int mapScrollAmountToScrollButtonPos() {
            return MathUtil.map(
                    scrollAmount,
                    0, parentList.totalHeight() - getDisplayedSize().getYI() - 2,
                    1, getDisplayedSize().getYI() - getScrollButtonHeight() - 1
            );
        }

        public int getScrollButtonHeight() {
            int totalHeight = parentList.totalHeight();
            if (totalHeight == 0) totalHeight++;
            return Math.min(MathUtil.sqr(getDisplayedSize().getYI() - 2) / totalHeight, getDisplayedSize().getYI() - 2);
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

        public int mapScrollButtonPosToScrollAmount(Vector2D pos) {
            return MathUtil.map(
                    pos.getYI() - clickedYOffset - getDisplayedPos().getYI(),
                    1, getDisplayedSize().getYI() - getScrollButtonHeight() - 1,
                    0, parentList.totalHeight() - getDisplayedSize().getYI() - 2
            );
        }

        public void constrainScrollAmountToScreen() {
            scrollAmount = MathUtil.constrain(scrollAmount, 0, parentList.totalHeight() - getDisplayedSize().getYI() - 2);
        }

        public void scrollBy(int delta) {
            scrollAmount += delta;
            constrainScrollAmountToScreen();
        }
    }
}
