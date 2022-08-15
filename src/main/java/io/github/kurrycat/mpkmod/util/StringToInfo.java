package io.github.kurrycat.mpkmod.util;

import io.github.kurrycat.mpkmod.compatability.API;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringToInfo {
    public static final String FORMATTING_REGEX = "\\{(.*?)(,(\\d+))?\\}";
    public static final Pattern FORMATTING_PATTERN = Pattern.compile(FORMATTING_REGEX);

    public static String replaceVarsInString(String input) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = FORMATTING_PATTERN.matcher(input);

        int start = 0;

        while (matcher.find()) {
            sb.append(input, start, matcher.start());
            start = matcher.end();

            String fullMatch = matcher.group(0);
            String varName = matcher.group(1);
            String decimalsString = matcher.group(3);

            String value = getColorCodeOrValueFromString(varName);

            if (value == null)
                sb.append(fullMatch);
            else {
                Double valueAsDouble = MathUtil.parseDouble(value, null);
                if (valueAsDouble == null)
                    sb.append(value);
                else
                    sb.append(
                            MathUtil.formatDecimals(
                                    valueAsDouble,
                                    MathUtil.parseInt(decimalsString, 3)
                            )
                    );
            }

        }
        sb.append(input.substring(start));
        return sb.toString();
    }

    public static String getColorCodeOrValueFromString(String input) {
        String returnValue = getColorCodeFromString(input);
        if(returnValue == null) returnValue = getValueFromString(input);
        return returnValue;
    }

    public static String getColorCodeFromString(String input) {
        Colors c = Colors.fromName(input);
        return c == null ? null : c.getCode();
    }

    public static String getValueFromString(String input) {
        String[] splitVars = input.toLowerCase().split("\\.");
        String objectIdentifier = splitVars[0];
        String[] subVars = Arrays.copyOfRange(splitVars, 1, splitVars.length);

        Object currObj = null;
        if (objectIdentifier.equals("player")) currObj = API.getLastPlayer();

        if (currObj == null) return null;

        for (String subVar : subVars) {
            currObj = getValueOfObject(subVar, currObj);
            if (currObj == null)
                return null;
        }
        return currObj.toString();
    }

    public static Object getValueOfObject(String name, Object obj) {
        try {
            Class<?> objClass = obj.getClass();
            Field f = objClass.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }
}
