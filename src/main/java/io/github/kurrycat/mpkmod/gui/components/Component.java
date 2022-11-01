package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Vector2D;

public abstract class Component {
    @JsonProperty
    public Vector2D pos;
    public boolean selected = false;
    public boolean highlighted = false;
    protected Vector2D size;

    @JsonCreator
    public Component(@JsonProperty Vector2D pos) {
        this.pos = pos;
    }

    public abstract void render(Vector2D mouse);

    @JsonProperty
    public Vector2D getPos() {
        return this.pos;
    }

    public PopupMenu getPopupMenu() {
        return null;
    }

    public Component setPos(Vector2D pos) {
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
    }

    public Vector2D getDisplayPos() {
        return this.pos.asInRange(new Vector2D(0, 0), Renderer2D.getScaledSize());
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

    public boolean contains(Vector2D testPos) {
        return testPos.isInRectBetween(getDisplayPos(), getDisplayPos().add(getSize()));
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    public void setHighlighted(boolean highlighted) {this.highlighted = highlighted;}

    public enum Anchor {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;

        public static Anchor fromPos(Vector2D pos) {
            if (pos.getX() < 0) {
                if (pos.getY() < 0) return BOTTOM_RIGHT;
                else return TOP_RIGHT;
            } else {
                if (pos.getY() < 0) return BOTTOM_LEFT;
                else return TOP_LEFT;
            }
        }
    }
}
