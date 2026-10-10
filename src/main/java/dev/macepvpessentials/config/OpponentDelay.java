package dev.macepvpessentials.config;

/**
 * Opponent Delay: how many ticks other players are smoothed over when they move. Minecraft uses 3
 * ticks, which shows them a little behind where they really are. Less = closer to their real
 * position, but choppier movement.
 */
public enum OpponentDelay {
    VANILLA("Vanilla", 3),
    LOW("Low", 1),
    NONE("None", 0);

    private final String label;
    private final int steps;

    OpponentDelay(String label, int steps) {
        this.label = label;
        this.steps = steps;
    }

    public String label() {
        return label;
    }

    public int steps() {
        return steps;
    }

    public OpponentDelay next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
