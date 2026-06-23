package dev.swbf2c.gc;

public record GcNode(
        String code,
        String displayName,
        boolean planet
) {
    @Override
    public String toString() {
        return displayName;
    }
}