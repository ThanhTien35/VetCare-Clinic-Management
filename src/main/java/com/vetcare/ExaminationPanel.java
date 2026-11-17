
package com.vetcare;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class ExaminationPanel extends JPanel {

    private final DefaultTableModel queueModel;
    private final DefaultTableModel historyModel;
    private final JTable queueTable;
    private final JTable historyTable;

    private static final Color TEAL = new Color(15, 118, 110);
    private static final Color TEXT = new Color(35, 55, 55);

    public ExaminationPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel description = new JLabel(
            "Examine checked-in patients and manage medical records."
        );
        description.setForeground(new Color(115, 130, 130));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());

        toolbar.add(description, BorderLayout.WEST);
        toolbar.add(refreshButton, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        queueModel = createModel(
            "Appointment", "Pet", "Owner",
            "Veterinarian", "Weight (kg)", "Check-in"
        );

        historyModel = createModel(
            "EMR ID", "Appointment", "Pet",
            "Diagnosis", "Created At"
        );

        queueTable = createTable(queueModel);
        historyTable = createTable(historyModel);

        JPanel queueCard = createCard("Patients Waiting for Examination");
        queueCard.add(new JScrollPane(queueTable), BorderLayout.CENTER);

        JButton examineButton = new JButton("Start Examination");
        examineButton.setBackground(TEAL);
        examineButton.setForeground(Color.WHITE);
        examineButton.setFocusPainted(false);
        examineButton.addActionListener(e -> showExaminationDialog());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(examineButton);
        queueCard.add(actions, BorderLayout.SOUTH);

        JPanel historyCard = createCard("Medical Record History");
        historyCard.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        JButton viewButton = new JButton("View Medical Record");
        viewButton.addActionListener(e -> viewMedicalRecord());

        JPanel historyActions = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );
        historyActions.setOpaque(false);
        historyActions.add(viewButton);
        historyCard.add(historyActions, BorderLayout.SOUTH);

        JPanel sections = new JPanel(new GridLayout(2, 1, 0, 16));
        sections.setOpaque(false);
        sections.add(queueCard);
        sections.add(historyCard);

        add(sections, BorderLayout.CENTER);
        refresh();
    }

    private DefaultTableModel createModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
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

    private void refresh() {
        queueModel.setRowCount(0);
        historyModel.setRowCount(0);

        for (Appointment a : ClinicData.getWaitingQueue()) {
            Pet pet = ClinicData.getPet(a.petId);
            if (pet == null) continue;

            Owner owner = ClinicData.getOwner(pet.ownerId);

            queueModel.addRow(new Object[]{
                a.getCode(),
                pet.name,
                owner == null ? "" : owner.name,
                a.veterinarian,
                a.weightKg,
                a.getCheckInTime()
            });
        }

        for (MedicalRecord record : ClinicData.medicalRecords) {
            Appointment a = findAppointment(record.appointmentId);
            if (a == null) continue;

            Pet pet = ClinicData.getPet(a.petId);

            historyModel.addRow(new Object[]{
                record.getCode(),
                a.getCode(),
                pet == null ? "" : pet.name,
                record.diagnosis,
                record.getCreatedAt()
            });
        }
    }

    private Appointment findAppointment(int id) {
        for (Appointment a : ClinicData.appointments) {
            if (a.id == id) return a;
        }
        return null;
    }

    private JTextArea createTextArea() {
        JTextArea area = new JTextArea(3, 25);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private void showExaminationDialog() {
        int row = queueTable.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(
                this, "Please select a patient first."
            );
            return;
        }

        int modelRow = queueTable.convertRowIndexToModel(row);

        String code = queueModel.getValueAt(modelRow, 0).toString();
        String petName = queueModel.getValueAt(modelRow, 1).toString();
        int appointmentId = Integer.parseInt(code.substring(3));

        JTextArea symptoms = createTextArea();
        JTextArea diagnosis = createTextArea();
        JTextArea treatment = createTextArea();

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(440, 400));

        form.add(new JLabel("Appointment: " + code));
        form.add(new JLabel("Patient: " + petName));
        form.add(Box.createVerticalStrut(12));

        form.add(new JLabel("Symptoms:"));
        form.add(new JScrollPane(symptoms));
        form.add(Box.createVerticalStrut(10));

        form.add(new JLabel("Diagnosis:"));
        form.add(new JScrollPane(diagnosis));
        form.add(Box.createVerticalStrut(10));

        form.add(new JLabel("Treatment:"));
        form.add(new JScrollPane(treatment));

        int result = JOptionPane.showConfirmDialog(
            this, form, "Examination / EMR",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return;

        try {
            MedicalRecord record = ClinicData.saveMedicalRecord(
                appointmentId,
                symptoms.getText(),
                diagnosis.getText(),
                treatment.getText()
            );

            refresh();

            JOptionPane.showMessageDialog(
                this,
                "Medical record saved successfully!\n"
                + "Record: " + record.getCode()
                + "\nAppointment status: Completed"
            );

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                this, ex.getMessage(),
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void viewMedicalRecord() {
        int row = historyTable.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(
                this, "Please select a medical record."
            );
            return;
        }

        int modelRow = historyTable.convertRowIndexToModel(row);
        String code = historyModel.getValueAt(modelRow, 0).toString();

        MedicalRecord selected = null;

        for (MedicalRecord r : ClinicData.medicalRecords) {
            if (r.getCode().equals(code)) {
                selected = r;
                break;
            }
        }

        if (selected == null) return;

        JTextArea details = new JTextArea(
            "Medical Record: " + selected.getCode()
            + "\nAppointment: APT"
            + String.format("%03d", selected.appointmentId)
            + "\nCreated At: " + selected.getCreatedAt()
            + "\n\nSYMPTOMS\n" + selected.symptoms
            + "\n\nDIAGNOSIS\n" + selected.diagnosis
            + "\n\nTREATMENT\n" + selected.treatment
        );

        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        details.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(details);
        scroll.setPreferredSize(new Dimension(470, 300));

        JOptionPane.showMessageDialog(
            this, scroll, "Medical Record Details",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
