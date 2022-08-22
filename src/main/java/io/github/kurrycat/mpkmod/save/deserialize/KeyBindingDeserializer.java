package io.github.kurrycat.mpkmod.save.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import net.minecraft.client.settings.KeyBinding;

import java.io.IOException;

public class KeyBindingDeserializer extends JsonDeserializer<KeyBinding> {
    @Override
    public KeyBinding deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode jsonNode = p.getCodec().readTree(p);
        String desc = jsonNode.get("description").asText();
        int keyCode = jsonNode.get("keycode").asInt();
        String category = jsonNode.get("category").asText();
        return new KeyBinding(desc, keyCode, category);
    }
}
