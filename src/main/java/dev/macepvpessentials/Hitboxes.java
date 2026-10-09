package dev.macepvpessentials;

import dev.macepvpessentials.config.Config;
import dev.macepvpessentials.config.HitboxMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;

/**
 * Connects the Hitboxes setting to Minecraft's own Entity Hitboxes debug option (F3+F6).
 * "On" is that option turned on; the mod draws nothing itself. "Elytra only" and "Off" turn
 * it off, and "Elytra only" is drawn by {@link dev.macepvpessentials.mixin.DebugRendererMixin}.
 */
public final class Hitboxes {

    private Hitboxes() {
    }

    public static void set(HitboxMode mode) {
        Config.get().hitboxes = mode;
        Config.save();
        DebugScreenEntryList entries = Minecraft.getInstance().debugEntries;
        if (mode == HitboxMode.ON) {
            entries.setStatus(DebugScreenEntries.ENTITY_HITBOXES, DebugScreenEntryStatus.ALWAYS_ON);
        } else if (entries.getStatus(DebugScreenEntries.ENTITY_HITBOXES) != DebugScreenEntryStatus.NEVER) {
            entries.setStatus(DebugScreenEntries.ENTITY_HITBOXES, DebugScreenEntryStatus.NEVER);
        }
    }

    /**
     * The setting as it really is right now. Minecraft's option can also be changed in F3+F6 or
     * with F3+B: if it's on, the setting is On; if it was turned off there, On becomes Off.
     */
    public static HitboxMode current() {
        boolean minecraftOn = Minecraft.getInstance().debugEntries
                .getStatus(DebugScreenEntries.ENTITY_HITBOXES) == DebugScreenEntryStatus.ALWAYS_ON;
        HitboxMode saved = Config.get().hitboxes;
        if (minecraftOn) {
            return HitboxMode.ON;
        }
        return saved == HitboxMode.ON ? HitboxMode.OFF : saved;
    }
}
