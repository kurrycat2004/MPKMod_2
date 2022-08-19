package io.github.kurrycat.mpkmod.util;

public class Vector2D {
    public static Vector2D ZERO = new Vector2D(0, 0);
    private double x, y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Vector2D vector2D) {
        this.x = vector2D.getX();
        this.y = vector2D.getY();
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

    public Vector2D add(Vector2D v) {
        return new Vector2D(this.x + v.x, this.y + v.y);
    }

    public Vector2D add(double v) {
        return add(new Vector2D(v, v));
    }

    public Vector2D addXInPlace(double x) {
        this.x += x;
        return this;
    }

    public Vector2D addYInPlace(double y) {
        this.y += y;
        return this;
    }

    public Vector2D subXInPlace(double x) {
        return addXInPlace(-x);
    }

    public Vector2D subYInPlace(double y) {
        return addYInPlace(-y);
    }

    public Vector2D sub(Vector2D v) {
        return new Vector2D(this.x - v.x, this.y - v.y);
    }

    public Vector2D sub(double v) {
        return new Vector2D(this.x - v, this.y - v);
    }

    public Vector2D sub(double x, double y) {
        return new Vector2D(this.x - x, this.y - y);
    }

    public Vector2D mult(Vector2D v) {
        return new Vector2D(this.x * v.x, this.y * v.y);
    }

    public Vector2D mult(double v) {
        return new Vector2D(this.x * v, this.y * v);
    }

    public Vector2D div(double v) {
        return new Vector2D(this.x / v, this.y / v);
    }

    public Vector2D div(Vector2D v) {
        return new Vector2D(this.x / v.x, this.y / v.y);
    }

    public Vector2D copy() {
        return new Vector2D(this.x, this.y);
    }

    public Vector2D asInRange(Vector2D from, Vector2D to) {
        Vector2D diff = to.sub(from);
        Vector2D curr = this.copy();
        while (curr.getX() < from.getX()) curr.addXInPlace(diff.getX());
        while (curr.getY() < from.getY()) curr.addYInPlace(diff.getY());
        while (curr.getX() > to.getX()) curr.subXInPlace(diff.getX());
        while (curr.getY() > to.getY()) curr.subYInPlace(diff.getY());
        return curr;
    }

    public double lengthSqr() {
        return this.x * this.x + this.y * this.y;
    }

    public double length() {
        return Math.sqrt(lengthSqr());
    }

    public Vector2D add(double x, double y) {
        return new Vector2D(this.x + x, this.y + y);
    }

    public boolean isInRectBetween(Vector2D pos1, Vector2D pos2) {
        return this.x > pos1.x && this.x < pos2.x
                && this.y > pos1.y && this.y < pos2.y;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Vector2D
                && ((Vector2D) obj).equals(this);
    }

    @Override
    public int hashCode() {
        return Double.hashCode(x) + Double.hashCode(y);
    }

    public boolean equals(Vector2D other) {
        return other.x == x && other.y == y;
    }

    public Vector2D constrain(Vector2D v1, Vector2D v2) {
        return new Vector2D(MathUtil.constrain(this.x, v1.x, v2.x), MathUtil.constrain(this.y, v1.y, v2.y));
    }
}
