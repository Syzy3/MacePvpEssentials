package dev.macepvpessentials;

import dev.macepvpessentials.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ParticleStatus;

/**
 * The Particles setting: All, Decreased and Minimal are Minecraft's own Particles option
 * (Video Settings), so both menus always agree. None is extra: Minecraft is kept on Minimal and
 * this mod stops every particle from being added.
 */
public final class Particles {

    public enum Level {
        ALL("All"), DECREASED("Decreased"), MINIMAL("Minimal"), NONE("None");

        private final String label;

        Level(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public Level next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private Particles() {
    }

    /** The current level, read from Minecraft's option (None only while that is still Minimal). */
    public static Level current() {
        ParticleStatus status = Minecraft.getInstance().options.particles().get();
        if (Config.get().particlesNone) {
            if (status == ParticleStatus.MINIMAL) {
                return Level.NONE;
            }
            // Changed in Minecraft's own settings since: follow that.
            Config.get().particlesNone = false;
            Config.save();
        }
        return switch (status) {
            case ALL -> Level.ALL;
            case DECREASED -> Level.DECREASED;
            case MINIMAL -> Level.MINIMAL;
        };
    }

    public static void set(Level level) {
        ParticleStatus status = switch (level) {
            case ALL -> ParticleStatus.ALL;
            case DECREASED -> ParticleStatus.DECREASED;
            case MINIMAL, NONE -> ParticleStatus.MINIMAL;
        };
        Minecraft.getInstance().options.particles().set(status);
        Minecraft.getInstance().options.save();
        Config.get().particlesNone = level == Level.NONE;
        Config.save();
    }

    /** True when no particles should be shown at all. */
    public static boolean none() {
        return Config.on() && Config.get().particlesNone;
    }
}
