package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Minecraft;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Colors;
import io.github.kurrycat.mpkmod.util.InfoString;
import io.github.kurrycat.mpkmod.util.Vector2D;

public class InfoLabel extends Label {
    public final InfoString infoString;

    /**
     * @param text the text to be displayed<br><br>
     *             <code>"{COLOR}"</code> with COLOR being any <code>{@link Colors}.name</code> will be replaced by that color's code<br><br>
     *             <code>"{VARNAME}"</code> with VARNAME being any field that exists or has an according getter in {@link Minecraft} or
     *             {@link Player} will be replaced by the field's value<br>
     * @param pos  top left position of the text
     */
    @JsonCreator
    public InfoLabel(@JsonProperty("text") String text, @JsonProperty("pos") Vector2D pos) {
        super(text, pos);
        this.infoString = new InfoString(text);
    }

    public String getFormattedText() {
        return infoString.getFormattedText(this.text);
    }

    public void render(Vector2D mouse) {
        drawDefaultSelectedBackground();
        FontRenderer.drawString(getFormattedText(), getDisplayPos(), color, true);
    }

    @JsonIgnore
    public Vector2D getSize() {
        return FontRenderer.getStringSize(getFormattedText());
    }

    @Override
    public PopupMenu getPopupMenu() {
        PopupMenu menu = new PopupMenu();
        menu.addComponent(
                new InputField(text, Vector2D.OFFSCREEN, Renderer2D.getScaledSize().getX() / 4)
                        .setOnContentChange(content -> {
                            this.text = content.getContent();
                            infoString.input = this.text;
                            infoString.updateProviders();
                        })
        );
        return menu;
    }
}
