package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.util.Debug;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.util.ArrayList;

public abstract class ComponentHolder {
    protected ArrayList<Component> components = new ArrayList<>();
    protected ComponentHolder parent = null;
    protected boolean absolute = false;
    protected long lastUpdated = 0;
    /**
     * relative position, always positive, can be percentage
     */
    protected Vector2D pos = Vector2D.ZERO.copy();
    /**
     * absolute position used for rendering
     */
    protected Vector2D cpos = Vector2D.ZERO.copy();
    /**
     * relative size, always positive, can be percentage
     */
    protected Vector2D size = Vector2D.ZERO.copy();
    /**
     * absolute size used for rendering
     */
    protected Vector2D csize = Vector2D.ZERO.copy();
    /**
     * flag that determines whether posX, posY, sizeX and/or sizeY should be treated as a percentage of parent<br>
     * Just use {@link PERCENT} fields to set this (e.g. {@code PERCENT.POS_Y | PERCENT.SIZE_Y}
     * would make it so that posY and sizeY are treated as a percentage)
     */
    protected int percentFlag = PERCENT.NONE;
    /**
     * origin anchor for parent
     */
    protected Anchor parentAnchor = Anchor.TOP_LEFT;
    /**
     * origin anchor for this
     */
    protected Anchor anchor = Anchor.TOP_LEFT;
    private ComponentHolder root = null;

    private long getLastUpdated() {
        if (rParent() == null) return lastUpdated;
        return Math.max(lastUpdated, rParent().getLastUpdated());
    }

    public ComponentHolder getRoot() {
        return root;
    }

    public void setRoot(ComponentHolder root) {
        this.root = root;
    }

    public Vector2D getDisplayedPos() {
        if (parent != null && parent.getLastUpdated() > lastUpdated || root != null && root.getLastUpdated() > lastUpdated)
            updatePosAndSize();
        return this.cpos;
    }

    /**
     * Updates size and pos based on parent size.
     */
    public void updatePosAndSize() {
        if (parent != null) root = parent.root;

        //size update
        if (rParent() == null || rParent() == this) this.csize.set(this.size);
        else {
            Vector2D pSize = this.rParent().getDisplayedSize();
            this.csize.set(
                    PERCENT.HAS_SIZE_X(percentFlag) ? pSize.getX() * this.size.getX() :
                            (this.size.getX() >= 0 ? this.size.getX() : pSize.getX() + this.size.getX()),
                    PERCENT.HAS_SIZE_Y(percentFlag) ? pSize.getY() * this.size.getY() :
                            (this.size.getY() >= 0 ? this.size.getY() : pSize.getY() + this.size.getY())
            );
        }

        //pos update
        if (rParent() == null || rParent() == this) this.cpos.set(this.pos);
        else {
            this.cpos.set(
                    parentAnchor.getOriginPos(this.rParent().getDisplayedSize())
                            .add(this.rParent().getDisplayedPos())
                            .add(parentAnchor.transformVec(getPosWithoutPercentage()))
                            .sub(anchor.getOriginPos(this.csize))
            );
        }

        lastUpdated = System.nanoTime();
    }

    private ComponentHolder rParent() {
        return absolute ? root : parent;
    }

    private Vector2D getPosWithoutPercentage() {
        return new Vector2D(
                PERCENT.HAS_POS_X(percentFlag) ? this.rParent().getDisplayedSize().getX() * this.pos.getX() : this.pos.getX(),
                PERCENT.HAS_POS_Y(percentFlag) ? this.rParent().getDisplayedSize().getY() * this.pos.getY() : this.pos.getY()
        );
    }

    public Vector2D getDisplayedSize() {
        if (parent != null && parent.getLastUpdated() > lastUpdated || root != null && root.getLastUpdated() > lastUpdated)
            updatePosAndSize();
        return this.csize;
    }

    public void setAbsolute(boolean absolute) {
        this.absolute = absolute;
    }

    public void setSize(Vector2D size) {
        if (this.size.equals(size)) return;
        this.size.set(
                PERCENT.HAS_SIZE_X(percentFlag) ? MathUtil.constrain01(size.getX()) : size.getX(),
                PERCENT.HAS_SIZE_Y(percentFlag) ? MathUtil.constrain01(size.getY()) : size.getY()
        );
        updatePosAndSize();
    }

