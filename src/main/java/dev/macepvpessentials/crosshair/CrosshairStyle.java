package dev.macepvpessentials.crosshair;

public enum CrosshairStyle {
    CROSS("Cross"),
    T_SHAPE("T"),
    DOT("Dot"),
    CIRCLE("Circle"),
    SQUARE("Square");

    private final String label;

    private CrosshairStyle(String label) {
        this.label = label;
    }

    public String label() {
        return this.label;
    }

    public CrosshairStyle next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
