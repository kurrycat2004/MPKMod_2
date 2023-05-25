package io.github.kurrycat.mpkmod.ticks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Timing {
    private final LinkedHashMap<FormatCondition, FormatString> format;
    private final TimingEntry[] timingEntries;

    @JsonCreator
    public Timing(@JsonProperty("format") LinkedHashMap<FormatCondition, FormatString> format, @JsonProperty("timingEntries") TimingEntry[] timingEntries) {
        this.format = format;
        this.timingEntries = timingEntries;
    }

    public Match match(List<TimingInput> inputList) {
        Match result = null;
        for (int i = 0; i < inputList.size() - 1; i++) {
            if (inputList.get(i).isStopTick() && !inputList.get(i + 1).isStopTick()) {
                Match match = startsWithMatch(inputList.subList(i + 1, inputList.size()));
                if (match != null)
                    result = match;
            }
        }
        return result;
    }

    private Match startsWithMatch(List<TimingInput> inputList) {
        HashMap<String, TickMS> vars = new HashMap<>();
        int startIndex = 0;
        for (int i = 0; i < timingEntries.length; i++) {
            boolean repeatedVar = i >= 1 && timingEntries[i].varNameMatches(timingEntries[i - 1]);
            Integer matchCount = timingEntries[i].matches(inputList, startIndex, vars, repeatedVar);
            if (matchCount == null)
                return null;

            //System.out.printf("count: %d, entry: %s, inputList: %s\n", matchCount, entry.timingEntry, inputList);
            startIndex += matchCount;
            //inputList = inputList.subList(matchCount, inputList.size());
        }

        //System.out.printf("Match: %s\nVars: %s\n\n", inputList, vars);
        return new Match(getFormatString(vars), vars.size(), inputList.size() - startIndex);
    }

    private String getFormatString(HashMap<String, TickMS> vars) {
        StringBuilder sb = new StringBuilder();
        format.forEach((fc, fs) -> {
            if (fc.check(vars)) sb.append(fs.get(vars));
        });
        return sb.toString();
    }

    public static class FormatString {
        private static final String rawRegex = "(?<varName>[a-zA-Z]+)(?<ms>_ms)?";
        public static final Pattern regexPatternRaw = Pattern.compile(rawRegex);
        private static final String regex = "\\{(?<varName>[a-zA-Z]+)(?<ms>_ms)?}";
        public static final Pattern regexPattern = Pattern.compile(regex);

        String formatString;
        List<Tuple<String, Boolean>> vars = new ArrayList<>();

        @JsonCreator
        public FormatString(String formatString) {
            this.formatString = formatString;
            Matcher matcher = regexPattern.matcher(formatString);
            while (matcher.find()) {
                vars.add(new Tuple<>(
                        matcher.group("varName"),
                        matcher.group("varName") != null)
                );
            }
        }

        public String get(HashMap<String, TickMS> vars) {
            String returnString = formatString;
            for (Tuple<String, Boolean> var : this.vars) {
                if (vars.containsKey(var.getFirst())) {
                    if (var.getSecond()) {
                        returnString = returnString
                                .replaceAll(
                                        "\\{" + var.getFirst() + "_ms}",
                                        vars.get(var.getFirst()).getMSOrEmpty()
                                );
                    }
                    returnString = returnString
                            .replaceAll(
                                    "\\{" + var.getFirst() + "}",
                                    String.valueOf(vars.get(var.getFirst()).tickCount)
                            );
                } else return "";
            }
            return returnString;
        }
    }

    public static class FormatCondition {
        OR condition;
        boolean isDefault = false;
        String checkMS = null;

        @JsonCreator
        public FormatCondition(String formatCondition) {
            if (formatCondition.startsWith("default")) {
                isDefault = true;
                return;
            }
            Matcher matcher = FormatString.regexPatternRaw.matcher(formatCondition);
            if (matcher.matches()) {
                checkMS = matcher.group("varName");
                if (checkMS != null) return;
            }

            try {
                condition = new OR(formatCondition);
            } catch (Exception e) {
                API.LOGGER.debug(e.toString());
            }
        }

        public boolean check(HashMap<String, TickMS> vars) {
            if (isDefault) return true;
            if (checkMS != null)
                return TimingStorage.renderLastTimingMS && vars.containsKey(checkMS) && vars.get(checkMS).ms != null;
            if (condition == null) return false;
            return condition.check(vars);
        }

        public enum Operator {
            EQUALS("==", (a, b) -> a == b),
            NOT_EQUALS("!=", (a, b) -> a != b),
            GREATER_EQUALS(">=", (a, b) -> a >= b),
            GREATER(">", (a, b) -> a > b),
            SMALLER_EQUALS("<=", (a, b) -> a <= b),
            SMALLER("<", (a, b) -> a < b),
            NONE(null, (a, b) -> false);

            public final String operator;
            public final CheckLambda check;

            Operator(String operator, CheckLambda check) {
                this.operator = operator;
                this.check = check;
            }

            @FunctionalInterface
            private interface CheckLambda {
                boolean check(int a, int b);
            }
        }

        private static class OR {
            AND[] parts;

            OR(String orCond) {
                String[] parts = orCond.split("\\|\\|");
                this.parts = new AND[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    this.parts[i] = new AND(parts[i]);
                }
            }

            boolean check(HashMap<String, TickMS> vars) {
                for (AND p : parts) {
                    if (p.check(vars)) return true;
                }
                return false;
            }
        }

        private static class AND {
            COND[] parts;

            AND(String andCond) {
                String[] parts = andCond.split("&&");
                this.parts = new COND[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    this.parts[i] = new COND(parts[i]);
                }
            }

            boolean check(HashMap<String, TickMS> vars) {
                for (COND p : parts) {
                    if (!p.check(vars)) return false;
                }
                return true;
            }
        }

        private static class COND {
            ADD[] parts;
            Operator operator = null;

            COND(String cond) {
                for (Operator op : Operator.values()) {
                    if (op == Operator.NONE) continue;
                    if (cond.contains(op.operator)) {
                        if (operator != null)
                            throw new IllegalArgumentException(String.format("Multiple operators of different types found in '%s' when only one type was expected", cond));
                        operator = op;
                    }
                }

                if (operator == null)
                    throw new IllegalArgumentException(String.format("No operator found found in '%s' when only at least one was expected", cond));

                String[] parts = cond.split(operator.operator);
                this.parts = new ADD[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    this.parts[i] = new ADD(parts[i]);
                }
            }

            private boolean helperCheck(List<Integer> parts) {
                if (parts.size() == 2)
                    return operator.check.check(parts.get(0), parts.get(1));
                return operator.check.check(parts.get(0), parts.get(1)) && helperCheck(parts.subList(1, parts.size()));
            }

            boolean check(HashMap<String, TickMS> vars) {
                List<Integer> parts = new ArrayList<>();

                for (ADD part : this.parts) {
                    Integer v = part.get(vars);
                    if (v == null) return false;
                    parts.add(v);
                }
                return helperCheck(parts);
            }
        }

        private static class ADD {
            String[] parts;

            ADD(String add) {
                parts = add.split("\\+");
            }

            public Integer get(HashMap<String, TickMS> vars) {
                int result = 0;
                for (String part : this.parts) {
                    Integer v;
                    if (vars.containsKey(part)) v = vars.get(part).tickCount;
                    else v = MathUtil.parseInt(part, null);
                    if (v == null) return null;
                    result += v;
                }
                return result;
            }
        }
    }

    public static class Match implements Comparable<Match> {
        private final int numberOfVars;
        private final int endOffset;
        public String displayString;

        private Match(String displayString, int numberOfVars, int endOffset) {
            this.displayString = displayString;
            this.numberOfVars = numberOfVars;
            this.endOffset = endOffset;
        }

        @Override
        public int compareTo(Match o) {
            if (this.endOffset != o.endOffset)
                return Integer.compare(this.endOffset, o.endOffset);
            return Integer.compare(this.numberOfVars, o.numberOfVars);
        }
    }

    public static class TickMS {
        public int tickCount;
        public Integer ms = null;

        public TickMS(int tickCount) {
            this.tickCount = tickCount;
        }

        public String getMSOrEmpty() {
            if (ms == null) return "";
            return String.valueOf(ms);
        }
    }
}
