package dev.macepvpessentials.mixin;

import dev.macepvpessentials.optimize.CombatFps;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Combat FPS: options.txt always gets your real settings, never the Combat FPS ones. */
@Mixin(Options.class)
public abstract class OptionsSaveMixin {

    @Inject(method = "save", at = @At("HEAD"))
    private void macepvpessentials$saveStart(CallbackInfo ci) {
        CombatFps.setSaving(true);
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void macepvpessentials$saveEnd(CallbackInfo ci) {
        CombatFps.setSaving(false);
    }
}
