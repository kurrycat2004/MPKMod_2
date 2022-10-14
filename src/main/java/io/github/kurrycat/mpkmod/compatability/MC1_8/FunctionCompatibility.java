package io.github.kurrycat.mpkmod.compatability.MC1_8;

import io.github.kurrycat.mpkmod.compatability.MCClasses.*;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionCompatibility implements FunctionHolder,
        SoundManager.Interface,
        WorldInteraction.Interface,
        Renderer3D.Interface,
        Renderer2D.Interface,
        FontRenderer.Interface,
        io.github.kurrycat.mpkmod.compatability.MCClasses.Minecraft.Interface,
        io.github.kurrycat.mpkmod.compatability.MCClasses.Keyboard.Interface {
    /**
     * Is called in {@link SoundManager.Interface}
     */
    public void playButtonSound() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }

    /**
     * Is called in {@link WorldInteraction.Interface}
     */
    public List<BoundingBox3D> getCollisionBoundingBoxes(Vector3D blockPosVec) {
        BlockPos blockPos = new BlockPos(blockPosVec.getX(), blockPosVec.getY(), blockPosVec.getZ());
        World world = Minecraft.getMinecraft().theWorld;
        IBlockState blockState = world.getBlockState(blockPos);
        AxisAlignedBB mask = new AxisAlignedBB(blockPosVec.getX() - 1, blockPosVec.getY() - 1, blockPosVec.getZ() - 1, blockPosVec.getX() + 1, blockPosVec.getY() + 1, blockPosVec.getZ() + 1);
        ArrayList<AxisAlignedBB> result = new ArrayList<>();
        blockState.getBlock().addCollisionBoxesToList(world, blockPos, blockState, mask, result, null);

        return result.stream().map((aabb) -> new BoundingBox3D(new Vector3D(aabb.minX, aabb.minY, aabb.minZ), new Vector3D(aabb.maxX, aabb.maxY, aabb.maxZ))).collect(Collectors.toList());
    }

    /**
     * Is called in {@link Renderer3D.Interface}
     */
    public void drawBox(BoundingBox3D bb, Color color, float partialTicks) {
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = color.getAlpha();

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GL11.glLineWidth(2.0F);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();

        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();

        double entityX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
        double entityY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
        double entityZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;

        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        wr.setTranslation(-entityX, -entityY, -entityZ);

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

        wr.setTranslation(0, 0, 0);

        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.popMatrix();
    }

    /**
     * Is called in {@link Renderer2D.Interface}
     */
    public void drawRect(Vector2D pos, Vector2D size, Color color) {
        Gui.drawRect(
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
        ScaledResolution r = new ScaledResolution(Minecraft.getMinecraft());
        return new Vector2D(
                r.getScaledWidth_double(),
                r.getScaledHeight_double()
        );
    }

    /**
     * Is called in {@link FontRenderer.Interface}
     */
    public void drawString(String text, Vector2D pos, Color color, boolean shadow) {
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().fontRendererObj.drawString(text, pos.getXF(), pos.getYF(), color.getRGB(), shadow);
        GlStateManager.disableBlend();
    }

    /**
     * Is called in {@link FontRenderer.Interface}
     */
    public Vector2D getStringSize(String text) {
        return new Vector2D(
                Minecraft.getMinecraft().fontRendererObj.getStringWidth(text),
                Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT
        );
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatability.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getIP() {
        ServerData d = Minecraft.getMinecraft().getCurrentServerData();
        if (d == null) return "Multiplayer";
        else return d.serverIP;
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatability.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getFPS() {
        return String.valueOf(Minecraft.getDebugFPS());
    }


    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatability.MCClasses.Keyboard.Interface Keyboard.Interface}
     */
    public List<String> getPressedButtons() {
        List<String> keysDown = new ArrayList<>();
        for (int i = 0; i < Keyboard.getKeyCount(); i++)
            if (Keyboard.isKeyDown(i))
                keysDown.add(Keyboard.getKeyName(i));
        return keysDown;
    }
}
