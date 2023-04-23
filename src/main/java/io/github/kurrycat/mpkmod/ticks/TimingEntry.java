package io.github.kurrycat.mpkmod.ticks;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Range;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimingEntry {
    private static final String tickCountRegex = "^((((?<diffNumber>\\d+)-)?((?<varName>[a-zA-Z]+)(\\{(?<lowerRange>\\d+)?,(?<upperRange>\\d+)?})?))|(?<rawCount>\\d+))$";
    private static final Pattern tickCountPattern = Pattern.compile(tickCountRegex);
    private static final String tickInputRegex = "^W?A?S?D?P?N?J?(!?G)?$";
    private static final Pattern tickInputPattern = Pattern.compile(tickInputRegex);

    public String timingEntry;
    public TimingInput timingInput;

    private Integer number;
    private String varName;
    private Range range;

    @JsonCreator
    public TimingEntry(String timingEntry) {
        this.timingEntry = timingEntry;
        String[] split = timingEntry.split(":", -1);
        if (split.length == 1) {
            number = 1;
            varName = null;
            range = null;
            timingInput = new TimingInput(split[0]);
        } else if (split.length == 2) {
            Matcher matcher = tickCountPattern.matcher(split[0]);
            if (!matcher.matches())
                throw new IllegalArgumentException(String.format("Invalid tick count: %s", split[0]));

            Integer diffNumber = MathUtil.parseInt(matcher.group("diffNumber"), null);
            String varName = matcher.group("varName");
            Integer lower = MathUtil.parseInt(matcher.group("lowerRange"), null);
            Integer upper = MathUtil.parseInt(matcher.group("upperRange"), null);
            Integer rawCount = MathUtil.parseInt(matcher.group("rawCount"), null);

            number = diffNumber == null ? rawCount : diffNumber;
            this.varName = varName;
            range = new Range(lower, upper);

            timingInput = new TimingInput(split[1]);
        } else {
            throw new IllegalArgumentException(String.format("More than one : found in timingEntry '%s', expected one", timingEntry));
        }
    }

    /**
     * @param inputList List of {@link TimingInput} instances
     * @param vars      HashMap containing all variables for that {@link Timing}. Vars of this TimingEntry are added
     * @return amount of matched inputs or null if no match was found (returns 0 for variable entries with 0 within range)
     */
    public Integer matches(List<TimingInput> inputList, HashMap<String, Integer> vars) {
        if (number == null && varName == null) return null;

        // is variable
        if (number == null) {
            int i = 0;
            while (i < inputList.size() && inputList.get(i).equals(timingInput)) i++;
            if (range.includes(i) || range.isAbove(i)) {
                if(vars.containsKey(varName))
                    vars.put(varName, vars.get(varName) + range.constrain(i));
                else vars.put(varName, range.constrain(i));
                return range.constrain(i);
            }
            return null;
        }

        int countToMatch = number;
        if (varName != null) {
            if (!vars.containsKey(varName)) {
                throw new IllegalArgumentException(String.format("The variable %s has not been defined in a prior timing input", varName));
            } else {
                countToMatch -= vars.get(varName);
            }
        }

        if (inputList.isEmpty() && number != 0) return null;

        int i;
        for (i = 0; countToMatch > 0; countToMatch--, i++) {
            if (i >= inputList.size())
                return null;
            if (!inputList.get(i).equals(timingInput))
                return null;
        }
        return i;
    }
}
