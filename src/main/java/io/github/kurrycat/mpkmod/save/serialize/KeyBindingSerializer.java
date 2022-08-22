package io.github.kurrycat.mpkmod.save.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.minecraft.client.settings.KeyBinding;

import java.io.IOException;

public class KeyBindingSerializer extends JsonSerializer<KeyBinding> {
    @Override
    public void serialize(KeyBinding value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeStartObject();
        gen.writeStringField("description", value.getKeyDescription());
        gen.writeNumberField("keycode", value.getKeyCode());
        gen.writeStringField("category", value.getKeyCategory());
        gen.writeEndObject();
    }
}
