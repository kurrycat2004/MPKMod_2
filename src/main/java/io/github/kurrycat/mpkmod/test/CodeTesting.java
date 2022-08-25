package io.github.kurrycat.mpkmod.test;

import io.github.kurrycat.mpkmod.util.MathUtil;
import org.junit.jupiter.api.Test;

public class CodeTesting {
    @Test
    public void testAdd() {
        System.out.println(MathUtil.map(10, 0, 100, -10, 40));
    }

}
