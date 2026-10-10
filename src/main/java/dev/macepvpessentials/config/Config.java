package dev.macepvpessentials.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.macepvpessentials.MacePvpEssentials;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Settings, saved as config/mace-pvp-essentials.json. */
public final class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("mace-pvp-essentials.json");

    private static Config current = new Config();

    /** The whole mod on/off (the switch under the title). Off = everything back to normal. */
    public boolean enabled = true;
    public HitboxMode hitboxes = HitboxMode.OFF;
    public SlotColor breachColor = SlotColor.LIGHT_BLUE;
    public SlotColor densityColor = SlotColor.DARK_BLUE;
    public boolean shieldStatus = true;
    /** Particles: None (Minecraft's option stays on Minimal). */
    public boolean particlesNone = false;
    /** Offhand Size, in percent (first person). */
    public int offhandShieldSize = 100;
    public int offhandTotemSize = 100;
    /** Totem pop animation size, in percent. */
    public int totemPopSize = 100;
    /** Game Optimization (both off by default). */
    public boolean maceOptimization = false;
    public boolean pearlOptimization = false;
    public boolean windChargeOptimization = false;
    public boolean rocketOptimization = false;
    public OpponentDelay opponentDelay = OpponentDelay.VANILLA;
    /** Colored background behind Totems of Undying (off by default). */
    public boolean totemBackground = false;
    public SlotColor totemColor = SlotColor.GREEN;
    /** True when the totem uses the color picked with right-click. */
    public boolean totemCustom = false;
    public int totemCustomRgb = 0xFFD24A;
    public int totemOpacity = 100;
    /** Custom colors picked with right-click on the Breach / Density buttons. */
    public boolean breachCustom = false;
    public int breachCustomRgb = 0x55CCFF;
    public int breachOpacity = 100;
    public boolean densityCustom = false;
    public int densityCustomRgb = 0x2B4BE0;
    public int densityOpacity = 100;
    public dev.macepvpessentials.crosshair.CrosshairSettings crosshair = new dev.macepvpessentials.crosshair.CrosshairSettings();

    /** The totem background as a slot fill (0 when off). */
    public int totemSlotColor() {
        if (!totemBackground) {
            return 0;
        }
        return withOpacity(totemRgb(), totemOpacity);
    }

    /** The totem background color without transparency, for labels. */
    public int totemRgb() {
        return totemCustom ? totemCustomRgb & 0xFFFFFF : totemColor.rgb();
    }

    public int breachSlotColor() {
        return withOpacity(breachCustom ? breachCustomRgb : breachColor.rgb(), breachOpacity);
    }

    public int densitySlotColor() {
        return withOpacity(densityCustom ? densityCustomRgb : densityColor.rgb(), densityOpacity);
    }

    /** {@code rgb} as a slot fill at {@code opacity} percent. */
    public static int withOpacity(int rgb, int opacity) {
        int alpha = Math.round(Math.max(0, Math.min(100, opacity)) / 100.0F * 255.0F);
        return alpha << 24 | (rgb & 0xFFFFFF);
    }

    /** True while the mod is switched on. */
    public static boolean on() {
        return current.enabled;
    }

    /** A copy of every setting, to go back to (Cancel in the settings screen). */
    public static String snapshot() {
        return GSON.toJson(current);
    }

    /** Puts every setting back to a {@link #snapshot()} and saves it. */
    public static void restore(String snapshot) {
        Config restored = GSON.fromJson(snapshot, Config.class);
        if (restored != null) {
            current = restored;
            save();
        }
    }

    public static Config get() {
        return current;
    }

    public static void load() {
        if (!Files.exists(FILE)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(FILE)) {
            Config loaded = GSON.fromJson(reader, Config.class);
            if (loaded != null) {
                // Unknown or missing values (an older or hand-edited file) fall back to the default.
                if (loaded.hitboxes == null) {
                    loaded.hitboxes = HitboxMode.OFF;
                }
                if (loaded.breachColor == null) {
                    loaded.breachColor = SlotColor.LIGHT_BLUE;
                }
                if (loaded.densityColor == null) {
                    loaded.densityColor = SlotColor.DARK_BLUE;
                }
                if (loaded.totemColor == null) {
                    loaded.totemColor = SlotColor.GREEN;
                }
                loaded.offhandShieldSize = Math.max(10, Math.min(150, loaded.offhandShieldSize == 0 ? 100 : loaded.offhandShieldSize));
                loaded.offhandTotemSize = Math.max(10, Math.min(150, loaded.offhandTotemSize == 0 ? 100 : loaded.offhandTotemSize));
                loaded.totemPopSize = Math.max(10, Math.min(150, loaded.totemPopSize == 0 ? 100 : loaded.totemPopSize));
                if (loaded.opponentDelay == null) {
                    loaded.opponentDelay = OpponentDelay.VANILLA;
                }
                if (loaded.crosshair == null) {
                    loaded.crosshair = new dev.macepvpessentials.crosshair.CrosshairSettings();
                }
                loaded.crosshair.fix();
                current = loaded;
            }
        } catch (IOException | RuntimeException e) {
            MacePvpEssentials.LOGGER.warn("Could not read {}, using defaults", FILE, e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(current, writer);
            }
        } catch (IOException e) {
            MacePvpEssentials.LOGGER.warn("Could not save {}", FILE, e);
        }
    }
}
