package dev.macepvpessentials.config;

import dev.macepvpessentials.Hitboxes;
import dev.macepvpessentials.Particles;
import dev.macepvpessentials.crosshair.CrosshairColor;
import dev.macepvpessentials.crosshair.CrosshairSettings;
import dev.macepvpessentials.crosshair.CrosshairStyle;
import net.minecraft.client.Minecraft;

/** The Presets buttons. Recommended = our picks for mace PvP. */
public final class Presets {

    private Presets() {
    }

    /** Switches every setting to the recommended ones (custom colors you picked are kept). */
    public static void applyRecommended() {
        Config c = Config.get();
        c.enabled = true;

        // Visuals
        c.shieldStatus = true;
        c.totemBackground = true;
        c.totemCustom = false;
        c.totemColor = SlotColor.RED;
        c.totemOpacity = 100;
        c.offhandShieldSize = 50;
        c.offhandTotemSize = 75;
        c.totemPopSize = 30;

        // Mace Slots
        c.breachCustom = false;
        c.breachColor = SlotColor.LIGHT_BLUE;
        c.breachOpacity = 30;
        c.densityCustom = false;
        c.densityColor = SlotColor.DARK_BLUE;
        c.densityOpacity = 30;

        // Crosshair
        CrosshairSettings s = new CrosshairSettings();
        s.custom = true;
        s.style = CrosshairStyle.CROSS;
        s.color = CrosshairColor.WHITE;
        s.opacity = 100;
        s.length = 2;
        s.thickness = 1;
        s.gap = 2;
        s.centerDot = true;
        s.outline = false;
        s.targetColor = true;
        s.target = CrosshairColor.RED;
        s.thirdPerson = false;
        s.showCooldown = true;
        s.showElytra = true;
        c.crosshair = s;

        // Game Optimization
        c.maceOptimization = true;
        c.pearlOptimization = true;
        c.windChargeOptimization = true;
        c.rocketOptimization = true;
        c.opponentDelay = OpponentDelay.VANILLA;
        Config.save();

        // Minecraft's own options: Hitboxes (Elytra only), Particles None, Damage Tilt 1%.
        Hitboxes.set(HitboxMode.ELYTRA_ONLY);
        Particles.set(Particles.Level.NONE);
        Minecraft.getInstance().options.damageTiltStrength().set(0.01);
        Minecraft.getInstance().options.save();
    }
}
