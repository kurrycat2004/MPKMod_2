package io.github.kurrycat.mpkmod.compatibility.forge_1_19_4;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.*;
import io.github.kurrycat.mpkmod.gui.MPKGuiScreen;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.List;
import java.util.*;

public class FunctionCompatibility implements FunctionHolder,
        SoundManager.Interface,
        WorldInteraction.Interface,
        Renderer3D.Interface,
        Renderer2D.Interface,
        FontRenderer.Interface,
        io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface,
        io.github.kurrycat.mpkmod.compatibility.MCClasses.Keyboard.Interface,
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
    public List<BoundingBox3D> getCollisionBoundingBoxes(Vector3D blockPosVector) {
        final Vector3D blockPosVec = blockPosVector.copy();
        BlockPos blockPos = new BlockPos(blockPosVec.getXI(), blockPosVec.getYI(), blockPosVec.getZI());
        if (Minecraft.getInstance().level == null) return null;
        ArrayList<BoundingBox3D> boundingBoxes = new ArrayList<>();
        BlockState state = Minecraft.getInstance().level.getBlockState(blockPos);

        state.getCollisionShape(Minecraft.getInstance().level, blockPos).optimize().forAllBoxes(
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
        if (Minecraft.getInstance().getCameraEntity() == null) return null;
        HitResult hitResult = Minecraft.getInstance().getCameraEntity().pick(20, 0, false);
        if (hitResult instanceof BlockHitResult) {
            BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
            return new Vector3D(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }
        return null;
    }

    @Override
    public String getBlockName(Vector3D blockPos) {
        BlockPos blockpos = new BlockPos(blockPos.getXI(), blockPos.getYI(), blockPos.getZI());
        if (Minecraft.getInstance().level == null) return null;
        return BuiltInRegistries.BLOCK.getKey(
                Minecraft.getInstance().level.getBlockState(blockpos).getBlock()
        ).toString();
    }

    @Override
    public HashMap<String, String> getBlockProperties(Vector3D blockPos) {
        HashMap<String, String> properties = new HashMap<>();
        if (Minecraft.getInstance().level == null) return properties;
        BlockPos blockpos = new BlockPos(blockPos.getXI(), blockPos.getYI(), blockPos.getZI());
        BlockState blockState = Minecraft.getInstance().level.getBlockState(blockpos);
        blockState.getValues().forEach((key, value) ->
                properties.put(key.getName(), Util.getPropertyName(key, value))
        );
        return null;
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.WorldInteraction.Interface WorldInteraction.Interface}
     */
    public String getLookingAtBlock() {
        if (Minecraft.getInstance().getCameraEntity() == null) return null;
        HitResult hitResult = Minecraft.getInstance().getCameraEntity().pick(20, 0, false);
        if (hitResult.getType() == HitResult.Type.BLOCK && Minecraft.getInstance().level != null) {
            return BuiltInRegistries.BLOCK.getKey(Minecraft.getInstance().level.getBlockState(((BlockHitResult) hitResult).getBlockPos()).getBlock()).toString();
        }
        return null;
    }

    /**
     * Is called in {@link Renderer3D.Interface}
     */
    public void drawBox(BoundingBox3D bb, Color color, float partialTicks) {
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = color.getAlpha();

        RenderSystem.applyModelViewMatrix();

        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();

        RenderSystem.enableBlend();

        RenderSystem.lineWidth(1.0F);

        Vec3 pos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        bb = bb.move(-pos.x, -pos.y, -pos.z);

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

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

        tesselator.end();

        RenderSystem.enableBlend();
    }

    /**
     * Is called in {@link Renderer2D.Interface}
     */
    public void drawRect(Vector2D pos, Vector2D size, Color color) {
        poseStack.translate(0,0,0.04);
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
        poseStack.translate(0,0,0.04);
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
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getIP() {
        ServerData d = Minecraft.getInstance().getCurrentServer();
        if (d == null) return "Multiplayer";
        else return d.ip;
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getFPS() {
        String[] split = Minecraft.getInstance().fpsString.split(" ");
        if (split.length == 0) return "Error";
        return split[0];
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public void displayGuiScreen(MPKGuiScreen screen) {
        Minecraft.getInstance().setScreen(screen == null ? null : new io.github.kurrycat.mpkmod.compatibility.forge_1_19_4.MPKGuiScreen(screen));
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getCurrentGuiScreen() {
        Screen curr = Minecraft.getInstance().screen;
        if (curr == null) return null;
        else if (curr instanceof io.github.kurrycat.mpkmod.compatibility.forge_1_19_4.MPKGuiScreen) {
            String id = ((io.github.kurrycat.mpkmod.compatibility.forge_1_19_4.MPKGuiScreen) curr).eventReceiver.getID();
            if (id == null) id = "unknown";
            return id;
        }
        return curr.getClass().getSimpleName();
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getUserName() {
        if(Minecraft.getInstance().player == null) return null;
        return Minecraft.getInstance().player.getName().getString();
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

