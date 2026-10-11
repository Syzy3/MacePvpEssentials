package dev.macepvpessentials;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/**
 * Key bindings. They appear in Options &gt; Controls &gt; Key Binds under their own category and are
 * rebindable there; nothing here hard-codes a key at use time.
 *
 * <p>Note the 26.x API shape: categories are registered objects keyed by an {@link Identifier}
 * rather than free-form strings, and the helper is {@code KeyMappingHelper} in the
 * {@code keymapping.v1} package.
 */
public final class MaceKeys {

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MacePvpEssentials.MOD_ID, "main"));

    /** Unbound by default so it can never take a key a PvP player already uses. */
    public static final KeyMapping OPEN_SETTINGS = register("open_settings", -1 /* unbound */);

    private MaceKeys() {
    }

    private static KeyMapping register(String name, int defaultKey) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key." + MacePvpEssentials.MOD_ID + "." + name,
                InputConstants.Type.KEYSYM,
                defaultKey,
                CATEGORY));
    }

    /** Touch the class so the static initialisers run during client init. */
    public static void init() {
    }
}
