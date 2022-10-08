package io.github.kurrycat.mpkmod.util;

/**
 * 3-dimensional Vector<br>
 * Contains an x, y and z.<br>
 * Provides some utility functions that help with manipulating the vector.<br>
 * Every manipulation method that does not have <code>set</code> in its name, does not change the original vector but returns a new instance instead
 */
@SuppressWarnings("unused")
public class Vector3D {
    private double x, y, z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public Vector3D setX(double x) {
        this.x = x;
        return this;
    }

    public double getY() {
        return y;
    }

    public Vector3D setY(double y) {
        this.y = y;
        return this;
    }

    public double getZ() {
        return z;
    }

    public Vector3D setZ(double z) {
        this.z = z;
        return this;
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

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }

    public Vector3D copy() {
        return new Vector3D(this.x, this.y, this.z);
    }

    public double lengthXZSqr() {
        return this.x * this.x + this.z * this.z;
    }
}
