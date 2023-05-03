package io.github.kurrycat.mpkmod.compatibility.forge_1_14_4;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.*;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.state.IProperty;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionCompatibility implements FunctionHolder,
        SoundManager.Interface,
        WorldInteraction.Interface,
        Renderer3D.Interface,
        Renderer2D.Interface,
        FontRenderer.Interface,
        io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface,
        Keyboard.Interface,
        Profiler.Interface {
    /**
     * Is called in {@link SoundManager.Interface}
     */
    public void playButtonSound() {
        Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    /**
     * Is called in {@link WorldInteraction.Interface}
     */
    public List<BoundingBox3D> getCollisionBoundingBoxes(Vector3D blockPosVector) {
        final Vector3D blockPosVec = blockPosVector.copy();
        BlockPos blockPos = new BlockPos(blockPosVec.getX(), blockPosVec.getY(), blockPosVec.getZ());
        if (Minecraft.getInstance().world == null) return null;
        ArrayList<BoundingBox3D> boundingBoxes = new ArrayList<>();
        BlockState state = Minecraft.getInstance().world.getBlockState(blockPos);

        state.getCollisionShape(Minecraft.getInstance().world, blockPos).simplify().forEachBox(
                (minX, minY, minZ, maxX, maxY, maxZ) -> boundingBoxes.add(
                        new BoundingBox3D(new Vector3D(minX, minY, minZ), new Vector3D(maxX, maxY, maxZ)).move(blockPosVec)
                )
        );

        return boundingBoxes;
    }

    /**
     * Is called in {@link WorldInteraction.Interface}
     */
    public Vector3D getLookingAt() {
        Vec3d hitVec = Minecraft.getInstance().objectMouseOver.getHitVec();
        if (Minecraft.getInstance().objectMouseOver.getType() == RayTraceResult.Type.MISS)
            return null;
        return new Vector3D(hitVec.getX(), hitVec.getY(), hitVec.getZ());
    }

    /**
     * Is called in {@link WorldInteraction.Interface WorldInteraction.Interface}
     */
    @SuppressWarnings("deprecation")
    public String getBlockName(Vector3D blockPos) {
        BlockPos blockpos = new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        BlockState blockstate = Minecraft.getInstance().world.getBlockState(blockpos);
        return String.valueOf(Registry.BLOCK.getKey(blockstate.getBlock()));
    }

    public HashMap<String, String> getBlockProperties(Vector3D blockPos) {
        HashMap<String, String> properties = new HashMap<>();
        BlockPos blockpos = new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        BlockState blockstate = Minecraft.getInstance().world.getBlockState(blockpos);

        for (Map.Entry<IProperty<?>, Comparable<?>> entry : blockstate.getValues().entrySet()) {
            properties.put(entry.getKey().getName(), Util.getValueName(entry.getKey(), entry.getValue()));
        }
        return properties;
    }

    /**
     * Is called in {@link Renderer3D.Interface}
     */
    public void drawBox(BoundingBox3D bb, Color color, float partialTicks) {
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = color.getAlpha();

        GlStateManager.pushMatrix();
        GlStateManager.clear(256, Minecraft.IS_RUNNING_ON_MAC);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder wr = tessellator.getBuffer();

        Entity entity = Minecraft.getInstance().getRenderViewEntity();
        if (entity == null) return;

        double entityX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
        double entityY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
        double entityZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;

        wr.setTranslation(-entityX, -entityY, -entityZ);

        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        wr.pos(bb.minX(), bb.maxY(), bb.minZ()).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX(), bb.maxY(), bb.minZ()).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX(), bb.minY(), bb.minZ()).color(r, g, b, a).endVertex();
        wr.pos(bb.minX(), bb.minY(), bb.minZ()).color(r, g, b, a).endVertex();

        wr.pos(bb.minX(), bb.minY(), bb.maxZ()).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX(), bb.minY(), bb.maxZ()).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).endVertex();
        wr.pos(bb.minX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).endVertex();

        wr.pos(bb.minX(), bb.minY(), bb.minZ()).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX(), bb.minY(), bb.minZ()).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX(), bb.minY(), bb.maxZ()).color(r, g, b, a).endVertex();
        wr.pos(bb.minX(), bb.minY(), bb.maxZ()).color(r, g, b, a).endVertex();

        wr.pos(bb.minX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX(), bb.maxY(), bb.minZ()).color(r, g, b, a).endVertex();
        wr.pos(bb.minX(), bb.maxY(), bb.minZ()).color(r, g, b, a).endVertex();

        wr.pos(bb.minX(), bb.minY(), bb.maxZ()).color(r, g, b, a).endVertex();
        wr.pos(bb.minX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).endVertex();
        wr.pos(bb.minX(), bb.maxY(), bb.minZ()).color(r, g, b, a).endVertex();
        wr.pos(bb.minX(), bb.minY(), bb.minZ()).color(r, g, b, a).endVertex();

        wr.pos(bb.maxX(), bb.minY(), bb.minZ()).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX(), bb.maxY(), bb.minZ()).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX(), bb.minY(), bb.maxZ()).color(r, g, b, a).endVertex();

        tessellator.draw();
        GlStateManager.disableBlend();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        wr.setTranslation(0, 0, 0);

        GlStateManager.popMatrix();
    }

    /**
     * Is called in {@link Renderer2D.Interface}
     */
    public void drawRect(Vector2D pos, Vector2D size, Color color) {
        Screen.fill(
                pos.getXI(),
                pos.getYI(),
                pos.getXI() + size.getXI(),
                pos.getYI() + size.getYI(),
                color.getRGB()
        );
    }


    /**
     * Is called in {@link Renderer2D.Interface}
     */
    public Vector2D getScaledSize() {
        return new Vector2D(
                Minecraft.getInstance().mainWindow.getScaledWidth(),
                Minecraft.getInstance().mainWindow.getScaledHeight()
        );
    }

    /**
     * Is called in {@link FontRenderer.Interface}
     */
    public void drawString(String text, Vector2D pos, Color color, boolean shadow) {
        if (shadow)
            Minecraft.getInstance().fontRenderer.drawStringWithShadow(text, pos.getXF(), pos.getYF(), color.getRGB());
        else
            Minecraft.getInstance().fontRenderer.drawString(text, pos.getXF(), pos.getYF(), color.getRGB());
    }

    /**
     * Is called in {@link FontRenderer.Interface}
     */
    public Vector2D getStringSize(String text) {
        return new Vector2D(
                Minecraft.getInstance().fontRenderer.getStringWidth(text),
                Minecraft.getInstance().fontRenderer.FONT_HEIGHT
        );
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getIP() {
        ServerData d = Minecraft.getInstance().getCurrentServerData();
        if (d == null) return "Multiplayer";
        else return d.serverIP;
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getFPS() {
        return String.valueOf(Minecraft.getDebugFPS());
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public void displayGuiScreen(io.github.kurrycat.mpkmod.gui.MPKGuiScreen screen) {
        Minecraft.getInstance().displayGuiScreen(screen == null ? null : new MPKGuiScreen(screen));
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getCurrentGuiScreen() {
        Screen curr = Minecraft.getInstance().currentScreen;
        if (curr == null) return null;
        else if (curr instanceof MPKGuiScreen) {
            String id = ((MPKGuiScreen) curr).eventReceiver.getID();
            if (id == null) id = "unknown";
            return id;
        }
        return curr.getClass().getSimpleName();
    }


    /**
     * Is called in {@link Keyboard.Interface Keyboard.Interface}
     */
    public List<String> getPressedButtons() {
        List<String> keysDown = new ArrayList<>();
        //TODO: Fix for 1.14 - inputs list is private
        return keysDown;
    }

    /**
     * Is called in {@link Profiler.Interface}
     */
    public void startSection(String name) {
        Minecraft.getInstance().getProfiler().startSection(name);
    }

    /**
     * Is called in {@link Profiler.Interface}
     */
    public void endStartSection(String name) {
        Minecraft.getInstance().getProfiler().endStartSection(name);
    }

    /**
     * Is called in {@link Profiler.Interface}
     */
    public void endSection() {
        Minecraft.getInstance().getProfiler().endSection();
    }
}
