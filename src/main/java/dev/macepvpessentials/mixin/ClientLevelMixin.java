package dev.macepvpessentials.mixin;

import dev.macepvpessentials.optimize.PearlOptimizer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Pearl Optimization: the server's real pearl arriving replaces the stand-in. */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @Inject(method = "addEntity", at = @At("TAIL"))
    private void macepvpessentials$entityAdded(Entity entity, CallbackInfo ci) {
        PearlOptimizer.onEntityAdded(entity);
    }
}
