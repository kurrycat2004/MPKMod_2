package io.github.kurrycat.mpkmod.util;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Minecraft;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class InfoString {
    @SuppressWarnings("all")
    public static final String FORMATTING_REGEX = "\\{(.*?)(,(\\d+)(!?))?\\}";
    public static final Pattern FORMATTING_PATTERN = Pattern.compile(FORMATTING_REGEX);
    public static final HashMap<String, Object> START_OBJECTS_MAP;

    static {
        START_OBJECTS_MAP = new HashMap<>();
        START_OBJECTS_MAP.put("player", Player.displayInstance);
        START_OBJECTS_MAP.put("minecraft", Minecraft.class);
        START_OBJECTS_MAP.put("mc", Minecraft.class);
    }

    public String input;
    private ArrayList<StringProvider> providers = new ArrayList<>();

    public InfoString(String input) {
        this.input = input;
        updateProviders();
    }

    public static ArrayList<StringProvider> inputStringToProviderList(String input) {
        ArrayList<StringProvider> output = new ArrayList<>();

        Matcher matcher = FORMATTING_PATTERN.matcher(input);

        int start = 0;

        while (matcher.find()) {
            int finalStart = start;
            int finalEnd = matcher.start();
            output.add((StringProvider) () -> input.substring(finalStart, finalEnd));

            start = matcher.end();

            String fullMatch = matcher.group(0);
            String varName = matcher.group(1);
            String decimalsString = matcher.group(3);
            String keepZeros = matcher.group(4);
            if (keepZeros == null) keepZeros = "";

            StringProvider colorProvider = getColorCodeFromString(varName);
            if (colorProvider != null) {
                output.add((StringProvider) colorProvider);
                continue;
            }

            StringProvider valueProvider = getValueFromString(fullMatch, varName, MathUtil.parseInt(decimalsString, 3), !keepZeros.isEmpty());
            output.add((StringProvider) valueProvider);
        }
        int finalStart1 = start;
        output.add((StringProvider) () -> input.substring(finalStart1));

        return output;
    }

    public static StringProvider getColorCodeFromString(String input) {
        Colors c = Colors.fromName(input);
        return c == null ? null : c::getCode;
    }

    public static StringProvider getValueFromString(String fullMatch, String input, int decimals, boolean keepZeros) {
        String[] splitVars = input.split("\\.");
        if(splitVars.length < 1) {
            return () -> null;
        }
        String objectIdentifier = splitVars[0];
        String[] subVars = Arrays.copyOfRange(splitVars, 1, splitVars.length);

        ObjectProvider currObj = START_OBJECTS_MAP.containsKey(objectIdentifier) ? (() -> START_OBJECTS_MAP.get(objectIdentifier)) : null;

        if (currObj == null) return () -> fullMatch;

        return () -> {
            ObjectProvider finalCurrObj = getValueOfObject(subVars, currObj);
            if (finalCurrObj.getObj() == null) return fullMatch;
            if (finalCurrObj.getObj() instanceof Double)
                return MathUtil.formatDecimals((Double) finalCurrObj.getObj(), decimals, keepZeros);
            if (finalCurrObj.getObj() instanceof Float)
                return MathUtil.formatDecimals((Float) finalCurrObj.getObj(), decimals, keepZeros);
            return finalCurrObj.getObj().toString();
        };
    }

    public static ObjectProvider getValueOfObject(String[] splitVars, ObjectProvider objectProvider) {
        if(splitVars.length < 1) {
            return () -> null;
        }
        String objectIdentifier = splitVars[0];
        String[] subVars = Arrays.copyOfRange(splitVars, 1, splitVars.length);
        return () -> {
            if(objectProvider == null) return null;
            Object obj = objectProvider.getObj();
            if (obj == null) return null;
            Class<?> objClass = obj instanceof Class ? (Class<?>) obj : obj.getClass();
            try {
                Field f = objClass.getDeclaredField(objectIdentifier);
                if (!f.isAccessible()) f.setAccessible(true);
                Object returnObj = f.get(obj);
                if(splitVars.length == 1) return returnObj;
                else return getValueOfObject(subVars, () -> returnObj).getObj();
            } catch (NoSuchFieldException e) {
                try {
                    Method m = objClass.getDeclaredMethod("get" + StringUtil.capitalize(objectIdentifier));
                    Object returnObj = m.invoke(obj);
                    if(splitVars.length == 1) return returnObj;
                    else return getValueOfObject(subVars, () -> returnObj).getObj();
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
                    return null;
                }
            } catch (IllegalAccessException e) {
                return null;
            }
        };
    }

    public String getFormattedText(String input) {
        if (!input.equals(this.input)) {
            this.input = input;
            updateProviders();
        }
        return getFormattedTextFromProviders();
    }

    private String getFormattedTextFromProviders() {
        StringBuilder sb = new StringBuilder();
        for (StringProvider provider : providers) {
            sb.append(provider.getString());
        }
        return sb.toString();
    }

    public void updateProviders() {
        providers.clear();
        providers = inputStringToProviderList(this.input);
    }

    @FunctionalInterface
    public interface StringProvider {
        String getString();
    }

    @FunctionalInterface
    public interface ObjectProvider {
        Object getObj();
    }
}
