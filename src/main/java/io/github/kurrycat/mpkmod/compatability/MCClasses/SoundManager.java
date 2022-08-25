package io.github.kurrycat.mpkmod.compatability.MCClasses;

import io.github.kurrycat.mpkmod.compatability.functions.PlayButtonSoundFunction;

public class SoundManager {
    private static PlayButtonSoundFunction playButtonSoundFunction;

    public static void registerPlayButtonSound(PlayButtonSoundFunction f) {
        playButtonSoundFunction = f;
    }

    public static void playButtonSound() {
        playButtonSoundFunction.apply();
    }
}
