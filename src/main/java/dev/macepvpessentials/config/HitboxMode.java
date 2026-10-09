package dev.macepvpessentials.config;

/** What the Hitboxes setting does. */
public enum HitboxMode {
    /** No hitboxes. */
    OFF("Off"),
    /** Hitboxes only on players flying with an elytra, drawn by this mod. */
    ELYTRA_ONLY("Elytra only"),
    /** Hitboxes on everything: Minecraft's own Entity Hitboxes debug option (F3+F6) turned on. */
    ON("On");

    private final String label;

    HitboxMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /** The next choice when the button is clicked: Off, Elytra only, On, then back to Off. */
    public HitboxMode next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
