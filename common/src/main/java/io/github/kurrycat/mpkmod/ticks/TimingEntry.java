package io.github.kurrycat.mpkmod.ticks;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Range;
import io.github.kurrycat.mpkmod.util.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public boolean varNameMatches(TimingEntry other) {
        return varName != null && other.varName != null && varName.equals(other.varName);
    }

    /**
     * @param inputList List of {@link TimingInput} instances
     * @param startIndex the index it should start matching from
     * @param vars      HashMap containing all variables for that {@link Timing}. Vars of this TimingEntry are added
     * @param repeatedVar if the current var already appeared before
     * @return amount of matched inputs or null if no match was found (returns 0 for variable entries with 0 within range)
     */
    public Integer matches(List<TimingInput> inputList, int startIndex, HashMap<String, Timing.TickMS> vars, boolean repeatedVar) {
        if (number == null && varName == null) return null;

        // is variable
        if (number == null) {
            int i = startIndex;
            while (i < inputList.size() && inputList.get(i).equals(timingInput)) i++;

            i -= startIndex;

            if (range.includes(i) || range.isAbove(i)) {
                if (vars.containsKey(varName) && repeatedVar)
                    vars.get(varName).tickCount += range.constrain(i);
                else vars.put(varName, new Timing.TickMS(range.constrain(i)));

                vars.get(varName).ms = getMS(
                        startIndex + i - vars.get(varName).tickCount,
                        vars.get(varName).tickCount,
                        inputList
                );

                return range.constrain(i);
            }
            return null;
        }

        int countToMatch = number;
        if (varName != null) {
            if (!vars.containsKey(varName)) {
                throw new IllegalArgumentException(String.format("The variable %s has not been defined in a prior timing input (at %s)", varName, timingEntry));
            } else {
                countToMatch -= vars.get(varName).tickCount;
            }
        }

        if (inputList.size() == startIndex && number != 0) return null;

        int i;
        for (i = startIndex; countToMatch > 0; countToMatch--, i++) {
            if (i >= inputList.size()) return null;
            if (!inputList.get(i).equals(timingInput)) return null;
        }
        return i - startIndex;
    }

    private Integer getMS(int startIndex, int matchCount, List<TimingInput> inputList) {
        // if at least one tick matches and a tick after match exists
        if (matchCount == 0 || startIndex + matchCount >= inputList.size())
            return null;

        TimingInput before = startIndex == 0 ? TimingInput.stopTick() : inputList.get(startIndex - 1);
        TimingInput after = inputList.get(startIndex + matchCount);
        ArrayList<TimingInput> curr = new ArrayList<>();
        for (int i = startIndex; i < startIndex + matchCount; i++) {
            if (curr.isEmpty() || !curr.get(curr.size() - 1).equals(inputList.get(i))) {
                curr.add(inputList.get(i));
            }
        }

        Tuple<ButtonMS.Button, ButtonMS.Button> range = TimingInput.findMSButtons(before, after, curr);
        if (range == null)
            return null;

        ButtonMS startMS = curr.get(0).msList.forKey(range.getFirst());
        if (startMS == null) return null;
        ButtonMS endMS = after.msList.forKey(range.getSecond());
        if (endMS == null) return null;
        return endMS.msFrom(startMS);
    }
}
