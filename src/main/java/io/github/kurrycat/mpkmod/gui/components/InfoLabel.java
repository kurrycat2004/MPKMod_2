package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.save.deserialize.InfoLabelDeserializer;
import io.github.kurrycat.mpkmod.save.serialize.InfoLabelSerializer;
import io.github.kurrycat.mpkmod.util.StringToInfo;
import io.github.kurrycat.mpkmod.util.Vector2D;

@JsonSerialize(using = InfoLabelSerializer.class)
@JsonDeserialize(using = InfoLabelDeserializer.class)
public class InfoLabel extends Label {
    public InfoLabel(String text, Vector2D pos) {
        super(text, pos);
    }

    public String getFormattedText() {
        return StringToInfo.replaceVarsInString(text);
    }

    public void render(Vector2D mouse) {
        drawDefaultSelectedBackground();
        FontRenderer.drawString(getFormattedText(), getDisplayPos(), color, true);
    }

    public Vector2D getSize() {
        return FontRenderer.getStringSize(getFormattedText());
    }
}
