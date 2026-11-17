
package com.vetcare;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class PrescriptionsPanel extends JPanel {

    private final DefaultTableModel recordModel;
    private final DefaultTableModel prescriptionModel;
    private final JTable recordTable;
    private final JTable prescriptionTable;

    private static final Color TEAL = new Color(15, 118, 110);
    private static final Color TEXT = new Color(35, 55, 55);

    public PrescriptionsPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel info = new JLabel(
            "Create prescriptions from completed medical records."
        );

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());

        toolbar.add(info, BorderLayout.WEST);
        toolbar.add(refreshButton, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        recordModel = createModel(
            "EMR ID", "Pet", "Diagnosis", "Prescription"
        );

        prescriptionModel = createModel(
            "RX ID", "EMR ID", "Pet", "Medicines", "Created At"
        );

        recordTable = createTable(recordModel);
        prescriptionTable = createTable(prescriptionModel);

        JPanel recordsCard = createCard("Medical Records");

        recordsCard.add(
            new JScrollPane(recordTable), BorderLayout.CENTER
        );

        JButton createButton = new JButton("+ Create Prescription");
        createButton.setBackground(TEAL);
        createButton.setForeground(Color.WHITE);
        createButton.addActionListener(e -> createPrescription());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(createButton);
        recordsCard.add(actions, BorderLayout.SOUTH);

        JPanel historyCard = createCard("Prescription History");

        historyCard.add(
            new JScrollPane(prescriptionTable),
            BorderLayout.CENTER
        );

        JButton viewButton = new JButton("View Prescription");
        viewButton.addActionListener(e -> viewPrescription());

        JPanel historyActions = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );
        historyActions.setOpaque(false);
        historyActions.add(viewButton);
        historyCard.add(historyActions, BorderLayout.SOUTH);

        JPanel sections = new JPanel(new GridLayout(2, 1, 0, 16));
        sections.setOpaque(false);
        sections.add(recordsCard);
        sections.add(historyCard);

        add(sections, BorderLayout.CENTER);
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

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(220, 242, 238));
        table.setSelectionForeground(new Color(25, 65, 60));
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 40));

        return table;
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(225, 233, 233), 1, true),
            new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        heading.setForeground(TEXT);

        card.add(heading, BorderLayout.NORTH);
        return card;
    }

    private MedicalRecord findRecord(int id) {
        for (MedicalRecord r : ClinicData.medicalRecords) {
            if (r.id == id) return r;
        }
        return null;
    }

    private Appointment findAppointment(int id) {
        for (Appointment a : ClinicData.appointments) {
            if (a.id == id) return a;
        }
        return null;
    }

    private String getPetName(MedicalRecord record) {
        Appointment a = findAppointment(record.appointmentId);
        if (a == null) return "-";

        Pet pet = ClinicData.getPet(a.petId);
        return pet == null ? "-" : pet.name;
    }

    private void refresh() {
        recordModel.setRowCount(0);
        prescriptionModel.setRowCount(0);

        for (MedicalRecord record : ClinicData.medicalRecords) {
            Prescription existing =
                ClinicData.getPrescription(record.id);

            recordModel.addRow(new Object[]{
                record.getCode(),
                getPetName(record),
                record.diagnosis,
                existing == null ? "Not Created" : "Created"
            });
        }

        for (Prescription p : ClinicData.prescriptions) {
            MedicalRecord record = findRecord(p.medicalRecordId);

            prescriptionModel.addRow(new Object[]{
                p.getCode(),
                record == null ? "-" : record.getCode(),
                record == null ? "-" : getPetName(record),
                p.medicines.size(),
                p.getCreatedAt()
            });
        }
    }

    private void createPrescription() {
        int row = recordTable.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(
                this, "Please select a medical record."
            );
            return;
        }

        int modelRow = recordTable.convertRowIndexToModel(row);
        String code = recordModel.getValueAt(modelRow, 0).toString();
        int recordId = Integer.parseInt(code.substring(3));

        if (ClinicData.getPrescription(recordId) != null) {
            JOptionPane.showMessageDialog(
                this, "This medical record already has a prescription."
            );
            return;
        }

        List<Prescription.MedicineItem> items = new ArrayList<>();

        while (true) {
            JTextField name = new JTextField();
            JTextField dosage = new JTextField();
            JTextField frequency = new JTextField();
            JSpinner days = new JSpinner(
                new SpinnerNumberModel(1, 1, 365, 1)
            );
            JTextField instructions = new JTextField();

            Object[] fields = {
                "Medical Record: " + code,
                "Medicine Name:", name,
                "Dosage:", dosage,
                "Frequency:", frequency,
                "Duration (days):", days,
                "Instructions:", instructions
            };

            int result = JOptionPane.showConfirmDialog(
                this, fields,
                "Add Medicine " + (items.size() + 1),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            if (name.getText().isBlank()
                    || dosage.getText().isBlank()
                    || frequency.getText().isBlank()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Medicine name, dosage and frequency are required."
                );
                continue;
            }

            items.add(new Prescription.MedicineItem(
                name.getText().trim(),
                dosage.getText().trim(),
                frequency.getText().trim(),
                (Integer) days.getValue(),
                instructions.getText().trim()
            ));

            int next = JOptionPane.showConfirmDialog(
                this,
                "Medicine added. Add another medicine?",
                "Continue",
                JOptionPane.YES_NO_OPTION
            );

            if (next != JOptionPane.YES_OPTION) break;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Save prescription with " + items.size() + " medicine(s)?",
            "Confirm Prescription",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Prescription saved =
                ClinicData.savePrescription(recordId, items);

            refresh();

            JOptionPane.showMessageDialog(
                this,
                "Prescription saved successfully!\n"
                + "Prescription ID: " + saved.getCode()
            );

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                this, ex.getMessage(),
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void viewPrescription() {
        int row = prescriptionTable.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(
                this, "Please select a prescription."
            );
            return;
        }

        int modelRow =
            prescriptionTable.convertRowIndexToModel(row);

        String code =
            prescriptionModel.getValueAt(modelRow, 0).toString();

        Prescription selected = null;

        for (Prescription p : ClinicData.prescriptions) {
            if (p.getCode().equals(code)) {
                selected = p;
                break;
            }
        }

        if (selected == null) return;

        StringBuilder details = new StringBuilder();

        details.append("Prescription: ")
            .append(selected.getCode())
            .append("\nCreated: ")
            .append(selected.getCreatedAt())
            .append("\n\n");

        int number = 1;

        for (Prescription.MedicineItem item : selected.medicines) {
            details.append("MEDICINE ").append(number++).append("\n")
                .append("Name: ").append(item.name).append("\n")
                .append("Dosage: ").append(item.dosage).append("\n")
                .append("Frequency: ").append(item.frequency).append("\n")
                .append("Duration: ").append(item.days)
                .append(" day(s)\n")
                .append("Instructions: ")
                .append(item.instructions).append("\n\n");
        }

        JTextArea area = new JTextArea(details.toString());
        area.setEditable(false);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(470, 330));

        JOptionPane.showMessageDialog(
            this, scroll,
            "Prescription Details",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
