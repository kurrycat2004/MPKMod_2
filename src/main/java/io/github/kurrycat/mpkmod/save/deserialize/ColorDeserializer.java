package io.github.kurrycat.mpkmod.save.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.awt.*;
import java.io.IOException;

public class ColorDeserializer extends JsonDeserializer<Color> {
    @Override
    public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node.isObject()) {
            int red = node.get("red").asInt();
            int green = node.get("green").asInt();
            int blue = node.get("blue").asInt();
            int alpha = node.get("alpha").asInt();
            return new Color(red, green, blue, alpha);
        } else {
            String c = node.asText();
            return new Color(
                    Integer.valueOf(c.substring(3, 5), 16),
                    Integer.valueOf(c.substring(5, 7), 16),
                    Integer.valueOf(c.substring(7, 9), 16),
                    Integer.valueOf(c.substring(1, 3), 16)
            );
        }
    }
}
