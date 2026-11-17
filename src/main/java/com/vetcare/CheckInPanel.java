
package com.vetcare;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class CheckInPanel extends JPanel {

    private final DefaultTableModel appointmentModel;
    private final DefaultTableModel queueModel;
    private final JTable appointmentTable;

    private final Color teal = new Color(15, 118, 110);
    private final Color text = new Color(35, 55, 55);

    public CheckInPanel() {
        setLayout(new BorderLayout(0, 18));
        setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel description = new JLabel(
            "Manage arrivals and the examination waiting queue."
        );
        description.setForeground(new Color(115, 130, 130));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());

        toolbar.add(description, BorderLayout.WEST);
        toolbar.add(refreshButton, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        appointmentModel = createModel(
            "ID", "Pet", "Owner", "Veterinarian", "Date", "Time"
        );

        queueModel = createModel(
            "Queue No.", "Appointment", "Pet",
            "Veterinarian", "Weight (kg)", "Check-in Time", "Notes"
        );

        appointmentTable = createTable(appointmentModel);
        JTable queueTable = createTable(queueModel);

        JPanel scheduledCard = createCard(
            "Scheduled Appointments"
        );

        scheduledCard.add(
            new JScrollPane(appointmentTable),
            BorderLayout.CENTER
        );

        JButton checkInButton = new JButton("Check In Selected");
        checkInButton.setBackground(teal);
        checkInButton.setForeground(Color.WHITE);
        checkInButton.setFocusPainted(false);

        checkInButton.addActionListener(e -> showCheckInDialog());

        JPanel bottomActions = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );
        bottomActions.setOpaque(false);
        bottomActions.add(checkInButton);

        scheduledCard.add(bottomActions, BorderLayout.SOUTH);

        JPanel queueCard = createCard("Waiting Queue");
        queueCard.add(
            new JScrollPane(queueTable),
            BorderLayout.CENTER
        );

        JPanel sections = new JPanel(
            new GridLayout(2, 1, 0, 18)
        );
        sections.setOpaque(false);
        sections.add(scheduledCard);
        sections.add(queueCard);

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
        table.setForeground(text);
        table.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );
        table.setSelectionBackground(
            new Color(220, 242, 238)
        );
        table.setSelectionForeground(
            new Color(25, 65, 60)
        );
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(
            new Font("Segoe UI", Font.BOLD, 13)
        );
        header.setBackground(
            new Color(242, 247, 246)
        );
        header.setForeground(text);
        header.setPreferredSize(new Dimension(0, 40));

        return table;
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel(
            new BorderLayout(0, 12)
        );

        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(
                new Color(225, 233, 233), 1, true
            ),
            new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel heading = new JLabel(title);
        heading.setFont(
            new Font("Segoe UI", Font.BOLD, 18)
        );
        heading.setForeground(text);

        card.add(heading, BorderLayout.NORTH);
        return card;
    }

    private void refresh() {
        appointmentModel.setRowCount(0);
        queueModel.setRowCount(0);

        // Appointments waiting for check-in
        for (Appointment a : ClinicData.appointments) {
            if (!a.status.equals("Scheduled")) {
                continue;
            }

            Pet pet = ClinicData.getPet(a.petId);
            if (pet == null) continue;

            Owner owner = ClinicData.getOwner(pet.ownerId);

            appointmentModel.addRow(new Object[]{
                a.getCode(),
                pet.name,
                owner == null ? "" : owner.name,
                a.veterinarian,
                a.getDate(),
                a.getTime()
            });
        }

        // Checked-in appointments waiting for examination
        List<Appointment> queue = ClinicData.getWaitingQueue();

        int queueNumber = 1;

        for (Appointment a : queue) {
            Pet pet = ClinicData.getPet(a.petId);
            if (pet == null) continue;

            queueModel.addRow(new Object[]{
                queueNumber++,
                a.getCode(),
                pet.name,
                a.veterinarian,
                a.weightKg,
                a.getCheckInTime(),
                a.checkInNotes
            });
        }
    }

    private void showCheckInDialog() {
        int selected = appointmentTable.getSelectedRow();

        if (selected < 0) {
            JOptionPane.showMessageDialog(
                this,
                "Please select an appointment first."
            );
            return;
        }

        int modelRow = appointmentTable
            .convertRowIndexToModel(selected);

        String code = appointmentModel
            .getValueAt(modelRow, 0).toString();

        String petName = appointmentModel
            .getValueAt(modelRow, 1).toString();

        JTextField weightField = new JTextField();
        JTextArea notesField = new JTextArea(3, 20);
        notesField.setLineWrap(true);
        notesField.setWrapStyleWord(true);

        Object[] fields = {
            "Appointment: " + code,
            "Pet: " + petName,
            "Weight (kg):",
            weightField,
            "Initial Notes:",
            new JScrollPane(notesField)
        };

        int result = JOptionPane.showConfirmDialog(
            this,
            fields,
            "Patient Check-in",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            double weight = Double.parseDouble(
                weightField.getText().trim()
            );

            int appointmentId = Integer.parseInt(
                code.substring(3)
            );

            ClinicData.checkInAppointment(
                appointmentId,
                weight,
                notesField.getText()
            );

            refresh();

            JOptionPane.showMessageDialog(
                this,
                "Check-in successful!\n"
                + petName + " has been added to the waiting queue."
            );

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter a valid weight, e.g. 4.5",
                "Invalid Input",
                JOptionPane.WARNING_MESSAGE
            );

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Check-in Error",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }
}
