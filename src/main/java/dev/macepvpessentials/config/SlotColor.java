package dev.macepvpessentials.config;

public enum SlotColor {
    RED("Red", 16726843),
    LIGHT_BLUE("Light Blue", 5623039),
    DARK_BLUE("Dark Blue", 2837472),
    GREEN("Green", 4513092),
    PURPLE("Purple", 10502399);

    private static final int ALPHA = 96;
    private final String label;
    private final int rgb;

    private SlotColor(String label, int rgb) {
        this.label = label;
        this.rgb = rgb;
    }

    public String label() {
        return this.label;
    }

    public int rgb() {
        return this.rgb;
    }

    public int slotColor() {
        return 1610612736 | this.rgb;
    }

    public SlotColor next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
