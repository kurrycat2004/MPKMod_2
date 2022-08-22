package io.github.kurrycat.mpkmod.save.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.kurrycat.mpkmod.gui.components.InfoLabel;
import io.github.kurrycat.mpkmod.save.DeserializeManager;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.io.IOException;

public class InfoLabelDeserializer extends JsonDeserializer<InfoLabel> {

    @Override
    public InfoLabel deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        JsonNode posNode = node.get("pos");
        JsonNode colorNode = node.get("color");

        Color color = DeserializeManager.deserialize(colorNode.asText(), Color.class);
        String text = node.get("text").asText();
        Vector2D vector2D = DeserializeManager.deserialize(posNode.asText(), Vector2D.class);

        InfoLabel infoLabel = new InfoLabel(text, vector2D);
        infoLabel.setColor(color);
        return infoLabel;
    }
}
