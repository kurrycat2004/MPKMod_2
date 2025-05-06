package io.github.kurrycat.mpkmod.stonecutter.shared;

import io.github.kurrycat.mpkmod.api.ModPlatform;

public abstract class AbstractModPlatform implements ModPlatform {
    private final boolean isActive;

    public AbstractModPlatform(String testClass) {
        isActive = isClassLoaded(testClass);
    }

    private static boolean isClassLoaded(String className) {
        try {
            Class.forName(className, false, AbstractModPlatform.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean isActive() {
        return isActive;
    }
}
