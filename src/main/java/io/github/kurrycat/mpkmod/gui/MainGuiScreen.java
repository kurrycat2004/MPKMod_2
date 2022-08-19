package io.github.kurrycat.mpkmod.gui;

import io.github.kurrycat.mpkmod.gui.components.InfoLabel;
import io.github.kurrycat.mpkmod.gui.components.KeyBindingLabel;
import io.github.kurrycat.mpkmod.util.Colors;
import io.github.kurrycat.mpkmod.util.FormatStringBuilder;
import io.github.kurrycat.mpkmod.util.Vector2D;

public class MainGuiScreen extends ComponentScreen {
    public void onGuiInit() {
        super.onGuiInit();

        components.add(
                new InfoLabel(
                        new FormatStringBuilder()
                                .addString("IP: ")
                                .setColor(Colors.WHITE)
                                .addVar("mc.IP")
                                .toString(),
                        new Vector2D(5, 5))
                        .setColor(Colors.GOLD.getColor()));
        components.add(
                new InfoLabel(
                        new FormatStringBuilder()
                                .addString("X: ")
                                .setColor(Colors.WHITE)
                                .addVar("player.pos.x", 10)
                                .toString(),
                        new Vector2D(5, 20))
                        .setColor(Colors.GOLD.getColor())
        );
        components.add(
                new InfoLabel(
                        new FormatStringBuilder()
                                .addString("Pos: [")
                                .setColor(Colors.WHITE)
                                .addVar("player.pos.x")
                                .setColor(Colors.GOLD)
                                .addString(", ")
                                .setColor(Colors.LIGHT_PURPLE)
                                .addVar("player.pos.y")
                                .setColor(Colors.GOLD)
                                .addString(", ")
                                .setColor(Colors.BLUE)
                                .addVar("player.pos.z")
                                .setColor(Colors.GOLD)
                                .addString("]")
                                .toString(),
                        new Vector2D(5, 30)
                )
                        .setColor(Colors.GOLD.getColor())
        );

        components.add(new KeyBindingLabel(new Vector2D(20, 50), "key.forward"));
        components.add(new KeyBindingLabel(new Vector2D(5, 65), "key.left"));
        components.add(new KeyBindingLabel(new Vector2D(20, 65), "key.back"));
        components.add(new KeyBindingLabel(new Vector2D(35, 65), "key.right"));
        components.add(new KeyBindingLabel(new Vector2D(5, 80), "key.sneak"));
        components.add(new KeyBindingLabel(new Vector2D(27, 80), "key.sprint"));

    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
