package io.github.kurrycat.mpkmod.util;

public class Mouse {
    public enum Button {
        LEFT(0),
        RIGHT(1),
        WHEEL(2);

        public final int value;

        Button(int v) {
            this.value = v;
        }

        public boolean equals(int v) {
            return v == this.value;
        }

        public static Button fromInt(int v) {
            for (Button b : values())
                if (b.value == v) return b;
            return null;
        }
    }

    public enum State {
        DOWN,
        DRAG,
        UP;
    }
}