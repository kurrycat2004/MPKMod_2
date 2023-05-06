package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.*;

import java.awt.*;
import java.util.ArrayList;

public abstract class ResizableComponent extends Component implements MouseInputListener {
    private Vector2D minSize = new Vector2D(5, 5);
    private BoundingBox2D.Edge[] areBeingResized = null;

    public ResizableComponent(Vector2D pos, Vector2D size) {
        super(pos);
        this.setSize(size);
    }

    public Vector2D getMinSize() {
        return minSize;
    }

    public ResizableComponent setMinSize(Vector2D minSize) {
        this.minSize = minSize;
        return this;
    }

    @Override
    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        if (button == Mouse.Button.LEFT) {
            BoundingBox2D bb = getComponentBoundingBox();
            switch (state) {
                case DOWN:
                    areBeingResized = bb.allOnEdge(mousePos, 2);
                    if (areBeingResized.length == 0)
                        areBeingResized = null;
                    break;
                case DRAG:
                    if (areBeingResized != null) resizeAccordingToSavedEdges(mousePos);
                    break;
                case UP:
                    if (areBeingResized != null) {
                        resizeAccordingToSavedEdges(mousePos);
                        areBeingResized = null;
                        return true;
                    }
                    break;
            }
            return areBeingResized != null;
        }

        return false;
    }

    public void renderHoverEdges(Vector2D mouse) {
        BoundingBox2D bb = getComponentBoundingBox();
        BoundingBox2D.Edge[] edges = bb.allOnEdge(mouse, 2);
        if (areBeingResized != null) edges = areBeingResized;
        for (BoundingBox2D.Edge e : edges) {
            Line2D l = e.getLine(bb);
            BoundingBox2D lineExpandedBB = new BoundingBox2D(l.p1.sub(1), l.p2.add(1));
            Renderer2D.drawRect(lineExpandedBB.getMin(), lineExpandedBB.getSize(), Color.RED);
        }
    }

    public void resizeAccordingToSavedEdges(Vector2D pos) {
        if (areBeingResized == null) return;

        ArrayList<BoundingBox2D.Edge> beingResized = new ArrayList<>();
        for(BoundingBox2D.Edge e : areBeingResized) {
            beingResized.add(BoundingBox2D.Edge.byDir(e.dir.mult(getParentAnchor().multiplier)));
        }

        pos = getParentAnchor().getAnchorPointPos(Vector2D.ZERO, Renderer2D.getScaledSize()).sub(pos).abs();

        Vector2D topLeftRelativePos = pos.sub(getPos());
        topLeftRelativePos = new Vector2D(
                Math.min(topLeftRelativePos.getX(), this.size.getX() - minSize.getX()),
                Math.min(topLeftRelativePos.getY(), this.size.getY() - minSize.getY())
        );

        Vector2D bottomRightRelativePos = pos.sub(getPos().add(getDisplayedSize()));
        bottomRightRelativePos = new Vector2D(
                Math.max(bottomRightRelativePos.getX(), -this.size.getX() + minSize.getX()),
                Math.max(bottomRightRelativePos.getY(), -this.size.getY() + minSize.getY())
        );

        if (beingResized.contains(BoundingBox2D.Edge.TOP)) {
            this.pos.addYInPlace(topLeftRelativePos.getY());
            this.size.addYInPlace(-topLeftRelativePos.getY());
        }
        if (beingResized.contains(BoundingBox2D.Edge.BOTTOM)) {
            this.size.addYInPlace(bottomRightRelativePos.getY());
        }
        if (beingResized.contains(BoundingBox2D.Edge.LEFT)) {
            this.pos.addXInPlace(topLeftRelativePos.getX());
            this.size.addXInPlace(-topLeftRelativePos.getX());
        }
        if (beingResized.contains(BoundingBox2D.Edge.RIGHT)) {
            this.size.addXInPlace(bottomRightRelativePos.getX());
        }
    }

    public BoundingBox2D getComponentBoundingBox() {
        return BoundingBox2D.fromPosSize(this.getDisplayedPos(), this.getDisplayedSize());
    }

}
