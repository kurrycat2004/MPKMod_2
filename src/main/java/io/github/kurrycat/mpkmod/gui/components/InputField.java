package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.Arrays;

//TODO: Multiline InputField
public class InputField extends Component implements KeyInputListener, MouseInputListener {
    public static String FILTER_ALL = "[0-9a-zA-Z!?,.;\\-{} ]";
    public static String FILTER_NUMBERS = "[0-9.,\\-!]";
    public static String FILTER_HEX = "[#0-9a-fA-F]";

    public boolean numbersOnly;
    public String content;
    public ContentProvider onContentChange = null;
    public String name = null;
    public Color normalColor = new Color(31, 31, 31, 150);
    public Color cursorColor = new Color(255, 255, 255, 150);
    public Color highlightColor = new Color(255, 255, 255, 175);
    private boolean isFocused = false;
    private int cursorPos = 0;
    private int highlightStart = 0;
    private int highlightEnd = 0;
    private String customFilter = null;

    public InputField(Vector2D pos, double width) {
        this("", pos, width, false);
    }

    public InputField(String content, Vector2D pos, double width) {
        this(content, pos, width, false);
    }

    public InputField(String content, Vector2D pos, double width, boolean numbersOnly) {
        super(pos);
        this.setSize(new Vector2D(width, 11));
        this.content = content;
        this.numbersOnly = numbersOnly;
    }

    private String getFilter() {
        if (customFilter != null) {
            return customFilter;
        }
        return numbersOnly ? FILTER_NUMBERS : FILTER_ALL;
    }

    public InputField setFilter(String filter) {
        this.customFilter = filter;
        return this;
    }

    public InputField setName(String name) {
        this.name = name;
        return this;
    }

    public InputField setOnContentChange(ContentProvider onContentChange) {
        this.onContentChange = onContentChange;
        return this;
    }

    @Override
    public void render(Vector2D mouse) {
        Vector2D nameSize = name == null ? Vector2D.ZERO : FontRenderer.getStringSize(name);
        Vector2D rectPos = getDisplayPos().add(nameSize.getX(), 0);
        Vector2D rectSize = getSize().sub(nameSize.getX(), 0);

        if (name != null) {
            FontRenderer.drawCenteredString(
                    name,
                    getDisplayPos().add(nameSize.getX() / 2D, getSize().getY() / 2D + 1),
                    Color.WHITE,
                    false
            );
        }

        Renderer2D.drawRectWithEdge(rectPos.round(), rectSize.round(), 1, normalColor, normalColor);

        FontRenderer.drawString(
                content.substring(0, highlightStart),
                rectPos.add(2, 2),
                Color.WHITE, false
        );
        if (highlightStart != highlightEnd)
            Renderer2D.drawRect(
                    rectPos.add(2 + FontRenderer.getStringSize(content.substring(0, highlightStart)).getX(), 2),
                    new Vector2D(FontRenderer.getStringSize(content.substring(highlightStart, highlightEnd)).getX(), rectSize.getY() - 4),
                    highlightColor
            );
        FontRenderer.drawString(
                content.substring(highlightStart, highlightEnd),
                rectPos.add(2 + FontRenderer.getStringSize(content.substring(0, highlightStart)).getX(), 2),
                Color.BLACK, false
        );
        FontRenderer.drawString(
                content.substring(highlightEnd),
                rectPos.add(2 + FontRenderer.getStringSize(content.substring(0, highlightEnd)).getX(), 2),
                Color.WHITE, false
        );

        if (isFocused && highlightStart == highlightEnd)
            Renderer2D.drawRect(
                    new Vector2D(rectPos.getX() + getCursorX(), rectPos.getY() + 1),
                    new Vector2D(1, rectSize.getY() - 2),
                    cursorColor
            );
    }

