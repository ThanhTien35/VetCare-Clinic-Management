
package com.vetcare;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Invoice {
    public final int id;
    public final int appointmentId;
    public final LocalDateTime createdAt;
    public final List<LineItem> items;
    public String status;
    public String paymentMethod;

    public static class LineItem {
        public final int serviceId;
        public final String name;
        public final long unitPrice;
        public final int quantity;

        public LineItem(int serviceId, String name, long unitPrice, int quantity) {
            this.serviceId = serviceId;
            this.name = name;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }

        public LineItem(ServiceItem service, int quantity) {
            this.serviceId = service.id;
            this.name = service.name;
            this.unitPrice = service.price;
            this.quantity = quantity;
        }

        public long getSubtotal() {
            return unitPrice * quantity;
        }
    }

    public Invoice(int id, int appointmentId, List<LineItem> items) {
        this(id, appointmentId, items, LocalDateTime.now());
    }

    public Invoice(int id, int appointmentId, List<LineItem> items,
                   LocalDateTime createdAt) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.items = new ArrayList<>(items);
        this.createdAt = createdAt;
        this.status = "Unpaid";
        this.paymentMethod = "-";
    }

    public String getCode() {
        return String.format("INV%03d", id);
    }

    public long getTotal() {
        long total = 0;
        for (LineItem item : items) total += item.getSubtotal();
        return total;
    }

    public String getCreatedAt() {
        return createdAt.format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        );
    }
}
