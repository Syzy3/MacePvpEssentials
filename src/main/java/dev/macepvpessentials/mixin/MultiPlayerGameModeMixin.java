package dev.macepvpessentials.mixin;

import dev.macepvpessentials.optimize.MaceOptimizer;
import dev.macepvpessentials.optimize.ThrowOptimizer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
    @Unique
    private ItemStack macepvpessentials$rocket = ItemStack.EMPTY;

    @Inject(method = "attack", at = @At("HEAD"))
    private void macepvpessentials$attack(Player player, Entity target, CallbackInfo ci) {
        MaceOptimizer.onAttack(player, target);
    }

    /** Remember a pearl or wind charge that can be thrown right now (not on cooldown). */
    @Inject(method = "useItem", at = @At("HEAD"))
    private void macepvpessentials$beforeUse(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack held = player.getItemInHand(hand);
        macepvpessentials$pearl = ThrowOptimizer.handles(held) && !player.getCooldowns().isOnCooldown(held)
                ? held.copyWithCount(1) : ItemStack.EMPTY;
        macepvpessentials$rocket = dev.macepvpessentials.optimize.FireworkOptimizer.handles(player, held)
                ? held.copyWithCount(1) : ItemStack.EMPTY;
    }

    @Inject(method = "useItem", at = @At("RETURN"))
    private void macepvpessentials$afterUse(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!macepvpessentials$pearl.isEmpty() && cir.getReturnValue().consumesAction()) {
            ThrowOptimizer.onThrow(player, macepvpessentials$pearl);
        }
        if (!macepvpessentials$rocket.isEmpty() && cir.getReturnValue().consumesAction()) {
            dev.macepvpessentials.optimize.FireworkOptimizer.onUse(player, macepvpessentials$rocket);
        }
        macepvpessentials$pearl = ItemStack.EMPTY;
        macepvpessentials$rocket = ItemStack.EMPTY;
    }
}
