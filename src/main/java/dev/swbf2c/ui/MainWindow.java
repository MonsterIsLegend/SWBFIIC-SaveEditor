package dev.swbf2c.ui;

import dev.swbf2c.profile.BattlefrontProfile;
import dev.swbf2c.profile.ProfileFileService;
import dev.swbf2c.profile.ProfileFormat;
import dev.swbf2c.profile.ProfileLocations;
import dev.swbf2c.rote.RoteCampaignMission;
import dev.swbf2c.rote.RoteCampaignSave;
import dev.swbf2c.rote.RoteFileService;
import dev.swbf2c.rote.SaveFileType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

public final class MainWindow extends JFrame {
    private static final String WINDOW_TITLE = "SWBFIIC SaveEditor";

    private static final String CARD_EMPTY = "empty";
    private static final String CARD_PROFILE = "profile";
    private static final String CARD_ROTE = "rote";

    private static final String BOTTOM_EMPTY = "empty";
    private static final String BOTTOM_PROFILE = "profile";

    private final ProfileFileService profileFileService = new ProfileFileService();
    private final RoteFileService roteFileService = new RoteFileService();

    private BattlefrontProfile currentProfile;
    private RoteCampaignSave currentRoteSave;

    private Path currentPath;
    private SaveFileType currentFileType = SaveFileType.UNKNOWN;

    private boolean dirty;
    private boolean suppressDirtyEvents;

    private JTextField profileNameField;

    private final JTextField[] medalFields =
            new JTextField[ProfileFormat.MEDAL_COUNT];

    private final JTextField[] statFields =
            new JTextField[ProfileFormat.STAT_COUNT];

    private JComboBox<RoteCampaignMission> missionComboBox;

    private final JLabel currentFileLabel = new JLabel("No save file loaded");

    private final JButton openButton = new JButton("Open Save File");

    private final JButton saveButton = new JButton("Save");
    private final JButton saveAsButton = new JButton("Save As");
    private final JButton restoreButton = new JButton("Restore Backup");

    private final JButton saveRoteButton = new JButton("Save Campaign");
    private final JButton restoreRoteButton = new JButton("Restore Campaign Backup");

    private CardLayout editorCardLayout;
    private JPanel editorCardPanel;

    private CardLayout bottomCardLayout;
    private JPanel bottomCardPanel;

