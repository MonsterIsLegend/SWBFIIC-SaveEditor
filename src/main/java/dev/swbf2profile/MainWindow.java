package dev.swbf2profile;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

public final class MainWindow extends JFrame {
    private static final String WINDOW_TITLE = "SWBFIIC SaveEditor";

    private final ProfileFileService fileService = new ProfileFileService();

    private BattlefrontProfile currentProfile;
    private Path currentPath;

    private boolean dirty;
    private boolean suppressDirtyEvents;

    private final JTextField[] medalFields =
            new JTextField[ProfileFormat.MEDAL_COUNT];

    private final JTextField[] statFields =
            new JTextField[ProfileFormat.STAT_COUNT];

    private final JLabel profileNameLabel = new JLabel("No profile loaded");

    private final JButton openButton = new JButton("Open Profile");
    private final JButton saveButton = new JButton("Save");
    private final JButton saveAsButton = new JButton("Save As");
    private final JButton restoreButton = new JButton("Restore Backup");

    public MainWindow() {
        super(WINDOW_TITLE);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setContentPane(createRootPanel());

        configureActions();

        setFieldsEnabled(false);
        updateButtonState();
        updateTitle();
        updateProfileNameLabel();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                exitApplication();
            }
        });

        setMinimumSize(new Dimension(760, 460));
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createRootPanel() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBorder(new EmptyBorder(18, 22, 18, 22));

        root.add(createHeaderPanel(), BorderLayout.NORTH);
        root.add(createEditorPanel(), BorderLayout.CENTER);
        root.add(createBottomPanel(), BorderLayout.SOUTH);

        return root;
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(createProfileInfoPanel(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createProfileInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Current Profile"),
                new EmptyBorder(10, 12, 10, 12)
        ));

        profileNameLabel.setFont(profileNameLabel.getFont().deriveFont(Font.BOLD, 18.0f));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.add(openButton);

        panel.add(profileNameLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createEditorPanel() {
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

        JLabel label = new JLabel(labelText);
        panel.add(label, gbc);

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

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.add(restoreButton);
        buttonPanel.add(saveAsButton);
        buttonPanel.add(saveButton);

        JLabel hintLabel = new JLabel("A .bak file is created automatically before overwriting an existing profile.");
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

        openButton.addActionListener(event -> openProfile());
        saveButton.addActionListener(event -> saveCurrentProfile());
        saveAsButton.addActionListener(event -> saveCurrentProfileAs());
        restoreButton.addActionListener(event -> restoreBackup());
    }

    private void styleButton(JButton button) {
        button.setFocusable(false);
        button.setFocusPainted(false);
        button.setDefaultCapable(false);
        button.putClientProperty("JButton.buttonType", "roundRect");
    }

    private void openProfile() {
        if (!confirmDiscardUnsavedChanges()) {
            return;
        }

        JFileChooser chooser = createProfileFileChooser();

        int result = chooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path path = chooser.getSelectedFile().toPath();

        try {
            BattlefrontProfile profile = fileService.load(path);

            currentPath = path;
            currentProfile = profile;

            displayProfile(profile);
            setFieldsEnabled(true);
            setDirty(false);
            updateProfileNameLabel();

        } catch (IOException | IllegalArgumentException exception) {
            showError("Could not load profile.", exception);
        }
    }

    private boolean saveCurrentProfile() {
        if (currentProfile == null || currentPath == null) {
            showError("No profile is currently loaded.", null);
            return false;
        }

        try {
            updateProfileFromFields();
            fileService.save(currentPath, currentProfile);

            setDirty(false);
            updateProfileNameLabel();

            JOptionPane.showMessageDialog(
                    this,
                    "Profile saved successfully.",
                    "Saved",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return true;

        } catch (IOException | IllegalArgumentException exception) {
            showError("Could not save profile.", exception);
            return false;
        }
    }

    private boolean saveCurrentProfileAs() {
        if (currentProfile == null) {
            showError("No profile is currently loaded.", null);
            return false;
        }

        JFileChooser chooser = createProfileFileChooser();

        if (currentPath != null) {
            chooser.setSelectedFile(currentPath.toFile());
        }

        int result = chooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File selectedFile = chooser.getSelectedFile();
        Path selectedPath = ensureProfileExtension(selectedFile).toPath();

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
            updateProfileFromFields();
            fileService.saveAs(selectedPath, currentProfile);

            currentPath = selectedPath;

            setDirty(false);
            updateProfileNameLabel();

            JOptionPane.showMessageDialog(
                    this,
                    "Profile saved successfully.",
                    "Saved",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return true;

        } catch (IOException | IllegalArgumentException exception) {
            showError("Could not save profile.", exception);
            return false;
        }
    }

    private void restoreBackup() {
        if (currentPath == null) {
            showError("No profile is currently loaded.", null);
            return;
        }

        if (!fileService.backupExists(currentPath)) {
            showError("No backup exists for this profile yet.", null);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Restore the .bak file for this profile?\n\n"
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
            fileService.restoreBackup(currentPath);

            currentProfile = fileService.load(currentPath);

            displayProfile(currentProfile);
            setDirty(false);
            updateProfileNameLabel();

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

    private void updateProfileFromFields() {
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

    private JFileChooser createProfileFileChooser() {
        JFileChooser chooser = new JFileChooser();

        Path startDirectory;

        if (currentPath != null && currentPath.getParent() != null) {
            startDirectory = currentPath.getParent();
        } else {
            startDirectory = ProfileLocations.findDefaultSaveDirectory();
        }

        chooser.setCurrentDirectory(startDirectory.toFile());

        chooser.setFileFilter(new FileNameExtensionFilter(
                "Battlefront II profile files (*.profile)",
                "profile"
        ));

        chooser.setDialogTitle("Open Battlefront II Profile");

        return chooser;
    }

    private File ensureProfileExtension(File file) {
        String name = file.getName();

        if (name.toLowerCase(Locale.ROOT).endsWith(".profile")) {
            return file;
        }

        return new File(file.getParentFile(), name + ".profile");
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
        if (!suppressDirtyEvents && currentProfile != null) {
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
            String profileName = stripProfileExtension(
                    currentPath.getFileName().toString()
            );

            title += " - " + profileName;
        }

        if (dirty) {
            title += " *";
        }

        setTitle(title);
    }

    private void updateButtonState() {
        boolean profileLoaded = currentProfile != null;

        saveButton.setEnabled(profileLoaded && dirty);
        saveAsButton.setEnabled(profileLoaded);
        restoreButton.setEnabled(profileLoaded && currentPath != null);
    }

    private void setFieldsEnabled(boolean enabled) {
        for (JTextField field : medalFields) {
            field.setEnabled(enabled);
        }

        for (JTextField field : statFields) {
            field.setEnabled(enabled);
        }
    }

    private void updateProfileNameLabel() {
        if (currentPath == null) {
            profileNameLabel.setText("No profile loaded");
            profileNameLabel.setToolTipText("No profile loaded");
            updateTitle();
            return;
        }

        String fileName = currentPath.getFileName().toString();
        String displayName = stripProfileExtension(fileName);

        profileNameLabel.setText(displayName);
        profileNameLabel.setToolTipText(currentPath.toAbsolutePath().toString());

        updateTitle();
    }

    private String stripProfileExtension(String fileName) {
        if (fileName.toLowerCase(Locale.ROOT).endsWith(".profile")) {
            return fileName.substring(
                    0,
                    fileName.length() - ".profile".length()
            );
        }

        return fileName;
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
            return saveCurrentProfile();
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