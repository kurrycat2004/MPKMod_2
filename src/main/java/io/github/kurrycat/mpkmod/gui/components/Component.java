package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Vector2D;

public abstract class Component {
    public Vector2D pos;
    @JsonIgnore
    public boolean selected = false;

    @JsonCreator
    public Component(@JsonProperty("pos") Vector2D pos) {
        this.pos = pos;
    }

    public abstract void render(Vector2D mouse);

    @JsonProperty("pos")
    public Vector2D getPos() {
        return this.pos;
    }

    @JsonIgnore
    public Component setPos(Vector2D pos) {
        this.pos = new Vector2D(
                MathUtil.constrain(
                        pos.getX(),
                        this.pos.getX() < 0 ? -Renderer2D.getScaledSize().getX() : 0,
                        this.pos.getX() < 0 ? -1 : Renderer2D.getScaledSize().getX() - getSize().getX()
                ),
                MathUtil.constrain(
                        pos.getY(),
                        this.pos.getY() < 0 ? -Renderer2D.getScaledSize().getY() : 0,
                        this.pos.getY() < 0 ? -getSize().getY() : Renderer2D.getScaledSize().getY() - getSize().getY()
                )
        );
        return this;
    }

    @JsonIgnore
    public Vector2D getDisplayPos() {
        return this.pos.asInRange(new Vector2D(0, 0), Renderer2D.getScaledSize());
    }

    public abstract Vector2D getSize();

    public boolean contains(Vector2D testPos) {
        return testPos.isInRectBetween(getDisplayPos(), getDisplayPos().add(getSize()));
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

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
