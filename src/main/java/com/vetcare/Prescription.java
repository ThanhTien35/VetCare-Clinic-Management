
package com.vetcare;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Prescription {
    public final int id;
    public final int medicalRecordId;
    public final LocalDateTime createdAt;
    public final List<MedicineItem> medicines;

    public Prescription(int id, int medicalRecordId,
                        List<MedicineItem> medicines) {
        this(id, medicalRecordId, medicines, LocalDateTime.now());
    }

    public Prescription(int id, int medicalRecordId,
                        List<MedicineItem> medicines, LocalDateTime createdAt) {
        this.id = id;
        this.medicalRecordId = medicalRecordId;
        this.createdAt = createdAt;
        this.medicines = new ArrayList<>(medicines);
    }

    public String getCode() {
        return String.format("RX%03d", id);
    }

    public String getCreatedAt() {
        return createdAt.format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        );
    }

    public static class MedicineItem {
        public final String name;
        public final String dosage;
        public final String frequency;
        public final int days;
        public final String instructions;

        public MedicineItem(String name, String dosage,
                            String frequency, int days,
                            String instructions) {
            this.name = name;
            this.dosage = dosage;
            this.frequency = frequency;
            this.days = days;
            this.instructions = instructions;
        }
    }
}
