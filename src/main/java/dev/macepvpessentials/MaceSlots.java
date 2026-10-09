package dev.macepvpessentials;

import dev.macepvpessentials.config.Config;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Colored slot backgrounds for Breach and Density maces, drawn behind the item in inventory
 * screens and the hotbar. Only maces are looked at, so other items cost one comparison.
 */
public final class MaceSlots {

    private MaceSlots() {
    }

    /** Fills the 16x16 slot at (x, y) if the stack gets a color. */
    public static void drawBackground(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y) {
        int color = colorFor(stack);
        if (color != 0) {
            graphics.fill(x, y, x + 16, y + 16, color);
        }
    }

    private static int colorFor(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.getItem() == Items.MACE) {
            for (Holder<Enchantment> enchantment : stack.getEnchantments().keySet()) {
                if (enchantment.is(Enchantments.BREACH)) {
                    return Config.get().breachColor.slotColor();
                }
                if (enchantment.is(Enchantments.DENSITY)) {
                    return Config.get().densityColor.slotColor();
                }
            }
            return 0;
        }
        return 0;
    }
}
