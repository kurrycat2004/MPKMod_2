package io.github.kurrycat.mpkmod.util;

import java.awt.*;

public enum Colors {
	BLACK("black", "0", new Color(0, 0, 0)),
	DARK_BLUE("dblue", "1", new Color(0, 0, 170)),
	DARK_GREEN("dgreen", "2", new Color(0, 170, 0)),
	DARK_AQUA("daqua", "3", new Color(0, 170, 170)),
	DARK_RED("dred", "4", new Color(170, 0, 0)),
	DARK_PURPLE("dpurple", "5", new Color(170, 0, 170)),
	GOLD("gold", "6", new Color(255, 170, 0)),
	GRAY("gray", "7", new Color(170, 170, 170)),
	DARK_GRAY("dgray", "8", new Color(85, 85, 85)),
	BLUE("blue", "9", new Color(85, 85, 255)),
	GREEN("green", "a", new Color(85, 255, 85)),
	AQUA("aqua", "b", new Color(85, 255, 255)),
	RED("red", "c", new Color(255, 85, 85)),
	LIGHT_PURPLE("lpurple", "d", new Color(255, 85, 255)),
	YELLOW("yellow", "e", new Color(255, 255, 85)),
	WHITE("white", "f", new Color(255, 255, 255)),
	RESET("reset", "r", null),
	OBFUSCATED("obfuscated", "k", null),
	BOLD("bold", "l", null),
	STRIKETHROUGH("strikethrough", "m", null),
	UNDERLINE("underline", "n", null),
	ITALIC("italic", "o", null);

	public static final String PREFIX = "\u00a7";

	private final String name;
	private final String code;
	private final Color color;

	Colors(String name, String code, Color color) {
		this.name = name;
		this.code = PREFIX + code;
		this.color = color;
	}

	public static Colors fromCode(String code) {
		for (Colors c : Colors.values())
			if (c.code.equals(code)) return c;
		return null;
	}

	public static Colors fromCode(String code, Colors defaultValue) {
		Colors c = fromCode(code);
		if (c == null) c = defaultValue;
		return c;
	}

	public static Colors fromName(String name) {
		for (Colors c : Colors.values())
			if (c.name.equals(name)) return c;
		return null;
	}

	public static Colors fromName(String name, Colors defaultValue) {
		Colors c = fromName(name);
		if (c == null) c = defaultValue;
		return c;
	}

	public static String[] keys() {
		Colors[] values = values();
		String[] keys = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			Colors n = fromCode(values[i].code);
			if (n == null) continue;
			keys[i] = n.name;
		}
		return keys;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public Color getColor() {
		return color;
	}

	@Override
	public String toString() {
		return this.code;
	}
}
