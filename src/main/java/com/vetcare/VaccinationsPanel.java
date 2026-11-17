
package com.vetcare;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class VaccinationsPanel extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField search = new JTextField();

    private static final Color TEAL = new Color(15, 118, 110);
    private static final Color TEXT = new Color(35, 55, 55);

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    public VaccinationsPanel() {
        setLayout(new BorderLayout(0, 18));
        setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        search.putClientProperty(
            "JTextField.placeholderText",
            "Search by pet, owner or vaccine..."
        );

        JButton addButton = new JButton("+ Record Vaccination");
        addButton.setBackground(TEAL);
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> showAddDialog());

        toolbar.add(search, BorderLayout.CENTER);
        toolbar.add(addButton, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(
            new String[]{
                "ID", "Pet", "Owner", "Vaccine",
                "Date", "Next Booster", "Status"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setSelectionBackground(new Color(220, 242, 238));
        table.setSelectionForeground(new Color(25, 65, 60));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 42));

        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(225, 233, 233)),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Vaccination History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        card.add(title, BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton viewButton = new JButton("View Details");
        viewButton.addActionListener(e -> showDetails());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(viewButton);
        card.add(actions, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);

        search.getDocument().addDocumentListener(
            new javax.swing.event.DocumentListener() {
                public void insertUpdate(
                    javax.swing.event.DocumentEvent e) { refresh(); }
                public void removeUpdate(
                    javax.swing.event.DocumentEvent e) { refresh(); }
                public void changedUpdate(
                    javax.swing.event.DocumentEvent e) { refresh(); }
            }
        );

        refresh();
    }

    private void refresh() {
        model.setRowCount(0);
        String keyword = search.getText().trim().toLowerCase();

        for (Vaccination v : ClinicData.vaccinations) {
            Pet pet = ClinicData.getPet(v.petId);
            if (pet == null) continue;

            Owner owner = ClinicData.getOwner(pet.ownerId);
            String ownerName = owner == null ? "" : owner.name;

            String searchable = (
                pet.name + " " + ownerName + " " + v.vaccineName
            ).toLowerCase();

            if (!searchable.contains(keyword)) continue;

            model.addRow(new Object[]{
                v.getCode(),
                pet.name,
                ownerName,
                v.vaccineName,
                v.getDate(),
                v.getNextDueDate(),
                v.getStatus()
            });
        }
    }

    private void showAddDialog() {
        if (ClinicData.pets.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Please add a pet first."
            );
            return;
        }

        JComboBox<String> petBox = new JComboBox<>();

        for (Pet p : ClinicData.pets) {
            Owner owner = ClinicData.getOwner(p.ownerId);
            String ownerName = owner == null ? "" : owner.name;
            petBox.addItem(
                p.id + " - " + p.name + " (" + ownerName + ")"
            );
        }

        JTextField vaccineName = new JTextField();
        JTextField date = new JTextField(
            LocalDate.now().format(DATE_FORMAT)
        );
        JTextField nextDue = new JTextField();
        JTextField notes = new JTextField();

        Object[] fields = {
            "Pet:", petBox,
            "Vaccine Name:", vaccineName,
            "Vaccination Date (dd/MM/yyyy):", date,
            "Next Booster (optional):", nextDue,
            "Notes:", notes
        };

        int result = JOptionPane.showConfirmDialog(
            this, fields, "Record Vaccination",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return;

        try {
            String selected = (String) petBox.getSelectedItem();
            if (selected == null) return;

            int petId = Integer.parseInt(
                selected.substring(0, selected.indexOf(" - "))
            );

            LocalDate vaccinationDate = LocalDate.parse(
                date.getText().trim(), DATE_FORMAT
            );

            LocalDate nextDueDate = null;

            if (!nextDue.getText().isBlank()) {
                nextDueDate = LocalDate.parse(
                    nextDue.getText().trim(), DATE_FORMAT
                );
            }

            ClinicData.addVaccination(
                petId,
                vaccineName.getText(),
                vaccinationDate,
                nextDueDate,
                notes.getText()
            );

            refresh();

            JOptionPane.showMessageDialog(
                this, "Vaccination recorded successfully!"
            );

        } catch (DateTimeException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Invalid date. Please use dd/MM/yyyy.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                this, ex.getMessage(),
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void showDetails() {
        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(
                this, "Please select a vaccination record."
            );
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        String code = model.getValueAt(modelRow, 0).toString();

        for (Vaccination v : ClinicData.vaccinations) {
            if (v.getCode().equals(code)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Vaccination: " + v.getCode()
                    + "\nVaccine: " + v.vaccineName
                    + "\nDate: " + v.getDate()
                    + "\nNext Booster: " + v.getNextDueDate()
                    + "\nStatus: " + v.getStatus()
                    + "\nNotes: " + v.notes,
                    "Vaccination Details",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }
        }
    }
}
