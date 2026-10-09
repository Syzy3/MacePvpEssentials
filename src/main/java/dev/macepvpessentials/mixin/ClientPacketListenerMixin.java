package dev.macepvpessentials.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.macepvpessentials.ShieldStatus;
import dev.macepvpessentials.optimize.MaceOptimizer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hears shield "disabled" sounds so other players' disabled shields can be shown red, and skips the
 * server's late copy of a mace bounce that Mace Optimization already applied.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    // TAIL: only reached on the game thread (the network thread hands the packet over first).
    @Inject(method = "handleSoundEvent", at = @At("TAIL"))
    private void macepvpessentials$shieldSound(ClientboundSoundPacket packet, CallbackInfo ci) {
        ShieldStatus.onSound(packet.getSound(), packet.getX(), packet.getY(), packet.getZ());
    }

    @WrapOperation(method = "handleSetEntityMotion", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;lerpMotion(Lnet/minecraft/world/phys/Vec3;)V"))
    private void macepvpessentials$maceBounce(Entity entity, Vec3 movement, Operation<Void> original) {
        if (!MaceOptimizer.skipServerCopy(entity, movement)) {
            original.call(entity, movement);
        }
    }
}
