package io.github.kurrycat.mpkmod.save.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.kurrycat.mpkmod.gui.components.InfoLabel;
import io.github.kurrycat.mpkmod.save.Serializer;

import java.io.IOException;

public class InfoLabelSerializer extends JsonSerializer<InfoLabel> {

    @Override
    public void serialize(InfoLabel value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeStartObject();
        gen.writeStringField("@type", value.getClass().getSimpleName());
        gen.writeStringField("pos", Serializer.serializeToString(value.pos));
        gen.writeStringField("color", Serializer.serializeToString(value.color));
        gen.writeStringField("text", value.text);
        gen.writeEndObject();
    }
}