    public void addSize(Vector2D offset) {
        Vector2D transformed = parentAnchor.transformVec(offset);
        if (rParent() != null) {
            if (PERCENT.HAS_SIZE_X(percentFlag))
                transformed.setX(transformed.getX() / rParent().getDisplayedSize().getX());
            if (PERCENT.HAS_SIZE_Y(percentFlag))
                transformed.setY(transformed.getY() / rParent().getDisplayedSize().getY());
        }
        this.setSize(this.size.add(transformed));
    }

    public void addPos(Vector2D offset) {
        Vector2D transformed = parentAnchor.transformVec(offset);
        if (rParent() != null) {
            if (PERCENT.HAS_POS_X(percentFlag))
                transformed.setX(transformed.getX() / rParent().getDisplayedSize().getX());
            if (PERCENT.HAS_POS_Y(percentFlag))
                transformed.setY(transformed.getY() / rParent().getDisplayedSize().getY());
        }
        this.setPos(this.pos.add(transformed));
    }

    public void setCPos(Vector2D pos) {
        this.cpos.set(pos);
    }

    public void setPos(Vector2D pos) {
        if (this.pos.equals(pos)) return;
        if (PERCENT.HAS_POS_X(percentFlag) && MathUtil.constrain01(pos.getX()) != pos.getX() ||
                PERCENT.HAS_POS_Y(percentFlag) && MathUtil.constrain01(pos.getY()) != pos.getY()) {
            Debug.stacktrace("Warning: position not in range 0 - 1 even though percent flag is true for this field");
        }
        this.pos.set(
                PERCENT.HAS_POS_X(percentFlag) ? MathUtil.constrain01(pos.getX()) : pos.getX(),
                PERCENT.HAS_POS_Y(percentFlag) ? MathUtil.constrain01(pos.getY()) : pos.getY()
        );
        updatePosAndSize();
    }

    public void addChild(Component child) {
        addChild(child, PERCENT.NONE, Anchor.TOP_LEFT);
    }

    public void addChild(Component child, int percentFlag) {
        addChild(child, percentFlag, Anchor.TOP_LEFT);
    }

    /**
     * @param child       child component to add to parent
     * @param percentFlag flag built of {@link PERCENT} fields that determines which fields of posX, posY, sizeX and sizeY should be treated as a percentage of the parent
     * @param anchor      {@link Anchor}point of both the parent and child
     */
    public void addChild(Component child, int percentFlag, Anchor anchor) {
        addChild(child, percentFlag, anchor, anchor);
    }

    public void addChild(Component child, int percentFlag, Anchor anchor, Anchor parentAnchor) {
        passPositionTo(child, percentFlag, anchor, parentAnchor);
        this.components.add(child);
    }

    public void passPositionTo(ComponentHolder child, int percentFlag, Anchor anchor, Anchor parentAnchor) {
        child.parentAnchor = parentAnchor;
        child.anchor = anchor;
        passPositionTo(child, percentFlag);
    }

    public void passPositionTo(ComponentHolder child, int percentFlag) {
        child.percentFlag = percentFlag;
        passPositionTo(child);
    }

    public void passPositionTo(ComponentHolder child) {
        child.root = root;
        child.parent = this;

        child.updatePosAndSize();
    }

    public void passPositionTo(ComponentHolder child, int percentFlag, Anchor anchor) {
        passPositionTo(child, percentFlag, anchor, anchor);
    }

    public void removeChild(Component child) {
        this.components.remove(child);
        child.setRoot(null);
        child.parent = null;
    }

    @SuppressWarnings("unused")
    public static class PERCENT {
        public static final int NONE = 0;
        public static final int POS_X = 1;
        public static final int POS_Y = 1 << 1;
        public static final int SIZE_X = 1 << 2;
        public static final int SIZE_Y = 1 << 3;
        public static final int ALL = POS_X | POS_Y | SIZE_X | SIZE_Y;
        public static final int POS = POS_X | POS_Y;
        public static final int SIZE = SIZE_X | SIZE_Y;
        public static final int X = POS_X | SIZE_X;
        public static final int Y = POS_Y | SIZE_Y;

        public static boolean HAS_POS_X(int flag) {
            return (flag & POS_X) != 0;
        }

        public static boolean HAS_POS_Y(int flag) {
            return (flag & POS_Y) != 0;
        }

        public static boolean HAS_SIZE_X(int flag) {
            return (flag & SIZE_X) != 0;
        }

        public static boolean HAS_SIZE_Y(int flag) {
            return (flag & SIZE_Y) != 0;
        }
    }
}
