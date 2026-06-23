package dev.swbf2c.gc;

import java.util.List;

public final class GcMapDefinition {
    private final GcEra era;
    private final List<GcNode> nodes;

    private GcMapDefinition(GcEra era, List<GcNode> nodes) {
        this.era = era;
        this.nodes = nodes;
    }

    public static GcMapDefinition fromFaction(GcFaction faction) {
        return switch (faction) {
            case REPUBLIC, CIS -> cloneWars();
            case EMPIRE, REBELLION -> galacticCivilWar();
            case UNKNOWN -> unknown();
        };
    }

    public static GcMapDefinition cloneWars() {
        return new GcMapDefinition(
                GcEra.CLONE_WARS,
                List.of(
                        planet("cor", "Coruscant"),
                        space("star20"),
                        planet("myg", "Mygeeto"),
                        space("star17"),
                        planet("dag", "Dagobah"),
                        space("star05"),
                        space("star06"),
                        planet("nab", "Naboo"),
                        planet("fel", "Felucia"),
                        space("star13"),
                        planet("yav", "Yavin IV"),
                        planet("geo", "Geonosis"),
                        space("star07"),
                        planet("tat", "Tatooine"),
                        planet("kas", "Kashyyyk"),
                        space("star12"),
                        space("star15"),
                        planet("kam", "Kamino"),
                        planet("mus", "Mustafar"),
                        space("star02"),
                        space("star04"),
                        planet("pol", "Polis Massa"),
                        planet("uta", "Utapau")
                )
        );
    }

    public static GcMapDefinition galacticCivilWar() {
        return new GcMapDefinition(
                GcEra.GALACTIC_CIVIL_WAR,
                List.of(
                        planet("cor", "Coruscant"),
                        space("star20"),
                        space("star18"),
                        space("star17"),
                        planet("dag", "Dagobah"),
                        space("star05"),
                        space("star06"),
                        planet("nab", "Naboo"),
                        planet("end", "Endor"),
                        planet("fel", "Felucia"),
                        space("star13"),
                        planet("yav", "Yavin IV"),
                        planet("hot", "Hoth"),
                        space("star02"),
                        space("star03"),
                        planet("kas", "Kashyyyk"),
                        space("star12"),
                        space("star15"),
                        new GcNode("kam_star", "Kamino Space Node", false),
                        planet("tat", "Tatooine"),
                        planet("mus", "Mustafar"),
                        space("star04"),
                        planet("myg", "Mygeeto"),
                        space("star07"),
                        planet("pol", "Polis Massa"),
                        space("star10"),
                        planet("uta", "Utapau")
                )
        );
    }

    public static GcMapDefinition unknown() {
        return new GcMapDefinition(GcEra.UNKNOWN, List.of());
    }

    public GcEra era() {
        return era;
    }

    public List<GcNode> nodes() {
        return nodes;
    }

    public List<GcNode> planets() {
        return nodes.stream()
                .filter(GcNode::planet)
                .toList();
    }

    public List<GcNode> spaceNodes() {
        return nodes.stream()
                .filter(node -> !node.planet())
                .toList();
    }

    public GcNode findNode(String code) {
        for (GcNode node : nodes) {
            if (node.code().equals(code)) {
                return node;
            }
        }

        return new GcNode(code, code, false);
    }

    private static GcNode planet(String code, String displayName) {
        return new GcNode(code, displayName, true);
    }

    private static GcNode space(String code) {
        return new GcNode(code, "Space Node " + code, false);
    }
}