package io.github.kurrycat.mpkmod.compatibility.fabric_1_21_8.mixin;

import io.github.kurrycat.mpkmod.compatibility.fabric_1_21_8.MPKMod;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At(value = "RETURN"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if(key != -1) {
            MPKMod.INSTANCE.eventHandler.onKey(key, scancode, action);
        }
    }
}
