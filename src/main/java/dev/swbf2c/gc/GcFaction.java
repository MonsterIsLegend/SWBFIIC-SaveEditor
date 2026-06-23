package dev.swbf2c.gc;

public enum GcFaction {
    REPUBLIC("rep", "Republic"),
    CIS("cis", "CIS"),
    EMPIRE("imp", "Empire"),
    REBELLION("all", "Rebel Alliance"),
    UNKNOWN("", "Unknown");

    private final String code;
    private final String displayName;

    GcFaction(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static GcFaction fromCode(String code) {
        for (GcFaction faction : values()) {
            if (faction.code.equalsIgnoreCase(code)) {
                return faction;
            }
        }

        return UNKNOWN;
    }

    public String code() {
        return code;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isCloneWars() {
        return this == REPUBLIC || this == CIS;
    }

    public boolean isGalacticCivilWar() {
        return this == EMPIRE || this == REBELLION;
    }

    @Override
    public String toString() {
        return displayName;
    }
}