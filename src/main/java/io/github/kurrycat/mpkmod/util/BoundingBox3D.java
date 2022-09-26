package io.github.kurrycat.mpkmod.util;

public class BoundingBox3D {
    private Vector3D min, max;

    public BoundingBox3D(Vector3D corner1, Vector3D corner2) {
        this.min = new Vector3D(
                Math.min(corner1.getX(), corner2.getX()),
                Math.min(corner1.getY(), corner2.getY()),
                Math.min(corner1.getZ(), corner2.getZ())
        );
        this.max = new Vector3D(
                Math.max(corner1.getX(), corner2.getX()),
                Math.max(corner1.getY(), corner2.getY()),
                Math.max(corner1.getZ(), corner2.getZ())
        );
    }

    public Vector3D getMin() {
        return min;
    }

    public Vector3D getMax() {
        return max;
    }

    public double minX() {
        return min.getX();
    }

    public double maxX() {
        return max.getX();
    }

    public double midX() {
        return (minX() + maxX()) / 2D;
    }

    public double minY() {
        return min.getY();
    }

    public double maxY() {
        return max.getY();
    }

    public double midY() {
        return (minY() + maxY()) / 2D;
    }

    public double minZ() {
        return min.getZ();
    }

    public double maxZ() {
        return max.getZ();
    }

    public double midZ() {
        return (minZ() + maxZ()) / 2D;
    }

    public boolean intersectsOrTouchesXZ(BoundingBox3D other) {
        return other.maxX() >= this.minX() &&
                other.minX() <= this.maxX() &&
                other.maxZ() >= this.minZ() &&
                other.minZ() <= this.maxZ();
    }

    public Vector3D distanceTo(BoundingBox3D other) {
        return new Vector3D(
                this.midX() > other.midX() ? this.minX() - other.maxX() : other.minX() - this.maxX(),
                this.midY() > other.midY() ? this.minY() - other.maxY() : other.minY() - this.maxY(),
                this.midZ() > other.midZ() ? this.minZ() - other.maxZ() : other.minZ() - this.maxZ()
        );
    }

    @Override
    public String toString() {
        return "BoundingBox3D{" + min + " - " + max + "}";
    }
}
