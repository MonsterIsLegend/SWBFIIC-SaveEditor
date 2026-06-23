package dev.swbf2profile;

import javax.swing.*;

public final class App {
    public static void main(String[] args) {
        com.formdev.flatlaf.FlatDarkLaf.setup();

        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ProgressBar.arc", 10);

        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}