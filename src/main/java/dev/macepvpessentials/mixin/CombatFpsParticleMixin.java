package dev.macepvpessentials.mixin;

import dev.macepvpessentials.config.Config;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Combat FPS: skips explosion smoke clouds (crystals, anchors, TNT), the heaviest fight particles.
 * Totem pops are left alone.
 */
@Mixin(ParticleEngine.class)
public abstract class CombatFpsParticleMixin {

    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void macepvpessentials$explosions(ParticleOptions options, double x, double y, double z,
                                              double xa, double ya, double za, CallbackInfoReturnable<Particle> cir) {
        if (Config.on() && Config.get().combatFps
                && (options.getType() == ParticleTypes.EXPLOSION_EMITTER || options.getType() == ParticleTypes.EXPLOSION)) {
            cir.setReturnValue(null);
        }
    }
}
