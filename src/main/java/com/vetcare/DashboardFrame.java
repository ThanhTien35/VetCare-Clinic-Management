
package com.vetcare;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class DashboardFrame extends JFrame {

    private static final Color TEAL = new Color(15, 118, 110);
    private static final Color DARK = new Color(18, 74, 71);
    private static final Color BG = new Color(246, 249, 249);
    private static final Color TEXT = new Color(35, 55, 55);
    private static final Color MUTED = new Color(115, 130, 130);

    private final String role;
    private final JPanel content = new JPanel(new BorderLayout());
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    public DashboardFrame(String role) {
        super("VetCare | " + role);
        this.role = role;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 740);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        root.add(createSidebar(), BorderLayout.WEST);

        content.setBackground(BG);
        content.setBorder(new EmptyBorder(30, 35, 30, 35));

        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        showPage("Dashboard");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(DARK);
        sidebar.setBorder(new EmptyBorder(28, 16, 22, 16));
        sidebar.setPreferredSize(new Dimension(235, 0));

        JLabel brand = new JLabel("VetCare");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 28));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Clinic Management");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(180, 215, 210));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(subtitle);
        sidebar.add(Box.createVerticalStrut(35));

        addNav(sidebar, "Dashboard");

        if (!role.equals("Veterinarian")) {
            addNav(sidebar, "Owners & Pets");
            addNav(sidebar, "Appointments");
            addNav(sidebar, "Check-in & Queue");
            addNav(sidebar, "Invoices");
        }

        if (role.equals("Veterinarian")) {
            addNav(sidebar, "Examination / EMR");
            addNav(sidebar, "Prescriptions");
            addNav(sidebar, "Vaccinations");
        }

        if (role.equals("Admin")) {
            addNav(sidebar, "Services");
            addNav(sidebar, "Staff Accounts");
        }

        sidebar.add(Box.createVerticalGlue());

        JLabel user = new JLabel("Signed in: " + role);
        user.setForeground(new Color(200, 230, 225));
        user.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(user);
        sidebar.add(Box.createVerticalStrut(12));

        JButton logout = new JButton("Log out");
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        logout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        sidebar.add(logout);
        return sidebar;
    }

    private void addNav(JPanel sidebar, String page) {
        JButton button = new JButton(page);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(new Color(31, 93, 89));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(11, 14, 11, 14));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        button.addActionListener(e -> showPage(page));
        navButtons.put(page, button);

        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(8));
    }

    private void showPage(String page) {
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean selected = entry.getKey().equals(page);
            entry.getValue().setBackground(
                selected ? TEAL : new Color(31, 93, 89)
            );
            entry.getValue().setFont(new Font(
                "Segoe UI",
                selected ? Font.BOLD : Font.PLAIN,
                14
            ));
        }

        content.removeAll();

        JPanel pagePanel = new JPanel(new BorderLayout(0, 24));
        pagePanel.setOpaque(false);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(page);
        title.setFont(new Font("Segoe UI", Font.BOLD, 29));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(
            page.equals("Dashboard")
                ? "Welcome back! Here's your clinic overview."
                : "VetCare Clinic Management System"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(7));
        header.add(subtitle);

        pagePanel.add(header, BorderLayout.NORTH);

        if (page.equals("Dashboard")) {
            pagePanel.add(createDashboard(), BorderLayout.CENTER);
        } else if (page.equals("Owners & Pets")) {
            pagePanel.add(new OwnersPetsPanel(), BorderLayout.CENTER);
        } else if (page.equals("Appointments")) {
            pagePanel.add(new AppointmentsPanel(), BorderLayout.CENTER);
        } else if (page.equals("Check-in & Queue")) {
            pagePanel.add(new CheckInPanel(), BorderLayout.CENTER);
        } else if (page.equals("Examination / EMR")) {
            pagePanel.add(new ExaminationPanel(), BorderLayout.CENTER);
        } else if (page.equals("Prescriptions")) {
            pagePanel.add(new PrescriptionsPanel(), BorderLayout.CENTER);
        } else if (page.equals("Vaccinations")) {
            pagePanel.add(new VaccinationsPanel(), BorderLayout.CENTER);
        } else if (page.equals("Services") && role.equals("Admin")) {
            pagePanel.add(new ServicesPanel(), BorderLayout.CENTER);
        } else if (page.equals("Invoices")
                && !role.equals("Veterinarian")) {
            pagePanel.add(new InvoicesPanel(), BorderLayout.CENTER);
        } else {
        pagePanel.add(createPlaceholder(page), BorderLayout.CENTER);
        }

        content.add(pagePanel, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    private JPanel createDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout(0, 25));
        dashboard.setOpaque(false);

        JPanel stats = new JPanel(new GridLayout(1, 4, 15, 0));
        stats.setOpaque(false);

        if (role.equals("Veterinarian")) {
            stats.add(statCard("Today's Patients", "12", "Scheduled"));
            stats.add(statCard("Waiting", "05", "In queue"));
            stats.add(statCard("Completed", "07", "Today"));
            stats.add(statCard("Vaccinations", "03", "Upcoming"));
        } else {
            stats.add(statCard("Appointments", "24", "Today"));
            stats.add(statCard("Checked In", "08", "Arrived"));
            stats.add(statCard("Completed", "12", "Consultations"));
            stats.add(statCard("Pending Bills", "04", "Awaiting payment"));
        }

        dashboard.add(stats, BorderLayout.NORTH);

        JPanel tableCard = new JPanel(new BorderLayout(0, 18));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(new CompoundBorder(
            new LineBorder(new Color(225, 233, 233), 1, true),
            new EmptyBorder(22, 22, 22, 22)
        ));

        JLabel tableTitle = new JLabel(
            role.equals("Veterinarian")
                ? "Patient Queue"
                : "Recent Appointments"
        );
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        tableTitle.setForeground(TEXT);

        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {
            "ID", "Pet Name", "Owner", "Time", "Status"
        };

        Object[][] rows = {
            {"APT001", "Milo", "Nguyen An", "08:30", "Completed"},
            {"APT002", "Luna", "Tran Minh", "09:00", "Checked In"},
            {"APT003", "Coco", "Le Anh", "09:30", "Scheduled"},
            {"APT004", "Max", "Pham Ha", "10:00", "Scheduled"},
            {"APT005", "Bella", "Vo Linh", "10:30", "Waiting"}
        };

        DefaultTableModel model = new DefaultTableModel(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(43);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(235, 240, 240));
        table.setSelectionBackground(new Color(220, 242, 238));
        table.setSelectionForeground(new Color(25, 65, 60));
        table.setFillsViewportHeight(true);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHeader.setBackground(new Color(242, 247, 246));
        tableHeader.setForeground(TEXT);
        tableHeader.setPreferredSize(new Dimension(0, 42));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        tableCard.add(scroll, BorderLayout.CENTER);
        dashboard.add(tableCard, BorderLayout.CENTER);

        return dashboard;
    }

    private JPanel statCard(String title, String value, String description) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(225, 233, 233), 1, true),
            new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel label = new JLabel(title);
        label.setForeground(MUTED);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel number = new JLabel(value);
        number.setForeground(TEAL);
        number.setFont(new Font("Segoe UI", Font.BOLD, 30));

        JLabel detail = new JLabel(description);
        detail.setForeground(MUTED);
        detail.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        card.add(label, BorderLayout.NORTH);
        card.add(number, BorderLayout.CENTER);
        card.add(detail, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createPlaceholder(String page) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(225, 233, 233), 1, true),
            new EmptyBorder(30, 30, 30, 30)
        ));

        JLabel title = new JLabel(page);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT);

        JLabel description = new JLabel(
            "This module will be implemented in the next stages."
        );
        description.setForeground(MUTED);
        description.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(title);
        top.add(Box.createVerticalStrut(12));
        top.add(description);

        card.add(top, BorderLayout.NORTH);
        return card;
    }
}
