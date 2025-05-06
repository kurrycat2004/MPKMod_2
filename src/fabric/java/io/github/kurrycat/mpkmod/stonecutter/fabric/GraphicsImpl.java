package io.github.kurrycat.mpkmod.stonecutter.fabric;

import io.github.kurrycat.mpkmod.api.minecraft.IGraphics;

public class GraphicsImpl implements IGraphics {
    public static final GraphicsImpl INSTANCE = new GraphicsImpl();

    private GraphicsImpl() {
    }

    @Override
    public void drawRect(int x, int y, int width, int height, int color) {

    }

    @Override
    public void drawString(String text, int x, int y, int color) {
    }
}
