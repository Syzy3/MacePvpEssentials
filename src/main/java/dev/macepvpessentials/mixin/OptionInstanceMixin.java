package dev.macepvpessentials.mixin;

import dev.macepvpessentials.optimize.CombatFps;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Combat FPS: Minecraft reads the lighter value; your own value stays stored untouched. */
@Mixin(OptionInstance.class)
public abstract class OptionInstanceMixin<T> {

    @Shadow
    private T value;

    @SuppressWarnings("unchecked")
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void macepvpessentials$combatFps(CallbackInfoReturnable<T> cir) {
        if (CombatFps.active()) {
            Object override = CombatFps.override((OptionInstance<?>) (Object) this, value);
            if (override != null) {
                cir.setReturnValue((T) override);
            }
        }
    }
}
