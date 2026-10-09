package dev.macepvpessentials.mixin;

import dev.macepvpessentials.optimize.ThrowOptimizer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Wind Charge Optimization: a stand-in wind charge just disappears when it hits something. */
@Mixin(AbstractWindCharge.class)
public abstract class AbstractWindChargeMixin {

    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void macepvpessentials$standInHit(HitResult hitResult, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (ThrowOptimizer.isStandIn(self)) {
            ci.cancel();
            self.discard();
        }
    }
}
