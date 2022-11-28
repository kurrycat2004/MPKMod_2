package io.github.kurrycat.mpkmod.util;

import java.util.HashMap;

public class BoundingBox2D {
    private Vector2D min, max;

    public BoundingBox2D(Vector2D corner1, Vector2D corner2) {
        this.min = new Vector2D(
                Math.min(corner1.getX(), corner2.getX()),
                Math.min(corner1.getY(), corner2.getY())
        );
        this.max = new Vector2D(
                Math.max(corner1.getX(), corner2.getX()),
                Math.max(corner1.getY(), corner2.getY())
        );
    }

    public static BoundingBox2D fromPosSize(Vector2D pos, Vector2D size) {
        return new BoundingBox2D(pos.copy(), pos.add(size));
    }

    public Vector2D getMin() {
        return min.copy();
    }

    public Vector2D getMax() {
        return max.copy();
    }

    public Vector2D topLeft() {
        return min.copy();
    }

    public Vector2D bottomRight() {
        return max.copy();
    }

    public Vector2D topRight() {
        return new Vector2D(max.getX(), min.getY());
    }

    public Vector2D bottomLeft() {
        return new Vector2D(min.getX(), max.getY());
    }

    public Vector2D getSize() {
        return max.sub(min);
    }

    public double minX() {
        return min.getX();
    }

    public double maxX() {
        return max.getX();
    }

    public double minY() {
        return min.getY();
    }

    public double maxY() {
        return max.getY();
    }

    public boolean contains(Vector2D pos) {
        return pos.isInRectBetween(min, max);
    }

    public boolean onEdge(Vector2D pos, Edge edge, double maxError) {
        Line2D l = edge.getLine(this);
        return l.distanceToPos(pos) < maxError;
    }

    public Edge[] allOnEdge(Vector2D pos, double maxError) {
        HashMap<Edge, Double> edges = new HashMap<>();
        for (Edge e : Edge.values()) {
            double dist = e.getLine(this).distanceToPos(pos);
            if (dist < maxError && (!edges.containsKey(e.getOpposite()) || dist < edges.get(e.getOpposite()))) {
                edges.put(e, dist);
                edges.remove(e.getOpposite());
            }
        }
        return edges.keySet().toArray(new Edge[0]);
    }

    public enum Edge {
        LEFT(Vector2D.LEFT),
        RIGHT(Vector2D.RIGHT),
        TOP(Vector2D.UP),
        BOTTOM(Vector2D.DOWN);

        public final Vector2D dir;

        Edge(Vector2D dir) {
            this.dir = dir;
        }

        public static Edge byDir(Vector2D dir) {
            for (Edge e : values())
                if (e.dir.equals(dir))
                    return e;
            return null;
        }

        public Line2D getLine(BoundingBox2D bb) {
            switch (this) {
                default:
                case TOP:
                    return new Line2D(bb.topLeft(), bb.topRight());
                case BOTTOM:
                    return new Line2D(bb.bottomLeft(), bb.bottomRight());
                case LEFT:
                    return new Line2D(bb.topLeft(), bb.bottomLeft());
                case RIGHT:
                    return new Line2D(bb.topRight(), bb.bottomRight());
            }
        }

        public Edge getOpposite() {
            return byDir(dir.mult(-1));
        }
    }
}
