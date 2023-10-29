package io.github.kurrycat.mpkmod.compatibility.forge_1_20;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.*;
import io.github.kurrycat.mpkmod.gui.MPKGuiScreen;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.Debug;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
    public GuiGraphics guiGraphics = null;

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
    public Vector3D getLookingAt(double distance) {
        if (Minecraft.getInstance().getCameraEntity() == null) return null;
        HitResult hitResult = Minecraft.getInstance().getCameraEntity().pick(distance, 0, false);
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
        if (guiGraphics == null) return;
        //0.04 because drawString SHADOW_OFFSET is 0.03
        guiGraphics.pose().translate(0, 0, 0.04);
        Matrix4f posMat = guiGraphics.pose().last().pose();
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = color.getAlpha();
        double x = pos.getX(), y = pos.getY(), w = size.getX(), h = size.getY();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bb = tesselator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bb.vertex(posMat, (float) x, (float) (y + h), 0).color(r, g, b, a).endVertex();
        bb.vertex(posMat, (float) (x + w), (float) (y + h), 0).color(r, g, b, a).endVertex();
        bb.vertex(posMat, (float) (x + w), (float) y, 0).color(r, g, b, a).endVertex();
        bb.vertex(posMat, (float) x, (float) y, 0).color(r, g, b, a).endVertex();
        tesselator.end();

        RenderSystem.disableBlend();
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

        guiGraphics.pose().translate(0, 0, 0.04);
        Matrix4f posMat = guiGraphics.pose().last().pose();

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();

        RenderSystem.lineWidth(1.0f);

        builder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (Vector2D p : points) {
            builder.vertex(posMat, (float) p.getX(), (float) p.getY(), 0).color(r, g, b, a).endVertex();
        }

        tesselator.end();

        RenderSystem.enableBlend();
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

    public Vector2D getScreenSize() {
        return new Vector2D(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());
    }

    public void enableScissor(double x, double y, double w, double h) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        Window r = Minecraft.getInstance().getWindow();

        double scaleFactor = r.getGuiScale();
        double posX = x * scaleFactor;
        double posY = r.getHeight() - (y + h) * scaleFactor;
        double width = w * scaleFactor;
        double height = h * scaleFactor;
        GL11.glScissor((int) posX, (int) posY, Math.max(0, (int) width), Math.max(0, (int) height));
    }

    public void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    /**
     * Is called in {@link FontRenderer.Interface}
     */
    public void drawString(String text, double x, double y, Color color, double fontSize, boolean shadow) {
        if (guiGraphics == null) return;
        PoseStack poseStack = guiGraphics.pose();
        poseStack.translate(0, 0, 0.04);
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        double scale = fontSize / Minecraft.getInstance().font.lineHeight;
        poseStack.scale((float) scale, (float) scale, 1);
        guiGraphics.drawString(
                Minecraft.getInstance().font, text,
                0, 0, color.getRGB(), shadow
        );
        poseStack.popPose();
    }

    /**
     * Is called in {@link FontRenderer.Interface}
     */
    public Vector2D getStringSize(String text, double fontSize) {
        return new Vector2D(
                Minecraft.getInstance().font.width(text) *
                        (float) (fontSize / Minecraft.getInstance().font.lineHeight),
                (float) fontSize
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
        Minecraft.getInstance().setScreen(screen == null ? null : new io.github.kurrycat.mpkmod.compatibility.forge_1_20.MPKGuiScreen(screen));
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getCurrentGuiScreen() {
        Screen curr = Minecraft.getInstance().screen;
        if (curr == null) return null;
        else if (curr instanceof io.github.kurrycat.mpkmod.compatibility.forge_1_20.MPKGuiScreen) {
            String id = ((io.github.kurrycat.mpkmod.compatibility.forge_1_20.MPKGuiScreen) curr).eventReceiver.getID();
            if (id == null) id = "unknown";
            return id;
        }
        return curr.getClass().getSimpleName();
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getUserName() {
        if (Minecraft.getInstance().player == null) return null;
        return Minecraft.getInstance().player.getName().getString();
    }

    public void copyToClipboard(String content) {
        Minecraft.getInstance().keyboardHandler.setClipboard(content);
    }

    public boolean setInputs(Float yaw, boolean relYaw, Float pitch, boolean relPitch, int pressedInputs, int releasedInputs, int L, int R) {
        if (!io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.isSingleplayer()) return false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;
        Options op = Minecraft.getInstance().options;

        float prevYaw = player.getXRot();
        float prevPitch = player.getYRot();

        if (yaw != null) {
            player.setXRot(relYaw ? (player.getXRot() + yaw) : yaw);
            player.xRotO += player.getXRot() - prevYaw;
        }
        if (pitch != null) {
            player.setYRot(relPitch ? (player.getYRot() + pitch) : pitch);
            player.setYRot(Mth.clamp(player.getYRot(), -90.0F, 90.0F));

            player.yRotO += player.getYRot() - prevPitch;
            player.yRotO = Mth.clamp(player.yRotO, -90.0F, 90.0F);
        }

        if (player.getVehicle() != null) {
            player.getVehicle().onPassengerTurned(player);
        }

        InputConstants.Key[] keys = new InputConstants.Key[]{
                op.keyUp.getKey(),
                op.keyLeft.getKey(),
                op.keyDown.getKey(),
                op.keyRight.getKey(),
                op.keySprint.getKey(),
                op.keyShift.getKey(),
                op.keyJump.getKey()
        };

        for (int i = 0; i < keys.length; i++) {
            if ((releasedInputs & 1 << i) != 0) {
                KeyMapping.set(keys[i], false);
            }
            if ((pressedInputs & 1 << i) != 0) {
                KeyMapping.set(keys[i], true);
                KeyMapping.click(keys[i]);
            }
        }

        KeyMapping.set(op.keyAttack.getKey(), L > 0);
        for (int i = 0; i < L; i++)
            KeyMapping.click(op.keyAttack.getKey());

        KeyMapping.set(op.keyUse.getKey(), R > 0);
        for (int i = 0; i < R; i++)
            KeyMapping.click(op.keyUse.getKey());

        return true;
    }

    public boolean isF3Enabled() {
        return Minecraft.getInstance().options.renderDebug;
    }

    /**
     * Is called in {@link Keyboard.Interface Keyboard.Interface}
     */
    public List<Integer> getPressedButtons() {
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

