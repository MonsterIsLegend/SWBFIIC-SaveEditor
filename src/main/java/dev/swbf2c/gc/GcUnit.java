package dev.swbf2c.gc;

public enum GcUnit {
    SOLDIER("soldier", "Soldier"),
    ASSAULT("assault", "Assault / Rocket Unit"),
    ENGINEER("engineer", "Engineer"),
    SNIPER("sniper", "Sniper"),
    SPECIAL("special", "Special Unit"),
    OFFICER("officer", "Officer / Commander"),
    MARINE("marine", "Marine"),
    PILOT("pilot", "Pilot");

    private final String code;
    private final String displayName;

    GcUnit(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static GcUnit fromCode(String code) {
        for (GcUnit unit : values()) {
            if (unit.code.equals(code)) {
                return unit;
            }
        }

        throw new IllegalArgumentException("Unknown Galactic Conquest unit code: " + code);
    }

    public String code() {
        return code;
    }

    @Override
    public String toString() {
        return displayName;
    }
}