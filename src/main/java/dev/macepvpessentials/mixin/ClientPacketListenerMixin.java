package dev.macepvpessentials.mixin;

import dev.macepvpessentials.ShieldStatus;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hears shield "disabled" sounds so other players' disabled shields can be shown red. */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    // TAIL: only reached on the game thread (the network thread hands the packet over first).
    @Inject(method = "handleSoundEvent", at = @At("TAIL"))
    private void macepvpessentials$shieldSound(ClientboundSoundPacket packet, CallbackInfo ci) {
        ShieldStatus.onSound(packet.getSound(), packet.getX(), packet.getY(), packet.getZ());
    }
}
