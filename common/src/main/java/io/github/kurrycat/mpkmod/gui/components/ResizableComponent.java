package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.interfaces.MouseInputListener;
import io.github.kurrycat.mpkmod.util.*;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public abstract class ResizableComponent extends Component implements MouseInputListener {
    private Vector2D minSize = new Vector2D(5, 5);
    private BoundingBox2D.Edge[] areBeingResized = null;

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

    public BoundingBox2D getComponentBoundingBox() {
        return BoundingBox2D.fromPosSize(this.getDisplayedPos(), this.getDisplayedSize());
    }

    public void resizeAccordingToSavedEdges(Vector2D mousePos) {
        if (areBeingResized == null) return;
        BoundingBox2D bb = getComponentBoundingBox();
        List<BoundingBox2D.Edge> beingResized = Arrays.asList(areBeingResized);
        Boolean xResizing = beingResized.contains(BoundingBox2D.Edge.LEFT) ? Boolean.FALSE :
                (beingResized.contains(BoundingBox2D.Edge.RIGHT) ? Boolean.TRUE : null);
        Boolean yResizing = beingResized.contains(BoundingBox2D.Edge.TOP) ? Boolean.FALSE :
                (beingResized.contains(BoundingBox2D.Edge.BOTTOM) ? Boolean.TRUE : null);

        if (xResizing != null) {
            double mouseX = mousePos.getX();
            double edgePos = xResizing ? bb.maxX() : bb.minX();
            double oppositeEdgePos = xResizing ? bb.minX() : bb.maxX();

            if(xResizing)
                mouseX = MathUtil.constrain(mouseX, oppositeEdgePos, getRoot().getDisplayedSize().getX());
            else mouseX = MathUtil.constrain(mouseX, 0, oppositeEdgePos);

            double multiplier = xResizing == parentAnchor.invertedX ? -1 : 1;
            addSize(new Vector2D((mouseX - edgePos) * multiplier, 0));

            if(xResizing == anchor.invertedX) {
                addPos(new Vector2D(mouseX - edgePos, 0));
            }
        }
        if (yResizing != null) {
            double mouseY = mousePos.getY();
            double edgePos = yResizing ? bb.maxY() : bb.minY();
            double oppositeEdgePos = yResizing ? bb.minY() : bb.maxY();

            if(yResizing)
                mouseY = MathUtil.constrain(mouseY, oppositeEdgePos, getRoot().getDisplayedSize().getY());
            else mouseY = MathUtil.constrain(mouseY, 0, oppositeEdgePos);

            double multiplier = yResizing == parentAnchor.invertedY ? -1 : 1;
            addSize(new Vector2D(0, (mouseY - edgePos) * multiplier));

            if(yResizing == anchor.invertedY) {
                addPos(new Vector2D(0, mouseY - edgePos));
            }
        }
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
}
