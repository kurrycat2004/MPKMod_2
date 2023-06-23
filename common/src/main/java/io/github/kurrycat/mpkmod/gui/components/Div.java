package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.interfaces.KeyInputListener;
import io.github.kurrycat.mpkmod.gui.interfaces.MouseInputListener;
import io.github.kurrycat.mpkmod.gui.interfaces.MouseScrollListener;
import io.github.kurrycat.mpkmod.util.ArrayListUtil;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Div extends Component implements MouseInputListener, MouseScrollListener, KeyInputListener {
    public Color backgroundColor = null;
    public Color borderColor = null;
    public Color textColor = null;

    private String text = null;
    private String[] lines = null;

    private boolean hasSetSizeX = false;
    private boolean hasSetSizeY = false;

    public Div() {
    }

    public Div(Vector2D pos, Vector2D size) {
        this.setPos(pos);
        this.setSize(size);
    }

    @Override
    public void updatePosAndSize() {
        super.updatePosAndSize();
        if (text != null) {
            if (!hasSetSizeX) lines = new String[]{text};
            else if (getDisplayedSize().getX() > 2) {
                Vector2D s = updateSplitLines();
                if (!hasSetSizeY) {
                    this.csize.set(s);
                }
            }
        }
    }

    @Override
    public void setSize(Vector2D size) {
        hasSetSizeX = size.getX() != this.size.getX();
        hasSetSizeY = size.getY() != this.size.getY();
        super.setSize(size);
    }

    private Vector2D updateSplitLines() {
        List<String> lines = new ArrayList<>();
        double height = 2;
        double width = 0;
        Vector2D spaceSize = FontRenderer.getStringSize(" ");
        double spaceWidth = spaceSize.getX();
        double fontHeight = spaceSize.getY();

        for (String input : text.split("\n")) {
            StringTokenizer tok = new StringTokenizer(input, " ");
            StringBuilder line = new StringBuilder();

            double lineLen = 0;
            while (tok.hasMoreTokens()) {
                String word = tok.nextToken();

                Vector2D size;
                double maxWidth = getDisplayedSize().getX() - 2;

                while ((size = FontRenderer.getStringSize(word)).getX() > maxWidth) {
                    if (lineLen != 0) {
                        lines.add(line.toString());
                        height += fontHeight;
                        width = Math.max(width, lineLen);
                        line = new StringBuilder();
                        lineLen = 0;
                    }
                    int maxLen = findLargestPossLenInLine(word, maxWidth);
                    lines.add(word.substring(0, maxLen));
                    height += fontHeight;
                    width = Math.max(width, size.getX());
                    word = word.substring(maxLen);
                }

                if (lineLen + (size = FontRenderer.getStringSize(word)).getX() > maxWidth) {
                    lines.add(line.toString());
                    height += fontHeight;
                    width = Math.max(width, lineLen);
                    line = new StringBuilder();
                    lineLen = 0;
                }
                line.append(word).append(" ");

                lineLen += size.getX() + spaceWidth;
            }
            lines.add(line.toString());
            height += fontHeight;
            width = Math.max(width, FontRenderer.getStringSize(line.toString()).getX());
        }

        this.lines = lines.toArray(new String[0]);
        return new Vector2D(width, height);
    }

    private int findLargestPossLenInLine(String word, double lineWidth) {
        int min = 0, max = word.length();
        while (min != max) {
            int mid = (min + max) / 2;
            if (FontRenderer.getStringSize(word.substring(0, mid)).getX() > lineWidth) {
                max = mid;
            } else {
                min = mid;
            }
        }
        return min;
    }

    public Div setMaxWidth(double width) {
        setSize(new Vector2D(width, this.size.getY()));
        return this;
    }

    public Div setText(String text) {
        this.text = text;
        setSize(this.size);
        return this;
    }

    public void addChildBelow(Component child) {
        addChild(child, PERCENT.NONE, Anchor.TOP_LEFT);
        child.setPos(new Vector2D(1, getDisplayedSize().getY()));
        this.setSize(new Vector2D(
                Math.max(child.getDisplayedSize().getX() + 2, this.getDisplayedSize().getX()),
                getDisplayedSize().getY() + child.getDisplayedSize().getY() + 1
        ));
    }

    @Override
    public void render(Vector2D mouse) {
        if (backgroundColor != null)
            Renderer2D.drawRect(getDisplayedPos(), getDisplayedSize(), backgroundColor);
        if (borderColor != null)
            Renderer2D.drawHollowRect(getDisplayedPos(), getDisplayedSize(), 1, borderColor);

        if (lines != null) {
            Vector2D pos = getDisplayedPos().add(3, 1);
            for (String line : lines) {
                FontRenderer.drawString(line, pos, textColor != null ? textColor : Color.WHITE, false);
                pos.addYInPlace(FontRenderer.getStringSize(line).getY());
            }
        }
        components.forEach(c -> c.render(mouse));
    }

    @Override
    public boolean handleKeyInput(int keyCode, int scanCode, int modifiers, boolean isCharTyped) {
        return ArrayListUtil.orMapAll(
                ArrayListUtil.getAllOfType(KeyInputListener.class, components),
                e -> e.handleKeyInput(keyCode, scanCode, modifiers, isCharTyped)
        );
    }

    @Override
    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        return ArrayListUtil.orMapAll(
                ArrayListUtil.getAllOfType(MouseInputListener.class, components),
                e -> e.handleMouseInput(state, mousePos, button)
        );
    }

    @Override
    public boolean handleMouseScroll(Vector2D mousePos, int delta) {
        return ArrayListUtil.orMapAll(
                ArrayListUtil.getAllOfType(MouseScrollListener.class, components),
                e -> e.handleMouseScroll(mousePos, delta)
        );
    }
}
