package dev.macepvpessentials;

import dev.macepvpessentials.config.Config;
import dev.macepvpessentials.gui.SettingsScreen;
import dev.macepvpessentials.kinetic.KineticButton;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Client entry point. Client-only: nothing here talks to the server. */
public final class MacePvpEssentials implements ClientModInitializer {

    public static final String MOD_ID = "mace-pvp-essentials";
    public static final Logger LOGGER = LoggerFactory.getLogger("Mace PvP Essentials");

    @Override
    public void onInitializeClient() {
        Config.load();
        MaceKeys.init();
        ClientTickEvents.END_CLIENT_TICK.register(MacePvpEssentials::onClientTick);
        KineticButton.register();
        dev.macepvpessentials.optimize.ThrowOptimizer.register();
        dev.macepvpessentials.optimize.FireworkOptimizer.register();
        dev.macepvpessentials.optimize.CombatFps.register();
        LOGGER.info("Mace PvP Essentials ready");
    }

    private static void onClientTick(Minecraft mc) {
        while (MaceKeys.OPEN_SETTINGS.consumeClick()) {
            mc.gui.setScreen(new SettingsScreen(null));
        }
    }
}
