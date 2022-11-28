package io.github.kurrycat.mpkmod.compatability.MCClasses;

import io.github.kurrycat.mpkmod.compatability.API;

import java.util.Optional;

public class Profiler {
    public static void startSection(String section) {
        Interface.get().ifPresent(i -> i.startSection(section));
    }

    public static void endStartSection(String section) {
        Interface.get().ifPresent(i -> i.endStartSection(section));
    }

    public static void endSection() {
        Interface.get().ifPresent(Interface::endSection);
    }

    public interface Interface extends FunctionHolder {
        static Optional<Interface> get() {
            return API.getFunctionHolder(Interface.class);
        }

        void startSection(String name);

        void endStartSection(String name);

        void endSection();
    }
}
