package io.github.kurrycat.mpkmod.test;

import io.github.kurrycat.mpkmod.util.ColorUtil;
import org.junit.jupiter.api.Test;

import java.awt.*;


public class CodeTesting {
    @Test
    public void testAdd() {
        System.out.println(ColorUtil.hexToColor("#ffffffff"));
        System.out.println(ColorUtil.colorToHex(new Color(31, 31, 31, 150)));
    }

}
