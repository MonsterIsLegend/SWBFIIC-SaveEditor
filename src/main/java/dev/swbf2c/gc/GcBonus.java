package dev.swbf2c.gc;

public enum GcBonus {
    EMPTY(0, "Empty"),
    ENERGY_BOOST(1, "Energy Boost"),
    SUPPLIES(2, "Supplies"),
    GARRISON(3, "Garrison"),
    AUTO_TURRETS(4, "Auto Turrets"),
    BACTA_TANKS(5, "Bacta Tanks"),
    SABOTAGE(6, "Sabotage"),
    ENHANCED_BLASTERS(7, "Enhanced Blasters"),
    COMBAT_SHIELDING(8, "Combat Shielding"),
    LEADER(9, "Leader");

    private final int id;
    private final String displayName;

    GcBonus(int id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public static GcBonus fromId(int id) {
        for (GcBonus bonus : values()) {
            if (bonus.id == id) {
                return bonus;
            }
        }

        throw new IllegalArgumentException("Unknown Galactic Conquest bonus ID: " + id);
    }

    public int id() {
        return id;
    }

    @Override
    public String toString() {
        return displayName;
    }
}