package io.github.kurrycat.mpkmod.util;

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

    public Vector3D sub(Vector3D v) {
        return new Vector3D(this.x - v.x, this.y - v.y, this.z - v.z);
    }

    public Vector3D mult(double v) {
        return new Vector3D(this.x * v, this.y * v, this.z * v);
    }
}
