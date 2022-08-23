package io.github.kurrycat.mpkmod.util;

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

    public Vector2D getMin() {
        return min;
    }

    public Vector2D getMax() {
        return max;
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
}
