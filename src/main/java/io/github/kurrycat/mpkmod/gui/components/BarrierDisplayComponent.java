package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.compatability.MCClasses.WorldInteraction;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

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
        if (API.mainGUI.isCached()) {
            color = new Color(255, 255, 255, 255);
        }
        String lookingAtBlock = WorldInteraction.getLookingAtBlock();
        if (lookingAtBlock != null && lookingAtBlock.equals("minecraft:barrier")) {
            color = new Color(227, 0, 0);
        }
        if (selected) {
            color = new Color(255, 170, 0, 100);
        }
        if (color != null) {
            if (fullscreen && !API.mainGUI.isCached()) {
                Vector2D windowSize = Renderer2D.getScaledSize();
                Renderer2D.drawRect(Vector2D.ZERO, new Vector2D(windowSize.getX(), lineThickness), color);
                Renderer2D.drawRect(Vector2D.ZERO, new Vector2D(lineThickness, windowSize.getY()), color);
                Renderer2D.drawRect(Vector2D.ZERO.add(0, windowSize.getY() - lineThickness), new Vector2D(windowSize.getX(), lineThickness), color);
                Renderer2D.drawRect(Vector2D.ZERO.add(windowSize.getX() - lineThickness, 0), new Vector2D(lineThickness, windowSize.getY()), color);
            } else {
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
                new NumberSlider(1, Math.floor(Math.min(getDisplayedSize().getX() / 2, getDisplayedSize().getY()) / 2), 1, lineThickness, Vector2D.OFFSCREEN, new Vector2D(56, 11), sliderValue -> {
                    lineThickness = (int) sliderValue;
                })
        );
        menu.addComponent(
                new Button("Delete", Vector2D.OFFSCREEN, new Vector2D(56, 11), mouseButton -> {
                    if (Mouse.Button.LEFT.equals(mouseButton)) {
                        menu.paneHolder.removeComponent(this);
                        menu.paneHolder.closePane(menu);
                    }
                })
        );
        return menu;
    }
}
