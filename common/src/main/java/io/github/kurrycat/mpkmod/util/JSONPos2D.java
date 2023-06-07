package io.github.kurrycat.mpkmod.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.gui.components.Anchor;
import io.github.kurrycat.mpkmod.gui.components.ComponentHolder;

public class JSONPos2D {
    private final Anchor anchor;
    private final Anchor parentAnchor;
    private final double x, y;
    private final int percentFlag;

    public JSONPos2D(Vector2D pos, Anchor anchor, Anchor parentAnchor, int percentFlag) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.anchor = anchor;
        this.parentAnchor = parentAnchor;
        this.percentFlag = percentFlag;
    }

    @JsonCreator
    public JSONPos2D(@JsonProperty("x") double x, @JsonProperty("y") double y, @JsonProperty("anchor") Anchor anchor, @JsonProperty("parentAnchor") Anchor parentAnchor, @JsonProperty("percentFlag") int percentFlag) {
        //backwards compatibility
        if (anchor == null && parentAnchor == null) {
            this.x = Math.abs(x);
            this.y = Math.abs(y);
            this.anchor = Anchor.fromPos(new Vector2D(x,y));
            this.parentAnchor = this.anchor;
            this.percentFlag = ComponentHolder.PERCENT.NONE;
        } else {
            this.x = x;
            this.y = y;
            this.anchor = anchor;
            this.parentAnchor = parentAnchor;
            this.percentFlag = percentFlag;
        }
    }

    public Vector2D getPos() {
        return new Vector2D(x, y);
    }

    @JsonGetter("x")
    public double getX() {
        return x;
    }

    @JsonGetter("y")
    public double getY() {
        return y;
    }

    @JsonGetter("anchor")
    public Anchor getAnchor() {
        return anchor;
    }

    @JsonGetter("parentAnchor")
    public Anchor getParentAnchor() {
        return parentAnchor;
    }

    @JsonGetter("percentFlag")
    public int getPercentFlag() {
        return percentFlag;
    }
}
