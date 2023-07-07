package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;

import java.util.ArrayList;
import java.util.List;

public class Last45Plot extends Plot {
    private final List<List<Double>> data = new ArrayList<>();
    private long lastTick = -1;

    @JsonCreator
    public Last45Plot() {
        super();
        this.minY = -100;
        this.maxY = 100;
        this.stepY = 20;
        this.setDataSupplier(() -> {
            if (Player.getLatest() == null) return data;

            if (lastTick != API.tickTime) {
                lastTick = API.tickTime;

                Player latest = Player.getLatest();
                Player bLatest = Player.getBeforeLatest();

                if (bLatest == null || latest == null) return data;

                if (bLatest.jumpTick && !bLatest.keyInput.isMovingSideways() && latest.keyInput.isMovingSideways()) {
                    data.clear();

                    if (startX <= 0)
                        for (int i = startX - 2; i < -1; i++) {
                            if (Player.tickHistory.size() + i < 0) continue;
                            List<Double> tick = fromPlayer(Player.tickHistory.get(Player.tickHistory.size() + i));
                            if (tick == null) continue;
                            data.add(tick);
                        }
                } else if (data.size() < dataSize) {
                    List<Double> tick = fromPlayer(bLatest);
                    data.add(tick);
                }
            }

            return data;
        });
    }

    private List<Double> fromPlayer(Player p) {
        if (p == null || p.deltaX == null) return null;
        List<Double> x = new ArrayList<>();
        for (int dx : p.deltaX)
            x.add((double) dx);

        return x;
    }
}
