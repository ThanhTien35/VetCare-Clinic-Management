
package com.vetcare;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

public class AppointmentsPanel extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField search = new JTextField();
    private final JComboBox<String> statusFilter =
        new JComboBox<>(new String[]{
            "All Status", "Scheduled", "Cancelled",
            "Checked In", "Completed"
        });

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    public AppointmentsPanel() {
        setLayout(new BorderLayout(0, 18));
        setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        search.putClientProperty(
            "JTextField.placeholderText",
            "Search by pet, owner or veterinarian..."
        );

        JPanel actions = new JPanel(new FlowLayout(
            FlowLayout.RIGHT, 8, 0
        ));
        actions.setOpaque(false);

        JButton add = new JButton("+ New Appointment");
        JButton cancel = new JButton("Cancel Appointment");

        actions.add(statusFilter);
        actions.add(add);
        actions.add(cancel);

        toolbar.add(search, BorderLayout.CENTER);
        toolbar.add(actions, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(
            new String[]{
                "ID", "Pet", "Owner", "Veterinarian",
                "Date", "Time", "Status"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(220, 242, 238));
        table.setSelectionForeground(new Color(25, 65, 60));
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

        JLabel heading = new JLabel("Appointment List");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 19));

        card.add(heading, BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        add.addActionListener(e -> showAddDialog());
        cancel.addActionListener(e -> cancelSelected());
        statusFilter.addActionListener(e -> refresh());

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
        String filter = (String) statusFilter.getSelectedItem();

        for (Appointment a : ClinicData.appointments) {
            Pet pet = ClinicData.getPet(a.petId);
            if (pet == null) continue;

            Owner owner = ClinicData.getOwner(pet.ownerId);
            String ownerName = owner == null ? "" : owner.name;

            String searchable = (
                a.getCode() + " " + pet.name + " " +
                ownerName + " " + a.veterinarian
            ).toLowerCase();

            if (!searchable.contains(keyword)) continue;

            if (!"All Status".equals(filter)
                    && !a.status.equals(filter)) continue;

            model.addRow(new Object[]{
                a.getCode(),
                pet.name,
                ownerName,
                a.veterinarian,
                a.getDate(),
                a.getTime(),
                a.status
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

        JComboBox<String> vetBox = new JComboBox<>(
            ClinicData.VETERINARIANS
        );

        JTextField date = new JTextField(
            LocalDate.now().plusDays(1).format(DATE_FORMAT)
        );
        JTextField time = new JTextField("09:00");

        Object[] fields = {
            "Pet:", petBox,
            "Veterinarian:", vetBox,
            "Date (dd/MM/yyyy):", date,
            "Time (HH:mm):", time
        };

        int result = JOptionPane.showConfirmDialog(
            this, fields, "New Appointment",
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

            LocalDate d = LocalDate.parse(
                date.getText().trim(), DATE_FORMAT
            );

            LocalTime t = LocalTime.parse(
                time.getText().trim(),
                DateTimeFormatter.ofPattern("HH:mm")
            );

            ClinicData.addAppointment(
                petId,
                (String) vetBox.getSelectedItem(),
                LocalDateTime.of(d, t)
            );

            refresh();

            JOptionPane.showMessageDialog(
                this, "Appointment created successfully!"
            );

        } catch (DateTimeException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Invalid date or time. Use dd/MM/yyyy and HH:mm.",
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

    private void cancelSelected() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                this, "Please select an appointment."
            );
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        String code = model.getValueAt(modelRow, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Cancel appointment " + code + "?",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            int id = Integer.parseInt(code.substring(3));
            ClinicData.cancelAppointment(id);
            refresh();

            JOptionPane.showMessageDialog(
                this, "Appointment cancelled."
            );

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                this, ex.getMessage(),
                "Cannot Cancel",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }
}
