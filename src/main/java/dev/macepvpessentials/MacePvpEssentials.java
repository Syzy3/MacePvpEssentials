package dev.macepvpessentials;

import dev.macepvpessentials.config.Config;
import dev.macepvpessentials.kinetic.KineticButton;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Client entry point. Client-only: nothing here talks to the server. */
public final class MacePvpEssentials implements ClientModInitializer {

    public static final String MOD_ID = "mace-pvp-essentials";
    public static final Logger LOGGER = LoggerFactory.getLogger("Mace PvP Essentials");

    @Override
    public void onInitializeClient() {
        Config.load();
        KineticButton.register();
        dev.macepvpessentials.optimize.ThrowOptimizer.register();
        LOGGER.info("Mace PvP Essentials ready");
    }
}
