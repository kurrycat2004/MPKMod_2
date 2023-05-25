package io.github.kurrycat.mpkmod.ticks;

public class ButtonMS {
    private final Long nanos;
    private final boolean state;
    private final Button button;

    private ButtonMS(Button button, Long nanos, boolean state) {
        this.button = button;
        this.nanos = nanos;
        this.state = state;
    }

    public static ButtonMS of(Button button, long nanos, boolean state) {
        return new ButtonMS(button, nanos, state);
    }

    public static ButtonMS down(Button button, long nanos) {
        return new ButtonMS(button, nanos, true);
    }

    public static ButtonMS up(Button button, long nanos) {
        return new ButtonMS(button, nanos, false);
    }

    public boolean isState(boolean state) {
        return state == this.state;
    }

    public String getKey() {
        return button.key;
    }

    public Button getButton() {
        return button;
    }

    @Override
    public String toString() {
        return "ButtonMS{" +
                button + "." +
                (state ? "DOWN" : "UP") +
                ": " + nanos +
                "}";
    }

    public Integer msFrom(ButtonMS before) {
        return (int) ((nanos - before.nanos) / 1_000_000D);
    }

    public enum Button {
        FORWARD("W"),
        LEFT("A"),
        BACKWARD("S"),
        RIGHT("D"),
        SPRINT("P"),
        SNEAK("N"),
        JUMP("J");

        public static final int[] ONLY_MOVE_INDICES = {0, 1, 2, 3, 6};
        public static final int[] ONLY_MOVE_MOD_INDICES = {4, 5, 6};
        public static final int[] ALL = {0, 1, 2, 3, 4, 5, 6};

        private final String key;

        Button(String button) {
            this.key = button;
        }

        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return "Button." + key;
        }
    }
}
