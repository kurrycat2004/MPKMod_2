package io.github.kurrycat.mpkmod.util;

public class Line2D {
    public Vector2D p1, p2;

    public Line2D(Vector2D p1, Vector2D p2) {
        this.p1 = p2.getX() < p1.getX() ? p2 : p1;
        this.p2 = p2.getX() < p1.getX() ? p1 : p2;
    }

    public double length() {
        return p2.sub(p1).length();
    }

    public double distanceToPos(Vector2D pos) {
        if (p1.equals(p2)) return p1.dist(pos);

        Vector2D len = p2.sub(p1);
        double t = len.dot(pos.sub(p1)) / len.lengthSqr();

        double cT = MathUtil.constrain(t, 0, 1);
        Vector2D pOnLine = p1.add(len.mult(cT));

        return pOnLine.dist(pos);
    }
}
