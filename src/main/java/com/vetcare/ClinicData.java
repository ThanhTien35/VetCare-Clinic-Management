
package com.vetcare;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClinicData {
    public static final List<Owner> owners = new ArrayList<>();
    public static final List<Pet> pets = new ArrayList<>();

    private static int nextOwnerId = 1;
    private static int nextPetId = 1;

    
    static {
        System.out.println(">>> ClinicData is loading...");

        DatabaseManager.initialize();

        owners.addAll(DatabaseManager.loadOwners());
        pets.addAll(DatabaseManager.loadPets());

        for (Owner owner : owners) {
            nextOwnerId = Math.max(nextOwnerId, owner.id + 1);
        }

        for (Pet pet : pets) {
            nextPetId = Math.max(nextPetId, pet.id + 1);
        }

        // Add sample data only when database is empty.
        if (owners.isEmpty()) {
            Owner o1 = addOwner(
                "Nguyen An", "0901234567", "an@example.com"
            );
            Owner o2 = addOwner(
                "Tran Minh", "0912345678", "minh@example.com"
            );

            addPet(o1.id, "Milo", "Dog", "Poodle", 3);
            addPet(o2.id, "Luna", "Cat", "British Shorthair", 2);
        }
    }


    
    public static Owner addOwner(
            String name, String phone, String email) {

        if (name == null || name.isBlank()
                || phone == null || !phone.matches("[0-9]{9,11}")) {
            throw new IllegalArgumentException(
                "Invalid owner name or phone."
            );
        }

        for (Owner owner : owners) {
            if (owner.phone.equals(phone)) {
                throw new IllegalArgumentException(
                    "This phone number already exists."
                );
            }
        }

        Owner owner = new Owner(
            nextOwnerId, name.trim(), phone, email
        );

        DatabaseManager.saveOwner(owner);
        owners.add(owner);
        nextOwnerId++;

        return owner;
    }


    
    public static Pet addPet(
            int ownerId, String name, String species,
            String breed, int age) {

        if (getOwner(ownerId) == null) {
            throw new IllegalArgumentException("Owner not found.");
        }

        if (name == null || name.isBlank()
                || species == null || species.isBlank()
                || age < 0) {
            throw new IllegalArgumentException("Invalid pet information.");
        }

        Pet pet = new Pet(
            nextPetId, ownerId, name.trim(), species, breed, age
        );

        DatabaseManager.savePet(pet);
        pets.add(pet);
        nextPetId++;

        return pet;
    }


    public static Owner getOwner(int id) {
        for (Owner owner : owners) {
            if (owner.id == id) return owner;
        }
        return null;
    }

    public static final List<Appointment> appointments = new ArrayList<>();
    private static int nextAppointmentId = 1;

    static {
        appointments.addAll(DatabaseManager.loadAppointments());

        for (Appointment a : appointments) {
            nextAppointmentId = Math.max(nextAppointmentId, a.id + 1);
        }
    }

    public static final String[] VETERINARIANS = {
        "Dr. Nguyen Minh",
        "Dr. Tran Anh",
        "Dr. Le Hoang"
    };

    public static Appointment addAppointment(
            int petId, String veterinarian, LocalDateTime dateTime) {

        if (pets.stream().noneMatch(p -> p.id == petId)) {
            throw new IllegalArgumentException("Pet not found.");
        }

        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                "Appointment must be scheduled in the future."
            );
        }

        // Each appointment uses a 30-minute time slot.
        LocalDateTime end = dateTime.plusMinutes(30);

        for (Appointment a : appointments) {
            if (a.status.equals("Cancelled")) continue;

            LocalDateTime existingEnd = a.dateTime.plusMinutes(30);

            boolean overlap = dateTime.isBefore(existingEnd)
                && a.dateTime.isBefore(end);

            if (overlap && a.veterinarian.equals(veterinarian)) {
                throw new IllegalArgumentException(
                    "This veterinarian already has an appointment "
                    + "during the selected time slot."
                );
            }

            if (overlap && a.petId == petId) {
                throw new IllegalArgumentException(
                    "This pet already has an overlapping appointment."
                );
            }
        }

        Appointment appointment = new Appointment(
            nextAppointmentId, petId, veterinarian, dateTime
        );

        DatabaseManager.saveAppointment(appointment);

        appointments.add(appointment);
        nextAppointmentId++;

        return appointment;
    }

    public static Pet getPet(int id) {
        for (Pet pet : pets) {
            if (pet.id == id) return pet;
        }
        return null;
    }

    public static void cancelAppointment(int id) {
        for (Appointment a : appointments) {
            if (a.id == id) {
                if (!a.status.equals("Scheduled")) {
                    throw new IllegalArgumentException(
                        "Only scheduled appointments can be cancelled."
                    );
                }
                String oldStatus = a.status;
                    a.status = "Cancelled";

                    try {
                        DatabaseManager.updateAppointment(a);
                    } catch (RuntimeException ex) {
                        a.status = oldStatus;
                        throw ex;
                    }

                    return;
            }
        }
        throw new IllegalArgumentException("Appointment not found.");
    }

    public static void checkInAppointment(
            int appointmentId, double weightKg, String notes) {

        Appointment appointment = null;

        for (Appointment a : appointments) {
            if (a.id == appointmentId) {
                appointment = a;
                break;
            }
        }

        if (appointment == null) {
            throw new IllegalArgumentException(
                "Appointment not found."
            );
        }

        if (!appointment.status.equals("Scheduled")) {
            throw new IllegalArgumentException(
                "Only scheduled appointments can be checked in."
            );
        }

        if (!Double.isFinite(weightKg) || weightKg <= 0) {
            throw new IllegalArgumentException(
                "Weight must be greater than zero."
            );
        }

        Double oldWeight = appointment.weightKg;
        String oldNotes = appointment.checkInNotes;
        LocalDateTime oldCheckedInAt = appointment.checkedInAt;
        String oldStatus = appointment.status;

        appointment.weightKg = weightKg;
        appointment.checkInNotes = notes == null ? "" : notes.trim();
        appointment.checkedInAt = LocalDateTime.now();
        appointment.status = "Checked In";

        try {
            DatabaseManager.updateAppointment(appointment);
        } catch (RuntimeException ex) {
            appointment.weightKg = oldWeight;
            appointment.checkInNotes = oldNotes;
            appointment.checkedInAt = oldCheckedInAt;
            appointment.status = oldStatus;
            throw ex;
        }
    }

    public static List<Appointment> getWaitingQueue() {
        List<Appointment> queue = new ArrayList<>();

        for (Appointment a : appointments) {
            if (a.status.equals("Checked In")) {
                queue.add(a);
            }
        }

        queue.sort((a, b) ->
            a.checkedInAt.compareTo(b.checkedInAt)
        );

        return queue;
    }

    // Medical records
    public static final List<MedicalRecord> medicalRecords =
        new ArrayList<>();

    private static int nextMedicalRecordId = 1;

    static {
        medicalRecords.addAll(DatabaseManager.loadMedicalRecords());
        for (MedicalRecord r : medicalRecords) {
            nextMedicalRecordId = Math.max(nextMedicalRecordId, r.id + 1);
        }
    }

    public static MedicalRecord getMedicalRecord(int appointmentId) {
        for (MedicalRecord record : medicalRecords) {
            if (record.appointmentId == appointmentId) {
                return record;
            }
        }
        return null;
    }

    public static MedicalRecord saveMedicalRecord(
            int appointmentId, String symptoms,
            String diagnosis, String treatment) {

        Appointment appointment = null;

        for (Appointment a : appointments) {
            if (a.id == appointmentId) {
                appointment = a;
                break;
            }
        }

        if (appointment == null) {
            throw new IllegalArgumentException(
                "Appointment not found."
            );
        }

        if (!appointment.status.equals("Checked In")) {
            throw new IllegalArgumentException(
                "Only checked-in patients can be examined."
            );
        }

        if (getMedicalRecord(appointmentId) != null) {
            throw new IllegalArgumentException(
                "Medical record already exists."
            );
        }

        if (symptoms == null || symptoms.isBlank()
                || diagnosis == null || diagnosis.isBlank()
                || treatment == null || treatment.isBlank()) {
            throw new IllegalArgumentException(
                "All examination fields are required."
            );
        }

        MedicalRecord record = new MedicalRecord(
            nextMedicalRecordId,
            appointmentId,
            symptoms.trim(),
            diagnosis.trim(),
            treatment.trim()
        );

        DatabaseManager.saveMedicalRecord(record, appointment);
        medicalRecords.add(record);
        nextMedicalRecordId++;
        appointment.status = "Completed";

        return record;
    }

    // Prescription management
    public static final List<Prescription> prescriptions =
        new ArrayList<>();

    private static int nextPrescriptionId = 1;

    static {
        prescriptions.addAll(DatabaseManager.loadPrescriptions());
        for (Prescription p : prescriptions) {
            nextPrescriptionId = Math.max(nextPrescriptionId, p.id + 1);
        }
    }

    public static Prescription getPrescription(int medicalRecordId) {
        for (Prescription p : prescriptions) {
            if (p.medicalRecordId == medicalRecordId) {
                return p;
            }
        }
        return null;
    }

    public static Prescription savePrescription(
            int medicalRecordId,
            List<Prescription.MedicineItem> medicines) {

        boolean recordExists = false;

        for (MedicalRecord record : medicalRecords) {
            if (record.id == medicalRecordId) {
                recordExists = true;
                break;
            }
        }

        if (!recordExists) {
            throw new IllegalArgumentException(
                "Medical record not found."
            );
        }

        if (getPrescription(medicalRecordId) != null) {
            throw new IllegalArgumentException(
                "This medical record already has a prescription."
            );
        }

        if (medicines == null || medicines.isEmpty()) {
            throw new IllegalArgumentException(
                "Add at least one medicine."
            );
        }

        for (Prescription.MedicineItem item : medicines) {
            if (item == null || item.name == null
                    || item.name.isBlank()
                    || item.dosage == null || item.dosage.isBlank()
                    || item.frequency == null
                    || item.frequency.isBlank()
                    || item.days <= 0) {
                throw new IllegalArgumentException(
                    "Medicine name, dosage, frequency and valid "
                    + "duration are required."
                );
            }
        }

        Prescription prescription = new Prescription(
            nextPrescriptionId, medicalRecordId, medicines
        );

        DatabaseManager.savePrescription(prescription);
        prescriptions.add(prescription);
        nextPrescriptionId++;
        return prescription;
    }

    public static final List<Vaccination> vaccinations =
        new ArrayList<>();

    private static int nextVaccinationId = 1;

    static {
        vaccinations.addAll(DatabaseManager.loadVaccinations());
        for (Vaccination v : vaccinations) {
            nextVaccinationId = Math.max(nextVaccinationId, v.id + 1);
        }
    }

    public static Vaccination addVaccination(
            int petId, String vaccineName,
            LocalDate vaccinationDate,
            LocalDate nextDueDate, String notes) {

        if (getPet(petId) == null) {
            throw new IllegalArgumentException("Pet not found.");
        }

        if (vaccineName == null || vaccineName.isBlank()) {
            throw new IllegalArgumentException(
                "Vaccine name is required."
            );
        }

        if (vaccinationDate == null ||
                vaccinationDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Vaccination date cannot be in the future."
            );
        }

        if (nextDueDate != null &&
                !nextDueDate.isAfter(vaccinationDate)) {
            throw new IllegalArgumentException(
                "Next booster date must be after vaccination date."
            );
        }

        Vaccination vaccination = new Vaccination(
            nextVaccinationId,
            petId,
            vaccineName.trim(),
            vaccinationDate,
            nextDueDate,
            notes == null ? "" : notes.trim()
        );

        DatabaseManager.saveVaccination(vaccination);
        vaccinations.add(vaccination);
        nextVaccinationId++;
        return vaccination;
    }

    public static final List<ServiceItem> services = new ArrayList<>();
    public static final List<Invoice> invoices = new ArrayList<>();

    private static int nextServiceId = 1;
    private static int nextInvoiceId = 1;

    static {
        services.addAll(DatabaseManager.loadServices());
        for (ServiceItem item : services) {
            nextServiceId = Math.max(nextServiceId, item.id + 1);
        }
        if (services.isEmpty()) {
            addService("General Examination", 150000);
            addService("Vaccination Service", 200000);
            addService("Laboratory Test", 250000);
            addService("Ultrasound", 300000);
        }
        invoices.addAll(DatabaseManager.loadInvoices());
        for (Invoice invoice : invoices) {
            nextInvoiceId = Math.max(nextInvoiceId, invoice.id + 1);
        }
    }

    public static ServiceItem addService(String name, long price) {
        if (name == null || name.isBlank() || price <= 0) {
            throw new IllegalArgumentException(
                "Service name and positive price are required."
            );
        }

        for (ServiceItem s : services) {
            if (s.name.equalsIgnoreCase(name.trim())) {
                throw new IllegalArgumentException(
                    "Service name already exists."
                );
            }
        }

        ServiceItem service = new ServiceItem(
            nextServiceId, name.trim(), price
        );
        DatabaseManager.saveService(service);
        services.add(service);
        nextServiceId++;
        return service;
    }

    public static Invoice getInvoice(int appointmentId) {
        for (Invoice invoice : invoices) {
            if (invoice.appointmentId == appointmentId) {
                return invoice;
            }
        }
        return null;
    }

    public static Invoice createInvoice(
            int appointmentId, List<Invoice.LineItem> items) {

        Appointment appointment = null;
        for (Appointment a : appointments) {
            if (a.id == appointmentId) {
                appointment = a;
                break;
            }
        }

        if (appointment == null
                || !appointment.status.equals("Completed")) {
            throw new IllegalArgumentException(
                "Only completed appointments can be invoiced."
            );
        }

        if (getInvoice(appointmentId) != null) {
            throw new IllegalArgumentException(
                "This appointment already has an invoice."
            );
        }

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                "Add at least one service."
            );
        }

        for (Invoice.LineItem item : items) {
            if (item == null || item.quantity <= 0
                    || item.unitPrice <= 0) {
                throw new IllegalArgumentException(
                    "Invalid invoice item."
                );
            }
        }

        Invoice invoice = new Invoice(
            nextInvoiceId, appointmentId, items
        );

        DatabaseManager.saveInvoice(invoice);
        invoices.add(invoice);
        nextInvoiceId++;
        return invoice;
    }

    public static void payInvoice(int invoiceId, String method) {
        if (!"Cash".equals(method) && !"Bank Transfer".equals(method)) {
            throw new IllegalArgumentException(
                "Invalid payment method."
            );
        }

        for (Invoice invoice : invoices) {
            if (invoice.id == invoiceId) {
                if (invoice.status.equals("Paid")) {
                    throw new IllegalArgumentException(
                        "Invoice already paid."
                    );
                }

                String previousStatus = invoice.status;
                String previousMethod = invoice.paymentMethod;
                invoice.status = "Paid";
                invoice.paymentMethod = method;
                try {
                    DatabaseManager.updateInvoice(invoice);
                } catch (RuntimeException ex) {
                    invoice.status = previousStatus;
                    invoice.paymentMethod = previousMethod;
                    throw ex;
                }
                return;
            }
        }

        throw new IllegalArgumentException("Invoice not found.");
    }
}
