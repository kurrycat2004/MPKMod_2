package io.github.kurrycat.mpkmod.compatibility.fabric_1_19_4.mixin;

import io.github.kurrycat.mpkmod.compatibility.fabric_1_19_4.MPKMod;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRenderer.class)
public class GameRendererMixin {
    @Inject(at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=hand"), method = "renderWorld")
    public void render(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo info) {
        MPKMod.INSTANCE.eventHandler.onRenderWorldOverlay(matrix, tickDelta);
    }
}