    public MainWindow() {
        super(WINDOW_TITLE);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setContentPane(createRootPanel());

        configureActions();

        setAllEditorFieldsEnabled(false);
        updateButtonState();
        updateTitle();
        updateCurrentFileLabel();

        showEditorCard(CARD_EMPTY);
        showBottomCard(BOTTOM_EMPTY);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                exitApplication();
            }
        });

        setMinimumSize(new Dimension(820, 540));
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createRootPanel() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBorder(new EmptyBorder(18, 22, 18, 22));

        root.add(createHeaderPanel(), BorderLayout.NORTH);
        root.add(createEditorCardPanel(), BorderLayout.CENTER);
        root.add(createBottomCardPanel(), BorderLayout.SOUTH);

        return root;
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createCurrentFilePanel(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCurrentFilePanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Current File"),
                new EmptyBorder(10, 12, 10, 12)
        ));

        currentFileLabel.setFont(currentFileLabel.getFont().deriveFont(Font.BOLD, 18.0f));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.add(openButton);

        panel.add(currentFileLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createEditorCardPanel() {
        editorCardLayout = new CardLayout();
        editorCardPanel = new JPanel(editorCardLayout);

        editorCardPanel.add(createEmptyPanel(), CARD_EMPTY);
        editorCardPanel.add(createProfileEditorPanel(), CARD_PROFILE);
        editorCardPanel.add(createRoteEditorPanel(), CARD_ROTE);

        return editorCardPanel;
    }

    private JPanel createEmptyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Editor"),
                new EmptyBorder(24, 24, 24, 24)
        ));

        JLabel label = new JLabel("Open a .profile or .rote save file to begin.");
        label.setFont(label.getFont().deriveFont(15.0f));

        panel.add(label, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createProfileEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));

        panel.add(createProfileInfoPanel(), BorderLayout.NORTH);
        panel.add(createProfileValuesPanel(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createProfileInfoPanel() {
        JPanel panel = createCardPanel("Profile");

        profileNameField = new JTextField(22);
        attachDirtyListener(profileNameField);

        addFieldRow(panel, 0, "Profile Name", profileNameField);

        JLabel note = new JLabel("Maximum profile name length: "
                + ProfileFormat.PROFILE_NAME_MAX_CHARS
                + " characters.");
        note.setFont(note.getFont().deriveFont(12.0f));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        panel.add(note, gbc);

        return panel;
    }

    private JPanel createProfileValuesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 12);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;

        panel.add(createMedalsPanel(), gbc);

        gbc.insets = new Insets(0, 12, 0, 0);
        gbc.gridx = 1;

        panel.add(createStatsPanel(), gbc);

        return panel;
    }

    private JPanel createMedalsPanel() {
        JPanel panel = createCardPanel("Career Medals");

        for (int i = 0; i < ProfileFormat.MEDAL_COUNT; i++) {
            JTextField field = new JTextField(12);
            field.setHorizontalAlignment(JTextField.RIGHT);
            attachDirtyListener(field);

            medalFields[i] = field;

            addFieldRow(panel, i, ProfileFormat.MEDAL_NAMES[i], field);
        }

        addVerticalGlue(panel, ProfileFormat.MEDAL_COUNT);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = createCardPanel("Career Stats");

        for (int i = 0; i < ProfileFormat.STAT_COUNT; i++) {
            JTextField field = new JTextField(12);
            field.setHorizontalAlignment(JTextField.RIGHT);
            attachDirtyListener(field);

            statFields[i] = field;

            addFieldRow(panel, i, ProfileFormat.STAT_NAMES[i], field);
        }

        addVerticalGlue(panel, ProfileFormat.STAT_COUNT);

        return panel;
    }

    private JPanel createRoteEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Rise of the Empire Campaign"),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.weightx = 0;

        fieldsPanel.add(new JLabel("Current Mission"), gbc);

        missionComboBox = new JComboBox<>();
        missionComboBox.setModel(createMissionComboModel(null));
        missionComboBox.addActionListener(event -> markDirtyFromRoteSelection());

        gbc.gridx = 1;
        gbc.weightx = 1;

        fieldsPanel.add(missionComboBox, gbc);

        JLabel noteLabel = new JLabel(
                "<html>"
                        + "Rise of the Empire mission selector. "
                        + "Choose the campaign mission that should be loaded from this .rote save file. "
                        + "A .bak backup is created before saving changes."
                        + "</html>"
        );

        noteLabel.setFont(noteLabel.getFont().deriveFont(12.0f));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.add(restoreRoteButton);
        buttonPanel.add(saveRoteButton);

        panel.add(fieldsPanel, BorderLayout.NORTH);
        panel.add(noteLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private DefaultComboBoxModel<RoteCampaignMission> createMissionComboModel(
            RoteCampaignMission selectedMission
    ) {
        DefaultComboBoxModel<RoteCampaignMission> model =
                new DefaultComboBoxModel<>();

        if (selectedMission != null && !selectedMission.known()) {
            model.addElement(selectedMission);
        }

        for (RoteCampaignMission mission : RoteCampaignMission.knownMissions()) {
            model.addElement(mission);
        }

        return model;
    }

    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                new EmptyBorder(12, 12, 12, 12)
        ));

        return panel;
    }

    private void addFieldRow(
            JPanel panel,
            int row,
            String labelText,
            JTextField field
    ) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.weightx = 0;

        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;

        panel.add(field, gbc);
    }

    private void addVerticalGlue(JPanel panel, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.VERTICAL;

        panel.add(Box.createVerticalGlue(), gbc);
    }

    private JPanel createBottomCardPanel() {
        bottomCardLayout = new CardLayout();
        bottomCardPanel = new JPanel(bottomCardLayout);

        bottomCardPanel.add(createEmptyBottomPanel(), BOTTOM_EMPTY);
        bottomCardPanel.add(createProfileBottomPanel(), BOTTOM_PROFILE);

        return bottomCardPanel;
    }

    private JPanel createEmptyBottomPanel() {
        return new JPanel(new BorderLayout());
    }

    private JPanel createProfileBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.add(restoreButton);
        buttonPanel.add(saveAsButton);
        buttonPanel.add(saveButton);

        JLabel hintLabel = new JLabel("A .bak file is created before overwriting an existing profile.");
        hintLabel.setFont(hintLabel.getFont().deriveFont(12.0f));

        panel.add(hintLabel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private void configureActions() {
        styleButton(openButton);

        styleButton(saveButton);
        styleButton(saveAsButton);
        styleButton(restoreButton);

        styleButton(saveRoteButton);
        styleButton(restoreRoteButton);

        openButton.addActionListener(event -> openSaveFile());

        saveButton.addActionListener(event -> saveCurrentFile());
        saveAsButton.addActionListener(event -> saveCurrentFileAs());
        restoreButton.addActionListener(event -> restoreBackup());

        saveRoteButton.addActionListener(event -> saveCurrentFile());
        restoreRoteButton.addActionListener(event -> restoreBackup());
    }

    private void styleButton(JButton button) {
        button.setFocusable(false);
        button.setFocusPainted(false);
        button.setDefaultCapable(false);
        button.putClientProperty("JButton.buttonType", "roundRect");
    }

    private void openSaveFile() {
        if (!confirmDiscardUnsavedChanges()) {
            return;
        }

        JFileChooser chooser = createSaveFileChooser();

        int result = chooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path path = chooser.getSelectedFile().toPath();
        SaveFileType detectedType = SaveFileType.detect(path);

        try {
            switch (detectedType) {
                case PROFILE -> openProfile(path);
                case ROTE -> openRote(path);
                case UNKNOWN -> throw new IllegalArgumentException(
                        "Unsupported file type. Open a .profile or .rote file."
                );
            }

        } catch (IOException | IllegalArgumentException exception) {
            showError("Could not load save file.", exception);
        }
    }

    private void openProfile(Path path) throws IOException {
        BattlefrontProfile profile = profileFileService.load(path);

        currentPath = path;
        currentFileType = SaveFileType.PROFILE;
        currentProfile = profile;
        currentRoteSave = null;

        displayProfile(profile);
        setAllEditorFieldsEnabled(true);
        setDirty(false);
        updateCurrentFileLabel();

        showEditorCard(CARD_PROFILE);
        showBottomCard(BOTTOM_PROFILE);
    }

    private void openRote(Path path) throws IOException {
        RoteCampaignSave roteSave = roteFileService.load(path);

        currentPath = path;
        currentFileType = SaveFileType.ROTE;
        currentProfile = null;
        currentRoteSave = roteSave;

        displayRote(roteSave);
        setAllEditorFieldsEnabled(true);
        setDirty(false);
        updateCurrentFileLabel();

        showEditorCard(CARD_ROTE);
        showBottomCard(BOTTOM_EMPTY);
    }

    private boolean saveCurrentFile() {
        if (currentPath == null || currentFileType == SaveFileType.UNKNOWN) {
            showError("No save file is currently loaded.", null);
            return false;
        }

        try {
            switch (currentFileType) {
                case PROFILE -> {
                    updateProfileFromFields();
                    currentPath = profileFileService.saveWithPossibleRename(
                            currentPath,
                            currentProfile
                    );
                }
                case ROTE -> {
                    updateRoteFromFields();
                    roteFileService.save(currentPath, currentRoteSave);
                }
                case UNKNOWN -> throw new IllegalStateException("Unknown save file type.");
            }

            setDirty(false);
            updateCurrentFileLabel();

            JOptionPane.showMessageDialog(
                    this,
                    "Save file saved successfully.",
                    "Saved",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return true;

        } catch (IOException | IllegalArgumentException exception) {
            showError("Could not save file.", exception);
            return false;
        }
    }

    private boolean saveCurrentFileAs() {
        if (currentFileType == SaveFileType.UNKNOWN) {
            showError("No save file is currently loaded.", null);
            return false;
        }

        JFileChooser chooser = createSaveFileChooser();

        if (currentPath != null) {
            chooser.setSelectedFile(currentPath.toFile());
        }

        int result = chooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File selectedFile = chooser.getSelectedFile();
        Path selectedPath = ensureExpectedExtension(selectedFile, currentFileType).toPath();

        if (selectedPath.toFile().exists()) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "This file already exists. Overwrite it?",
                    "Confirm Overwrite",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (choice != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        try {
            switch (currentFileType) {
                case PROFILE -> {
                    updateProfileFromFields();
                    profileFileService.saveAs(selectedPath, currentProfile);
                }
                case ROTE -> {
                    updateRoteFromFields();
                    roteFileService.saveAs(selectedPath, currentRoteSave);
                }
                case UNKNOWN -> throw new IllegalStateException("Unknown save file type.");
            }

            currentPath = selectedPath;

            setDirty(false);
            updateCurrentFileLabel();

            JOptionPane.showMessageDialog(
                    this,
                    "Save file saved successfully.",
                    "Saved",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return true;

        } catch (IOException | IllegalArgumentException exception) {
            showError("Could not save file.", exception);
            return false;
        }
    }

    private void restoreBackup() {
        if (currentPath == null || currentFileType == SaveFileType.UNKNOWN) {
            showError("No save file is currently loaded.", null);
            return;
        }

        boolean backupExists = switch (currentFileType) {
            case PROFILE -> profileFileService.backupExists(currentPath);
            case ROTE -> roteFileService.backupExists(currentPath);
            case UNKNOWN -> false;
        };

        if (!backupExists) {
            showError("No backup exists for this save file yet.", null);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Restore the .bak file for this save?\n\n"
                        + "The current file will be replaced.\n"
                        + "The current file will first be copied to a .before-restore file.",
                "Restore Backup",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            switch (currentFileType) {
                case PROFILE -> {
                    profileFileService.restoreBackup(currentPath);
                    currentProfile = profileFileService.load(currentPath);
                    displayProfile(currentProfile);
                    showEditorCard(CARD_PROFILE);
                    showBottomCard(BOTTOM_PROFILE);
                }
                case ROTE -> {
                    roteFileService.restoreBackup(currentPath);
                    currentRoteSave = roteFileService.load(currentPath);
                    displayRote(currentRoteSave);
                    showEditorCard(CARD_ROTE);
                    showBottomCard(BOTTOM_EMPTY);
                }
                case UNKNOWN -> throw new IllegalStateException("Unknown save file type.");
            }

            setDirty(false);
            updateCurrentFileLabel();

            JOptionPane.showMessageDialog(
                    this,
                    "Backup restored successfully.",
                    "Restored",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IOException | IllegalArgumentException exception) {
            showError("Could not restore backup.", exception);
        }
    }

    private void displayProfile(BattlefrontProfile profile) {
        suppressDirtyEvents = true;

        try {
            profileNameField.setText(profile.getProfileName());

            for (int i = 0; i < ProfileFormat.MEDAL_COUNT; i++) {
                medalFields[i].setText(String.valueOf(profile.getMedal(i)));
            }

            for (int i = 0; i < ProfileFormat.STAT_COUNT; i++) {
                statFields[i].setText(String.valueOf(profile.getStat(i)));
            }
        } finally {
            suppressDirtyEvents = false;
        }
    }

    private void displayRote(RoteCampaignSave roteSave) {
        suppressDirtyEvents = true;

        try {
            int routeState = roteSave.getRouteState();
            RoteCampaignMission selectedMission =
                    RoteCampaignMission.fromRouteState(routeState);

            missionComboBox.setModel(createMissionComboModel(selectedMission));
            missionComboBox.setSelectedItem(selectedMission);

        } finally {
            suppressDirtyEvents = false;
        }
    }

    private void updateProfileFromFields() {
        if (currentProfile == null) {
            throw new IllegalStateException("No profile file is loaded.");
        }

        currentProfile.setProfileName(profileNameField.getText());

        for (int i = 0; i < ProfileFormat.MEDAL_COUNT; i++) {
            int value = parseIntField(
                    medalFields[i],
                    ProfileFormat.MEDAL_NAMES[i],
                    0,
                    0xFFFF
            );

            currentProfile.setMedal(i, value);
        }

        for (int i = 0; i < ProfileFormat.STAT_COUNT; i++) {
            long value = parseLongField(
                    statFields[i],
                    ProfileFormat.STAT_NAMES[i],
                    0,
                    0xFFFF_FFFFL
            );

            currentProfile.setStat(i, value);
        }
    }

    private void updateRoteFromFields() {
        if (currentRoteSave == null) {
            throw new IllegalStateException("No ROTE campaign file is loaded.");
        }

        Object selectedItem = missionComboBox.getSelectedItem();

        if (!(selectedItem instanceof RoteCampaignMission selectedMission)) {
            throw new IllegalArgumentException("No campaign mission is selected.");
        }

        currentRoteSave.setRouteState(selectedMission.routeState());
    }

    private void markDirtyFromRoteSelection() {
        if (!suppressDirtyEvents && currentFileType == SaveFileType.ROTE) {
            setDirty(true);
        }
    }

    private int parseIntField(
            JTextField field,
            String label,
            int min,
            int max
    ) {
        String text = field.getText().trim();

        try {
            int value = Integer.parseInt(text);

            if (value < min || value > max) {
                throw new IllegalArgumentException(
                        label + " must be between " + min + " and " + max + "."
                );
            }

            return value;

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " must be a whole number.");
        }
    }

    private long parseLongField(
            JTextField field,
            String label,
            long min,
            long max
    ) {
        String text = field.getText().trim();

        try {
            long value = Long.parseLong(text);

            if (value < min || value > max) {
                throw new IllegalArgumentException(
                        label + " must be between " + min + " and " + max + "."
                );
            }

            return value;

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " must be a whole number.");
        }
    }

    private JFileChooser createSaveFileChooser() {
        JFileChooser chooser = new JFileChooser();

        Path startDirectory;

        if (currentPath != null && currentPath.getParent() != null) {
            startDirectory = currentPath.getParent();
        } else {
            startDirectory = ProfileLocations.findDefaultSaveDirectory();
        }

        chooser.setCurrentDirectory(startDirectory.toFile());

        FileNameExtensionFilter allSupportedFilter = new FileNameExtensionFilter(
                "Battlefront II save files (*.profile, *.rote)",
                "profile",
                "rote"
        );

        chooser.addChoosableFileFilter(allSupportedFilter);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                "Profile files (*.profile)",
                "profile"
        ));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                "Rise of the Empire saves (*.rote)",
                "rote"
        ));

        chooser.setFileFilter(allSupportedFilter);
        chooser.setDialogTitle("Open Battlefront II Save File");

        return chooser;
    }

    private File ensureExpectedExtension(File file, SaveFileType fileType) {
        String name = file.getName();
        String expectedExtension = fileType.extension();

        if (expectedExtension.isBlank()) {
            return file;
        }

        if (name.toLowerCase(Locale.ROOT).endsWith(expectedExtension)) {
            return file;
        }

        return new File(file.getParentFile(), name + expectedExtension);
    }

    private void attachDirtyListener(JTextField field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                markDirtyFromUserEdit();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                markDirtyFromUserEdit();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                markDirtyFromUserEdit();
            }
        });
    }

    private void markDirtyFromUserEdit() {
        if (!suppressDirtyEvents && currentFileType == SaveFileType.PROFILE) {
            setDirty(true);
        }
    }

    private void setDirty(boolean dirty) {
        this.dirty = dirty;
        updateTitle();
        updateButtonState();
    }

    private void updateTitle() {
        String title = WINDOW_TITLE;

        if (currentPath != null) {
            title += " - " + stripExtension(currentPath.getFileName().toString());
        }

        if (dirty) {
            title += " *";
        }

        setTitle(title);
    }

    private void updateButtonState() {
        boolean profileLoaded = currentFileType == SaveFileType.PROFILE;
        boolean roteLoaded = currentFileType == SaveFileType.ROTE;

        saveButton.setEnabled(profileLoaded && dirty);
        saveAsButton.setEnabled(profileLoaded);
        restoreButton.setEnabled(profileLoaded && currentPath != null);

        saveRoteButton.setEnabled(roteLoaded && dirty);
        restoreRoteButton.setEnabled(roteLoaded && currentPath != null);
    }

    private void setAllEditorFieldsEnabled(boolean enabled) {
        if (profileNameField != null) {
            profileNameField.setEnabled(enabled);
        }

        for (JTextField field : medalFields) {
            if (field != null) {
                field.setEnabled(enabled);
            }
        }

        for (JTextField field : statFields) {
            if (field != null) {
                field.setEnabled(enabled);
            }
        }

        if (missionComboBox != null) {
            missionComboBox.setEnabled(enabled);
        }
    }

    private void updateCurrentFileLabel() {
        if (currentPath == null || currentFileType == SaveFileType.UNKNOWN) {
            currentFileLabel.setText("No save file loaded");
            currentFileLabel.setToolTipText("No save file loaded");
            updateTitle();
            return;
        }

        String displayName;

        if (currentFileType == SaveFileType.PROFILE && currentProfile != null) {
            displayName = currentProfile.getProfileName();
        } else {
            displayName = stripExtension(currentPath.getFileName().toString());
        }

        currentFileLabel.setText(currentFileType.displayName() + ": " + displayName);
        currentFileLabel.setToolTipText(currentPath.toAbsolutePath().toString());

        updateTitle();
    }

    private String stripExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex <= 0) {
            return fileName;
        }

        return fileName.substring(0, dotIndex);
    }

    private void showEditorCard(String cardName) {
        editorCardLayout.show(editorCardPanel, cardName);
    }

    private void showBottomCard(String cardName) {
        bottomCardLayout.show(bottomCardPanel, cardName);
    }

    private boolean confirmDiscardUnsavedChanges() {
        if (!dirty) {
            return true;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "You have unsaved changes. Save them before continuing?",
                "Unsaved Changes",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
            return false;
        }

        if (choice == JOptionPane.YES_OPTION) {
            return saveCurrentFile();
        }

        return true;
    }

    private void exitApplication() {
        if (!confirmDiscardUnsavedChanges()) {
            return;
        }

        dispose();
        System.exit(0);
    }

    private void showError(String message, Exception exception) {
        String fullMessage = message;

        if (exception != null && exception.getMessage() != null) {
            fullMessage += "\n\n" + exception.getMessage();
        }

        JOptionPane.showMessageDialog(
                this,
                fullMessage,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}