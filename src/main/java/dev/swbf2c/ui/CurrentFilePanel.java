package dev.swbf2c.ui;

import dev.swbf2c.common.SaveFileType;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.nio.file.Path;

public final class CurrentFilePanel extends JPanel {
    private final JLabel fileValueLabel = new JLabel();
    private final JLabel typeValueLabel = new JLabel();

    public CurrentFilePanel(Runnable openAction) {
        super(new BorderLayout(12, 12));

        setBorder(BorderFactory.createTitledBorder("Current File"));

        JPanel labelsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = UiSupport.createDefaultConstraints();

        addRow(labelsPanel, constraints, 0, "File", fileValueLabel);
        addRow(labelsPanel, constraints, 1, "Type", typeValueLabel);

        JButton openButton = new JButton("Open Save File");
        UiSupport.styleButton(openButton);
        openButton.addActionListener(event -> openAction.run());

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 12));
        buttonPanel.add(openButton);

        add(labelsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.EAST);
    }

    public void clear() {
        fileValueLabel.setText("No save file loaded");
        fileValueLabel.setToolTipText(null);
        typeValueLabel.setText("-");
    }

    public void display(Path path, SaveFileType type, String displayName) {
        fileValueLabel.setText(displayName);
        fileValueLabel.setToolTipText(path.toAbsolutePath().toString());
        typeValueLabel.setText(type.displayName());
    }

    private void addRow(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String labelText,
            JLabel valueLabel
    ) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0;
        constraints.weighty = 0;

        panel.add(new JLabel(labelText), constraints);

        constraints.gridx = 1;
        constraints.gridy = row;
        constraints.weightx = 1;
        constraints.weighty = 0;

        valueLabel.setFont(valueLabel.getFont().deriveFont(java.awt.Font.BOLD));
        panel.add(valueLabel, constraints);
    }
}