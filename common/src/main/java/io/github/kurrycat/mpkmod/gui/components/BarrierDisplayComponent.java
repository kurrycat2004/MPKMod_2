package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.WorldInteraction;
import io.github.kurrycat.mpkmod.gui.Theme;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class BarrierDisplayComponent extends ResizableComponent {
    @JsonProperty("fullscreen")
    boolean fullscreen = false;

    @JsonProperty("lineThickness")
    int lineThickness = 2;

    public Color selectedColor = new Color(255, 170, 0, 100);

    @Override
    public void render(Vector2D mouse) {
        String lookingAtBlock = WorldInteraction.getLookingAtBlock();
        boolean lookingAtBarrier = lookingAtBlock != null && lookingAtBlock.contains("minecraft:barrier");

        Color color = lookingAtBarrier ? Theme.warnText : Theme.defaultText;
        if (selected) color = selectedColor;

        String currentGui = Minecraft.getCurrentGuiScreen();

        if (lookingAtBarrier && fullscreen && currentGui == null) {
            Vector2D windowSize = Renderer2D.getScaledSize();
            Renderer2D.drawRect(Vector2D.ZERO, new Vector2D(windowSize.getX(), lineThickness), color);
            Renderer2D.drawRect(Vector2D.ZERO, new Vector2D(lineThickness, windowSize.getY()), color);
            Renderer2D.drawRect(Vector2D.ZERO.add(0, windowSize.getY() - lineThickness), new Vector2D(windowSize.getX(), lineThickness), color);
            Renderer2D.drawRect(Vector2D.ZERO.add(windowSize.getX() - lineThickness, 0), new Vector2D(lineThickness, windowSize.getY()), color);
        }
        if ((lookingAtBarrier && !fullscreen) || (currentGui != null && currentGui.equals("main_gui"))) {
            FontRenderer.drawCenteredString(
                    "!",
                    getDisplayedPos().add(getDisplayedSize().div(2)).add(new Vector2D(0, 1)),
                    color,
                    false
            );
            Renderer2D.drawRect(getDisplayedPos().add(1, 0), new Vector2D(getDisplayedSize().getX() - 2, lineThickness), color);
            Renderer2D.drawRect(getDisplayedPos().add(0, 1), new Vector2D(lineThickness, getDisplayedSize().getY() - 2), color);
            Renderer2D.drawRect(getDisplayedPos().add(1, getDisplayedSize().getY() - lineThickness), new Vector2D(getDisplayedSize().getX() - 2, lineThickness), color);
            Renderer2D.drawRect(getDisplayedPos().add(getDisplayedSize().getX() - lineThickness, 1), new Vector2D(lineThickness, getDisplayedSize().getY() - 2), color);
        }
        if (highlighted) Renderer2D.drawDottedRect(getDisplayedPos(), getDisplayedSize(), 1, 1, 1, Color.BLACK);

        renderHoverEdges(mouse);
    }

    @Override
    public PopupMenu getPopupMenu() {
        PopupMenu menu = new PopupMenu();
        menu.addComponent(
                new TextCheckButton(Vector2D.OFFSCREEN, "Fullscreen", fullscreen, checked -> {
                    fullscreen = checked;
                })
        );
        menu.addComponent(
                new NumberSlider(1, Math.floor(Math.min(getDisplayedSize().getX() / 2, getDisplayedSize().getY()) / 2), 1, lineThickness, Vector2D.OFFSCREEN, new Vector2D(56, 11), sliderValue -> {
                    lineThickness = (int) sliderValue;
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
}
