package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.ColorUtil;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class InputHistory extends ResizableComponent {
    static ArrayList<InputHistoryMessage> inputHistory = new ArrayList<>();
    @JsonProperty
    public Color backgroundColor = new Color(100, 100, 100, 40);
    @JsonProperty
    public Color messageColor = new Color(255, 255, 255, 255);
    @JsonProperty
    public Color edgeColor = new Color(100, 100, 100, 50);
    public Color selectedColor = new Color(255, 170, 0, 100);
    @JsonProperty
    public boolean inverted = false;
    @JsonProperty
    int maxTickAge = 80;
    @JsonProperty
    public boolean transparentBackground = false;
    public static int preferredWidth = 86;
    public int maxStoredMessages = 30;
    private static final HashMap<Vector2D, Character> movementCharacters = new HashMap<>();

    static {
        movementCharacters.put(new Vector2D(0, 0), ' ');
        movementCharacters.put(new Vector2D(-1, 0), '⬇');
        movementCharacters.put(new Vector2D(1, 0), '⬆');
        movementCharacters.put(new Vector2D(0, -1), '⬅');
        movementCharacters.put(new Vector2D(0, 1), '➡');
        movementCharacters.put(new Vector2D(1, 1), '⬈');
        movementCharacters.put(new Vector2D(1, -1), '⬉');
        movementCharacters.put(new Vector2D(-1, 1), '⬊');
        movementCharacters.put(new Vector2D(-1, -1), '⬋');
    }

    @JsonCreator
    public InputHistory() {
        this.setMinSize(new Vector2D(preferredWidth, 60));
        this.setXResizeLocked(true);
    }

    @Override
    public void render(Vector2D mouse) {
        if (!transparentBackground)
            Renderer2D.drawRectWithEdge(getDisplayedPos(), getDisplayedSize(), 1, selected ? selectedColor : backgroundColor, edgeColor);
        if (highlighted) Renderer2D.drawDottedRect(getDisplayedPos(), getDisplayedSize(), 1, 1, 1, Color.BLACK);

        double lineHeight = 10;
        int maxDisplayedMessages = (int) (((getDisplayedSize().getY() - 2) / lineHeight) - 0.5f);
        for (int i = 0; i < inputHistory.size() && i < maxDisplayedMessages; i++) {
            double yOffset = inverted ? getDisplayedSize().getYI() - (1 + i) * lineHeight : (1 + i) * lineHeight;
            inputHistory.get(i).render(new Vector2D(getDisplayedPos().getXI(), getDisplayedPos().getYI() + yOffset));
        }
        renderHoverEdges(mouse);
    }

    public void onTick() {
        Player.KeyInput keyInput = Player.getLatest() == null ? null : Player.getLatest().keyInput;
        if (keyInput == null) return;
        StringBuilder keysPressed = new StringBuilder();
        keysPressed.append(movementCharacters.getOrDefault(keyInput.getMovementVector(), ' '));
        keysPressed.append(' ');
        keysPressed.append(keyInput.jump ? 'J' : ' ');
        keysPressed.append(' ');
        keysPressed.append(keyInput.sneak ? "Sn" : "  ");
        keysPressed.append(' ');
        keysPressed.append(keyInput.sprint ? "Sp" : "  ");

        if (!inputHistory.isEmpty() && keysPressed.toString().equals(inputHistory.get(0).keysPressed)) {
            inputHistory.get(0).stillPressed();
        } else {
            inputHistory.add(0, new InputHistoryMessage(keysPressed.toString()));
        }
        if (inputHistory.size() > maxStoredMessages) inputHistory.subList(maxStoredMessages, inputHistory.size()).clear();

        for (InputHistoryMessage message : inputHistory) message.tick();
    }

    @Override
    public PopupMenu getPopupMenu() {
        PopupMenu menu = new PopupMenu();

        menu.addComponent(
                new TextCheckButton(Vector2D.OFFSCREEN, "Inverted", inverted, checked -> {
                    inverted = checked;
                })
        );
        menu.addComponent(new TextCheckButton(Vector2D.OFFSCREEN, "Background", !transparentBackground, checked -> {
            transparentBackground = !checked;
        }));
        menu.addComponent(
                new NumberSlider(20, 300, 1, maxTickAge - 20, Vector2D.OFFSCREEN, new Vector2D(56, 11), sliderValue -> {
                    maxTickAge = (int) sliderValue + 20;
                })
        );
        menu.addComponent(
                new Button("Delete", mouseButton -> {
                    if (Mouse.Button.LEFT.equals(mouseButton)) {
                        menu.paneHolder.removeComponent(this);
                        menu.close();
                    }
                })
        );
        return menu;
    }

    public class InputHistoryMessage {
        int tickAge = 0;
        int ticksPressed = 1;
        final String keysPressed;
        private int fadeOutAlpha = 255;

        public InputHistoryMessage(String keysPressed) {
            this.keysPressed = keysPressed;
        }

        public void render(Vector2D pos) {
            Color finalMessageColor = ColorUtil.withAlpha(messageColor, fadeOutAlpha);

            FontRenderer.drawLeftCenteredMonospaceString(keysPressed, pos.add(3, 0), finalMessageColor, true);
            FontRenderer.drawRightCenteredString(String.valueOf(ticksPressed), pos.add(preferredWidth - 3, 0), finalMessageColor, true);
        }

        public void stillPressed() {
            this.tickAge = 0;
            this.ticksPressed++;
        }

        public void tick() {
            this.tickAge++;
            int fadeOutTime = 20;
            this.fadeOutAlpha = this.tickAge < maxTickAge - fadeOutTime ? 255 : MathUtil.map(maxTickAge - Math.min(tickAge, maxTickAge), fadeOutTime, 0, 255, 0);
        }
    }
}
