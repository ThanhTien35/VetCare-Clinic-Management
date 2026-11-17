
package com.vetcare;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Appointment {
    public final int id;
    public final int petId;
    public final String veterinarian;
    public final LocalDateTime dateTime;

    public String status;
    public LocalDateTime checkedInAt;
    public Double weightKg;
    public String checkInNotes;

    public Appointment(int id, int petId, String veterinarian,
                       LocalDateTime dateTime) {
        this.id = id;
        this.petId = petId;
        this.veterinarian = veterinarian;
        this.dateTime = dateTime;

        this.status = "Scheduled";
        this.checkedInAt = null;
        this.weightKg = null;
        this.checkInNotes = "";
    }

    public String getCode() {
        return String.format("APT%03d", id);
    }

    public String getDate() {
        return dateTime.format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        );
    }

    public String getTime() {
        return dateTime.format(
            DateTimeFormatter.ofPattern("HH:mm")
        );
    }

    public String getCheckInTime() {
        if (checkedInAt == null) return "-";

        return checkedInAt.format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        );
    }
}
