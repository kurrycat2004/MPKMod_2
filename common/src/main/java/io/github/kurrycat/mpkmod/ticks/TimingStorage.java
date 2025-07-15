package io.github.kurrycat.mpkmod.ticks;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.gui.screens.options_gui.Option;
import io.github.kurrycat.mpkmod.save.Serializer;
import io.github.kurrycat.mpkmod.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimingStorage {
    private final static String defaultStratFileName = "/assets/mpkmod/strats/strats.json";
    private final static String stratFileName = "config/mpk/config/strats.json";
    public static Map<String, Timing> patterns = new HashMap<>();

    @Option.Field(
            category = "labels",
            displayName = "Display ms for lastTiming",
            description = "Enable whether milliseconds should be shown in the lastTiming infoVar"
    )
    public static boolean renderLastTimingMS = false;

    public static void init() {
        InputStream stratFile = FileUtil.getResource(stratFileName);
        patterns = Serializer.deserializeAny(stratFile, new TypeReference<HashMap<String, Timing>>() {
        });

        File file = new File(stratFileName);
        if (!file.exists()) {
            //Make an empty json file if it doesn't exist
            FileUtil.createFile(stratFileName, "{}");
        }

        //TODO: Fix null pointer issue when the file has no timings
        //I am too tired to do this rn
//        try {
//            patterns.putAll(Serializer.deserializeAny(file, new TypeReference<HashMap<String, Timing>>() {}));
//        } catch (NullPointerException ignored) {}
        if (patterns == null) return;

        API.LOGGER.info(API.CONFIG_MARKER, "{} Timings loaded from {}", patterns.size(), stratFileName);
    }

    public static String match(List<TimingInput> inputList) {
        List<Timing.Match> matches = new ArrayList<>();
        for (Map.Entry<String, Timing> entry : patterns.entrySet()) {
            Timing.Match match = entry.getValue().match(inputList);
            if (match != null) {
                matches.add(match);
            }
        }

        if (matches.isEmpty())
            return null;

        matches.sort(null);

        return matches.get(0).displayString;
    }
}
