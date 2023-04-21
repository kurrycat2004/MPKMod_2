package io.github.kurrycat.mpkmod.ticks;

import io.github.kurrycat.mpkmod.util.Copyable;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputPattern {
    private static final String pRegex = "^((?<tickCount>([0-9]+)|([a-zA-Z]+))\\*)?(?<input>W?A?S?D?P?N?J?)$";
    private static final Pattern pRegexPattern = Pattern.compile(pRegex);
    private final List<Tuple<TickInput, TickCount>> pattern;
    private final List<String> varNames;

    public InputPattern(List<Tuple<TickInput, TickCount>> pattern) {
        this.pattern = pattern;
        this.varNames = getVarNames();
    }

    public static InputPattern fromString(List<String> inputString) {
        List<Tuple<TickInput, TickCount>> pattern = new ArrayList<>();
        for (String s : inputString) {
            Matcher matcher = pRegexPattern.matcher(s);
            if (matcher.find()) {
                TickInput input = new TickInput(matcher.group("input"));
                String tickCount = matcher.group("tickCount");
                if (tickCount != null) {
                    pattern.add(new Tuple<>(input, new TickCount(tickCount)));
                } else {
                    pattern.add(new Tuple<>(input, new TickCount(1, null)));
                }
            }
        }
        return new InputPattern(pattern);
    }

    private List<String> getVarNames() {
        List<String> list = new ArrayList<>();
        for (Tuple<TickInput, TickCount> t : this.pattern) {
            if (t.getSecond().varName != null)
                list.add(t.getSecond().varName);
        }
        return list;
    }

    private List<Tuple<TickInput, TickCount>> patternCopy() {
        List<Tuple<TickInput, TickCount>> copy = new ArrayList<>();
        pattern.forEach(p -> copy.add(p.copy()));
        return copy;
    }

    private Map<String, Integer> createVarMap() {
        Map<String, Integer> varMap = new HashMap<>();
        for (String var : varNames) {
            varMap.put(var, null);
        }
        return varMap;
    }


    /**
     * @param inputList List that is checked for pattern
     * @return -1 if no match, else offset from the end of the list
     */
    public Match match(List<TickInput> inputList) {
        List<Tuple<TickInput, TickCount>> pattern = patternCopy();
        Map<String, Integer> varNames = createVarMap();

        int firstTickMatch = -1;
        int i, j = 0;

        inputList:
        for (i = 1; i < inputList.size(); i++) {
            //check for stop tick
            if (inputList.get(i - 1).isMoving()) continue;

            //skip if first tick is not equals
            if (!inputList.get(i).equals(pattern.get(0).getFirst())) continue;

            firstTickMatch = i;
            for (j = 0; j < pattern.size(); ) {
                TickInput pInput = pattern.get(j).getFirst();
                TickCount pCount = pattern.get(j).getSecond();

                //reached end of input list using last pattern tick which is variable -> match
                if (i + j == inputList.size() && j == pattern.size() - 1 && pCount.isVariable) {
                    break;
                }

                //pattern is longer than rest of input list -> will not find match
                if (i + j >= inputList.size()) {
                    return null;
                }

                TickInput input = inputList.get(i + j);

                if (input.equals(pInput)) {
                    if (pCount.count != null) {
                        pCount.count--;
                        if (pCount.count == 0) j++;
                        else i++;
                    } else {
                        if (varNames.get(pCount.varName) == null) varNames.put(pCount.varName, 1);
                        else varNames.put(pCount.varName, varNames.get(pCount.varName) + 1);
                        i++;
                    }
                }
                //no current match but current pattern tick is variable -> test next tick
                else if (pCount.isVariable) {
                    varNames.putIfAbsent(pCount.varName, 0);
                    j++;
                    i--;
                }
                //pattern failed to match -> try testing from next tick in inputList again
                else {
                    pattern = patternCopy();
                    varNames = createVarMap();
                    i = firstTickMatch;
                    firstTickMatch = -1;
                    continue inputList;
                }
            }
            break;
        }
        if (firstTickMatch == -1) {
            return null;
        }
        return new Match(firstTickMatch, inputList.size() - 1 - (i + j), varNames);
    }

    @Override
    public String toString() {
        return "InputPattern{" +
                "pattern=" + pattern +
                '}';
    }

    public static class Match implements Comparable<Match> {
        public final int start;
        public final int endOffset;
        public final Map<String, Integer> varNames;

        public String displayString = "null";

        private Match(int start, int endOffset, Map<String, Integer> varNames) {
            this.start = start;
            this.endOffset = endOffset;
            this.varNames = varNames;
        }

        @Override
        public String toString() {
            return "Match{" +
                    "start=" + start +
                    ", endOffset=" + endOffset +
                    ", varNames=" + varNames +
                    '}';
        }

        @Override
        public int compareTo(Match o) {
            return Integer.compare(this.endOffset, o.endOffset);
        }

        public Match setDisplayString(String displayString) {
            this.displayString = displayString;
            return this;
        }
    }

    public static class TickCount implements Copyable<TickCount> {
        private static final String regex = "^((?<count>[0-9]+)|(?<varName>[a-zA-Z]+))$";
        private static final Pattern pattern = Pattern.compile(regex);

        public Integer count = null;
        public String varName = null;
        public boolean isVariable;

        /**
         * @param countString count in the format "[0-9]+|[a-zA-Z]+",
         */
        public TickCount(String countString) {
            Matcher matcher = pattern.matcher(countString);
            if (matcher.find()) {
                varName = matcher.group("varName");
                if (varName == null) {
                    count = MathUtil.parseInt(matcher.group("count"), 0);
                }
            }
            isVariable = count == null;
        }

        public TickCount(Integer count, String varName) {
            this.count = count;
            this.varName = varName;
            isVariable = count == null;
        }

        public TickCount copy() {
            return new TickCount(this.count, this.varName);
        }

        @Override
        public String toString() {
            return "TickCount{" +
                    "count=" + count +
                    ", varName='" + varName + '\'' +
                    '}';
        }
    }
}
