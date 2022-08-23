package io.github.kurrycat.mpkmod.save.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.io.IOException;

public class Vector2DSerializer extends JsonSerializer<Vector2D> {
    @Override
    public void serialize(Vector2D value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeStartObject();
        gen.writeNumberField("x", value.getX());
        gen.writeNumberField("y", value.getY());
        gen.writeEndObject();
    }
}
