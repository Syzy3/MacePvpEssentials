package dev.macepvpessentials.mixin;

import dev.macepvpessentials.config.Config;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Combat FPS: no clouds (your Clouds setting itself isn't changed). */
@Mixin(Options.class)
public abstract class CombatFpsOptionsMixin {

    @Inject(method = "getCloudStatus", at = @At("HEAD"), cancellable = true)
    private void macepvpessentials$noClouds(CallbackInfoReturnable<CloudStatus> cir) {
        if (Config.on() && Config.get().combatFps) {
            cir.setReturnValue(CloudStatus.OFF);
        }
    }
}
