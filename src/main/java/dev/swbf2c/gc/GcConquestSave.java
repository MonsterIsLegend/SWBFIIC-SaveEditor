package dev.swbf2c.gc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public final class GcConquestSave {
    private static final int ZLIB_OFFSET = 0x04;

    private static final int FACTION_INDEX = 2;
    private static final int PROFILE_SECTION_INDEX = 3;
    private static final int CURRENT_FLEET_SECTION_INDEX = 5;
    private static final int PLANET_OWNERS_SECTION_INDEX = 6;
    private static final int FLEETS_SECTION_INDEX = 7;
    private static final int SELECTED_FLEET_INDEX = 8;
    private static final int CREDITS_SECTION_INDEX = 9;
    private static final int UNITS_SECTION_INDEX = 10;
    private static final int BONUSES_SECTION_INDEX = 12;

    private static final int SIDE_1 = 1;
    private static final int SIDE_2 = 2;
    private static final int NEUTRAL = 0;

    private final byte[] originalContainerData;

    private byte[] decompressedData;
    private List<Node> topItems;

    public GcConquestSave(byte[] fileData) {
        if (fileData.length < ZLIB_OFFSET + 2) {
            throw new IllegalArgumentException("File is too small to be a Galactic Conquest save.");
        }

        if ((fileData[ZLIB_OFFSET] & 0xFF) != 0x78) {
            throw new IllegalArgumentException("Expected zlib data at offset 0x04.");
        }

        this.originalContainerData = fileData.clone();
        this.decompressedData = decompress(fileData);
        refreshTopItems();

        validateStructure();
    }

    public GcFaction getFaction() {
        return GcFaction.fromCode(requireString(topItems.get(FACTION_INDEX)));
    }

    public GcEra getEra() {
        return getMapDefinition().era();
    }

    public GcMapDefinition getMapDefinition() {
        return GcMapDefinition.fromFaction(getFaction());
    }

    public List<GcNode> getMapPlanets() {
        return getMapDefinition().planets();
    }

    public List<GcNode> getFleetSelectableNodes() {
        List<GcNode> nodes = new ArrayList<>(getMapDefinition().nodes());

        for (GcNode fleetNode : getAllFleetNodes()) {
            if (!containsNodeCode(nodes, fleetNode.code())) {
                nodes.add(fleetNode);
            }
        }

        return nodes;
    }

    public GcNode getMapNode(String code) {
        return getMapDefinition().findNode(code);
    }

    public String getProfileName() {
        Node profileSection = topItems.get(PROFILE_SECTION_INDEX);
        return requireString(profileSection.children().get(0));
    }

    public int getPlayerSideId() {
        Node profileSection = topItems.get(PROFILE_SECTION_INDEX);
        return Math.round(requireFloat(profileSection.children().get(1)));
    }

    public int getEnemySideId() {
        return getPlayerSideId() == SIDE_1 ? SIDE_2 : SIDE_1;
    }

    public int getPlayerCredits() {
        return getCreditsForSide(getPlayerSideId());
    }

    public void setPlayerCredits(int gameCredits) {
        setCreditsForSide(getPlayerSideId(), gameCredits);
    }

    public int getEnemyCredits() {
        return getCreditsForSide(getEnemySideId());
    }

    public void setEnemyCredits(int gameCredits) {
        setCreditsForSide(getEnemySideId(), gameCredits);
    }

    public int getCreditsForSide(int sideId) {
        validateSideId(sideId);

        Node creditsSection = topItems.get(CREDITS_SECTION_INDEX);
        Node valueNode = sideId == SIDE_1
                ? creditsSection.children().get(1)
                : creditsSection.children().get(3);

        return storedCreditsToGameCredits(readFloat(valueNode.valueOffset()));
    }

    public void setCreditsForSide(int sideId, int gameCredits) {
        validateSideId(sideId);
        validateCredits(gameCredits);

        Node creditsSection = topItems.get(CREDITS_SECTION_INDEX);
        Node valueNode = sideId == SIDE_1
                ? creditsSection.children().get(1)
                : creditsSection.children().get(3);

        writeFloat(valueNode.valueOffset(), gameCreditsToStoredCredits(gameCredits));
    }

    public GcBonus getPlayerBonusSlot(int slotIndex) {
        return getBonusSlot(getPlayerSideId(), slotIndex);
    }

    public void setPlayerBonusSlot(int slotIndex, GcBonus bonus) {
        setBonusSlot(getPlayerSideId(), slotIndex, bonus);
    }

    public GcBonus getEnemyBonusSlot(int slotIndex) {
        return getBonusSlot(getEnemySideId(), slotIndex);
    }

    public void setEnemyBonusSlot(int slotIndex, GcBonus bonus) {
        setBonusSlot(getEnemySideId(), slotIndex, bonus);
    }

    public GcBonus getBonusSlot(int sideId, int slotIndex) {
        validateSideId(sideId);
        validateSlotIndex(slotIndex);

        Node bonusList = getBonusListForSide(sideId);
        Node valueNode = bonusList.children().get((slotIndex * 2) + 1);

        int bonusId = Math.round(readFloat(valueNode.valueOffset()));

        return GcBonus.fromId(bonusId);
    }

    public void setBonusSlot(int sideId, int slotIndex, GcBonus bonus) {
        validateSideId(sideId);
        validateSlotIndex(slotIndex);

        if (bonus == null) {
            throw new IllegalArgumentException("Bonus cannot be null.");
        }

        Node bonusList = getBonusListForSide(sideId);
        Node valueNode = bonusList.children().get((slotIndex * 2) + 1);

        writeFloat(valueNode.valueOffset(), bonus.id());
    }

    public List<GcNode> getPlayerControlledPlanets() {
        return getControlledPlanetsForSide(getPlayerSideId());
    }

    public List<GcNode> getEnemyControlledPlanets() {
        return getControlledPlanetsForSide(getEnemySideId());
    }

    public List<GcNode> getControlledPlanetsForSide(int sideId) {
        validateSideId(sideId);

        List<GcNode> planets = new ArrayList<>();
        Node section = topItems.get(PLANET_OWNERS_SECTION_INDEX);

        for (int index = 0; index < section.children().size() - 1; index += 2) {
            String code = requireString(section.children().get(index));
            int owner = Math.round(requireFloat(section.children().get(index + 1)));

            if (owner == sideId) {
                planets.add(getMapNode(code));
            }
        }

        return planets;
    }

    public void setPlayerAndEnemyControlledPlanets(
            List<GcNode> playerPlanets,
            List<GcNode> enemyPlanets
    ) {
        int playerSide = getPlayerSideId();
        int enemySide = getEnemySideId();

        Set<String> playerCodes = toNodeCodeSet(playerPlanets);
        Set<String> enemyCodes = toNodeCodeSet(enemyPlanets);

        for (String code : playerCodes) {
            if (enemyCodes.contains(code)) {
                throw new IllegalArgumentException(
                        "Planet cannot be controlled by both Player and AI: "
                                + getMapNode(code)
                );
            }
        }

        Node section = topItems.get(PLANET_OWNERS_SECTION_INDEX);

        for (int index = 0; index < section.children().size() - 1; index += 2) {
            String code = requireString(section.children().get(index));
            Node ownerNode = section.children().get(index + 1);

            if (playerCodes.contains(code)) {
                writeFloat(ownerNode.valueOffset(), playerSide);
            } else if (enemyCodes.contains(code)) {
                writeFloat(ownerNode.valueOffset(), enemySide);
            } else {
                writeFloat(ownerNode.valueOffset(), NEUTRAL);
            }
        }
    }

    public List<GcNode> getPlayerFleets() {
        return getFleetsForSide(getPlayerSideId());
    }

    public List<GcNode> getEnemyFleets() {
        return getFleetsForSide(getEnemySideId());
    }

    public List<GcNode> getFleetsForSide(int sideId) {
        validateSideId(sideId);

        List<GcNode> fleets = new ArrayList<>();
        Node section = topItems.get(FLEETS_SECTION_INDEX);

        for (int index = 0; index < section.children().size() - 1; index += 2) {
            String code = requireString(section.children().get(index));
            int owner = Math.round(requireFloat(section.children().get(index + 1)));

            if (owner == sideId) {
                fleets.add(getMapNode(code));
            }
        }

        return fleets;
    }

    public void setPlayerAndEnemyFleets(
            List<GcNode> playerFleets,
            List<GcNode> enemyFleets
    ) {
        int playerSide = getPlayerSideId();
        int enemySide = getEnemySideId();

        Set<String> playerCodes = toNodeCodeSet(playerFleets);
        Set<String> enemyCodes = toNodeCodeSet(enemyFleets);

        for (String code : playerCodes) {
            if (enemyCodes.contains(code)) {
                throw new IllegalArgumentException(
                        "Fleet node cannot be used by both Player and AI: "
                                + getMapNode(code)
                );
            }
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        output.write(0x05);

        for (GcNode node : getFleetSelectableNodes()) {
            if (playerCodes.contains(node.code())) {
                writeString(output, node.code());
                writeFloat(output, playerSide);
            } else if (enemyCodes.contains(node.code())) {
                writeString(output, node.code());
                writeFloat(output, enemySide);
            }
        }

        output.write(0x00);

        replaceTopItem(FLEETS_SECTION_INDEX, output.toByteArray());
    }

    public List<GcUnit> getPlayerUnits() {
        return getUnitsForSide(getPlayerSideId());
    }

    public List<GcUnit> getEnemyUnits() {
        return getUnitsForSide(getEnemySideId());
    }

    public List<GcUnit> getUnitsForSide(int sideId) {
        validateSideId(sideId);

        List<GcUnit> units = new ArrayList<>();
        Node unitList = getUnitListForSide(sideId);

        for (Node child : unitList.children()) {
            if (child.type() != NodeType.STRING) {
                continue;
            }

            try {
                units.add(GcUnit.fromCode(requireString(child)));
            } catch (IllegalArgumentException ignored) {
                // Unknown unit code. Do not expose it in the editor yet.
            }
        }

        return units;
    }

    public void setPlayerAndEnemyUnits(
            List<GcUnit> playerUnits,
            List<GcUnit> enemyUnits
    ) {
        int playerSide = getPlayerSideId();
        int enemySide = getEnemySideId();

        List<GcUnit> side1Units = playerSide == SIDE_1 ? playerUnits : enemyUnits;
        List<GcUnit> side2Units = playerSide == SIDE_1 ? enemyUnits : playerUnits;

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        output.write(0x05);

        writeFloat(output, SIDE_1);
        writeUnitList(output, side1Units);

        writeFloat(output, SIDE_2);
        writeUnitList(output, side2Units);

        output.write(0x00);

        replaceTopItem(UNITS_SECTION_INDEX, output.toByteArray());
    }

    public String getFleetSummary() {
        int summaryIndex = findTopStringIndex("summary_fleet");

        if (summaryIndex >= 0
                && summaryIndex + 3 < topItems.size()
                && topItems.get(summaryIndex + 2).type() == NodeType.STRING
                && topItems.get(summaryIndex + 3).type() == NodeType.STRING) {
            String from = requireString(topItems.get(summaryIndex + 2));
            String to = requireString(topItems.get(summaryIndex + 3));

            return getMapNode(from) + " -> " + getMapNode(to);
        }

        Node currentFleetSection = topItems.get(CURRENT_FLEET_SECTION_INDEX);
        String playerFleet = null;
        String enemyFleet = null;

        for (int index = 0; index < currentFleetSection.children().size() - 1; index += 2) {
            int side = Math.round(requireFloat(currentFleetSection.children().get(index)));
            String node = requireString(currentFleetSection.children().get(index + 1));

            if (side == getPlayerSideId()) {
                playerFleet = node;
            } else if (side == getEnemySideId()) {
                enemyFleet = node;
            }
        }

        if (playerFleet != null && enemyFleet != null) {
            return "Player: " + getMapNode(playerFleet) + " / AI: " + getMapNode(enemyFleet);
        }

        if (playerFleet != null) {
            return getMapNode(playerFleet).toString();
        }

        String selectedFleet = getSelectedFleetCode();

        if (selectedFleet != null) {
            return getMapNode(selectedFleet).toString();
        }

        return "Unknown";
    }

    public byte[] toByteArray() {
        byte[] compressedPayload = compress(decompressedData);

        if (compressedPayload.length > originalContainerData.length - ZLIB_OFFSET) {
            throw new IllegalStateException(
                    "Compressed Galactic Conquest save is larger than the original container."
            );
        }

        byte[] rebuilt = new byte[originalContainerData.length];

        System.arraycopy(originalContainerData, 0, rebuilt, 0, ZLIB_OFFSET);
        System.arraycopy(compressedPayload, 0, rebuilt, ZLIB_OFFSET, compressedPayload.length);

        return rebuilt;
    }

    private List<GcNode> getAllFleetNodes() {
        List<GcNode> fleets = new ArrayList<>();
        Node section = topItems.get(FLEETS_SECTION_INDEX);

        for (int index = 0; index < section.children().size() - 1; index += 2) {
            String code = requireString(section.children().get(index));
            fleets.add(getMapNode(code));
        }

        return fleets;
    }

    private String getSelectedFleetCode() {
        Node selectedFleetNode = topItems.get(SELECTED_FLEET_INDEX);

        if (selectedFleetNode.type() == NodeType.STRING) {
            return requireString(selectedFleetNode);
        }

        return null;
    }

    private Node getBonusListForSide(int sideId) {
        Node section = topItems.get(BONUSES_SECTION_INDEX);

        if (sideId == SIDE_1) {
            return section.children().get(1);
        }

        return section.children().get(3);
    }

    private Node getUnitListForSide(int sideId) {
        Node section = topItems.get(UNITS_SECTION_INDEX);

        if (sideId == SIDE_1) {
            return section.children().get(1);
        }

        return section.children().get(3);
    }

    private int findTopStringIndex(String text) {
        for (int index = 0; index < topItems.size(); index++) {
            Node node = topItems.get(index);

            if (node.type() == NodeType.STRING && text.equals(node.text())) {
                return index;
            }
        }

        return -1;
    }

    private void replaceTopItem(int topIndex, byte[] replacement) {
        Node target = topItems.get(topIndex);

        byte[] rebuilt = new byte[
                decompressedData.length
                        - (target.end() - target.start())
                        + replacement.length
                ];

        System.arraycopy(decompressedData, 0, rebuilt, 0, target.start());
        System.arraycopy(replacement, 0, rebuilt, target.start(), replacement.length);
        System.arraycopy(
                decompressedData,
                target.end(),
                rebuilt,
                target.start() + replacement.length,
                decompressedData.length - target.end()
        );

        decompressedData = rebuilt;
        refreshTopItems();
    }

    private void refreshTopItems() {
        this.topItems = parseTopItems(decompressedData);
    }

    private void validateStructure() {
        if (topItems.size() <= BONUSES_SECTION_INDEX) {
            throw new IllegalArgumentException("Galactic Conquest save structure is incomplete.");
        }

        if (topItems.get(FACTION_INDEX).type() != NodeType.STRING) {
            throw new IllegalArgumentException("Could not locate Galactic Conquest faction.");
        }

        if (topItems.get(PROFILE_SECTION_INDEX).type() != NodeType.LIST) {
            throw new IllegalArgumentException("Could not locate Galactic Conquest profile section.");
        }

        if (topItems.get(PLANET_OWNERS_SECTION_INDEX).type() != NodeType.LIST) {
            throw new IllegalArgumentException("Could not locate Galactic Conquest planet ownership section.");
        }

        if (topItems.get(FLEETS_SECTION_INDEX).type() != NodeType.LIST) {
            throw new IllegalArgumentException("Could not locate Galactic Conquest fleet section.");
        }

        if (topItems.get(CREDITS_SECTION_INDEX).type() != NodeType.LIST) {
            throw new IllegalArgumentException("Could not locate Galactic Conquest credits section.");
        }

        if (topItems.get(UNITS_SECTION_INDEX).type() != NodeType.LIST) {
            throw new IllegalArgumentException("Could not locate Galactic Conquest unit section.");
        }

        if (topItems.get(BONUSES_SECTION_INDEX).type() != NodeType.LIST) {
            throw new IllegalArgumentException("Could not locate Galactic Conquest bonus section.");
        }
    }

    private static List<Node> parseTopItems(byte[] data) {
        List<Node> items = new ArrayList<>();

        int index = 0;

        while (index < data.length) {
            ParseResult result = parseNode(data, index);
            items.add(result.node());
            index = result.nextIndex();
        }

        return items;
    }

    private static ParseResult parseNode(byte[] data, int index) {
        int start = index;
        int tag = data[index] & 0xFF;
        index++;

        if (tag == 0x1A) {
            return new ParseResult(
                    new Node(NodeType.HEADER, start, index, -1, null, Float.NaN, List.of()),
                    index
            );
        }

        if (tag == 0x00) {
            return new ParseResult(
                    new Node(NodeType.END, start, index, -1, null, Float.NaN, List.of()),
                    index
            );
        }

        if (tag == 0x05) {
            List<Node> children = new ArrayList<>();

            while (index < data.length && (data[index] & 0xFF) != 0x00) {
                ParseResult child = parseNode(data, index);
                children.add(child.node());
                index = child.nextIndex();
            }

            if (index < data.length && (data[index] & 0xFF) == 0x00) {
                index++;
            }

            return new ParseResult(
                    new Node(NodeType.LIST, start, index, -1, null, Float.NaN, children),
                    index
            );
        }

        if (tag == 0x04) {
            int stringStart = index;

            while (index < data.length && data[index] != 0) {
                index++;
            }

            String text = new String(
                    data,
                    stringStart,
                    index - stringStart,
                    StandardCharsets.US_ASCII
            );

            if (index < data.length) {
                index++;
            }

            return new ParseResult(
                    new Node(NodeType.STRING, start, index, -1, text, Float.NaN, List.of()),
                    index
            );
        }

        if (tag == 0x03) {
            if (index + 4 > data.length) {
                throw new IllegalArgumentException("Malformed Galactic Conquest float token.");
            }

            int valueOffset = index;

            float value = ByteBuffer.wrap(data, index, 4)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .getFloat();

            index += 4;

            return new ParseResult(
                    new Node(NodeType.FLOAT, start, index, valueOffset, null, value, List.of()),
                    index
            );
        }

        if (tag == 0x01) {
            return new ParseResult(
                    new Node(NodeType.BOOLEAN_BYTE, start, index, -1, null, 1.0f, List.of()),
                    index
            );
        }

        return new ParseResult(
                new Node(NodeType.UNKNOWN, start, index, -1, null, tag, List.of()),
                index
        );
    }

    private static byte[] decompress(byte[] fileData) {
        try (
                ByteArrayInputStream input = new ByteArrayInputStream(
                        fileData,
                        ZLIB_OFFSET,
                        fileData.length - ZLIB_OFFSET
                );
                InflaterInputStream inflaterInput = new InflaterInputStream(input);
                ByteArrayOutputStream output = new ByteArrayOutputStream()
        ) {
            inflaterInput.transferTo(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not decompress Galactic Conquest save.", exception);
        }
    }

    private static byte[] compress(byte[] data) {
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);

        try (
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                DeflaterOutputStream deflaterOutput = new DeflaterOutputStream(output, deflater)
        ) {
            deflaterOutput.write(data);
            deflaterOutput.finish();
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not compress Galactic Conquest save.", exception);
        } finally {
            deflater.end();
        }
    }

    private float readFloat(int valueOffset) {
        return ByteBuffer.wrap(decompressedData, valueOffset, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getFloat();
    }

    private void writeFloat(int valueOffset, float value) {
        ByteBuffer.wrap(decompressedData, valueOffset, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(value);
    }

    private static void writeUnitList(
            ByteArrayOutputStream output,
            List<GcUnit> units
    ) {
        output.write(0x05);

        for (GcUnit unit : units) {
            writeString(output, unit.code());
            output.write(0x01);
            output.write(0x01);
        }

        output.write(0x00);
    }

    private static void writeString(ByteArrayOutputStream output, String text) {
        output.write(0x04);
        output.writeBytes(text.getBytes(StandardCharsets.US_ASCII));
        output.write(0x00);
    }

    private static void writeFloat(ByteArrayOutputStream output, float value) {
        output.write(0x03);

        byte[] bytes = ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(value)
                .array();

        output.writeBytes(bytes);
    }

    private static boolean containsNodeCode(List<GcNode> nodes, String code) {
        for (GcNode node : nodes) {
            if (node.code().equals(code)) {
                return true;
            }
        }

        return false;
    }

    private static Set<String> toNodeCodeSet(List<GcNode> nodes) {
        Set<String> codes = new LinkedHashSet<>();

        for (GcNode node : nodes) {
            codes.add(node.code());
        }

        return codes;
    }

    private static String requireString(Node node) {
        if (node.type() != NodeType.STRING) {
            throw new IllegalArgumentException("Expected string node.");
        }

        return node.text();
    }

    private static float requireFloat(Node node) {
        if (node.type() != NodeType.FLOAT) {
            throw new IllegalArgumentException("Expected float node.");
        }

        return node.number();
    }

    private static int storedCreditsToGameCredits(float storedCredits) {
        return Math.round(storedCredits * 10.0f);
    }

    private static float gameCreditsToStoredCredits(int gameCredits) {
        return gameCredits / 10.0f;
    }

    private static void validateCredits(int gameCredits) {
        if (gameCredits < 0) {
            throw new IllegalArgumentException("Credits cannot be negative.");
        }

        if (gameCredits > 9_999_990) {
            throw new IllegalArgumentException("Credits value is too high.");
        }
    }

    private static void validateSideId(int sideId) {
        if (sideId != SIDE_1 && sideId != SIDE_2) {
            throw new IllegalArgumentException("Side ID must be 1 or 2.");
        }
    }

    private static void validateSlotIndex(int slotIndex) {
        if (slotIndex < 0 || slotIndex > 2) {
            throw new IllegalArgumentException("Bonus slot index must be between 0 and 2.");
        }
    }

    private enum NodeType {
        HEADER,
        END,
        LIST,
        STRING,
        FLOAT,
        BOOLEAN_BYTE,
        UNKNOWN
    }

    private record Node(
            NodeType type,
            int start,
            int end,
            int valueOffset,
            String text,
            float number,
            List<Node> children
    ) {}

    private record ParseResult(
            Node node,
            int nextIndex
    ) {}
}