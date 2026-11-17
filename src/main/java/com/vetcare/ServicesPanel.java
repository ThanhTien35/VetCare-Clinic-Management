
package com.vetcare;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class ServicesPanel extends JPanel {
    private final DefaultTableModel model;

    public ServicesPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        JButton addButton = new JButton("+ Add Service");
        addButton.addActionListener(e -> addService());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        toolbar.setOpaque(false);
        toolbar.add(addButton);
        add(toolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(
            new String[]{"ID", "Service Name", "Price (VND)"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(38);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(220, 242, 238));
        table.setSelectionForeground(new Color(25, 65, 60));

        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Service Catalog");
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));

        card.add(title, BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        refresh();
    }

    private void refresh() {
        model.setRowCount(0);
        for (ServiceItem s : ClinicData.services) {
            model.addRow(new Object[]{
                s.getCode(), s.name, String.format("%,d", s.price)
            });
        }
    }

    private void addService() {
        JTextField name = new JTextField();
        JTextField price = new JTextField();

        Object[] fields = {
            "Service Name:", name,
            "Price (VND):", price
        };

        int result = JOptionPane.showConfirmDialog(
            this, fields, "Add Service",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return;

        try {
            long amount = Long.parseLong(price.getText().trim());
            ClinicData.addService(name.getText(), amount);
            refresh();

            JOptionPane.showMessageDialog(
                this, "Service added successfully!"
            );
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                this, "Price must be a whole number, e.g. 150000."
            );
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}
