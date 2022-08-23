package io.github.kurrycat.mpkmod.save.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.kurrycat.mpkmod.gui.components.KeyBindingLabel;
import io.github.kurrycat.mpkmod.save.DeserializeManager;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.io.IOException;

public class KeyBindingLabelDeserializer extends JsonDeserializer<KeyBindingLabel> {
    @Override
    public KeyBindingLabel deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        JsonNode vecSizeNode = node.get("size");
        JsonNode posSizeNode = node.get("pos");

        String name = node.get("name").asText();
        Vector2D posVec = DeserializeManager.deserialize(posSizeNode.asText(), Vector2D.class);
        Vector2D sizeVec = DeserializeManager.deserialize(vecSizeNode.asText(), Vector2D.class);

        return new KeyBindingLabel(posVec, name);
    }
}
