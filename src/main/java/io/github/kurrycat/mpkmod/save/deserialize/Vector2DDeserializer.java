package io.github.kurrycat.mpkmod.save.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.io.IOException;

public class Vector2DDeserializer extends JsonDeserializer<Vector2D> {
    @Override
    public Vector2D deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        double x = node.get("x").asDouble();
        double y = node.get("y").asDouble();
        return new Vector2D(x, y);
    }
}
