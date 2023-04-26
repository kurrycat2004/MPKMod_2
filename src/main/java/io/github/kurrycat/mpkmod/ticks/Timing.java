package io.github.kurrycat.mpkmod.ticks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.util.MathUtil;

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
        for (int i = 0; i < inputList.size() - 1; i++) {
            if (inputList.get(i).isStopTick() && !inputList.get(i + 1).isStopTick()) {
                Match match = startsWithMatch(inputList.subList(i + 1, inputList.size()));
                if (match != null)
                    return match;
            }
        }
        return null;
    }

    private Match startsWithMatch(List<TimingInput> inputList) {
        HashMap<String, Integer> vars = new HashMap<>();
        for (TimingEntry entry : timingEntries) {
            Integer matchCount = entry.matches(inputList, vars);
            if (matchCount == null)
                return null;

            //System.out.printf("count: %d, entry: %s, inputList: %s\n", matchCount, entry.timingEntry, inputList);
            inputList = inputList.subList(matchCount, inputList.size());
        }

        //System.out.printf("Match: %s\nVars: %s\n\n", inputList, vars);
        return new Match(getFormatString(vars), vars.size(), inputList.size());
    }

    private String getFormatString(HashMap<String, Integer> vars) {
        StringBuilder sb = new StringBuilder();
        format.forEach((fc, fs) -> {
            if (fc.check(vars)) sb.append(fs.get(vars));
        });
        return sb.toString();
    }


    public static class FormatString {
        private static final String regex = "\\{(?<varName>[a-zA-Z]+)}";
        private static final Pattern regexPattern = Pattern.compile(regex);

        String formatString;
        List<String> vars = new ArrayList<>();

        @JsonCreator
        public FormatString(String formatString) {
            this.formatString = formatString;
            Matcher matcher = regexPattern.matcher(formatString);
            while (matcher.find()) {
                vars.add(matcher.group("varName"));
            }
        }

        public String get(HashMap<String, Integer> vars) {
            String returnString = formatString;
            for (String var : this.vars) {
                if (vars.containsKey(var))
                    returnString = returnString.replaceAll("\\{" + var + "}", String.valueOf(vars.get(var)));
                else
                    return "";
            }
            return returnString;
        }
    }

    public static class FormatCondition {
        OR condition;
        boolean isDefault = false;

        @JsonCreator
        public FormatCondition(String formatCondition) {
            if (formatCondition.equals("default")) {
                isDefault = true;
            } else {
                try {
                    condition = new OR(formatCondition);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public boolean check(HashMap<String, Integer> vars) {
            if (isDefault) return true;
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

            boolean check(HashMap<String, Integer> vars) {
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

            boolean check(HashMap<String, Integer> vars) {
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

            boolean check(HashMap<String, Integer> vars) {
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

            public Integer get(HashMap<String, Integer> vars) {
                int result = 0;
                for (String part : this.parts) {
                    Integer v = vars.getOrDefault(part, MathUtil.parseInt(part, null));
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
}
