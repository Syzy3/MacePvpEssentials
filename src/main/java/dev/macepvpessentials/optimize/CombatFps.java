package dev.macepvpessentials.optimize;

import dev.macepvpessentials.config.Config;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.PrioritizeChunkUpdates;

/**
 * Combat FPS. While it's on, Minecraft reads lighter values for some of its own video settings
 * (Fast graphics, lower render distance, no V-Sync, no Smooth Lighting, no Biome Blend, ...).
 * Your real settings are never changed: they stay in options.txt exactly as you set them, so
 * turning Combat FPS off, switching the mod off, or removing the mod brings them straight back.
 */
public final class CombatFps {

    /** True while Minecraft saves options.txt, so your real values are what get saved. */
    private static volatile boolean saving;
    private static boolean wasActive;

    private CombatFps() {
    }

    public static void register() {
        // When Combat FPS turns on or off (button, mod switch, presets...), redraw the world and
        // re-apply V-Sync so the change shows right away.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean active = active();
            if (active != wasActive) {
                wasActive = active;
                if (client.levelExtractor != null) {
                    client.levelExtractor.allChanged();
                }
                client.invalidateSurfaceConfiguration();
            }
        });
    }

    public static void setSaving(boolean value) {
        saving = value;
    }

    public static boolean active() {
        return !saving && Config.on() && Config.get().combatFps;
    }

    /**
     * The value Minecraft should use for {@code option} while Combat FPS is on, or null to use
     * your own. {@code real} is your own value.
     */
    public static Object override(OptionInstance<?> option, Object real) {
        Minecraft mc = Minecraft.getInstance();
        Options o = mc == null ? null : mc.options;
        if (o == null) {
            return null;
        }
        if (option == o.renderDistance()) {
            // Turned down, but not too much: about a quarter less (at least 2 chunks less).
            int rd = (Integer) real;
            return Math.max(Math.min(rd, 4), rd - Math.max(2, rd / 4));
        }
        if (option == o.biomeBlendRadius()) {
            return 0;
        }
        if (option == o.ambientOcclusion() || option == o.enableVsync() || option == o.cutoutLeaves()
                || option == o.improvedTransparency() || option == o.entityShadows()) {
            return false;
        }
        if (option == o.cloudStatus()) {
            return CloudStatus.OFF;
        }
        if (option == o.prioritizeChunkUpdates()) {
            return PrioritizeChunkUpdates.NONE;
        }
        if (option == o.weatherRadius()) {
            return Math.min((Integer) real, 5);
        }
        if (option == o.entityDistanceScaling()) {
            return Math.min((Double) real, 0.75);
        }
        return null;
    }
}
