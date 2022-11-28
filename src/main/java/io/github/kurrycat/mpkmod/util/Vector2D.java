package io.github.kurrycat.mpkmod.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Vector2D {
    public static final Vector2D ONE = new Vector2D(1, 1);
    public static final Vector2D ZERO = new Vector2D(0, 0);
    public static final Vector2D OFFSCREEN = null;
    public static final Vector2D LEFT = new Vector2D(-1, 0);
    public static final Vector2D RIGHT = new Vector2D(1, 0);
    public static final Vector2D UP = new Vector2D(0, -1);
    public static final Vector2D DOWN = new Vector2D(0, 1);
    private double x, y;

    @JsonCreator
    public Vector2D(@JsonProperty("x") double x, @JsonProperty("y") double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Vector2D vector2D) {
        this.x = vector2D.getX();
        this.y = vector2D.getY();
    }

    @JsonProperty("x")
    public double getX() {
        return x;
    }

    @JsonProperty("x")
    public Vector2D setX(double x) {
        this.x = x;
        return this;
    }

    @JsonIgnore
    public float getXF() {
        return (float) x;
    }

    @JsonIgnore
    public int getXI() {
        return (int) x;
    }

    @JsonProperty("y")
    public double getY() {
        return y;
    }

    @JsonProperty("y")
    public Vector2D setY(double y) {
        this.y = y;
        return this;
    }

    @JsonIgnore
    public float getYF() {
        return (float) y;
    }

    @JsonIgnore
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


    /**
     * @param from top left corner
     * @param to botton right corner
     * @return a new {@link Vector2D}, being a <code>this</code>, but looped to the other end of the input rectangle if outside it
     */
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

    public double dist(Vector2D other) {
        return this.sub(other).length();
    }

    public Vector2D add(double x, double y) {
        return new Vector2D(this.x + x, this.y + y);
    }

    public double dot(Vector2D other) {
        return x * other.x + y * other.y;
    }

    /**
     * @param pos1 top left corner
     * @param pos2 bottom right corner
     * @return whether <code>this</code> is inside the input rectangle
     */
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

    /**
     * @param v1 top left corner
     * @param v2 bottom right corner
     * @return a new {@link Vector2D} with <code>x</code> constrained between <code>v1.x</code> and <code>v2.x</code> and <code>y</code> constrained between
     * <code>v1.y</code> and <code>v2.y</code> using {@link MathUtil#constrain}
     */
    public Vector2D constrain(Vector2D v1, Vector2D v2) {
        return new Vector2D(MathUtil.constrain(this.x, v1.x, v2.x), MathUtil.constrain(this.y, v1.y, v2.y));
    }

    public Vector2D round() {
        return new Vector2D(Math.round(this.x), Math.round(this.y));
    }

    public Vector2D abs() {
        return new Vector2D(Math.abs(this.x), Math.abs(this.y));
    }
}
