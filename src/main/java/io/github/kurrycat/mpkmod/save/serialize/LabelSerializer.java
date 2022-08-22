package io.github.kurrycat.mpkmod.save.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.kurrycat.mpkmod.gui.components.Label;
import io.github.kurrycat.mpkmod.save.SerializeManager;

import java.io.IOException;

public class LabelSerializer extends JsonSerializer<Label> {

    @Override
    public void serialize(Label value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeStartObject();
        gen.writeStringField("text", value.text);
        gen.writeStringField("color", SerializeManager.serializeToString(value.color));
        gen.writeEndObject();
    }
}
