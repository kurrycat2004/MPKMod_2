package io.github.kurrycat.mpkmod.util;

public class BoundingBox {
    private Vector3D min, max;

    public BoundingBox(Vector3D corner1, Vector3D corner2) {
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
    public double minY() {
        return min.getY();
    }
    public double maxY() {
        return max.getY();
    }
    public double minZ() {
        return min.getZ();
    }
    public double maxZ() {
        return max.getZ();
    }
}
