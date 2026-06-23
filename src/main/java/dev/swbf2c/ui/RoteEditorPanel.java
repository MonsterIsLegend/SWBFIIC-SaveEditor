package dev.swbf2c.ui;

import dev.swbf2c.rote.RoteCampaignMission;
import dev.swbf2c.rote.RoteCampaignSave;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

public final class RoteEditorPanel extends JPanel {
    private final Runnable dirtyAction;

    private boolean loading;

    private JComboBox<RoteCampaignMission> missionBox;
    private JButton saveButton;
    private JButton restoreBackupButton;

    public RoteEditorPanel(
            Runnable dirtyAction,
            Runnable saveAction,
            Runnable restoreBackupAction
    ) {
        super(new BorderLayout(12, 12));

        this.dirtyAction = dirtyAction;

        setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel contentPanel = new JPanel(new BorderLayout(12, 12));

        JPanel editPanel = new JPanel(new GridBagLayout());
        editPanel.setBorder(BorderFactory.createTitledBorder("Rise of the Empire Mission Selector"));

        GridBagConstraints constraints = UiSupport.createDefaultConstraints();

        missionBox = new JComboBox<>();
        DefaultComboBoxModel<RoteCampaignMission> model = new DefaultComboBoxModel<>();

        for (RoteCampaignMission mission : RoteCampaignMission.knownMissions()) {
            model.addElement(mission);
        }

        missionBox.setModel(model);
        UiSupport.attachDirtyTracking(missionBox, this::markDirty);

        UiSupport.addComboRow(editPanel, constraints, 0, "Current Mission", missionBox);

        JLabel noteLabel = new JLabel(
                "<html>"
                        + "Select the current Rise of the Empire campaign mission. "
                        + "A .bak backup is created before saving changes."
                        + "</html>"
        );

        JPanel topPanel = new JPanel(new BorderLayout(12, 12));
        topPanel.add(editPanel, BorderLayout.NORTH);
        topPanel.add(noteLabel, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        restoreBackupButton = new JButton("Restore Campaign Backup");
        saveButton = new JButton("Save Campaign");

        UiSupport.styleButton(restoreBackupButton);
        UiSupport.styleButton(saveButton);

        restoreBackupButton.addActionListener(event -> restoreBackupAction.run());
        saveButton.addActionListener(event -> saveAction.run());

        saveButton.setEnabled(false);
        restoreBackupButton.setEnabled(false);

        buttonPanel.add(restoreBackupButton);
        buttonPanel.add(saveButton);

        contentPanel.add(topPanel, BorderLayout.NORTH);

        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void display(RoteCampaignSave save, boolean backupExists) {
        loading = true;

        try {
            RoteCampaignMission mission =
                    RoteCampaignMission.fromRouteState(save.getRouteState());

            ensureMissionExistsInComboBox(mission);
            missionBox.setSelectedItem(mission);

            restoreBackupButton.setEnabled(backupExists);
            saveButton.setEnabled(false);

        } finally {
            loading = false;
        }
    }

    public void setSaveEnabled(boolean enabled) {
        saveButton.setEnabled(enabled);
    }

    public void writeTo(RoteCampaignSave save) {
        RoteCampaignMission selectedMission =
                (RoteCampaignMission) missionBox.getSelectedItem();

        if (selectedMission == null) {
            throw new IllegalArgumentException("No campaign mission selected.");
        }

        save.setRouteState(selectedMission.routeState());
    }

    private void ensureMissionExistsInComboBox(RoteCampaignMission mission) {
        DefaultComboBoxModel<RoteCampaignMission> model =
                (DefaultComboBoxModel<RoteCampaignMission>) missionBox.getModel();

        for (int index = 0; index < model.getSize(); index++) {
            RoteCampaignMission existing = model.getElementAt(index);

            if (existing.equals(mission)) {
                return;
            }
        }

        model.addElement(mission);
    }

    private void markDirty() {
        if (!loading) {
            dirtyAction.run();
        }
    }
}