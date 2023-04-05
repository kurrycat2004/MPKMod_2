package io.github.kurrycat.mpkmod.save.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.awt.*;
import java.io.IOException;

public class ColorSerializer extends JsonSerializer<Color> {
    @Override
    public void serialize(Color value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("red", value.getRed());
        gen.writeNumberField("blue", value.getBlue());
        gen.writeNumberField("green", value.getGreen());
        gen.writeNumberField("alpha", value.getAlpha());
        gen.writeEndObject();
    }
}
