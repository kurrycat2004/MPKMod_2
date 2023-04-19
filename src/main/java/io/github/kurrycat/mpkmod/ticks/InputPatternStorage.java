package io.github.kurrycat.mpkmod.ticks;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputPatternStorage {
    private static final String regex = "\\{[a-zA-Z]+}";
    private static final Pattern regexPattern = Pattern.compile(regex);
    public static Map<String, InputPattern> patterns = new HashMap<>();

    static {
        patterns.put("FMM {x}t", InputPattern.fromString(Arrays.asList("WJ", "x*W", "y*WP")));
        patterns.put("c4.5", InputPattern.fromString(Arrays.asList("WJ", "2*W", "11*WP")));
        patterns.put("HH {x}t", InputPattern.fromString(Arrays.asList("x*WP", "WPJ")));
        System.out.println(patterns);
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
                matches.add(match);
            }
        }

        matches.sort(null);

        return null;
    }
}
