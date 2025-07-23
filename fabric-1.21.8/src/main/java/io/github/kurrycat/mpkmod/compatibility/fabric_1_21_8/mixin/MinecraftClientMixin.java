package io.github.kurrycat.mpkmod.compatibility.fabric_1_21_8.mixin;

import io.github.kurrycat.mpkmod.compatibility.fabric_1_21_8.MPKMod;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Inject(at = @At("TAIL"), method = "<init>")
	private void init(CallbackInfo info) {
		MPKMod.INSTANCE.init();
	}
}
