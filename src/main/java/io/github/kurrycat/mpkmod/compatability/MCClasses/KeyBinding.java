package io.github.kurrycat.mpkmod.compatability.MCClasses;

import io.github.kurrycat.mpkmod.compatability.API;

import java.util.HashMap;
import java.util.function.Supplier;

public class KeyBinding {
    private static final HashMap<String, KeyBinding> keyMap = new HashMap<>();
    private final String name;
    private final Supplier<Boolean> isKeyDown;
    private final Supplier<String> displayName;

    public KeyBinding(Supplier<String> displayName, String name, Supplier<Boolean> isKeyDown) {
        this.displayName = displayName;
        this.name = name;
        this.isKeyDown = isKeyDown;

        if (!keyMap.containsKey(this.name))
            keyMap.put(this.name, this);

        API.LOGGER.info(API.COMPATIBILITY_MARKER, "Loaded keybinding: " + this.name);
    }

    public static KeyBinding getByName(String name) {
        return keyMap.get(name);
    }

    public String getDisplayName() {
        return displayName.get();
    }

    public String getName() {
        return name;
    }

    public boolean isKeyDown() {
        return isKeyDown.get();
    }
}
