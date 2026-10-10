package dev.macepvpessentials.config;

/** Where the Smash Damage number shows, next to the crosshair. */
public enum SmashPosition {
    BELOW("Below"), ABOVE("Above"), LEFT("Left"), RIGHT("Right");

    private final String label;

    SmashPosition(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public SmashPosition next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
