package dev.macepvpessentials.mixin;

import dev.macepvpessentials.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Combat FPS: skips the heaviest fight particles. Other players' totem pops (hundreds of
 * sparkles each) and explosion clouds. Your own totem pop still shows.
 */
@Mixin(ParticleEngine.class)
public abstract class CombatFpsParticleMixin {

    @Inject(method = "createTrackingEmitter(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/particles/ParticleOptions;I)V",
            at = @At("HEAD"), cancellable = true)
    private void macepvpessentials$otherTotems(Entity entity, ParticleOptions options, int lifetime, CallbackInfo ci) {
        if (Config.on() && Config.get().combatFps && options.getType() == ParticleTypes.TOTEM_OF_UNDYING
                && entity != Minecraft.getInstance().player) {
            ci.cancel();
        }
    }

    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void macepvpessentials$explosions(ParticleOptions options, double x, double y, double z,
                                              double xa, double ya, double za, CallbackInfoReturnable<Particle> cir) {
        if (Config.on() && Config.get().combatFps
                && (options.getType() == ParticleTypes.EXPLOSION_EMITTER || options.getType() == ParticleTypes.EXPLOSION)) {
            cir.setReturnValue(null);
        }
    }
}
