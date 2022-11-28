package io.github.kurrycat.mpkmod.compatability.MC1_19;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.kurrycat.mpkmod.compatability.MCClasses.*;
import io.github.kurrycat.mpkmod.gui.MPKGuiScreen;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FunctionCompatibility implements FunctionHolder,
        SoundManager.Interface,
        WorldInteraction.Interface,
        Renderer3D.Interface,
        Renderer2D.Interface,
        FontRenderer.Interface,
        io.github.kurrycat.mpkmod.compatability.MCClasses.Minecraft.Interface,
        io.github.kurrycat.mpkmod.compatability.MCClasses.Keyboard.Interface,
        Profiler.Interface {
    public static final Set<String> pressedButtons = new HashSet<>();
    public PoseStack poseStack = new PoseStack();

    /**
     * Is called in {@link SoundManager.Interface}
     */
    public void playButtonSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    /**
     * Is called in {@link WorldInteraction.Interface}
     */
    public List<BoundingBox3D> getCollisionBoundingBoxes(Vector3D blockPosVec) {
        BlockPos blockPos = new BlockPos(blockPosVec.getX(), blockPosVec.getY(), blockPosVec.getZ());
        if (Minecraft.getInstance().level == null) return null;
        ArrayList<BoundingBox3D> boundingBoxes = new ArrayList<>();
        Minecraft.getInstance().level.getBlockState(blockPos).getCollisionShape(Minecraft.getInstance().level, blockPos).forAllBoxes(
                (minX, minY, minZ, maxX, maxY, maxZ) -> boundingBoxes.add(new BoundingBox3D(new Vector3D(minX, minY, minZ), new Vector3D(maxX, maxY, maxZ)))
        );
        return boundingBoxes;
    }

    /**
     * Is called in {@link WorldInteraction.Interface}
     */
    public Vector3D getLookingAt() {
        if (Minecraft.getInstance().getCameraEntity() == null) return null;
        HitResult hitResult = Minecraft.getInstance().getCameraEntity().pick(20, 0, false);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return new Vector3D(hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z);
        }
        return null;
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatability.MCClasses.WorldInteraction.Interface WorldInteraction.Interface}
     */
    public String getLookingAtBlock() {
        if (Minecraft.getInstance().getCameraEntity() == null) return null;
        HitResult hitResult = Minecraft.getInstance().getCameraEntity().pick(20, 0, false);
        if (hitResult.getType() == HitResult.Type.BLOCK && Minecraft.getInstance().level != null) {
            return Registry.BLOCK.getKey(Minecraft.getInstance().level.getBlockState(((BlockHitResult) hitResult).getBlockPos()).getBlock()).toString();
        }
        return null;
    }

    /**
     * Is called in {@link Renderer3D.Interface}
     */
    public void drawBox(BoundingBox3D bb, Color color, float partialTicks) {
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = color.getAlpha();

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        GL11.glLineWidth(2.0F);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        //WorldRenderer bb = bb.getWorldRenderer();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Vec3 pos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        bb = bb.move(pos.x, pos.y, pos.z);

        /*double entityX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
        double entityY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
        double entityZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;*/

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        //bb.(-entityX, -entityY, -entityZ);

        builder.vertex(bb.minX(), bb.maxY(), bb.minZ()).color(r, g, b, a).endVertex();
        builder.vertex(bb.maxX(), bb.maxY(), bb.minZ()).color(r, g, b, a).endVertex();
        builder.vertex(bb.maxX(), bb.minY(), bb.minZ()).color(r, g, b, a).endVertex();
        builder.vertex(bb.minX(), bb.minY(), bb.minZ()).color(r, g, b, a).endVertex();

        builder.vertex(bb.minX(), bb.minY(), bb.maxZ()).color(r, g, b, a).endVertex();
        builder.vertex(bb.maxX(), bb.minY(), bb.maxZ()).color(r, g, b, a).endVertex();
        builder.vertex(bb.maxX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).endVertex();
        builder.vertex(bb.minX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).endVertex();

        builder.vertex(bb.minX(), bb.minY(), bb.minZ()).color(r, g, b, a).endVertex();
        builder.vertex(bb.maxX(), bb.minY(), bb.minZ()).color(r, g, b, a).endVertex();
        builder.vertex(bb.maxX(), bb.minY(), bb.maxZ()).color(r, g, b, a).endVertex();
        builder.vertex(bb.minX(), bb.minY(), bb.maxZ()).color(r, g, b, a).endVertex();

        builder.vertex(bb.minX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).endVertex();
        builder.vertex(bb.maxX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).endVertex();
        builder.vertex(bb.maxX(), bb.maxY(), bb.minZ()).color(r, g, b, a).endVertex();
        builder.vertex(bb.minX(), bb.maxY(), bb.minZ()).color(r, g, b, a).endVertex();

        builder.vertex(bb.minX(), bb.minY(), bb.maxZ()).color(r, g, b, a).endVertex();
        builder.vertex(bb.minX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).endVertex();
        builder.vertex(bb.minX(), bb.maxY(), bb.minZ()).color(r, g, b, a).endVertex();
        builder.vertex(bb.minX(), bb.minY(), bb.minZ()).color(r, g, b, a).endVertex();

        builder.vertex(bb.maxX(), bb.minY(), bb.minZ()).color(r, g, b, a).endVertex();
        builder.vertex(bb.maxX(), bb.maxY(), bb.minZ()).color(r, g, b, a).endVertex();
        builder.vertex(bb.maxX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).endVertex();
        builder.vertex(bb.maxX(), bb.minY(), bb.maxZ()).color(r, g, b, a).endVertex();

        //bb.setTranslation(0, 0, 0);

        //bb.draw();
        BufferUploader.drawWithShader(builder.end());

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    /**
     * Is called in {@link Renderer2D.Interface}
     */
    public void drawRect(Vector2D pos, Vector2D size, Color color) {
        Screen.fill(
                poseStack,
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
                Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                Minecraft.getInstance().getWindow().getGuiScaledHeight()
        );
    }

    /**
     * Is called in {@link FontRenderer.Interface}
     */
    public void drawString(String text, Vector2D pos, Color color, boolean shadow) {
        if (shadow)
            Minecraft.getInstance().font.drawShadow(poseStack, text, pos.getXF(), pos.getYF(), color.getRGB());
        else
            Minecraft.getInstance().font.draw(poseStack, text, pos.getXF(), pos.getYF(), color.getRGB());
    }

    /**
     * Is called in {@link FontRenderer.Interface}
     */
    public Vector2D getStringSize(String text) {
        return new Vector2D(
                Minecraft.getInstance().font.width(text),
                Minecraft.getInstance().font.lineHeight
        );
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatability.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getIP() {
        ServerData d = Minecraft.getInstance().getCurrentServer();
        if (d == null) return "Multiplayer";
        else return d.ip;
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatability.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getFPS() {
        String[] split = Minecraft.getInstance().fpsString.split(" ");
        if (split.length == 0) return "Error";
        return split[0];
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatability.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public void displayGuiScreen(MPKGuiScreen screen) {
        Minecraft.getInstance().setScreen(screen == null ? null : new MPKGuiScreen_1_19(screen));
    }


    /**
     * Is called in {@link Keyboard.Interface Keyboard.Interface}
     */
    public List<String> getPressedButtons() {
        return new ArrayList<>(pressedButtons);
    }

    /**
     * Is called in {@link Profiler.Interface}
     */
    public void startSection(String name) {
        Minecraft.getInstance().getProfiler().push(name);
    }

    /**
     * Is called in {@link Profiler.Interface}
     */
    public void endStartSection(String name) {
        Minecraft.getInstance().getProfiler().popPush(name);
    }

    /**
     * Is called in {@link Profiler.Interface}
     */
    public void endSection() {
        Minecraft.getInstance().getProfiler().pop();
    }
}
