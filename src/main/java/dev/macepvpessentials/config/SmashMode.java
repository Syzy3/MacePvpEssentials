package dev.macepvpessentials.config;

/** When the Smash Damage number shows. */
public enum SmashMode {
    OFF("Off"),
    HOLDING("With Mace"),
    /** Also before you switch to your mace (uses the best mace in your hotbar). */
    ALWAYS("Always");

    private final String label;

    SmashMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public SmashMode next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
