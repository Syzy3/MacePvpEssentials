package dev.macepvpessentials.mixin;

import dev.macepvpessentials.Particles;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Particles: None stops every particle from being added. */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void macepvpessentials$noParticles(Particle particle, CallbackInfo ci) {
        if (Particles.none()) {
            ci.cancel();
        }
    }
}
