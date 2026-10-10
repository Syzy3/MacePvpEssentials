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

    /** Damage (in health points) a smash would deal right now, or -1 when you can't smash. */
    public static double damage(LocalPlayer p) {
        ItemStack held = p.getMainHandItem();
        if (!held.is(Items.MACE) || !MaceItem.canSmashAttack(p)) {
            return -1;
        }
        double fall = p.fallDistance;
        double smash = fall <= 3.0 ? 4.0 * fall : fall <= 8.0 ? 12.0 + 2.0 * (fall - 3.0) : 22.0 + fall - 8.0;
        int density = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> e : held.getEnchantments().entrySet()) {
            if (e.getKey().is(Enchantments.DENSITY)) {
                density = e.getIntValue();
            }
        }
        // Density: +0.5 damage per level for every block fallen.
        smash += 0.5 * density * fall;

        float charge = p.getAttackStrengthScale(0.5F);
        double base = p.getAttributeValue(Attributes.ATTACK_DAMAGE) * (0.2F + charge * charge * 0.8F);
        boolean crit = charge > 0.9F && !p.onGround() && !p.onClimbable() && !p.isInWater()
                && !p.hasEffect(MobEffects.BLINDNESS) && !p.isPassenger() && !p.isSprinting();
        return (base + smash) * (crit ? 1.5 : 1.0);
    }

    /** Draws the number next to the crosshair at (cx, cy). */
    public static void draw(GuiGraphicsExtractor g, int cx, int cy) {
        if (!Config.on() || !Config.get().smashMeter) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        double dmg = damage(mc.player);
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
