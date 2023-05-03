package io.github.kurrycat.mpkmod.compatibility.MCClasses;

import io.github.kurrycat.mpkmod.compatibility.API;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Keyboard {
    public static List<String> getPressedButtons() {
        return Interface.get().map(Interface::getPressedButtons).orElseGet(ArrayList::new);
    }

    public enum Modifier {
        CTRL,
        SHIFT,
        ALT,
        RCTRL,
        RSHIFT;
    }

    public interface Interface extends FunctionHolder {
        static Optional<Interface> get() {
            return API.getFunctionHolder(Interface.class);
        }

        List<String> getPressedButtons();
    }
}
