package dev.swbf2c.profile;

public final class ProfileFormat {
    private ProfileFormat() {}

    public static final int EXPECTED_FILE_SIZE = 4356;

    public static final int MEDALS_OFFSET = 0x57C;
    public static final int MEDAL_COUNT = 9;
    public static final int MEDAL_SIZE_BYTES = 2;

    public static final int STATS_OFFSET = 0xF8C;
    public static final int STAT_COUNT = 3;
    public static final int STAT_SIZE_BYTES = 4;

    public static final String[] MEDAL_NAMES = {
            "Gunslinger",
            "Frenzy",
            "Demolition",
            "Technician",
            "Marksman",
            "Regulator",
            "Endurance",
            "Guardian",
            "War Hero"
    };

    public static final String[] STAT_NAMES = {
            "Player Points",
            "Kills",
            "Deaths"
    };
}