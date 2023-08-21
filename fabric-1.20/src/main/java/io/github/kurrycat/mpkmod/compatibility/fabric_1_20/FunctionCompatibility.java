package io.github.kurrycat.mpkmod.compatibility.fabric_1_20;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.*;
import io.github.kurrycat.mpkmod.compatibility.fabric_1_20.mixin.KeyBindingAccessor;
import io.github.kurrycat.mpkmod.gui.MPKGuiScreen;
import io.github.kurrycat.mpkmod.ticks.TickInput;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.Debug;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
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
import net.minecraft.util.math.MathHelper;
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
    public DrawContext drawContext = null;

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

    public Vector3D getLookingAt(double distance) {
        if (MinecraftClient.getInstance().getCameraEntity() == null)
            return null;

        HitResult hitResult = MinecraftClient.getInstance().getCameraEntity().raycast(distance, 0, false);
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

        Matrix4f posMat = MPKMod.INSTANCE.matrixStack.peek().getPositionMatrix();

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
    public void drawRect(Vector2D pos, Vector2D size, Color color) {
        if (drawContext == null) return;
        //0.04 because drawString SHADOW_OFFSET is 0.03
        drawContext.getMatrices().translate(0, 0, 0.04);
        Matrix4f posMat = drawContext.getMatrices().peek().getPositionMatrix();
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

    /**
     * Is called in {@link Renderer2D.Interface}
     */
    public void drawLines(Collection<Vector2D> points, Color color) {
        if (points.size() < 2) {
            Debug.stacktrace("At least two points expected, got: " + points.size());
            return;
        }
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = color.getAlpha();

        drawContext.getMatrices().translate(0, 0, 0.04);
        Matrix4f posMat = drawContext.getMatrices().peek().getPositionMatrix();

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

    public Vector2D getScaledSize() {
        return new Vector2D(
                MinecraftClient.getInstance().getWindow().getScaledWidth(),
                MinecraftClient.getInstance().getWindow().getScaledHeight()
        );
    }

    public Vector2D getScreenSize() {
        return new Vector2D(MinecraftClient.getInstance().getWindow().getWidth(), MinecraftClient.getInstance().getWindow().getHeight());
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


    public void drawString(String text, double x, double y, Color color, double fontSize, boolean shadow) {
        if (drawContext == null) return;
        MatrixStack matrixStack = drawContext.getMatrices();
        matrixStack.translate(0, 0, 0.04);
        matrixStack.push();
        matrixStack.translate(x, y, 0);
        double scale = fontSize / MinecraftClient.getInstance().textRenderer.fontHeight;
        matrixStack.scale((float) scale, (float) scale, 1);
        drawContext.drawText(
                MinecraftClient.getInstance().textRenderer, text,
                0, 0, color.getRGB(), shadow
        );
        matrixStack.pop();
    }

    public Vector2D getStringSize(String text, double fontSize) {
        return new Vector2D(
                MinecraftClient.getInstance().textRenderer.getWidth(text) *
                        (float) (fontSize / MinecraftClient.getInstance().textRenderer.fontHeight),
                (float) fontSize
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
                        : new io.github.kurrycat.mpkmod.compatibility.fabric_1_20.MPKGuiScreen(screen));
    }

    public String getCurrentGuiScreen() {
        Screen curr = MinecraftClient.getInstance().currentScreen;

        if (curr == null)
            return null;
        else if (curr instanceof io.github.kurrycat.mpkmod.compatibility.fabric_1_20.MPKGuiScreen) {
            String id = ((io.github.kurrycat.mpkmod.compatibility.fabric_1_20.MPKGuiScreen) curr).eventReceiver.getID();
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
        if (MinecraftClient.getInstance().player == null) return null;
        return MinecraftClient.getInstance().player.getName().getString();
    }

    public void copyToClipboard(String content) {
        MinecraftClient.getInstance().keyboard.setClipboard(content);
    }

    public boolean setInputs(TickInput inputs) {
        if (!io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.isSingleplayer()) return false;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return false;
        GameOptions op = MinecraftClient.getInstance().options;

        player.setPitch(player.getPitch() + inputs.getYaw());
        player.setYaw(player.getYaw() + inputs.getPitch());
        player.setPitch(MathHelper.clamp(player.getPitch(), -90.0f, 90.0f));
        player.prevPitch += inputs.getYaw();
        player.prevYaw += inputs.getPitch();
        player.prevPitch = MathHelper.clamp(player.prevPitch, -90.0f, 90.0f);
        if (player.getVehicle() != null) {
            player.getVehicle().onPassengerLookAround(player);
        }

        KeyBinding[] keys = new KeyBinding[]{
                op.forwardKey,
                op.leftKey,
                op.backKey,
                op.rightKey,
                op.sprintKey,
                op.sneakKey,
                op.jumpKey
        };

        for (int i = 0; i < keys.length; i++) {
            KeyBinding.setKeyPressed(((KeyBindingAccessor) keys[i]).getBoundKey(), inputs.get(1 << i));
            if (inputs.get(1 << i))
                KeyBinding.onKeyPressed(((KeyBindingAccessor) keys[i]).getBoundKey());
        }

        KeyBinding.setKeyPressed(((KeyBindingAccessor) op.attackKey).getBoundKey(), inputs.getL() > 0);
        if (inputs.getL() > 0)
            for (int i = 0; i < inputs.getL(); i++)
                KeyBinding.onKeyPressed(((KeyBindingAccessor) op.attackKey).getBoundKey());

        KeyBinding.setKeyPressed(((KeyBindingAccessor) op.useKey).getBoundKey(), inputs.getR() > 0);
        if (inputs.getR() > 0)
            for (int i = 0; i < inputs.getR(); i++)
                KeyBinding.onKeyPressed(((KeyBindingAccessor) op.useKey).getBoundKey());

        return true;
    }

    public boolean isF3Enabled() {
        return MinecraftClient.getInstance().options.debugEnabled;
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
