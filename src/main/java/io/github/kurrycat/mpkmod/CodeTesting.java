package io.github.kurrycat.mpkmod;

import io.github.kurrycat.mpkmod.gui.components.InfoLabel;
import io.github.kurrycat.mpkmod.save.DeserializeManager;
import io.github.kurrycat.mpkmod.save.SerializeManager;
import io.github.kurrycat.mpkmod.util.JSONConfig;
import io.github.kurrycat.mpkmod.util.Vector2D;
import org.junit.jupiter.api.Test;

public class CodeTesting {

    @Test
    public void testAdd() {
        JSONConfig.setupFile();
        SerializeManager.registerSerializer();
        DeserializeManager.registerDeserializer();

        InfoLabel info1 = new InfoLabel("{gold}X: {white}{player.pos.x,5}", new Vector2D(5, 20));
        InfoLabel info2 = new InfoLabel("{gold}Y: {white}{player.pos.x,5}", new Vector2D(5, 20));
        InfoLabel info3 = new InfoLabel("{gold}Z: {white}{player.pos.x,5}", new Vector2D(5, 20));
        InfoLabel[] labels = new InfoLabel[3];
        labels[0] = info1;
        labels[1] = info2;
        labels[2] = info3;

        SerializeManager.serialize(JSONConfig.configFile, labels);
        InfoLabel[] deserializedInfo = DeserializeManager.deserialize(JSONConfig.configFile, InfoLabel[].class);
    }

}
