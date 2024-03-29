package io.github.kurrycat.mpkmod.compatibility.MCClasses;

import io.github.kurrycat.mpkmod.compatibility.API;

import java.util.Optional;

public class SoundManager {
    /**
     * Plays the default minecraft button sound
     */
    public static void playButtonSound() {
        Interface.get().ifPresent(Interface::playButtonSound);
    }
    public interface Interface extends FunctionHolder {
        static Optional<Interface> get() {
            return API.getFunctionHolder(Interface.class);
        }

        void playButtonSound();
    }
}
