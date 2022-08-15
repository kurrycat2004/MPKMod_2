package io.github.kurrycat.mpkmod.util;

public class Vector2D {
    private double x, y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }
    public float getXF() {
        return (float) x;
    }

    public Vector2D setX(double x) {
        this.x = x;
        return this;
    }

    public double getY() {
        return y;
    }
    public float getYF() {
        return (float) y;
    }

    public Vector2D setY(double y) {
        this.y = y;
        return this;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }
}
