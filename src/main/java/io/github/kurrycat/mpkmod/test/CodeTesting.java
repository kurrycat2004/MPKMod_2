package io.github.kurrycat.mpkmod.test;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;


public class CodeTesting {
    public static boolean test = false;

    @Test
    public void testAdd() throws NoSuchFieldException {
        Field f = CodeTesting.class.getField("test");
        System.out.println(f.getType() == boolean.class);
    }

}
