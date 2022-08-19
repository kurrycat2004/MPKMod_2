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

    public Vector2D setX(double x) {
        this.x = x;
        return this;
    }

    public float getXF() {
        return (float) x;
    }

    public int getXI() {
        return (int) x;
    }

    public double getY() {
        return y;
    }

    public Vector2D setY(double y) {
        this.y = y;
        return this;
    }

    public float getYF() {
        return (float) y;
    }

    public int getYI() {
        return (int) y;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }

    public Vector2D set(Vector2D vector2D) {
        this.setX(vector2D.getX());
        this.setY(vector2D.getY());
        return this;
    }
}
