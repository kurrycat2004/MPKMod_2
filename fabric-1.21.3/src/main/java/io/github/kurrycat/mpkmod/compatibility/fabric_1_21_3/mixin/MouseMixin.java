package io.github.kurrycat.mpkmod.compatibility.fabric_1_21_3.mixin;

import io.github.kurrycat.mpkmod.compatibility.API;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow
    private double cursorDeltaX;
    @Shadow
    private double cursorDeltaY;
    @Shadow
    private double x;
    @Shadow
    private double y;

    @Inject(method = "onCursorPos", at = @At(value = "TAIL"))
    private void onCursorPos(long window, double x, double y, CallbackInfo ci) {
        if (window == MinecraftClient.getInstance().getWindow().getHandle()) {
            API.Events.onMouseInput(
                    io.github.kurrycat.mpkmod.util.Mouse.Button.NONE,
                    io.github.kurrycat.mpkmod.util.Mouse.State.NONE,
                    (int) x, (int) y, (int) cursorDeltaX, (int) -cursorDeltaY,
                    0, System.nanoTime()
            );
        }
    }

    @Inject(method = "onMouseScroll", at = @At(value = "TAIL"))
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        API.Events.onMouseInput(
                io.github.kurrycat.mpkmod.util.Mouse.Button.NONE,
                io.github.kurrycat.mpkmod.util.Mouse.State.NONE,
                (int) x, (int) y, 0, 0,
                (int) vertical, System.nanoTime()
        );
    }

    @Inject(method = "onMouseButton", at = @At(value = "TAIL"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        API.Events.onMouseInput(
                io.github.kurrycat.mpkmod.util.Mouse.Button.fromInt(button),
                button == -1 ? io.github.kurrycat.mpkmod.util.Mouse.State.NONE :
                        (action == 1 ? io.github.kurrycat.mpkmod.util.Mouse.State.DOWN : io.github.kurrycat.mpkmod.util.Mouse.State.UP),
                (int) x, (int) y, 0, 0,
                0, System.nanoTime()
        );
    }
}
