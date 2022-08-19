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

        components.add(new InfoLabel("{gold}X: {white}{player.pos.x,5}", new Vector2D(5, 20)));
        components.add(new InfoLabel("{gold}Y: {white}{player.pos.y,5}", new Vector2D(5, 30)));
        components.add(new InfoLabel("{gold}Z: {white}{player.pos.z,5}", new Vector2D(5, 40)));
        components.add(new InfoLabel("{gold}Yaw: {white}{player.yaw,5!} {gold}{player.facing}", new Vector2D(5, 50)));
        components.add(new InfoLabel("{gold}Pitch: {white}{player.pitch,5}", new Vector2D(5, 60)));

        /*components.add(
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
        );*/

        components.add(new KeyBindingLabel(new Vector2D(-35, 70), "key.forward"));
        components.add(new KeyBindingLabel(new Vector2D(-50, 85), "key.left"));
        components.add(new KeyBindingLabel(new Vector2D(-35, 85), "key.back"));
        components.add(new KeyBindingLabel(new Vector2D(-20, 85), "key.right"));
        components.add(new KeyBindingLabel(new Vector2D(-50, 100), "key.sneak"));
        components.add(new KeyBindingLabel(new Vector2D(-28, 100), "key.sprint"));
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
