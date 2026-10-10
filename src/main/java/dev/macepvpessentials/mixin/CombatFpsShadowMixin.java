package dev.macepvpessentials.mixin;

import dev.macepvpessentials.config.Config;
import net.minecraft.client.renderer.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Combat FPS: no round shadows under players and mobs. */
@Mixin(EntityRenderer.class)
public abstract class CombatFpsShadowMixin {

    @Inject(method = "extractShadow", at = @At("HEAD"), cancellable = true)
    private void macepvpessentials$noShadow(CallbackInfo ci) {
        if (Config.on() && Config.get().combatFps) {
            ci.cancel();
        }
    }
}
