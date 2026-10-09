package dev.macepvpessentials.mixin;

import dev.macepvpessentials.optimize.ThrowOptimizer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Pearl Optimization: a stand-in pearl just disappears when it hits something. */
@Mixin(ThrownEnderpearl.class)
public abstract class ThrownEnderpearlMixin {

    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void macepvpessentials$standInHit(HitResult hitResult, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (ThrowOptimizer.isStandIn(self)) {
            ci.cancel();
            self.discard();
        }
    }
}
