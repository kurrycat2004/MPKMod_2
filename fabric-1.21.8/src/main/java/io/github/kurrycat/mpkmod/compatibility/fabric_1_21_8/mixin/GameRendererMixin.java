package io.github.kurrycat.mpkmod.compatibility.fabric_1_21_8.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.kurrycat.mpkmod.compatibility.fabric_1_21_8.MPKMod;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRenderer.class)
public class GameRendererMixin {
    @Inject(at = @At(value = "INVOKE_STRING",
            target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
            args = "ldc=hand"),
            method = "renderWorld")
    public void render(RenderTickCounter tickCounter, CallbackInfo ci) {
        GameRenderer gameRenderer = (GameRenderer) (Object) this;
        Camera camera = gameRenderer.getCamera();
        Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushMatrix();
        matrixStack.rotateXYZ(camera.getPitch() * ((float) Math.PI / 180), camera.getYaw() * ((float) Math.PI / 180) + (float) Math.PI, 0.0f);

        MPKMod.INSTANCE.eventHandler.onRenderWorldOverlay(new MatrixStack(), tickCounter.getTickProgress(false));

        matrixStack.popMatrix();
    }
}
