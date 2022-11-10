package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.compatability.MCClasses.WorldInteraction;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Minecraft;

import java.awt.*;

public class BarrierDisplayComponent extends ResizableComponent {
    @JsonProperty("fullscreen")
    boolean fullscreen = false;

    @JsonProperty("lineThickness")
    int lineThickness = 2;


    @JsonCreator
    public BarrierDisplayComponent(@JsonProperty("pos") Vector2D pos, @JsonProperty("size") Vector2D size) {
        super(pos, size);
    }

    @Override
    public void render(Vector2D mouse) {
        Color color = null;
        if (API.mainGUI.isOpen()) {
            color = new Color(255, 255, 255, 255);
        }
        if (WorldInteraction.getLookingAtBlock().equals("minecraft:barrier")) {
            color = new Color(227, 0, 0);
        }
        if (selected) {
            color = new Color(255, 170, 0, 100);
        }
        if (color != null) {
            if (fullscreen && !API.mainGUI.isOpen()) {
                Vector2D windowSize = Renderer2D.getScaledSize();
                Renderer2D.drawRect(Vector2D.ZERO, new Vector2D(windowSize.getX(), lineThickness), color);
                Renderer2D.drawRect(Vector2D.ZERO, new Vector2D(lineThickness, windowSize.getY()), color);
                Renderer2D.drawRect(Vector2D.ZERO.add(0, windowSize.getY() - lineThickness), new Vector2D(windowSize.getX(), lineThickness), color);
                Renderer2D.drawRect(Vector2D.ZERO.add(windowSize.getX() - lineThickness, 0), new Vector2D(lineThickness, windowSize.getY()), color);
            } else {
                FontRenderer.drawCenteredString(
                        "!",
                        getDisplayPos().add(getSize().div(2)).add(new Vector2D(0, 1)),
                        color,
                        false
                );
                Renderer2D.drawRect(getDisplayPos().add(1, 0), new Vector2D(getSize().getX() - 2, lineThickness), color);
                Renderer2D.drawRect(getDisplayPos().add(0, 1), new Vector2D(lineThickness, getSize().getY() - 2), color);
                Renderer2D.drawRect(getDisplayPos().add(1, getSize().getY() - lineThickness), new Vector2D(getSize().getX() - 2, lineThickness), color);
                Renderer2D.drawRect(getDisplayPos().add(getSize().getX() - lineThickness, 1), new Vector2D(lineThickness, getSize().getY() - 2), color);
            }
            if (highlighted) Renderer2D.drawDottedRect(getDisplayPos(), getSize(), 1, 1, 1, Color.BLACK);
        }
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
                new NumberSlider(1, Math.floor(Math.min(getSize().getX(), getSize().getY()) / 2), 1, lineThickness, Vector2D.OFFSCREEN, new Vector2D(56, 11), sliderValue -> {
                    lineThickness = (int) sliderValue;
                })
        );
        menu.addComponent(
                new Button("Delete", Vector2D.OFFSCREEN, new Vector2D(56, 11), mouseButton -> {
                    if (Mouse.Button.LEFT.equals(mouseButton)) {
                        menu.parent.removeComponent(this);
                        menu.parent.closePane(menu);
                    }
                })
        );
        return menu;
    }
}
