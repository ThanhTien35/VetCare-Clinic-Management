package com.vetcare;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        DatabaseManager.initialize();
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
