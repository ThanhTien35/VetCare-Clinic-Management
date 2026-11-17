
package com.vetcare;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class InvoicesPanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;

    public InvoicesPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        toolbar.setOpaque(false);

        JButton create = new JButton("+ Create Invoice");
        JButton pay = new JButton("Record Payment");
        JButton view = new JButton("View Invoice");

        create.addActionListener(e -> createInvoice());
        pay.addActionListener(e -> recordPayment());
        view.addActionListener(e -> viewInvoice());

        toolbar.add(create);
        toolbar.add(pay);
        toolbar.add(view);
        add(toolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(
            new String[]{
                "Invoice", "Appointment", "Pet", "Owner",
                "Total (VND)", "Status", "Created At"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(38);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(220, 242, 238));
        table.setSelectionForeground(new Color(25, 65, 60));
        table.setFillsViewportHeight(true);

        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel heading = new JLabel("Invoice Management");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 19));

        card.add(heading, BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        refresh();
    }

    private Appointment findAppointment(int id) {
        for (Appointment a : ClinicData.appointments) {
            if (a.id == id) return a;
        }
        return null;
    }

    private String petName(Appointment a) {
        Pet p = ClinicData.getPet(a.petId);
        return p == null ? "-" : p.name;
    }

    private String ownerName(Appointment a) {
        Pet p = ClinicData.getPet(a.petId);
        if (p == null) return "-";
        Owner o = ClinicData.getOwner(p.ownerId);
        return o == null ? "-" : o.name;
    }

    private void refresh() {
        model.setRowCount(0);

        for (Invoice i : ClinicData.invoices) {
            Appointment a = findAppointment(i.appointmentId);
            if (a == null) continue;

            model.addRow(new Object[]{
                i.getCode(),
                a.getCode(),
                petName(a),
                ownerName(a),
                String.format("%,d", i.getTotal()),
                i.status,
                i.getCreatedAt()
            });
        }
    }

    private Invoice selectedInvoice() {
        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(
                this, "Please select an invoice."
            );
            return null;
        }

        int modelRow = table.convertRowIndexToModel(row);
        String code = model.getValueAt(modelRow, 0).toString();

        for (Invoice i : ClinicData.invoices) {
            if (i.getCode().equals(code)) return i;
        }

        return null;
    }

    private void createInvoice() {
        List<Appointment> eligible = new ArrayList<>();

        for (Appointment a : ClinicData.appointments) {
            if (a.status.equals("Completed")
                    && ClinicData.getInvoice(a.id) == null) {
                eligible.add(a);
            }
        }

        if (eligible.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "No completed appointments awaiting an invoice."
            );
            return;
        }

        JComboBox<String> appointmentBox = new JComboBox<>();

        for (Appointment a : eligible) {
            appointmentBox.addItem(
                a.getCode() + " - " + petName(a)
                + " (" + ownerName(a) + ")"
            );
        }

        int result = JOptionPane.showConfirmDialog(
            this, new Object[]{"Appointment:", appointmentBox},
            "Select Appointment",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return;

        Appointment appointment =
            eligible.get(appointmentBox.getSelectedIndex());

        List<Invoice.LineItem> items = new ArrayList<>();

        while (true) {
            JComboBox<ServiceItem> serviceBox = new JComboBox<>(
                ClinicData.services.toArray(new ServiceItem[0])
            );

            if (serviceBox.getItemCount() == 0) {
                JOptionPane.showMessageDialog(
                    this, "No services available."
                );
                return;
            }

            JSpinner quantity = new JSpinner(
                new SpinnerNumberModel(1, 1, 100, 1)
            );

            result = JOptionPane.showConfirmDialog(
                this,
                new Object[]{
                    "Service:", serviceBox,
                    "Quantity:", quantity
                },
                "Add Invoice Service",
                JOptionPane.OK_CANCEL_OPTION
            );

            if (result != JOptionPane.OK_OPTION) return;

            ServiceItem service =
                (ServiceItem) serviceBox.getSelectedItem();

            if (service == null) return;

            items.add(new Invoice.LineItem(
                service, (Integer) quantity.getValue()
            ));

            int more = JOptionPane.showConfirmDialog(
                this, "Add another service?",
                "Continue", JOptionPane.YES_NO_OPTION
            );

            if (more != JOptionPane.YES_OPTION) break;
        }

        long total = 0;
        for (Invoice.LineItem item : items) {
            total += item.getSubtotal();
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Total: " + String.format("%,d", total)
                + " VND\nCreate this invoice?",
            "Confirm Invoice",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Invoice saved = ClinicData.createInvoice(
                appointment.id, items
            );
            refresh();

            JOptionPane.showMessageDialog(
                this, "Invoice created: " + saved.getCode()
            );
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void recordPayment() {
        Invoice invoice = selectedInvoice();
        if (invoice == null) return;

        if (invoice.status.equals("Paid")) {
            JOptionPane.showMessageDialog(
                this, "This invoice is already paid."
            );
            return;
        }

        String[] methods = {"Cash", "Bank Transfer"};

        String method = (String) JOptionPane.showInputDialog(
            this,
            "Payment amount: "
                + String.format("%,d", invoice.getTotal())
                + " VND\nSelect payment method:",
            "Record Payment",
            JOptionPane.QUESTION_MESSAGE,
            null, methods, methods[0]
        );

        if (method == null) return;

        int confirm = JOptionPane.showConfirmDialog(
            this, "Confirm full payment via " + method + "?",
            "Payment Confirmation", JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            ClinicData.payInvoice(invoice.id, method);
            refresh();
            JOptionPane.showMessageDialog(
                this, "Payment recorded successfully!"
            );
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void viewInvoice() {
        Invoice invoice = selectedInvoice();
        if (invoice == null) return;

        Appointment a = findAppointment(invoice.appointmentId);

        StringBuilder sb = new StringBuilder();
        sb.append("VETCARE - INVOICE\n\n")
          .append("Invoice: ").append(invoice.getCode()).append("\n")
          .append("Appointment: ").append(a.getCode()).append("\n")
          .append("Pet: ").append(petName(a)).append("\n")
          .append("Owner: ").append(ownerName(a)).append("\n")
          .append("Date: ").append(invoice.getCreatedAt()).append("\n\n")
          .append("SERVICES\n");

        for (Invoice.LineItem item : invoice.items) {
            sb.append(item.name)
              .append(" x ").append(item.quantity)
              .append(" = ")
              .append(String.format("%,d", item.getSubtotal()))
              .append(" VND\n");
        }

        sb.append("\nTOTAL: ")
          .append(String.format("%,d", invoice.getTotal()))
          .append(" VND\nStatus: ").append(invoice.status)
          .append("\nPayment Method: ").append(invoice.paymentMethod);

        JTextArea details = new JTextArea(sb.toString());
        details.setEditable(false);
        details.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JScrollPane scroll = new JScrollPane(details);
        scroll.setPreferredSize(new Dimension(470, 350));

        JOptionPane.showMessageDialog(
            this, scroll, "Invoice Details",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
