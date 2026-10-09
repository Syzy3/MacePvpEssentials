package dev.macepvpessentials.mixin;

import dev.macepvpessentials.optimize.MaceOptimizer;
import dev.macepvpessentials.optimize.PearlOptimizer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Mace and Pearl Optimization: see where your attacks and item uses start. */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    @Unique
    private ItemStack macepvpessentials$pearl = ItemStack.EMPTY;

    @Inject(method = "attack", at = @At("HEAD"))
    private void macepvpessentials$attack(Player player, Entity target, CallbackInfo ci) {
        MaceOptimizer.onAttack(player, target);
    }

    /** Remember a pearl that can be thrown right now (not on cooldown). */
    @Inject(method = "useItem", at = @At("HEAD"))
    private void macepvpessentials$beforeUse(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack held = player.getItemInHand(hand);
        macepvpessentials$pearl = held.is(Items.ENDER_PEARL) && !player.getCooldowns().isOnCooldown(held)
                ? held.copyWithCount(1) : ItemStack.EMPTY;
    }

    @Inject(method = "useItem", at = @At("RETURN"))
    private void macepvpessentials$afterUse(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!macepvpessentials$pearl.isEmpty() && cir.getReturnValue().consumesAction()) {
            PearlOptimizer.onThrow(player, macepvpessentials$pearl);
        }
        macepvpessentials$pearl = ItemStack.EMPTY;
    }
}
