package dev.swbf2c.ui;

import dev.swbf2c.gc.GcBonus;
import dev.swbf2c.gc.GcConquestSave;
import dev.swbf2c.gc.GcNode;
import dev.swbf2c.gc.GcUnit;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class GcEditorPanel extends JPanel {
    private final Runnable dirtyAction;

    private boolean loading;

    private JLabel factionValueLabel;
    private JLabel eraValueLabel;
    private JLabel profileValueLabel;
    private JLabel fleetValueLabel;

    private JTextField playerCreditsField;
    private JTextField aiCreditsField;

    private JComboBox<GcBonus> playerBonusSlot1Box;
    private JComboBox<GcBonus> playerBonusSlot2Box;
    private JComboBox<GcBonus> playerBonusSlot3Box;

    private JComboBox<GcBonus> aiBonusSlot1Box;
    private JComboBox<GcBonus> aiBonusSlot2Box;
    private JComboBox<GcBonus> aiBonusSlot3Box;

    private ListControl<GcNode> playerPlanetsControl;
    private ListControl<GcNode> aiPlanetsControl;

    private ListControl<GcNode> playerFleetsControl;
    private ListControl<GcNode> aiFleetsControl;

    private ListControl<GcUnit> playerUnitsControl;
    private ListControl<GcUnit> aiUnitsControl;

    private JButton saveButton;
    private JButton restoreBackupButton;

    private List<GcNode> currentMapPlanets = List.of();

    public GcEditorPanel(
            Runnable dirtyAction,
            Runnable saveAction,
            Runnable restoreBackupAction
    ) {
        super(new BorderLayout(12, 12));

        this.dirtyAction = dirtyAction;

        setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel infoPanel = createInfoPanel();

        JPanel playerPanel = createSidePanel(true);
        JPanel aiPanel = createSidePanel(false);

        JPanel sidePanel = new JPanel(new GridLayout(1, 2, 12, 12));
        sidePanel.add(playerPanel);
        sidePanel.add(aiPanel);

        JLabel noteLabel = new JLabel(
                "<html>"
                        + "Galactic Conquest editor. "
                        + "Edit credits, bonus slots, controlled planets, fleets, and unlocked units. "
                        + "Removing a planet transfers it to the other side. "
                        + "Each side must keep at least one fleet. "
                        + "A .bak backup is created before saving changes."
                        + "</html>"
        );

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        restoreBackupButton = new JButton("Restore GC Backup");
        saveButton = new JButton("Save GC");

        UiSupport.styleButton(restoreBackupButton);
        UiSupport.styleButton(saveButton);

        restoreBackupButton.addActionListener(event -> restoreBackupAction.run());
        saveButton.addActionListener(event -> saveAction.run());

        saveButton.setEnabled(false);
        restoreBackupButton.setEnabled(false);

        buttonPanel.add(restoreBackupButton);
        buttonPanel.add(saveButton);

        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.add(infoPanel, BorderLayout.NORTH);
        centerPanel.add(sidePanel, BorderLayout.CENTER);
        centerPanel.add(noteLabel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void display(GcConquestSave save, boolean backupExists) {
        loading = true;

        try {
            currentMapPlanets = save.getMapPlanets();

            factionValueLabel.setText(save.getFaction().displayName());
            eraValueLabel.setText(UiSupport.enumDisplayName(save.getEra()));
            profileValueLabel.setText(save.getProfileName());
            fleetValueLabel.setText(save.getFleetSummary());

            playerCreditsField.setText(Integer.toString(save.getPlayerCredits()));
            aiCreditsField.setText(Integer.toString(save.getEnemyCredits()));

            playerBonusSlot1Box.setSelectedItem(save.getPlayerBonusSlot(0));
            playerBonusSlot2Box.setSelectedItem(save.getPlayerBonusSlot(1));
            playerBonusSlot3Box.setSelectedItem(save.getPlayerBonusSlot(2));

            aiBonusSlot1Box.setSelectedItem(save.getEnemyBonusSlot(0));
            aiBonusSlot2Box.setSelectedItem(save.getEnemyBonusSlot(1));
            aiBonusSlot3Box.setSelectedItem(save.getEnemyBonusSlot(2));

            playerPlanetsControl.setItems(save.getPlayerControlledPlanets());
            aiPlanetsControl.setItems(save.getEnemyControlledPlanets());

            playerFleetsControl.setItems(save.getPlayerFleets());
            aiFleetsControl.setItems(save.getEnemyFleets());

            playerUnitsControl.setItems(save.getPlayerUnits());
            aiUnitsControl.setItems(save.getEnemyUnits());

            refreshAddOptions();

            restoreBackupButton.setEnabled(backupExists);
            saveButton.setEnabled(false);

        } finally {
            loading = false;
        }
    }

    public void setSaveEnabled(boolean enabled) {
        saveButton.setEnabled(enabled);
    }

    public void writeTo(GcConquestSave save) {
        int playerCredits = UiSupport.parseNonNegativeInt(
                playerCreditsField.getText(),
                "Player Credits"
        );

        int aiCredits = UiSupport.parseNonNegativeInt(
                aiCreditsField.getText(),
                "AI Credits"
        );

        save.setPlayerCredits(playerCredits);
        save.setEnemyCredits(aiCredits);

        save.setPlayerBonusSlot(
                0,
                (GcBonus) playerBonusSlot1Box.getSelectedItem()
        );

        save.setPlayerBonusSlot(
                1,
                (GcBonus) playerBonusSlot2Box.getSelectedItem()
        );

        save.setPlayerBonusSlot(
                2,
                (GcBonus) playerBonusSlot3Box.getSelectedItem()
        );

        save.setEnemyBonusSlot(
                0,
                (GcBonus) aiBonusSlot1Box.getSelectedItem()
        );

        save.setEnemyBonusSlot(
                1,
                (GcBonus) aiBonusSlot2Box.getSelectedItem()
        );

        save.setEnemyBonusSlot(
                2,
                (GcBonus) aiBonusSlot3Box.getSelectedItem()
        );

        List<GcNode> playerPlanets = playerPlanetsControl.getItems();
        List<GcNode> aiPlanets = aiPlanetsControl.getItems();

        ensureNoNodeOverlap(playerPlanets, aiPlanets, "controlled planet");
        ensureEveryPlanetIsControlled(playerPlanets, aiPlanets);

        save.setPlayerAndEnemyControlledPlanets(playerPlanets, aiPlanets);

        List<GcNode> playerFleets = playerFleetsControl.getItems();
        List<GcNode> aiFleets = aiFleetsControl.getItems();

        ensureAtLeastOneFleet(playerFleets, "Player");
        ensureAtLeastOneFleet(aiFleets, "AI");
        ensureNoNodeOverlap(playerFleets, aiFleets, "fleet node");
        ensurePlanetFleetsAreControlled(playerFleets, playerPlanets, "Player");
        ensurePlanetFleetsAreControlled(aiFleets, aiPlanets, "AI");

        save.setPlayerAndEnemyFleets(playerFleets, aiFleets);

        save.setPlayerAndEnemyUnits(
                playerUnitsControl.getItems(),
                aiUnitsControl.getItems()
        );
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Galactic Conquest Save"));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        factionValueLabel = new JLabel("-");
        eraValueLabel = new JLabel("-");
        profileValueLabel = new JLabel("-");
        fleetValueLabel = new JLabel("-");

        UiSupport.addValueRow(panel, constraints, 0, "Faction", factionValueLabel);
        UiSupport.addValueRow(panel, constraints, 1, "Era", eraValueLabel);
        UiSupport.addValueRow(panel, constraints, 2, "Profile", profileValueLabel);
        UiSupport.addValueRow(panel, constraints, 3, "Fleet", fleetValueLabel);

        return panel;
    }

    private JPanel createSidePanel(boolean playerSide) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(playerSide ? "Player" : "AI"));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 6, 5, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        JTextField creditsField = new JTextField(10);

        JComboBox<GcBonus> slot1Box = new JComboBox<>(GcBonus.values());
        JComboBox<GcBonus> slot2Box = new JComboBox<>(GcBonus.values());
        JComboBox<GcBonus> slot3Box = new JComboBox<>(GcBonus.values());

        UiSupport.attachDirtyTracking(creditsField, this::markDirty);
        UiSupport.attachDirtyTracking(slot1Box, this::markDirty);
        UiSupport.attachDirtyTracking(slot2Box, this::markDirty);
        UiSupport.attachDirtyTracking(slot3Box, this::markDirty);

        if (playerSide) {
            playerCreditsField = creditsField;

            playerBonusSlot1Box = slot1Box;
            playerBonusSlot2Box = slot2Box;
            playerBonusSlot3Box = slot3Box;

            playerPlanetsControl = new ListControl<>("Controlled Planets");
            playerFleetsControl = new ListControl<>("Fleets");
            playerUnitsControl = new ListControl<>("Unlocked Units");

        } else {
            aiCreditsField = creditsField;

            aiBonusSlot1Box = slot1Box;
            aiBonusSlot2Box = slot2Box;
            aiBonusSlot3Box = slot3Box;

            aiPlanetsControl = new ListControl<>("Controlled Planets");
            aiFleetsControl = new ListControl<>("Fleets");
            aiUnitsControl = new ListControl<>("Unlocked Units");
        }

        UiSupport.addFieldRow(panel, constraints, 0, "Credits", creditsField);
        UiSupport.addComboRow(panel, constraints, 1, "Bonus Slot 1", slot1Box);
        UiSupport.addComboRow(panel, constraints, 2, "Bonus Slot 2", slot2Box);
        UiSupport.addComboRow(panel, constraints, 3, "Bonus Slot 3", slot3Box);

        if (playerSide) {
            addControlRow(panel, constraints, 4, "Controlled Planets", playerPlanetsControl);
            addControlRow(panel, constraints, 5, "Fleets", playerFleetsControl);
            addControlRow(panel, constraints, 6, "Unlocked Units", playerUnitsControl);

            playerPlanetsControl.addButton.addActionListener(event -> addPlanetToPlayer());
            playerPlanetsControl.removeButton.addActionListener(event -> transferSelectedPlayerPlanetsToAi());

            playerFleetsControl.addButton.addActionListener(event -> addFleetToPlayer());
            playerFleetsControl.removeButton.addActionListener(event -> removeSelectedPlayerFleets());

            playerUnitsControl.addButton.addActionListener(event -> addUnitToPlayer());
            playerUnitsControl.removeButton.addActionListener(event -> removeSelectedPlayerUnits());

        } else {
            addControlRow(panel, constraints, 4, "Controlled Planets", aiPlanetsControl);
            addControlRow(panel, constraints, 5, "Fleets", aiFleetsControl);
            addControlRow(panel, constraints, 6, "Unlocked Units", aiUnitsControl);

            aiPlanetsControl.addButton.addActionListener(event -> addPlanetToAi());
            aiPlanetsControl.removeButton.addActionListener(event -> transferSelectedAiPlanetsToPlayer());

            aiFleetsControl.addButton.addActionListener(event -> addFleetToAi());
            aiFleetsControl.removeButton.addActionListener(event -> removeSelectedAiFleets());

            aiUnitsControl.addButton.addActionListener(event -> addUnitToAi());
            aiUnitsControl.removeButton.addActionListener(event -> removeSelectedAiUnits());
        }

        return panel;
    }

    private void addControlRow(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String labelText,
            ListControl<?> control
    ) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        panel.add(new JLabel(labelText), constraints);

        constraints.gridx = 1;
        constraints.gridy = row;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;

        panel.add(control.panel, constraints);
    }

    private void addPlanetToPlayer() {
        GcNode node = playerPlanetsControl.getSelectedAddItem();

        if (node == null) {
            return;
        }

        addNode(playerPlanetsControl, node);
        removeNode(aiPlanetsControl, node.code());
        removeNode(aiFleetsControl, node.code());

        removeInvalidPlanetFleets();
        refreshAddOptions();
        markDirty();
    }

    private void addPlanetToAi() {
        GcNode node = aiPlanetsControl.getSelectedAddItem();

        if (node == null) {
            return;
        }

        addNode(aiPlanetsControl, node);
        removeNode(playerPlanetsControl, node.code());
        removeNode(playerFleetsControl, node.code());

        removeInvalidPlanetFleets();
        refreshAddOptions();
        markDirty();
    }

    private void transferSelectedPlayerPlanetsToAi() {
        List<GcNode> selectedPlanets = playerPlanetsControl.getSelectedItems();

        if (selectedPlanets.isEmpty()) {
            return;
        }

        for (GcNode node : selectedPlanets) {
            removeNode(playerPlanetsControl, node.code());
            addNode(aiPlanetsControl, node);
            removeNode(playerFleetsControl, node.code());
        }

        removeInvalidPlanetFleets();
        refreshAddOptions();
        markDirty();
    }

    private void transferSelectedAiPlanetsToPlayer() {
        List<GcNode> selectedPlanets = aiPlanetsControl.getSelectedItems();

        if (selectedPlanets.isEmpty()) {
            return;
        }

        for (GcNode node : selectedPlanets) {
            removeNode(aiPlanetsControl, node.code());
            addNode(playerPlanetsControl, node);
            removeNode(aiFleetsControl, node.code());
        }

        removeInvalidPlanetFleets();
        refreshAddOptions();
        markDirty();
    }

    private void addFleetToPlayer() {
        GcNode node = playerFleetsControl.getSelectedAddItem();

        if (node == null) {
            return;
        }

        addNode(playerFleetsControl, node);
        removeNode(aiFleetsControl, node.code());

        refreshAddOptions();
        markDirty();
    }

    private void addFleetToAi() {
        GcNode node = aiFleetsControl.getSelectedAddItem();

        if (node == null) {
            return;
        }

        addNode(aiFleetsControl, node);
        removeNode(playerFleetsControl, node.code());

        refreshAddOptions();
        markDirty();
    }

    private void removeSelectedPlayerFleets() {
        removeSelectedFleets(playerFleetsControl, "Player");
    }

    private void removeSelectedAiFleets() {
        removeSelectedFleets(aiFleetsControl, "AI");
    }

    private void removeSelectedFleets(
            ListControl<GcNode> control,
            String sideName
    ) {
        List<GcNode> selected = control.getSelectedItems();

        if (selected.isEmpty()) {
            return;
        }

        int remainingCount = control.model.getSize() - selected.size();

        if (remainingCount < 1) {
            JOptionPane.showMessageDialog(
                    this,
                    sideName + " must keep at least one fleet.",
                    "Fleet Required",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        for (GcNode node : selected) {
            removeNode(control, node.code());
        }

        refreshAddOptions();
        markDirty();
    }

    private void addUnitToPlayer() {
        GcUnit unit = playerUnitsControl.getSelectedAddItem();

        if (unit == null) {
            return;
        }

        addUnit(playerUnitsControl, unit);

        refreshAddOptions();
        markDirty();
    }

    private void addUnitToAi() {
        GcUnit unit = aiUnitsControl.getSelectedAddItem();

        if (unit == null) {
            return;
        }

        addUnit(aiUnitsControl, unit);

        refreshAddOptions();
        markDirty();
    }

    private void removeSelectedPlayerUnits() {
        removeSelectedUnits(playerUnitsControl);
    }

    private void removeSelectedAiUnits() {
        removeSelectedUnits(aiUnitsControl);
    }

    private void removeSelectedUnits(ListControl<GcUnit> control) {
        List<GcUnit> selected = control.getSelectedItems();

        if (selected.isEmpty()) {
            return;
        }

        for (GcUnit unit : selected) {
            control.model.removeElement(unit);
        }

        refreshAddOptions();
        markDirty();
    }

    private void refreshAddOptions() {
        if (playerPlanetsControl == null || aiPlanetsControl == null) {
            return;
        }

        playerPlanetsControl.setAddOptions(
                nodesNotAlreadyIn(currentMapPlanets, playerPlanetsControl.getItems())
        );

        aiPlanetsControl.setAddOptions(
                nodesNotAlreadyIn(currentMapPlanets, aiPlanetsControl.getItems())
        );

        List<GcNode> usedFleetNodes = new ArrayList<>();
        usedFleetNodes.addAll(playerFleetsControl.getItems());
        usedFleetNodes.addAll(aiFleetsControl.getItems());

        playerFleetsControl.setAddOptions(
                nodesNotAlreadyIn(playerPlanetsControl.getItems(), usedFleetNodes)
        );

        aiFleetsControl.setAddOptions(
                nodesNotAlreadyIn(aiPlanetsControl.getItems(), usedFleetNodes)
        );

        playerUnitsControl.setAddOptions(
                unitsNotAlreadyIn(playerUnitsControl.getItems())
        );

        aiUnitsControl.setAddOptions(
                unitsNotAlreadyIn(aiUnitsControl.getItems())
        );
    }

    private void removeInvalidPlanetFleets() {
        removePlanetFleetsNotControlledBy(playerFleetsControl, playerPlanetsControl);
        removePlanetFleetsNotControlledBy(aiFleetsControl, aiPlanetsControl);
    }

    private void removePlanetFleetsNotControlledBy(
            ListControl<GcNode> fleetControl,
            ListControl<GcNode> planetControl
    ) {
        Set<String> controlledPlanetCodes = toNodeCodeSet(planetControl.getItems());

        List<GcNode> toRemove = new ArrayList<>();

        for (GcNode fleetNode : fleetControl.getItems()) {
            if (!fleetNode.planet()) {
                continue;
            }

            if (!controlledPlanetCodes.contains(fleetNode.code())) {
                toRemove.add(fleetNode);
            }
        }

        for (GcNode node : toRemove) {
            removeNode(fleetControl, node.code());
        }
    }

    private List<GcNode> nodesNotAlreadyIn(
            List<GcNode> source,
            List<GcNode> existing
    ) {
        Set<String> existingCodes = toNodeCodeSet(existing);
        List<GcNode> result = new ArrayList<>();

        for (GcNode node : source) {
            if (!existingCodes.contains(node.code())) {
                result.add(node);
            }
        }

        return result;
    }

    private List<GcUnit> unitsNotAlreadyIn(List<GcUnit> existing) {
        Set<GcUnit> existingSet = new HashSet<>(existing);
        List<GcUnit> result = new ArrayList<>();

        for (GcUnit unit : GcUnit.values()) {
            if (!existingSet.contains(unit)) {
                result.add(unit);
            }
        }

        return result;
    }

    private void addNode(ListControl<GcNode> control, GcNode node) {
        if (containsNode(control.getItems(), node.code())) {
            return;
        }

        control.model.addElement(node);
    }

    private void removeNode(ListControl<GcNode> control, String code) {
        for (int index = control.model.getSize() - 1; index >= 0; index--) {
            GcNode node = control.model.getElementAt(index);

            if (node.code().equals(code)) {
                control.model.remove(index);
            }
        }
    }

    private void addUnit(ListControl<GcUnit> control, GcUnit unit) {
        if (control.getItems().contains(unit)) {
            return;
        }

        control.model.addElement(unit);
    }

    private boolean containsNode(List<GcNode> nodes, String code) {
        for (GcNode node : nodes) {
            if (node.code().equals(code)) {
                return true;
            }
        }

        return false;
    }

    private Set<String> toNodeCodeSet(List<GcNode> nodes) {
        Set<String> codes = new HashSet<>();

        for (GcNode node : nodes) {
            codes.add(node.code());
        }

        return codes;
    }

    private void ensureNoNodeOverlap(
            List<GcNode> first,
            List<GcNode> second,
            String itemName
    ) {
        Set<String> firstCodes = toNodeCodeSet(first);

        for (GcNode node : second) {
            if (firstCodes.contains(node.code())) {
                throw new IllegalArgumentException(
                        "The same " + itemName + " cannot belong to both Player and AI: "
                                + node
                );
            }
        }
    }

    private void ensureEveryPlanetIsControlled(
            List<GcNode> playerPlanets,
            List<GcNode> aiPlanets
    ) {
        Set<String> controlledCodes = new HashSet<>();
        controlledCodes.addAll(toNodeCodeSet(playerPlanets));
        controlledCodes.addAll(toNodeCodeSet(aiPlanets));

        for (GcNode planet : currentMapPlanets) {
            if (!controlledCodes.contains(planet.code())) {
                throw new IllegalArgumentException(
                        "Every planet must be controlled by Player or AI. Missing: "
                                + planet
                );
            }
        }
    }

    private void ensureAtLeastOneFleet(
            List<GcNode> fleets,
            String sideName
    ) {
        if (fleets.isEmpty()) {
            throw new IllegalArgumentException(
                    sideName + " must have at least one fleet."
            );
        }
    }

    private void ensurePlanetFleetsAreControlled(
            List<GcNode> fleets,
            List<GcNode> controlledPlanets,
            String sideName
    ) {
        Set<String> controlledPlanetCodes = toNodeCodeSet(controlledPlanets);

        for (GcNode fleetNode : fleets) {
            if (!fleetNode.planet()) {
                continue;
            }

            if (!controlledPlanetCodes.contains(fleetNode.code())) {
                throw new IllegalArgumentException(
                        sideName + " cannot have a fleet on an uncontrolled planet: "
                                + fleetNode
                );
            }
        }
    }

    private void markDirty() {
        if (!loading) {
            saveButton.setEnabled(true);
            dirtyAction.run();
        }
    }

    private final class ListControl<T> {
        private final JPanel panel = new JPanel(new BorderLayout(6, 6));
        private final DefaultListModel<T> model = new DefaultListModel<>();
        private final JList<T> list = new JList<>(model);
        private final JComboBox<T> addBox = new JComboBox<>();
        private final JButton addButton = new JButton("Add");
        private final JButton removeButton = new JButton("Remove Selected");

        private ListControl(String title) {
            panel.setBorder(BorderFactory.createTitledBorder(title));

            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            list.setVisibleRowCount(4);

            UiSupport.styleButton(addButton);
            UiSupport.styleButton(removeButton);

            JPanel controlsPanel = new JPanel(new BorderLayout(6, 6));
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));

            buttonsPanel.add(addButton);
            buttonsPanel.add(removeButton);

            controlsPanel.add(addBox, BorderLayout.CENTER);
            controlsPanel.add(buttonsPanel, BorderLayout.EAST);

            panel.add(new JScrollPane(list), BorderLayout.CENTER);
            panel.add(controlsPanel, BorderLayout.SOUTH);
        }

        private void setItems(List<T> items) {
            model.clear();

            for (T item : items) {
                model.addElement(item);
            }
        }

        private List<T> getItems() {
            List<T> items = new ArrayList<>();

            for (int index = 0; index < model.getSize(); index++) {
                items.add(model.getElementAt(index));
            }

            return items;
        }

        private List<T> getSelectedItems() {
            return list.getSelectedValuesList();
        }

        private void setAddOptions(List<T> options) {
            DefaultComboBoxModel<T> comboBoxModel = new DefaultComboBoxModel<>();

            for (T option : options) {
                comboBoxModel.addElement(option);
            }

            addBox.setModel(comboBoxModel);
            addButton.setEnabled(!options.isEmpty());
        }

        private T getSelectedAddItem() {
            return (T) addBox.getSelectedItem();
        }
    }
}