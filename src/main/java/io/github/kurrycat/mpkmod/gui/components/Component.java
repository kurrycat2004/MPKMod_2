package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.*;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Vector2D;

public abstract class Component extends ComponentHolder {
    @JsonIgnore
    public Vector2D pos;
    public boolean selected = false;
    public boolean highlighted = false;
    protected Vector2D size;

    private ComponentHolder parent = null;
    private boolean usePercentPosX = false;
    private boolean usePercentPosY = false;
    private boolean usePercentSizeX = false;
    private boolean usePercentSizeY = false;
    private Anchor parentAnchor = Anchor.TOP_LEFT;

    public Component(Vector2D pos) {
        if (pos != null) {
            this.pos = pos.abs();
            this.setParentAnchor(Anchor.fromPos(pos));
        }
    }

    @JsonCreator
    public Component() {

    }

    public Anchor getParentAnchor() {
        return parentAnchor;
    }

    public Component setParentAnchor(Anchor parentAnchor) {
        this.parentAnchor = parentAnchor;
        if (this.parentAnchor == null) this.parentAnchor = Anchor.TOP_LEFT;
        return this;
    }

    public void setParent(ComponentHolder parent, boolean usePercentPosX, boolean usePercentPosY, boolean usePercentSizeX, boolean usePercentSizeY) {
        this.parent = parent;
        this.usePercentPosX = usePercentPosX;
        this.usePercentPosY = usePercentPosY;
        this.usePercentSizeX = usePercentSizeX;
        this.usePercentSizeY = usePercentSizeY;
    }

    public void setParent(ComponentHolder parent) {
        this.setParent(parent, false, false, false, false);
    }

    public abstract void render(Vector2D mouse);

    @JsonIgnore
    public Vector2D getPos() {
        return this.pos;
    }

    @JsonIgnore
    public Component setPos(Vector2D pos) {
        this.pos = pos;
        return this;
    }

    @JsonGetter("pos")
    public Vector2D getJsonPos() {
        if (parentAnchor != null)
            return this.pos.mult(parentAnchor.multiplier);
        else return this.pos;
    }

    @JsonSetter("pos")
    public void setPosFromJson(Vector2D pos) {
        System.out.println("called");
        this.setParentAnchor(Anchor.fromPos(pos));
        this.pos = pos.abs();
    }

    /*public Component setWrapPos(Vector2D pos) {
        Vector2D windowSize = Renderer2D.getScaledSize();
        this.pos = new Vector2D(
                MathUtil.constrain(
                        pos.getX(),
                        this.pos.getX() < 0 ? -windowSize.getX() : 0,
                        this.pos.getX() < 0 ? -1 : windowSize.getX() - getSize().getX()
                ),
                MathUtil.constrain(
                        pos.getY(),
                        this.pos.getY() < 0 ? -windowSize.getY() : 0,
                        this.pos.getY() < 0 ? -getSize().getY() : windowSize.getY() - getSize().getY()
                )
        );
        return this;
    }*/

    public PopupMenu getPopupMenu() {
        return null;
    }

    public Vector2D getDisplayedPos() {
        if (parent == null)
            if (parentAnchor != null) {
                return parentAnchor.offsetChild(getPos(), getDisplayedSize(), Vector2D.ZERO, Renderer2D.getScaledSize());
            } else return this.pos;//.asInRange(new Vector2D(0, 0), Renderer2D.getScaledSize());
        else {
            Vector2D parentPos = parent.getDisplayedPos();
            Vector2D parentSize = parent.getDisplayedSize();
            Vector2D displayedSize = getDisplayedSize();
            Vector2D percentPos = this.pos.constrain(Vector2D.ZERO, Vector2D.ONE);

            return parentAnchor.offsetChild(
                    new Vector2D(
                            usePercentPosX ? MathUtil.map(percentPos.getX(), 0, 1, 0, parentSize.getX() - displayedSize.getX()) : this.pos.getX(),
                            usePercentPosY ? MathUtil.map(percentPos.getY(), 0, 1, 0, parentSize.getY() - displayedSize.getY()) : this.pos.getY()
                    ),
                    displayedSize,
                    parentPos, parentSize
            );
        }
    }

    @JsonProperty("size")
    public Vector2D getSize() {
        return this.size;
    }

    @JsonProperty("size")
    public Component setSize(Vector2D size) {
        this.size = size;
        return this;
    }

    public Vector2D getDisplayedSize() {
        if (parent == null)
            return getSize();
        else {
            Vector2D parentSize = parent.getDisplayedSize();
            Vector2D rawSize = new Vector2D(
                    usePercentSizeX ? MathUtil.constrain(getSize().getX(), 0, 1) * parentSize.getX() : getSize().getX(),
                    usePercentSizeY ? MathUtil.constrain(getSize().getY(), 0, 1) * parentSize.getY() : getSize().getY()
            );

            return new Vector2D(
                    rawSize.getX() < 0 ? parentSize.getX() + rawSize.getX() : rawSize.getX(),
                    rawSize.getY() < 0 ? parentSize.getY() + rawSize.getY() : rawSize.getY()
            );

        }
    }

    public boolean contains(Vector2D testPos) {
        return testPos.isInRectBetween(getDisplayedPos(), getDisplayedPos().add(getDisplayedSize()));
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public enum Anchor {
        TOP_LEFT(1, 1), TOP_RIGHT(-1, 1), BOTTOM_LEFT(1, -1), BOTTOM_RIGHT(-1, -1);

        public final Vector2D multiplier;

        Anchor(int x, int y) {
            multiplier = new Vector2D(x, y);
        }

        public static Anchor fromPos(Vector2D pos) {
            if (pos.getX() < 0) {
                if (pos.getY() < 0) return BOTTOM_RIGHT;
                else return TOP_RIGHT;
            } else {
                if (pos.getY() < 0) return BOTTOM_LEFT;
                else return TOP_LEFT;
            }
        }

        public Vector2D translateMovement(Vector2D movement) {
            return movement.mult(multiplier);
        }

        public Vector2D getAnchorPointPos(Vector2D pos, Vector2D size) {
            switch (this) {
                default:
                case TOP_LEFT:
                    return pos;
                case TOP_RIGHT:
                    return pos.add(size.getX(), 0);
                case BOTTOM_LEFT:
                    return pos.add(0, size.getY());
                case BOTTOM_RIGHT:
                    return pos.add(size);
            }
        }

        public Vector2D offsetChild(Vector2D childPos, Vector2D childSize, Vector2D parentPos, Vector2D parentSize) {
            switch (this) {
                default:
                case TOP_LEFT:
                    return parentPos.add(childPos);
                case TOP_RIGHT:
                    return new Vector2D(
                            parentPos.getX() + parentSize.getX() - childSize.getX() - childPos.getX(),
                            parentPos.getY() + childPos.getY()
                    );
                case BOTTOM_LEFT:
                    return new Vector2D(
                            parentPos.getX() + childPos.getX(),
                            parentPos.getY() + parentSize.getY() - childSize.getY() - childPos.getY()
                    );
                case BOTTOM_RIGHT:
                    return new Vector2D(
                            parentPos.getX() + parentSize.getX() - childSize.getX() - childPos.getX(),
                            parentPos.getY() + parentSize.getY() - childSize.getY() - childPos.getY()
                    );
            }
        }
    }
}
