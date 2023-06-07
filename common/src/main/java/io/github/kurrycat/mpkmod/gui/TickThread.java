package io.github.kurrycat.mpkmod.gui;

import io.github.kurrycat.mpkmod.compatibility.API;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TickThread implements Runnable {
    private static final AtomicReference<List<Tickable>> tickables = new AtomicReference<>();
    private static final AtomicBoolean changed = new AtomicBoolean(false);

    public static void startThread() {
        Thread t = new Thread(
                new TickThread(),
                API.MODID + " GUI Tick Thread"
        );
        t.start();
    }

    public static void setTickables(List<Tickable> tickables) {
        TickThread.tickables.set(tickables);
        TickThread.changed.set(true);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (!changed.get()) continue;

            for (Tickable tickable : tickables.get()) {
                try {
                    tickable.runTick();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface Tickable {
        default void runTick() {
            synchronized (this) {
                tick();
            }
        }

        void tick();
    }
}
