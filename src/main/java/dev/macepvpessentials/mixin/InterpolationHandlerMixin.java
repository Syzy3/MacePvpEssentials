package dev.macepvpessentials.mixin;

import dev.macepvpessentials.config.Config;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Opponent Delay: other players are smoothed over fewer ticks (or none), so they show closer to
 * where they really are. Only changes what you see; nothing is sent to the server.
 */
@Mixin(InterpolationHandler.class)
public abstract class InterpolationHandlerMixin {

    @Shadow
    @Final
    private Entity entity;

    @Shadow
    private int interpolationSteps;

    @Inject(method = "interpolateTo", at = @At("HEAD"))
    private void macepvpessentials$opponentDelay(Vec3 position, float yRot, float xRot, CallbackInfo ci) {
        if (entity instanceof RemotePlayer) {
            interpolationSteps = Config.on() ? Config.get().opponentDelay.steps() : InterpolationHandler.DEFAULT_INTERPOLATION_STEPS;
        }
    }
}
