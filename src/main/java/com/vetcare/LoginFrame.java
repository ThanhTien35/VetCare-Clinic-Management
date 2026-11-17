package com.vetcare;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class LoginFrame extends JFrame {
    private static final Color TEAL = new Color(15, 118, 110);
    private final JTextField username = new JTextField(20);
    private final JPasswordField password = new JPasswordField(20);
    private final JLabel message = new JLabel(" ");
    private static final Map<String, String[]> ACCOUNTS = Map.of(
        "admin", new String[]{"admin123", "Admin"},
        "receptionist", new String[]{"reception123", "Receptionist"},
        "vet", new String[]{"vet123", "Veterinarian"}
    );

    public LoginFrame() {
        super("VetCare | Sign in");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 500);
        setMinimumSize(new Dimension(450, 450));
        setLocationRelativeTo(null);
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(new Color(241, 247, 246));
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(35, 45, 35, 45));
        form.setPreferredSize(new Dimension(380, 390));
        JLabel logo = new JLabel("VetCare");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        logo.setForeground(TEAL);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Clinic Management System");
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(logo); form.add(Box.createVerticalStrut(4)); form.add(subtitle);
        form.add(Box.createVerticalStrut(26));
        form.add(fieldLabel("Username")); form.add(Box.createVerticalStrut(7));
        username.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        form.add(username); form.add(Box.createVerticalStrut(16));
        form.add(fieldLabel("Password")); form.add(Box.createVerticalStrut(7));
        password.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        form.add(password); form.add(Box.createVerticalStrut(18));
        JButton signIn = new JButton("Sign In");
        signIn.setBackground(TEAL); signIn.setForeground(Color.WHITE);
        signIn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        signIn.setAlignmentX(Component.LEFT_ALIGNMENT);
        signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        signIn.addActionListener(e -> login());
        form.add(signIn);
        message.setForeground(new Color(185, 45, 45));
        message.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(Box.createVerticalStrut(10)); form.add(message);
        outer.add(form);
        setContentPane(outer);
        getRootPane().setDefaultButton(signIn);
    }

    private JLabel fieldLabel(String label) {
        JLabel result = new JLabel(label);
        result.setAlignmentX(Component.LEFT_ALIGNMENT);
        return result;
    }

    private void login() {
        String name = username.getText().trim().toLowerCase();
        String[] account = ACCOUNTS.get(name);
        if (account == null || !account[0].equals(new String(password.getPassword()))) {
            message.setText("Invalid username or password.");
            return;
        }
        new DashboardFrame(account[1]).setVisible(true);
        dispose();
    }
}
