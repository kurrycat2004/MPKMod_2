package io.github.kurrycat.mpkmod.compatibility.fabric_1_19_4;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.*;
import io.github.kurrycat.mpkmod.gui.MPKGuiScreen;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.Debug;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.render.*;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

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
    public static final Set<Integer> pressedButtons = new HashSet<>();
    public MatrixStack matrixStack = new MatrixStack();

    public void playButtonSound() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public List<BoundingBox3D> getCollisionBoundingBoxes(Vector3D blockPosVector) {
        final Vector3D blockPosVec = blockPosVector.copy();
        BlockPos blockPos = new BlockPos(blockPosVec.getXI(), blockPosVec.getYI(), blockPosVec.getZI());
        if (MinecraftClient.getInstance().world == null) return null;
        ArrayList<BoundingBox3D> boundingBoxes = new ArrayList<>();
        BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockPos);

        blockState.getCollisionShape(MinecraftClient.getInstance().world, blockPos).simplify().forEachBox(
                ((minX, minY, minZ, maxX, maxY, maxZ) -> boundingBoxes.add(
                        new BoundingBox3D(new Vector3D(minX, minY, minZ), new Vector3D(maxX, maxY, maxZ)).move(blockPosVec)
                ))
        );

        return boundingBoxes;
    }

    public Vector3D getLookingAt() {
        if (MinecraftClient.getInstance().getCameraEntity() == null)
            return null;

        HitResult hitResult = MinecraftClient.getInstance().getCameraEntity().raycast(20, 0, false);
        if (hitResult instanceof BlockHitResult) {
            BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
            return new Vector3D(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }

        return null;
    }

    @Override
    public String getBlockName(Vector3D blockPos) {
        BlockPos blockpos = new BlockPos(blockPos.getXI(), blockPos.getYI(), blockPos.getZI());
        if (MinecraftClient.getInstance().world == null)
            return null;

        return Registries.BLOCK.getKey(
                MinecraftClient.getInstance().world.getBlockState(blockpos).getBlock()
        ).toString();
    }

    @Override
    public HashMap<String, String> getBlockProperties(Vector3D blockPos) {
        HashMap<String, String> properties = new HashMap<>();
        if (MinecraftClient.getInstance().world == null)
            return properties;

        BlockPos blockpos = new BlockPos(blockPos.getXI(), blockPos.getYI(), blockPos.getZI());
        BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockpos);
        blockState.getEntries().forEach((key, value) ->
                properties.put(key.getName(), Util.getValueAsString(key, value))
        );
        return null;
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.WorldInteraction.Interface WorldInteraction.Interface}
     */
    public String getLookingAtBlock() {
        if (MinecraftClient.getInstance().getCameraEntity() == null)
            return null;

        HitResult hitResult = MinecraftClient.getInstance().getCameraEntity().raycast(20, 0, false);
        if (hitResult.getType() == HitResult.Type.BLOCK && MinecraftClient.getInstance().world != null) {
            return Registries.BLOCK.getKey(
                    MinecraftClient.getInstance().world.getBlockState(((BlockHitResult) hitResult).getBlockPos()).getBlock()
            ).toString();
        }
        return null;
    }

    public void drawBox(BoundingBox3D bb, Color color, float partialTicks) {
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = color.getAlpha();

        RenderSystem.applyModelViewMatrix();

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();

        RenderSystem.lineWidth(1.0F);

        Matrix4f posMat = matrixStack.peek().getPositionMatrix();

        float minX = (float) bb.minX();
        float minY = (float) bb.minY();
        float minZ = (float) bb.minZ();
        float maxX = (float) bb.maxX();
        float maxY = (float) bb.maxY();
        float maxZ = (float) bb.maxZ();

        builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        builder.vertex(posMat, minX, maxY, minZ).color(r, g, b, a).next();
        builder.vertex(posMat, maxX, maxY, minZ).color(r, g, b, a).next();
        builder.vertex(posMat, maxX, minY, minZ).color(r, g, b, a).next();
        builder.vertex(posMat, minX, minY, minZ).color(r, g, b, a).next();

        builder.vertex(posMat, minX, minY, maxZ).color(r, g, b, a).next();
        builder.vertex(posMat, maxX, minY, maxZ).color(r, g, b, a).next();
        builder.vertex(posMat, maxX, maxY, maxZ).color(r, g, b, a).next();
        builder.vertex(posMat, minX, maxY, maxZ).color(r, g, b, a).next();

        builder.vertex(posMat, minX, minY, minZ).color(r, g, b, a).next();
        builder.vertex(posMat, maxX, minY, minZ).color(r, g, b, a).next();
        builder.vertex(posMat, maxX, minY, maxZ).color(r, g, b, a).next();
        builder.vertex(posMat, minX, minY, maxZ).color(r, g, b, a).next();

        builder.vertex(posMat, minX, maxY, maxZ).color(r, g, b, a).next();
        builder.vertex(posMat, maxX, maxY, maxZ).color(r, g, b, a).next();
        builder.vertex(posMat, maxX, maxY, minZ).color(r, g, b, a).next();
        builder.vertex(posMat, minX, maxY, minZ).color(r, g, b, a).next();

        builder.vertex(posMat, minX, minY, maxZ).color(r, g, b, a).next();
        builder.vertex(posMat, minX, maxY, maxZ).color(r, g, b, a).next();
        builder.vertex(posMat, minX, maxY, minZ).color(r, g, b, a).next();
        builder.vertex(posMat, minX, minY, minZ).color(r, g, b, a).next();

        builder.vertex(posMat, maxX, minY, minZ).color(r, g, b, a).next();
        builder.vertex(posMat, maxX, maxY, minZ).color(r, g, b, a).next();
        builder.vertex(posMat, maxX, maxY, maxZ).color(r, g, b, a).next();
        builder.vertex(posMat, maxX, minY, maxZ).color(r, g, b, a).next();

        tessellator.draw();

        RenderSystem.enableBlend();
    }

    /**
     * Is called in {@link Renderer2D.Interface}
     */
    public void drawLines(List<Vector2D> points, Color color) {
        if (points.size() < 2) {
            Debug.stacktrace("At least two points expected, got: " + points.size());
            return;
        }
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = color.getAlpha();

        matrixStack.translate(0, 0, 0.04);
        Matrix4f posMat = matrixStack.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();

        RenderSystem.lineWidth(1.0f);

        builder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

        for (Vector2D p : points) {
            builder.vertex(posMat, (float) p.getX(), (float) p.getY(), 0).color(r, g, b, a).next();
        }

        tessellator.draw();

        RenderSystem.enableBlend();
    }

    /**
     * Is called in {@link Renderer2D.Interface}
     */
    public void drawRect(Vector2D pos, Vector2D size, Color color) {
        if (matrixStack == null) return;
        //0.04 because drawString SHADOW_OFFSET is 0.03
        matrixStack.translate(0, 0, 0.04);
        Matrix4f posMat = matrixStack.peek().getPositionMatrix();
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = color.getAlpha();
        double x = pos.getX(), y = pos.getY(), w = size.getX(), h = size.getY();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bb = tessellator.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bb.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bb.vertex(posMat, (float) x, (float) (y + h), 0).color(r, g, b, a).next();
        bb.vertex(posMat, (float) (x + w), (float) (y + h), 0).color(r, g, b, a).next();
        bb.vertex(posMat, (float) (x + w), (float) y, 0).color(r, g, b, a).next();
        bb.vertex(posMat, (float) x, (float) y, 0).color(r, g, b, a).next();
        tessellator.draw();

        RenderSystem.disableBlend();
    }

    public Vector2D getScaledSize() {
        return new Vector2D(
                MinecraftClient.getInstance().getWindow().getScaledWidth(),
                MinecraftClient.getInstance().getWindow().getScaledHeight()
        );
    }

    public void enableScissor(double x, double y, double w, double h) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        Window r = MinecraftClient.getInstance().getWindow();

        double scaleFactor = r.getScaleFactor();
        double posX = x * scaleFactor;
        double posY = r.getFramebufferHeight() - (y + h) * scaleFactor;
        double width = w * scaleFactor;
        double height = h * scaleFactor;
        GL11.glScissor((int) posX, (int) posY, Math.max(0, (int) width), Math.max(0, (int) height));
    }

    public void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public void drawString(String text, Vector2D pos, Color color, boolean shadow) {
        matrixStack.translate(0,0,0.04);
        if (shadow)
            MinecraftClient.getInstance().textRenderer.drawWithShadow(matrixStack, text, pos.getXF(), pos.getYF(), color.getRGB());
        else
            MinecraftClient.getInstance().textRenderer.draw(matrixStack, text, pos.getXF(), pos.getYF(), color.getRGB());
    }

    public Vector2D getStringSize(String text) {
        return new Vector2D(
                MinecraftClient.getInstance().textRenderer.getWidth(text),
                MinecraftClient.getInstance().textRenderer.fontHeight
        );
    }

    public String getIP() {
        ServerInfo d = MinecraftClient.getInstance().getCurrentServerEntry();

        if (d == null)
            return "Multiplayer";
        else
            return d.address;
    }

    public String getFPS() {
        String[] split = MinecraftClient.getInstance().fpsDebugString.split(" ");
        if (split.length == 0)
            return "Error";
        return split[0];
    }

    public void displayGuiScreen(MPKGuiScreen screen) {
        MinecraftClient.getInstance().setScreen(
                screen == null
                ? null
                : new io.github.kurrycat.mpkmod.compatibility.fabric_1_19_4.MPKGuiScreen(screen));
    }

    public String getCurrentGuiScreen() {
        Screen curr = MinecraftClient.getInstance().currentScreen;

        if (curr == null)
            return null;
        else if (curr instanceof io.github.kurrycat.mpkmod.compatibility.fabric_1_19_4.MPKGuiScreen) {
            String id = ((io.github.kurrycat.mpkmod.compatibility.fabric_1_19_4.MPKGuiScreen) curr).eventReceiver.getID();
            if (id == null)
                id = "unknown";

            return id;
        }

        return curr.getClass().getSimpleName();
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getUserName() {
        if(MinecraftClient.getInstance().player == null) return null;
        return MinecraftClient.getInstance().player.getName().getString();
    }

    public List<Integer> getPressedButtons() {
        return new ArrayList<>(pressedButtons);
    }

    public void startSection(String name) {
        MinecraftClient.getInstance().getProfiler().push(name);
    }

    public void endStartSection(String name) {
        MinecraftClient.getInstance().getProfiler().swap(name);
    }

    public void endSection() {
        MinecraftClient.getInstance().getProfiler().pop();
    }
}
