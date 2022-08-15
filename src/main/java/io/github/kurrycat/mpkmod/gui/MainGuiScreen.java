package io.github.kurrycat.mpkmod.gui;

import io.github.kurrycat.mpkmod.gui.components.InfoLabel;
import io.github.kurrycat.mpkmod.util.Colors;
import io.github.kurrycat.mpkmod.util.Vector2D;

public class MainGuiScreen extends ComponentScreen {
    public void onGuiInit() {
        super.onGuiInit();

        components.add(
                new InfoLabel("X: {white}{player.pos.x,10}", new Vector2D(5, 20))
                        .setColor(Colors.GOLD.getColor())
        );
        components.add(
                new InfoLabel("Pos: [{white}{player.pos.x}{gold}, {lpurple}{player.pos.y}{gold}, {blue}{player.pos.z}{gold}]", new Vector2D(5, 30))
                        .setColor(Colors.GOLD.getColor())
        );
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
