package dev.swbf2c.ui;

import dev.swbf2c.common.SaveFileType;
import dev.swbf2c.gc.GcConquestSave;
import dev.swbf2c.gc.GcFileService;
import dev.swbf2c.profile.BattlefrontProfile;
import dev.swbf2c.profile.ProfileFileService;
import dev.swbf2c.profile.ProfileLocations;
import dev.swbf2c.rote.RoteCampaignSave;
import dev.swbf2c.rote.RoteFileService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class MainWindow extends JFrame {
    private static final String APP_TITLE = "SWBFIIC SaveEditor";

    private static final String EDITOR_EMPTY = "empty";
    private static final String EDITOR_PROFILE = "profile";
    private static final String EDITOR_ROTE = "rote";
    private static final String EDITOR_GC = "gc";

    private final ProfileFileService profileFileService = new ProfileFileService();
    private final RoteFileService roteFileService = new RoteFileService();
    private final GcFileService gcFileService = new GcFileService();

    private final CardLayout editorCardLayout = new CardLayout();
    private final JPanel editorCards = new JPanel(editorCardLayout);

    private CurrentFilePanel currentFilePanel;
    private ProfileEditorPanel profileEditorPanel;
    private RoteEditorPanel roteEditorPanel;
    private GcEditorPanel gcEditorPanel;
    private JLabel statusLabel;

    private Path currentPath;
    private SaveFileType currentFileType = SaveFileType.UNKNOWN;

    private BattlefrontProfile currentProfile;
    private RoteCampaignSave currentRoteSave;
    private GcConquestSave currentGcSave;

    private boolean dirty;

    public MainWindow() {
        super(APP_TITLE);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 820));
        setPreferredSize(new Dimension(1400, 900));

        buildUi();
        installWindowCloseHandler();

        pack();
        setLocationRelativeTo(null);
        updateWindowTitle();
    }

    private void buildUi() {
        JPanel rootPanel = new JPanel(new BorderLayout(12, 12));
        rootPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        currentFilePanel = new CurrentFilePanel(this::openSaveFile);

        profileEditorPanel = new ProfileEditorPanel(
                this::markDirty,
                this::saveCurrentFile,
                this::saveProfileAs,
                this::restoreCurrentFileBackup
        );

        roteEditorPanel = new RoteEditorPanel(
                this::markDirty,
                this::saveCurrentFile,
                this::restoreCurrentFileBackup
        );

        gcEditorPanel = new GcEditorPanel(
                this::markDirty,
                this::saveCurrentFile,
                this::restoreCurrentFileBackup
        );

        editorCards.add(createEmptyEditorPanel(), EDITOR_EMPTY);
        editorCards.add(profileEditorPanel, EDITOR_PROFILE);
        editorCards.add(roteEditorPanel, EDITOR_ROTE);
        editorCards.add(gcEditorPanel, EDITOR_GC);

        statusLabel = new JLabel("Ready.");

        JPanel currentFileWrapper = new JPanel(new BorderLayout());
        currentFileWrapper.setBorder(new EmptyBorder(0, 16, 0, 16));
        currentFileWrapper.add(currentFilePanel, BorderLayout.CENTER);

        rootPanel.add(currentFileWrapper, BorderLayout.NORTH);
        rootPanel.add(editorCards, BorderLayout.CENTER);
        rootPanel.add(statusLabel, BorderLayout.SOUTH);

        setContentPane(rootPanel);

        currentFilePanel.clear();
        editorCardLayout.show(editorCards, EDITOR_EMPTY);
    }

    private JPanel createEmptyEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBorder(new EmptyBorder(32, 32, 32, 32));

        JLabel titleLabel = new JLabel("Open a Battlefront II Classic save file to begin.");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20.0f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 16, 16));

        cardsPanel.add(createStartCard(
                "Profile",
                ".profile",
                "Edit profile name, medals, player points, kills, and deaths."
        ));

        cardsPanel.add(createStartCard(
                "Rise of the Empire",
                ".rote",
                "Select the current campaign mission. Useful for skipping broken campaign states."
        ));

        cardsPanel.add(createStartCard(
                "Galactic Conquest",
                ".gc",
                "Edit credits, bonuses, controlled planets, fleets, and unlocked units."
        ));

        JPanel contentPanel = new JPanel(new BorderLayout(16, 16));
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(cardsPanel, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.NORTH);

        return panel;
    }

    private JPanel createStartCard(
            String title,
            String extension,
            String description
    ) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setPreferredSize(new Dimension(320, 105));

        JLabel extensionLabel = new JLabel(extension, SwingConstants.CENTER);
        extensionLabel.setFont(extensionLabel.getFont().deriveFont(Font.BOLD, 20.0f));

        JLabel descriptionLabel = new JLabel(
                "<html><div style='text-align:center;'>"
                        + description
                        + "</div></html>",
                SwingConstants.CENTER
        );

        panel.add(extensionLabel, BorderLayout.NORTH);
        panel.add(descriptionLabel, BorderLayout.CENTER);

        return panel;
    }

    private void openSaveFile() {
        if (!confirmDiscardUnsavedChanges()) {
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Battlefront II Save File");
        fileChooser.setFileFilter(
                new FileNameExtensionFilter(
                        "Battlefront II save files (*.profile, *.rote, *.gc)",
                        "profile",
                        "rote",
                        "gc"
                )
        );

        fileChooser.setCurrentDirectory(findInitialSaveDirectory().toFile());

        int result = fileChooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path selectedPath = fileChooser.getSelectedFile().toPath();

        try {
            loadSaveFile(selectedPath);
            setStatus("Loaded " + selectedPath.getFileName());
        } catch (RuntimeException | IOException exception) {
            showError("Could not open save file.", exception);
        }
    }

    private Path findInitialSaveDirectory() {
        if (currentPath != null && currentPath.getParent() != null) {
            return currentPath.getParent();
        }

        return ProfileLocations.findDefaultSaveDirectory();
    }

    private void loadSaveFile(Path path) throws IOException {
        SaveFileType detectedType = SaveFileType.detect(path);

        switch (detectedType) {
            case PROFILE -> {
                currentProfile = profileFileService.load(path);
                currentRoteSave = null;
                currentGcSave = null;

                currentPath = path;
                currentFileType = SaveFileType.PROFILE;

                profileEditorPanel.display(
                        currentProfile,
                        profileFileService.backupExists(currentPath)
                );

                editorCardLayout.show(editorCards, EDITOR_PROFILE);
            }

            case ROTE -> {
                currentRoteSave = roteFileService.load(path);
                currentProfile = null;
                currentGcSave = null;

                currentPath = path;
                currentFileType = SaveFileType.ROTE;

                roteEditorPanel.display(
                        currentRoteSave,
                        roteFileService.backupExists(currentPath)
                );

                editorCardLayout.show(editorCards, EDITOR_ROTE);
            }

            case GC -> {
                currentGcSave = gcFileService.load(path);
                currentProfile = null;
                currentRoteSave = null;

                currentPath = path;
                currentFileType = SaveFileType.GC;

                gcEditorPanel.display(
                        currentGcSave,
                        gcFileService.backupExists(currentPath)
                );

                editorCardLayout.show(editorCards, EDITOR_GC);
            }

            case UNKNOWN -> throw new IllegalArgumentException("Unsupported save file type.");
        }

        setDirty(false);
        updateCurrentFilePanel();
    }

    private void saveCurrentFile() {
        if (currentPath == null || currentFileType == SaveFileType.UNKNOWN) {
            return;
        }

        try {
            switch (currentFileType) {
                case PROFILE -> {
                    profileEditorPanel.writeTo(currentProfile);

                    currentPath = profileFileService.saveWithPossibleRename(
                            currentPath,
                            currentProfile
                    );

                    currentProfile = profileFileService.load(currentPath);

                    profileEditorPanel.display(
                            currentProfile,
                            profileFileService.backupExists(currentPath)
                    );

                    editorCardLayout.show(editorCards, EDITOR_PROFILE);
                }

                case ROTE -> {
                    roteEditorPanel.writeTo(currentRoteSave);

                    roteFileService.save(currentPath, currentRoteSave);

                    currentRoteSave = roteFileService.load(currentPath);

                    roteEditorPanel.display(
                            currentRoteSave,
                            roteFileService.backupExists(currentPath)
                    );

                    editorCardLayout.show(editorCards, EDITOR_ROTE);
                }

                case GC -> {
                    gcEditorPanel.writeTo(currentGcSave);

                    gcFileService.save(currentPath, currentGcSave);

                    currentGcSave = gcFileService.load(currentPath);

                    gcEditorPanel.display(
                            currentGcSave,
                            gcFileService.backupExists(currentPath)
                    );

                    editorCardLayout.show(editorCards, EDITOR_GC);
                }

                case UNKNOWN -> throw new IllegalStateException("No supported save file is loaded.");
            }

            setDirty(false);
            updateCurrentFilePanel();
            setStatus("Saved " + currentPath.getFileName());

        } catch (RuntimeException | IOException exception) {
            showError("Could not save file.", exception);
        }
    }

    private void saveProfileAs() {
        if (currentProfile == null) {
            return;
        }

        try {
            profileEditorPanel.writeTo(currentProfile);
        } catch (RuntimeException exception) {
            showError("Could not save profile.", exception);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Profile As");
        fileChooser.setFileFilter(
                new FileNameExtensionFilter(
                        "Battlefront II profile (*.profile)",
                        "profile"
                )
        );

        fileChooser.setCurrentDirectory(findInitialSaveDirectory().toFile());
        fileChooser.setSelectedFile(new File(currentProfile.getProfileName() + ".profile"));

        int result = fileChooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path selectedPath = UiSupport.ensureExtension(
                fileChooser.getSelectedFile().toPath(),
                ".profile"
        );

        try {
            profileFileService.saveAs(selectedPath, currentProfile);

            currentPath = selectedPath;
            currentFileType = SaveFileType.PROFILE;
            currentProfile = profileFileService.load(currentPath);

            profileEditorPanel.display(
                    currentProfile,
                    profileFileService.backupExists(currentPath)
            );

            editorCardLayout.show(editorCards, EDITOR_PROFILE);

            setDirty(false);
            updateCurrentFilePanel();
            setStatus("Saved " + currentPath.getFileName());

        } catch (RuntimeException | IOException exception) {
            showError("Could not save profile as new file.", exception);
        }
    }

    private void restoreCurrentFileBackup() {
        if (currentPath == null || currentFileType == SaveFileType.UNKNOWN) {
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "Restore from the .bak backup for this save file?",
                "Restore Backup",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            switch (currentFileType) {
                case PROFILE -> {
                    profileFileService.restoreBackup(currentPath);

                    currentProfile = profileFileService.load(currentPath);

                    profileEditorPanel.display(
                            currentProfile,
                            profileFileService.backupExists(currentPath)
                    );

                    editorCardLayout.show(editorCards, EDITOR_PROFILE);
                }

                case ROTE -> {
                    roteFileService.restoreBackup(currentPath);

                    currentRoteSave = roteFileService.load(currentPath);

                    roteEditorPanel.display(
                            currentRoteSave,
                            roteFileService.backupExists(currentPath)
                    );

                    editorCardLayout.show(editorCards, EDITOR_ROTE);
                }

                case GC -> {
                    gcFileService.restoreBackup(currentPath);

                    currentGcSave = gcFileService.load(currentPath);

                    gcEditorPanel.display(
                            currentGcSave,
                            gcFileService.backupExists(currentPath)
                    );

                    editorCardLayout.show(editorCards, EDITOR_GC);
                }

                case UNKNOWN -> throw new IllegalStateException("No supported save file is loaded.");
            }

            setDirty(false);
            updateCurrentFilePanel();
            setStatus("Restored backup for " + currentPath.getFileName());

        } catch (RuntimeException | IOException exception) {
            showError("Could not restore backup.", exception);
        }
    }

    private void updateCurrentFilePanel() {
        if (currentPath == null) {
            currentFilePanel.clear();
            return;
        }

        String displayName;

        if (currentFileType == SaveFileType.PROFILE && currentProfile != null) {
            displayName = currentProfile.getProfileName();
        } else {
            displayName = UiSupport.stripExtension(currentPath.getFileName().toString());
        }

        currentFilePanel.display(currentPath, currentFileType, displayName);
    }

    private void markDirty() {
        if (currentPath == null || currentFileType == SaveFileType.UNKNOWN) {
            return;
        }

        setDirty(true);
    }

    private void setDirty(boolean dirty) {
        this.dirty = dirty;

        profileEditorPanel.setSaveEnabled(dirty && currentFileType == SaveFileType.PROFILE);
        roteEditorPanel.setSaveEnabled(dirty && currentFileType == SaveFileType.ROTE);
        gcEditorPanel.setSaveEnabled(dirty && currentFileType == SaveFileType.GC);

        updateWindowTitle();
    }

    private void updateWindowTitle() {
        setTitle(APP_TITLE + (dirty ? " *" : ""));
    }

    private boolean confirmDiscardUnsavedChanges() {
        if (!dirty) {
            return true;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "You have unsaved changes. Discard them?",
                "Unsaved Changes",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        return result == JOptionPane.YES_OPTION;
    }

    private void installWindowCloseHandler() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                if (confirmDiscardUnsavedChanges()) {
                    dispose();
                }
            }
        });
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void showError(String message, Exception exception) {
        String details = exception.getMessage();

        if (details == null || details.isBlank()) {
            details = exception.getClass().getSimpleName();
        }

        JOptionPane.showMessageDialog(
                this,
                message + "\n\n" + details,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}