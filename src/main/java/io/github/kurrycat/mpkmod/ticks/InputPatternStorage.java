package io.github.kurrycat.mpkmod.ticks;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.kurrycat.mpkmod.save.Serializer;
import io.github.kurrycat.mpkmod.util.FileUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputPatternStorage {
    private static final String regex = "\\{[a-zA-Z]+}";
    private static final Pattern regexPattern = Pattern.compile(regex);
    private final static String stratFileName = "/assets/mpkmod/strats/strats.json";
    public static Map<String, InputPattern> patterns = new HashMap<>();

    /*static {
        patterns.put("FMM {x}t", InputPattern.fromString(Arrays.asList("WJ", "x*W", "y*WP")));
        patterns.put("c4.5", InputPattern.fromString(Arrays.asList("WJ", "2*W", "11*WP")));
        patterns.put("HH {x}t", InputPattern.fromString(Arrays.asList("x*WP", "WPJ")));
        System.out.println(patterns);
    }*/

    public static void init() {
        URL stratFile = FileUtil.getResource(stratFileName);
        if (stratFile == null) return;

        HashMap<String, ArrayList<String>> patterns = Serializer.deserializeAny(stratFile, new TypeReference<HashMap<String, ArrayList<String>>>() {});
        if (patterns == null) return;

        for (Map.Entry<String, ArrayList<String>> entry : patterns.entrySet()) {
            InputPatternStorage.patterns.put(entry.getKey(), InputPattern.fromString(entry.getValue()));
        }

        System.out.println(InputPatternStorage.patterns);
    }

    public static String match(List<TickInput> inputList) {
        List<InputPattern.Match> matches = new ArrayList<>();
        for (Map.Entry<String, InputPattern> entry : patterns.entrySet()) {
            String formatString = entry.getKey();
            Matcher matcher = regexPattern.matcher(formatString);

            InputPattern pattern = entry.getValue();

            InputPattern.Match match = pattern.match(inputList);
            if (match != null) {
                while (matcher.find()) {
                    String m = matcher.group(0);
                    String key = m.substring(1, m.length() - 1);
                    if (match.varNames.containsKey(key)) {
                        formatString = formatString.replace(matcher.group(0), String.valueOf(match.varNames.get(key)));
                    }
                }

                matches.add(match.setDisplayString(formatString));
            }
        }

        if (matches.isEmpty())
            return null;

        matches.sort(null);

        return matches.get(0).displayString;
    }
}
