package io.github.kurrycat.mpkmod.util;

import io.github.kurrycat.mpkmod.gui.infovars.InfoString;

/**
 * 3-dimensional Vector<br>
 * Contains an x, y and z.<br>
 * Provides some utility functions that help with manipulating the vector.<br>
 * Every manipulation method that does not have <code>set</code> in its name, does not change the original vector but returns a new instance instead
 */
@SuppressWarnings("unused")
@InfoString.DataClass
public class Vector3D implements Copyable<Vector3D>, FormatDecimals {
    public static final Vector3D ZERO = new Vector3D(0, 0, 0);
    private double x, y, z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @InfoString.Getter
    public double getX() {
        return x;
    }

    public Vector3D setX(double x) {
        this.x = x;
        return this;
    }

    public int getXI() {
        return (int) x;
    }

    @InfoString.Getter
    public double getY() {
        return y;
    }

    public Vector3D setY(double y) {
        this.y = y;
        return this;
    }

    public int getYI() {
        return (int) y;
    }

    @InfoString.Getter
    public double getZ() {
        return z;
    }

    public Vector3D setZ(double z) {
        this.z = z;
        return this;
    }

    public int getZI() {
        return (int) z;
    }

    public Vector3D add(Vector3D v) {
        return new Vector3D(this.x + v.x, this.y + v.y, this.z + v.z);
    }

    public Vector3D add(double x, double y, double z) {
        return new Vector3D(this.x + x, this.y + y, this.z + z);
    }

    public Vector3D add(double v) {
        return new Vector3D(this.x + v, this.y + v, this.z + v);
    }

    public Vector3D sub(Vector3D v) {
        return new Vector3D(this.x - v.x, this.y - v.y, this.z - v.z);
    }

    public Vector3D sub(double v) {
        return new Vector3D(this.x - v, this.y - v, this.z - v);
    }

    public Vector3D mult(double v) {
        return new Vector3D(this.x * v, this.y * v, this.z * v);
    }

    public double lengthSqr() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public double length() {
        return Math.sqrt(lengthSqr());
    }

    @InfoString.Getter
    public double lengthXZ() {
        return Math.sqrt(lengthXZSqr());
    }

    @InfoString.Getter
    public double angleXZ() {
        return Math.toDegrees(Math.atan2(x, z));
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }

    public Vector3D copy() {
        return new Vector3D(this.x, this.y, this.z);
    }

    public Vector3D round() {
        return new Vector3D(Math.round(this.x), Math.round(this.y), Math.round(this.z));
    }

    public Vector3D floor() {
        return new Vector3D(Math.floor(this.x), Math.floor(this.y), Math.floor(this.z));
    }

    public Vector3D toInts() {
        return new Vector3D((int) this.x, (int) this.y, (int) this.z);
    }

    public double lengthXZSqr() {
        return this.x * this.x + this.z * this.z;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(this.x) + Double.hashCode(this.y) + Double.hashCode(this.z);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Vector3D && this.x == ((Vector3D) obj).x && this.y == ((Vector3D) obj).y && this.z == ((Vector3D) obj).z;
    }

    public Vector3D abs() {
        return new Vector3D(Math.abs(this.x), Math.abs(this.y), Math.abs(this.z));
    }

    public String formatDecimals(int decimals, boolean keepZeros) {
        return "[" + MathUtil.formatDecimals(this.x, decimals, keepZeros) + "," +
                MathUtil.formatDecimals(this.y, decimals, keepZeros) + "," +
                MathUtil.formatDecimals(this.z, decimals, keepZeros) + "]";
    }
}
