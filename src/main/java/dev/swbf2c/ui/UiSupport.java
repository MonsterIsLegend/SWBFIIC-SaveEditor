package dev.swbf2c.ui;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.nio.file.Path;

final class UiSupport {
    private UiSupport() {}

    static GridBagConstraints createDefaultConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 8, 5, 8);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.weighty = 0;
        return constraints;
    }

    static void styleButton(JButton button) {
        button.setFocusable(false);
        button.setFocusPainted(false);
        button.setDefaultCapable(false);

        button.putClientProperty("JButton.buttonType", "roundRect");
        button.putClientProperty("JComponent.minimumHeight", 34);
        button.putClientProperty("JComponent.minimumWidth", 120);

        button.setMargin(new Insets(7, 18, 7, 18));

        Dimension preferredSize = button.getPreferredSize();

        int width = Math.max(preferredSize.width, 120);
        int height = Math.max(preferredSize.height, 34);

        button.setPreferredSize(new Dimension(width, height));
        button.setMinimumSize(new Dimension(width, height));
    }

    static void addValueRow(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String labelText,
            JLabel valueLabel
    ) {
        addComponentRow(panel, constraints, row, labelText, valueLabel);
    }

    static void addFieldRow(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String labelText,
            JTextField field
    ) {
        addComponentRow(panel, constraints, row, labelText, field);
    }

    static void addComboRow(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String labelText,
            JComboBox<?> comboBox
    ) {
        addComponentRow(panel, constraints, row, labelText, comboBox);
    }

    private static void addComponentRow(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String labelText,
            JComponent component
    ) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(labelText), constraints);

        constraints.gridx = 1;
        constraints.gridy = row;
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, constraints);
    }

    static void addVerticalFiller(
            JPanel panel,
            GridBagConstraints constraints,
            int row
    ) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel(), constraints);
        constraints.gridwidth = 1;
    }

    static void attachDirtyTracking(JTextField field, Runnable dirtyAction) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                dirtyAction.run();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                dirtyAction.run();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                dirtyAction.run();
            }
        });
    }

    static void attachDirtyTracking(JComboBox<?> comboBox, Runnable dirtyAction) {
        comboBox.addActionListener(event -> dirtyAction.run());
    }

    static int parseNonNegativeInt(String text, String fieldName) {
        return parseIntInRange(text, fieldName, 0, Integer.MAX_VALUE);
    }

    static int parseIntInRange(
            String text,
            String fieldName,
            int min,
            int max
    ) {
        try {
            int value = Integer.parseInt(text.trim());

            if (value < min || value > max) {
                throw new IllegalArgumentException(
                        fieldName + " must be between " + min + " and " + max + "."
                );
            }

            return value;

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " must be a whole number.");
        }
    }

    static long parseLongInRange(
            String text,
            String fieldName,
            long min,
            long max
    ) {
        try {
            long value = Long.parseLong(text.trim());

            if (value < min || value > max) {
                throw new IllegalArgumentException(
                        fieldName + " must be between " + min + " and " + max + "."
                );
            }

            return value;

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " must be a whole number.");
        }
    }

    static Path ensureExtension(Path path, String extension) {
        String fileName = path.getFileName().toString().toLowerCase();

        if (fileName.endsWith(extension)) {
            return path;
        }

        return path.resolveSibling(path.getFileName() + extension);
    }

    static String stripExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex <= 0) {
            return fileName;
        }

        return fileName.substring(0, dotIndex);
    }

    static String enumDisplayName(Enum<?> enumValue) {
        String raw = enumValue.name().toLowerCase().replace('_', ' ');
        StringBuilder result = new StringBuilder();

        boolean capitalizeNext = true;

        for (int index = 0; index < raw.length(); index++) {
            char character = raw.charAt(index);

            if (Character.isWhitespace(character)) {
                result.append(character);
                capitalizeNext = true;
                continue;
            }

            if (capitalizeNext) {
                result.append(Character.toUpperCase(character));
                capitalizeNext = false;
            } else {
                result.append(character);
            }
        }

        return result.toString();
    }
}