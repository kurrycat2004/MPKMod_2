package io.github.kurrycat.mpkmod.save.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.kurrycat.mpkmod.gui.components.KeyBindingLabel;
import io.github.kurrycat.mpkmod.save.Serializer;

import java.io.IOException;

public class KeyBindingLabelSerializer extends JsonSerializer<KeyBindingLabel> {
    @Override
    public void serialize(KeyBindingLabel value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeStartObject();
        gen.writeStringField("@type", value.getClass().getSimpleName());
        gen.writeStringField("name", value.getName());
        gen.writeStringField("pos", Serializer.serializeToString(value.getPos()));
        gen.writeStringField("size", Serializer.serializeToString(value.getSize()));
        gen.writeEndObject();
    }
}
