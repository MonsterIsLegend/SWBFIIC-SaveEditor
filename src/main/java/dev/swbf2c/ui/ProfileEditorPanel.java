package dev.swbf2c.ui;

import dev.swbf2c.profile.BattlefrontProfile;
import dev.swbf2c.profile.ProfileFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

public final class ProfileEditorPanel extends JPanel {
    private final Runnable dirtyAction;

    private boolean loading;

    private JTextField profileNameField;
    private final JTextField[] medalFields = new JTextField[ProfileFormat.MEDAL_COUNT];
    private final JTextField[] statFields = new JTextField[ProfileFormat.STAT_COUNT];

    private JButton saveButton;
    private JButton saveAsButton;
    private JButton restoreBackupButton;

    public ProfileEditorPanel(
            Runnable dirtyAction,
            Runnable saveAction,
            Runnable saveAsAction,
            Runnable restoreBackupAction
    ) {
        super(new BorderLayout(12, 12));

        this.dirtyAction = dirtyAction;

        setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.add(createProfileInfoPanel(), BorderLayout.NORTH);
        centerPanel.add(createProfileValuesPanel(), BorderLayout.CENTER);

        JLabel hintLabel = new JLabel(
                "A .bak file is created before overwriting an existing profile."
        );

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        restoreBackupButton = new JButton("Restore Backup");
        saveAsButton = new JButton("Save As");
        saveButton = new JButton("Save");

        UiSupport.styleButton(restoreBackupButton);
        UiSupport.styleButton(saveAsButton);
        UiSupport.styleButton(saveButton);

        restoreBackupButton.addActionListener(event -> restoreBackupAction.run());
        saveAsButton.addActionListener(event -> saveAsAction.run());
        saveButton.addActionListener(event -> saveAction.run());

        saveButton.setEnabled(false);
        restoreBackupButton.setEnabled(false);

        buttonPanel.add(restoreBackupButton);
        buttonPanel.add(saveAsButton);
        buttonPanel.add(saveButton);

        JPanel bottomPanel = new JPanel(new BorderLayout(12, 12));
        bottomPanel.add(hintLabel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void display(BattlefrontProfile profile, boolean backupExists) {
        loading = true;

        try {
            profileNameField.setText(profile.getProfileName());

            for (int index = 0; index < ProfileFormat.MEDAL_COUNT; index++) {
                medalFields[index].setText(Integer.toString(profile.getMedal(index)));
            }

            for (int index = 0; index < ProfileFormat.STAT_COUNT; index++) {
                statFields[index].setText(Long.toString(profile.getStat(index)));
            }

            restoreBackupButton.setEnabled(backupExists);
            saveAsButton.setEnabled(true);
            saveButton.setEnabled(false);

        } finally {
            loading = false;
        }
    }

    public void setSaveEnabled(boolean enabled) {
        saveButton.setEnabled(enabled);
    }

    public void writeTo(BattlefrontProfile profile) {
        profile.setProfileName(profileNameField.getText());

        for (int index = 0; index < ProfileFormat.MEDAL_COUNT; index++) {
            int value = UiSupport.parseIntInRange(
                    medalFields[index].getText(),
                    ProfileFormat.MEDAL_NAMES[index],
                    0,
                    65_535
            );

            profile.setMedal(index, value);
        }

        for (int index = 0; index < ProfileFormat.STAT_COUNT; index++) {
            long value = UiSupport.parseLongInRange(
                    statFields[index].getText(),
                    ProfileFormat.STAT_NAMES[index],
                    0L,
                    4_294_967_295L
            );

            profile.setStat(index, value);
        }
    }

    private JPanel createProfileInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Profile"));

        GridBagConstraints constraints = UiSupport.createDefaultConstraints();

        profileNameField = new JTextField(22);
        UiSupport.attachDirtyTracking(profileNameField, this::markDirty);

        JLabel noteLabel = new JLabel("Max 22 characters.");
        noteLabel.setFont(noteLabel.getFont().deriveFont(11.0f));

        UiSupport.addFieldRow(panel, constraints, 0, "Profile Name", profileNameField);

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(noteLabel, constraints);

        return panel;
    }

    private JPanel createProfileValuesPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 12));

        JPanel medalsPanel = new JPanel(new GridBagLayout());
        medalsPanel.setBorder(BorderFactory.createTitledBorder("Career Medals"));

        JPanel statsPanel = new JPanel(new GridBagLayout());
        statsPanel.setBorder(BorderFactory.createTitledBorder("Career Stats"));

        GridBagConstraints medalsConstraints = UiSupport.createDefaultConstraints();

        for (int index = 0; index < ProfileFormat.MEDAL_COUNT; index++) {
            JTextField field = new JTextField(10);
            medalFields[index] = field;
            UiSupport.attachDirtyTracking(field, this::markDirty);

            UiSupport.addFieldRow(
                    medalsPanel,
                    medalsConstraints,
                    index,
                    ProfileFormat.MEDAL_NAMES[index],
                    field
            );
        }

        UiSupport.addVerticalFiller(
                medalsPanel,
                medalsConstraints,
                ProfileFormat.MEDAL_COUNT
        );

        GridBagConstraints statsConstraints = UiSupport.createDefaultConstraints();

        for (int index = 0; index < ProfileFormat.STAT_COUNT; index++) {
            JTextField field = new JTextField(10);
            statFields[index] = field;
            UiSupport.attachDirtyTracking(field, this::markDirty);

            UiSupport.addFieldRow(
                    statsPanel,
                    statsConstraints,
                    index,
                    ProfileFormat.STAT_NAMES[index],
                    field
            );
        }

        UiSupport.addVerticalFiller(
                statsPanel,
                statsConstraints,
                ProfileFormat.STAT_COUNT
        );

        panel.add(medalsPanel);
        panel.add(statsPanel);

        return panel;
    }

    private void markDirty() {
        if (!loading) {
            dirtyAction.run();
        }
    }
}