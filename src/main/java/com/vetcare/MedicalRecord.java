
package com.vetcare;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MedicalRecord {
    public final int id;
    public final int appointmentId;
    public final String symptoms;
    public final String diagnosis;
    public final String treatment;
    public final LocalDateTime createdAt;

    public MedicalRecord(int id, int appointmentId,
            String symptoms, String diagnosis,
            String treatment) {
        this(id, appointmentId, symptoms, diagnosis, treatment, LocalDateTime.now());
    }

    public MedicalRecord(int id, int appointmentId,
            String symptoms, String diagnosis, String treatment,
            LocalDateTime createdAt) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.createdAt = createdAt;
    }

    public String getCode() {
        return String.format("EMR%03d", id);
    }

    public String getCreatedAt() {
        return createdAt.format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
