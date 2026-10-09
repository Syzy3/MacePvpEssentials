package dev.macepvpessentials.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.macepvpessentials.optimize.FireworkOptimizer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Elytra Optimization: the real rocket skips the boost ticks its stand-in already gave. */
@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {

    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void macepvpessentials$boost(LivingEntity flyer, Vec3 movement, Operation<Void> original) {
        if (!FireworkOptimizer.skipBoost((FireworkRocketEntity) (Object) this)) {
            original.call(flyer, movement);
        }
    }
}