    @Override
    public boolean handleKeyInput(char keyCode, String key, boolean pressed) {
        if (!isFocused) return false;

        if (pressed) {
            String character = null;

            if (Character.toString(keyCode).matches(getFilter())) {
                character = Character.toString(keyCode);
                //if(character.equals(",")) character = ".";
            }

            if (key != null) {
                switch (key) {
                /*case "LSHIFT":
                    System.out.println(cursorPos + ", " + highlightStart + ", " + highlightEnd);
                    break;*/
                    case "BACK":
                        deleteSelection();
                        if (highlightStart == highlightEnd)
                            cursorPos--;
                        else cursorPos = highlightStart;
                        break;
                    case "DELETE":
                        if (highlightStart == highlightEnd)
                            cursorPos++;
                        deleteSelection();
                        if (highlightStart == highlightEnd)
                            cursorPos--;
                        break;
                    case "LEFT":
                        if (highlightStart == highlightEnd)
                            cursorPos--;
                        else cursorPos = highlightStart;
                        break;
                    case "RIGHT":
                        if (highlightStart == highlightEnd)
                            cursorPos++;
                        else cursorPos = highlightEnd;
                        break;
                    default:
                        if (character != null) {
                            replaceSelectionWithChar(character);
                            cursorPos = highlightStart + 1;
                        } /*else {
                        System.out.println(key);
                    }*/
                        break;
                }
                if (Arrays.asList("BACK", "DELETE", "LEFT", "RIGHT").contains(key) || character != null) {
                    cursorPos = MathUtil.constrain(cursorPos, 0, content.length());
                    highlightStart = cursorPos;
                    highlightEnd = cursorPos;
                }
            }
        }

        return true;
    }

    private void deleteSelection() {
        if (highlightStart == highlightEnd)
            updateContent(content.substring(0, Math.max(cursorPos - 1, 0)) + (cursorPos >= content.length() ? "" : content.substring(cursorPos)));
        else
            updateContent(content.substring(0, highlightStart) + content.substring(highlightEnd));
    }

    private void replaceSelectionWithChar(String c) {
        updateContent(content.substring(0, highlightStart) + c + content.substring(highlightEnd));
    }

    private void updateContent(String content) {
        this.content = content;
        if (onContentChange != null)
            onContentChange.apply(new Content(content));
    }

    private double getCursorX() {
        return 2 + FontRenderer.getStringSize(content.substring(0, cursorPos)).getX();
    }

    private int getCursorPosFromMousePos(Vector2D mouse) {
        Vector2D nameSize = name == null ? Vector2D.ZERO : FontRenderer.getStringSize(name);
        Vector2D rectPos = getDisplayPos().add(nameSize.getX(), 0);

        double x = mouse.getX() - rectPos.getX() - 2;
        if (x < 0)
            return 0;
        else if (x > FontRenderer.getStringSize(content).getX())
            return content.length();

        for (int i = 1; i <= content.length(); i++) {
            int charWidth = FontRenderer.getStringSize(content.substring(i - 1, i)).getXI();
            if (x < charWidth / 2D)
                return i - 1;
            else if (x < charWidth)
                return i;

            x -= charWidth;
        }
        return content.length();
    }

    public void setWidth(double width) {
        this.size.setX(width);
    }

    @Override
    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        if (button == Mouse.Button.LEFT) {
            switch (state) {
                case DOWN:
                    if (contains(mousePos)) {
                        isFocused = true;
                        cursorPos = getCursorPosFromMousePos(mousePos);
                        highlightStart = cursorPos;
                        highlightEnd = cursorPos;
                        return true;
                    } else {
                        isFocused = false;
                        highlightStart = 0;
                        highlightEnd = 0;
                    }
                case DRAG:
                case UP:
                    if (isFocused) {
                        int c = getCursorPosFromMousePos(mousePos);
                        if (c < cursorPos) {
                            highlightStart = c;
                            highlightEnd = cursorPos;
                        } else {
                            highlightStart = cursorPos;
                            highlightEnd = c;
                        }

                        return contains(mousePos);
                    }
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface ContentProvider {
        void apply(Content content);
    }

    public static class Content {
        public String content;

        public Content(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public Double getNumber() {
            try {
                return Double.parseDouble(content);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}
