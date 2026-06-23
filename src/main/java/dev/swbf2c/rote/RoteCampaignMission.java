package dev.swbf2c.rote;

import java.util.List;
import java.util.Objects;

public final class RoteCampaignMission {
    private static final List<RoteCampaignMission> KNOWN_MISSIONS = List.of(
            new RoteCampaignMission(1, "Mygeeto - Amongst the Ruins", true),
            new RoteCampaignMission(2, "Coruscant Space - A Desperate Rescue", true),
            new RoteCampaignMission(3, "Felucia - Heart of Darkness", true),
            new RoteCampaignMission(4, "Kashyyyk Space - First Line of Defense", true),
            new RoteCampaignMission(5, "Kashyyyk - A Line in the Sand", true),
            new RoteCampaignMission(6, "Utapau - Underground Ambush", true),
            new RoteCampaignMission(7, "Coruscant - Knightfall", true),
            new RoteCampaignMission(8, "Naboo - Imperial Diplomacy", true),
            new RoteCampaignMission(9, "Mustafar Space - Preventive Measures", true),
            new RoteCampaignMission(10, "Mustafar - Tying Up Loose Ends", true),
            new RoteCampaignMission(11, "Kamino - Changing of the Guard", true),
            new RoteCampaignMission(12, "Death Star - Prison Break", true),
            new RoteCampaignMission(13, "Polis Massa - Birth of the Rebellion", true),
            new RoteCampaignMission(14, "Tantive IV - Recovering the Plans", true),
            new RoteCampaignMission(15, "Yavin IV Space - Vader's Fist Strikes Back", true),
            new RoteCampaignMission(16, "Yavin IV - Revenge of the Empire", true),
            new RoteCampaignMission(17, "Hoth - Our Finest Hour", true)
    );

    private final int routeState;
    private final String label;
    private final boolean known;

    private RoteCampaignMission(int routeState, String label, boolean known) {
        this.routeState = routeState;
        this.label = label;
        this.known = known;
    }

    public static List<RoteCampaignMission> knownMissions() {
        return KNOWN_MISSIONS;
    }

    public static RoteCampaignMission fromRouteState(int routeState) {
        for (RoteCampaignMission mission : KNOWN_MISSIONS) {
            if (mission.routeState == routeState) {
                return mission;
            }
        }

        return new RoteCampaignMission(
                routeState,
                "Unknown campaign mission",
                false
        );
    }

    public int routeState() {
        return routeState;
    }

    public boolean known() {
        return known;
    }

    @Override
    public String toString() {
        return routeState + " - " + label;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof RoteCampaignMission other)) {
            return false;
        }

        return routeState == other.routeState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(routeState);
    }
}