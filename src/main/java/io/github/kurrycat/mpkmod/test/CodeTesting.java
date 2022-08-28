package io.github.kurrycat.mpkmod.test;

import io.github.kurrycat.mpkmod.util.Line2D;
import io.github.kurrycat.mpkmod.util.Vector2D;
import org.junit.jupiter.api.Test;


public class CodeTesting {
    @Test
    public void testAdd() {
        Line2D line = new Line2D(new Vector2D(0, 0), new Vector2D(0, 10));
        Vector2D testPos = new Vector2D(5, 5);
        System.out.println(line.distanceToPos(testPos));
    }

}
