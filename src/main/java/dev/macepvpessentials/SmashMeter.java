package dev.macepvpessentials;

import dev.macepvpessentials.config.Config;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Smash Damage meter: while you fall holding a mace, shows how much damage a smash would deal
 * right now (in hearts, before the target's armor). Same math as Minecraft: the mace's smash
 * bonus from your fall distance, Density's bonus, your attack charge and the critical hit.
 */
public final class SmashMeter {

    private SmashMeter() {
    }

    /**
     * Damage (in health points) a smash would deal right now, or -1 when you can't smash. With
     * {@code anyMace}, a mace in your hotbar counts too: that's the damage if you switched to it
     * now (switching resets your attack charge, so no critical hit and weaker base damage).
     */
    public static double damage(LocalPlayer p, boolean anyMace) {
        ItemStack held = p.getMainHandItem();
        boolean holding = held.is(Items.MACE);
        if (!holding) {
            held = anyMace ? bestHotbarMace(p) : ItemStack.EMPTY;
        }
        if (held.isEmpty() || !MaceItem.canSmashAttack(p)) {
            return -1;
        }
        double fall = p.fallDistance;
        double smash = fall <= 3.0 ? 4.0 * fall : fall <= 8.0 ? 12.0 + 2.0 * (fall - 3.0) : 22.0 + fall - 8.0;
        int density = densityLevel(held);
        // Density: +0.5 damage per level for every block fallen.
        smash += 0.5 * density * fall;

        float charge = holding ? p.getAttackStrengthScale(0.5F) : 0.0F;
        // Not holding it yet: a mace hits for 6 (your 1 + the mace's 5).
        double attack = holding ? p.getAttributeValue(Attributes.ATTACK_DAMAGE) : 6.0;
        double base = attack * (0.2F + charge * charge * 0.8F);
        boolean crit = charge > 0.9F && !p.onGround() && !p.onClimbable() && !p.isInWater()
                && !p.hasEffect(MobEffects.BLINDNESS) && !p.isPassenger() && !p.isSprinting();
        return (base + smash) * (crit ? 1.5 : 1.0);
    }

    private static int densityLevel(ItemStack stack) {
        for (Object2IntMap.Entry<Holder<Enchantment>> e : stack.getEnchantments().entrySet()) {
            if (e.getKey().is(Enchantments.DENSITY)) {
                return e.getIntValue();
            }
        }
        return 0;
    }

    /** The mace in your hotbar with the most Density (any mace if none has it), or empty. */
    private static ItemStack bestHotbarMace(LocalPlayer p) {
        ItemStack best = ItemStack.EMPTY;
        int bestDensity = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = p.getInventory().getItem(i);
            if (stack.is(Items.MACE)) {
                int d = densityLevel(stack);
                if (d > bestDensity) {
                    best = stack;
                    bestDensity = d;
                }
            }
        }
        return best;
    }

    /** Draws the number next to the crosshair at (cx, cy). */
    public static void draw(GuiGraphicsExtractor g, int cx, int cy) {
        if (!Config.on() || Config.get().smashMode == dev.macepvpessentials.config.SmashMode.OFF) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        double dmg = damage(mc.player, Config.get().smashMode == dev.macepvpessentials.config.SmashMode.ALWAYS);
        if (dmg < 0) {
            return;
        }
        double hearts = dmg / 2.0;
        String text = (hearts >= 100 ? String.valueOf(Math.round(hearts)) : String.format("%.1f", hearts)) + "❤";
        // White under 5 hearts, yellow under 10, orange under 20, then red.
        int color = hearts < 5 ? 0xFFFFFFFF : hearts < 10 ? 0xFFFFE066 : hearts < 20 ? 0xFFFFA040 : 0xFFFF5050;
        int w = mc.font.width(text);
        int x;
        int y;
        switch (Config.get().smashMeterPosition) {
            case ABOVE -> { x = cx - w / 2; y = cy - 18; }
            case LEFT -> { x = cx - 12 - w; y = cy - 4; }
            case RIGHT -> { x = cx + 12; y = cy - 4; }
            default -> { x = cx - w / 2; y = cy + 12; }
        }
        g.text(mc.font, text, x, y, color, true);
    }
}
