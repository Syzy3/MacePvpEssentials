package dev.macepvpessentials.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.macepvpessentials.ShieldStatus;
import dev.macepvpessentials.optimize.MaceOptimizer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
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

    @WrapOperation(method = "handleSoundEvent", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;playSeededSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V"))
    private void macepvpessentials$smashSound(ClientLevel level, Entity except, double x, double y, double z,
                                              Holder<SoundEvent> sound, SoundSource source, float volume, float pitch,
                                              long seed, Operation<Void> original) {
        if (!MaceOptimizer.skipServerSound(sound.value(), x, y, z)) {
            original.call(level, except, x, y, z, sound, source, volume, pitch, seed);
        }
    }

    @WrapOperation(method = "handleLevelEvent", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;levelEvent(ILnet/minecraft/core/BlockPos;I)V"))
    private void macepvpessentials$smashParticles(ClientLevel level, int type, BlockPos pos, int data,
                                                  Operation<Void> original) {
        if (!MaceOptimizer.skipServerSmash(type, pos)) {
            original.call(level, type, pos, data);
        }
    }
}
