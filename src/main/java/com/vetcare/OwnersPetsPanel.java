
package com.vetcare;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

public class OwnersPetsPanel extends JPanel {
    private final DefaultTableModel ownerModel;
    private final DefaultTableModel petModel;
    private final JTextField search = new JTextField();

    public OwnersPetsPanel() {
        setLayout(new BorderLayout(0, 18));
        setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        search.putClientProperty("JTextField.placeholderText",
            "Search by owner, phone or pet name...");
        search.addActionListener(e -> refresh());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton addOwner = new JButton("+ Add Owner");
        JButton addPet = new JButton("+ Add Pet");

        addOwner.addActionListener(e -> showAddOwner());
        addPet.addActionListener(e -> showAddPet());

        actions.add(addOwner);
        actions.add(addPet);

        toolbar.add(search, BorderLayout.CENTER);
        toolbar.add(actions, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        ownerModel = createModel(
            "ID", "Full Name", "Phone", "Email"
        );
        petModel = createModel(
            "ID", "Pet Name", "Species", "Breed", "Age", "Owner"
        );

        JPanel tables = new JPanel(new GridLayout(2, 1, 0, 16));
        tables.setOpaque(false);

        tables.add(createTableCard("Owners", ownerModel));
        tables.add(createTableCard("Pets", petModel));

        add(tables, BorderLayout.CENTER);

        search.getDocument().addDocumentListener(
            new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    refresh();
                }
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    refresh();
                }
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    refresh();
                }
            }
        );

        refresh();
    }

    private DefaultTableModel createModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
    }

    private JPanel createTableCard(String title, DefaultTableModel model) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(225, 233, 233)),
            new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        card.add(heading, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setRowHeight(34);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(220, 242, 238));
        table.setSelectionForeground(new Color(25, 65, 60));
        table.setShowVerticalLines(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private void refresh() {
        String keyword = search.getText().trim().toLowerCase();
        ownerModel.setRowCount(0);
        petModel.setRowCount(0);

        for (Owner o : ClinicData.owners) {
            boolean match = (o.name + " " + o.phone + " " + o.email)
                .toLowerCase().contains(keyword);

            for (Pet p : ClinicData.pets) {
                if (p.ownerId == o.id &&
                    p.name.toLowerCase().contains(keyword)) {
                    match = true;
                }
            }

            if (match) {
                ownerModel.addRow(new Object[]{
                    o.id, o.name, o.phone, o.email
                });
            }
        }

        for (Pet p : ClinicData.pets) {
            Owner owner = ClinicData.getOwner(p.ownerId);
            String ownerName = owner == null ? "" : owner.name;
            String phone = owner == null ? "" : owner.phone;

            String text = (p.name + " " + p.species + " " +
                p.breed + " " + ownerName + " " + phone).toLowerCase();

            if (text.contains(keyword)) {
                petModel.addRow(new Object[]{
                    p.id, p.name, p.species, p.breed, p.age, ownerName
                });
            }
        }
    }

    private void showAddOwner() {
        JTextField name = new JTextField();
        JTextField phone = new JTextField();
        JTextField email = new JTextField();

        Object[] fields = {
            "Full Name:", name,
            "Phone:", phone,
            "Email:", email
        };

        int result = JOptionPane.showConfirmDialog(
            this, fields, "Add Owner",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return;

        String n = name.getText().trim();
        String p = phone.getText().trim();
        String e = email.getText().trim();

        if (n.isEmpty() || !p.matches("[0-9]{9,11}")) {
            JOptionPane.showMessageDialog(this,
                "Enter a name and a valid phone number (9-11 digits).");
            return;
        }

        try {
            ClinicData.addOwner(n, p, e);
            refresh();
            JOptionPane.showMessageDialog(this,
                "Owner added successfully!");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showAddPet() {
        if (ClinicData.owners.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add an owner first.");
            return;
        }

        JComboBox<Owner> ownerBox = new JComboBox<>(
            ClinicData.owners.toArray(new Owner[0])
        );
        JTextField name = new JTextField();
        JComboBox<String> species = new JComboBox<>(
            new String[]{"Dog", "Cat", "Other"}
        );
        JTextField breed = new JTextField();
        JSpinner age = new JSpinner(
            new SpinnerNumberModel(1, 0, 50, 1)
        );

        Object[] fields = {
            "Owner:", ownerBox,
            "Pet Name:", name,
            "Species:", species,
            "Breed:", breed,
            "Age:", age
        };

        int result = JOptionPane.showConfirmDialog(
            this, fields, "Add Pet",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return;

        if (name.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Pet name is required.");
            return;
        }

        Owner owner = (Owner) ownerBox.getSelectedItem();
        if (owner == null) return;

        ClinicData.addPet(
            owner.id,
            name.getText().trim(),
            (String) species.getSelectedItem(),
            breed.getText().trim(),
            (Integer) age.getValue()
        );

        refresh();
        JOptionPane.showMessageDialog(this,
            "Pet added successfully!");
    }
}
