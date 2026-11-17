
package com.vetcare;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Vaccination {
    public final int id;
    public final int petId;
    public final String vaccineName;
    public final LocalDate vaccinationDate;
    public final LocalDate nextDueDate;
    public final String notes;

    private static final DateTimeFormatter FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public Vaccination(int id, int petId, String vaccineName,
                       LocalDate vaccinationDate,
                       LocalDate nextDueDate, String notes) {
        this.id = id;
        this.petId = petId;
        this.vaccineName = vaccineName;
        this.vaccinationDate = vaccinationDate;
        this.nextDueDate = nextDueDate;
        this.notes = notes;
    }

    public String getCode() {
        return String.format("VAC%03d", id);
    }

    public String getDate() {
        return vaccinationDate.format(FORMAT);
    }

    public String getNextDueDate() {
        return nextDueDate == null ? "-" : nextDueDate.format(FORMAT);
    }

    public String getStatus() {
        if (nextDueDate == null) return "No Booster";

        return nextDueDate.isBefore(LocalDate.now())
            ? "Overdue" : "Upcoming";
    }
}
