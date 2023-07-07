package io.github.kurrycat.mpkmod.compatibility.forge_1_8;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.*;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.Debug;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
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
import net.minecraft.world.WorldType;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionCompatibility implements FunctionHolder,
        SoundManager.Interface,
        WorldInteraction.Interface,
        Renderer3D.Interface,
        Renderer2D.Interface,
        FontRenderer.Interface,
        io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface,
        io.github.kurrycat.mpkmod.compatibility.MCClasses.Keyboard.Interface,
        Profiler.Interface {
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
     * Is called in {@link WorldInteraction.Interface}
     */
    public Vector3D getLookingAt() {
        BlockPos blockPos = Minecraft.getMinecraft().thePlayer.rayTrace(20, 0).getBlockPos();
        if (blockPos == null) return null;
        return new Vector3D(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.WorldInteraction.Interface WorldInteraction.Interface}
     */
    public String getBlockName(Vector3D blockPos) {
        String blockName = "";
        //if (Minecraft.getMinecraft().objectMouseOver != null && Minecraft.getMinecraft().objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && Minecraft.getMinecraft().objectMouseOver.getBlockPos() != null && !(Minecraft.getMinecraft().thePlayer.hasReducedDebug() || Minecraft.getMinecraft().gameSettings.reducedDebugInfo)) {
        BlockPos blockpos = new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        IBlockState iblockstate = Minecraft.getMinecraft().theWorld.getBlockState(blockpos);
        if (Minecraft.getMinecraft().theWorld.getWorldType() != WorldType.DEBUG_WORLD) {
            iblockstate = iblockstate.getBlock().getActualState(iblockstate, Minecraft.getMinecraft().theWorld, blockpos);
        }
        blockName = String.valueOf(Block.blockRegistry.getNameForObject(iblockstate.getBlock()));
        //}
        return blockName;
    }

    public HashMap<String, String> getBlockProperties(Vector3D blockPos) {
        HashMap<String, String> properties = new HashMap<>();
        BlockPos blockpos = new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        IBlockState iblockstate = Minecraft.getMinecraft().theWorld.getBlockState(blockpos);
        if (Minecraft.getMinecraft().theWorld.getWorldType() != WorldType.DEBUG_WORLD) {
            iblockstate = iblockstate.getBlock().getActualState(iblockstate, Minecraft.getMinecraft().theWorld, blockpos);
        }
        //noinspection rawtypes
        for (Map.Entry<IProperty, Comparable> e : iblockstate.getProperties().entrySet()) {
            properties.put(e.getKey().getName(), e.getValue().toString());
        }
        return properties;
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
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = color.getAlpha();
        double x = pos.getX(), y = pos.getY(), w = size.getX(), h = size.getY();

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        //GlStateManager.shadeModel(GL11.GL_SMOOTH); // - for gradients
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(x, y + h, 0.0).color(r, g, b, a).endVertex();
        wr.pos(x + w, y + h, 0.0).color(r, g, b, a).endVertex();
        wr.pos(x + w, y, 0.0).color(r, g, b, a).endVertex();
        wr.pos(x, y, 0.0).color(r, g, b, a).endVertex();
        tessellator.draw();
        //GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    /**
     * Is called in {@link Renderer2D.Interface}
     */
    public void drawLines(Collection<Vector2D> points, Color color) {
        if (points.size() < 2) {
            Debug.stacktrace("At least two points expected, got: " + points.size());
            return;
        }
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = color.getAlpha();

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GL11.glLineWidth(1.0F);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();

        wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        for (Vector2D p : points) {
            wr.pos(p.getX(), p.getY(), 0).color(r, g, b, a).endVertex();
        }

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
    public Vector2D getScaledSize() {
        ScaledResolution r = new ScaledResolution(Minecraft.getMinecraft());
        return new Vector2D(
                r.getScaledWidth_double(),
                r.getScaledHeight_double()
        );
    }

    public void enableScissor(double x, double y, double w, double h) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        ScaledResolution r = new ScaledResolution(Minecraft.getMinecraft());

        double scaleFactor = r.getScaleFactor();
        double posX = x * scaleFactor;
        double posY = Minecraft.getMinecraft().displayHeight - (y + h) * scaleFactor;
        double width = w * scaleFactor;
        double height = h * scaleFactor;
        GL11.glScissor((int) posX, (int) posY, Math.max(0, (int) width), Math.max(0, (int) height));
    }

    public void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public Vector2D getScreenSize() {
        return new Vector2D(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
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
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getIP() {
        ServerData d = Minecraft.getMinecraft().getCurrentServerData();
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
        Minecraft.getMinecraft().displayGuiScreen(screen == null ? null : new MPKGuiScreen(screen));
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getCurrentGuiScreen() {
        GuiScreen curr = Minecraft.getMinecraft().currentScreen;
        if (curr == null) return null;
        else if (curr instanceof MPKGuiScreen) {
            String id = ((MPKGuiScreen) curr).eventReceiver.getID();
            if (id == null) id = "unknown";
            return id;
        }
        return curr.getClass().getSimpleName();
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getUserName() {
        if (Minecraft.getMinecraft().thePlayer == null) return null;
        return Minecraft.getMinecraft().thePlayer.getName();
    }


    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Keyboard.Interface Keyboard.Interface}
     */
    public List<Integer> getPressedButtons() {
        List<Integer> keysDown = new ArrayList<>();
        for (int i = 0; i < Keyboard.getKeyCount(); i++)
            if (Keyboard.isKeyDown(i))
                keysDown.add(InputConstants.convert(i));
        return keysDown;
    }

    /**
     * Is called in {@link Profiler.Interface}
     */
    public void startSection(String name) {
        Minecraft.getMinecraft().mcProfiler.startSection(name);
    }

    /**
     * Is called in {@link Profiler.Interface}
     */
    public void endStartSection(String name) {
        Minecraft.getMinecraft().mcProfiler.endStartSection(name);
    }

    /**
     * Is called in {@link Profiler.Interface}
     */
    public void endSection() {
        Minecraft.getMinecraft().mcProfiler.endSection();
    }
}
