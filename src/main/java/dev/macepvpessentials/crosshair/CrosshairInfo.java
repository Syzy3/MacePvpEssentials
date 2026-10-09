package dev.macepvpessentials.crosshair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class CrosshairInfo {
    private CrosshairInfo() {
    }

    public static float cooldown(LocalPlayer player, float partialTicks) {
        ItemStack held = player.getMainHandItem();
        if (held.getItem() == Items.MACE) {
            return player.getAttackStrengthScale(0.0F);
        } else {
            return !held.isEmpty() && player.getCooldowns().isOnCooldown(held) ? 1.0F - player.getCooldowns().getCooldownPercent(held, partialTicks) : -1.0F;
        }
    }

    public static boolean wearingElytra(LocalPlayer player) {
        return player.getItemBySlot(EquipmentSlot.CHEST).has(DataComponents.GLIDER);
    }

    public static boolean onPlayer() {
        return Minecraft.getInstance().crosshairPickEntity instanceof Player;
    }
}
