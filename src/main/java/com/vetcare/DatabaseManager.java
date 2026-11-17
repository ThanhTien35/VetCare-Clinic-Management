
package com.vetcare;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:vetcare.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initialize() {
        String owners = """
            CREATE TABLE IF NOT EXISTS OWNERS (
                owner_id INTEGER PRIMARY KEY,
                full_name TEXT NOT NULL,
                phone TEXT NOT NULL UNIQUE,
                email TEXT
            )
            """;

        String pets = """
            CREATE TABLE IF NOT EXISTS PETS (
                pet_id INTEGER PRIMARY KEY,
                owner_id INTEGER NOT NULL,
                pet_name TEXT NOT NULL,
                species TEXT NOT NULL,
                breed TEXT,
                age INTEGER,
                FOREIGN KEY (owner_id) REFERENCES OWNERS(owner_id)
            )
            """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute(owners);
            stmt.execute(pets);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS APPOINTMENTS (
                    appointment_id INTEGER PRIMARY KEY,
                    pet_id INTEGER NOT NULL,
                    veterinarian TEXT NOT NULL,
                    appointment_datetime TEXT NOT NULL,
                    status TEXT NOT NULL,
                    checked_in_at TEXT,
                    weight_kg REAL,
                    check_in_notes TEXT,
                    FOREIGN KEY (pet_id) REFERENCES PETS(pet_id)
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS MEDICAL_RECORDS (
                    record_id INTEGER PRIMARY KEY,
                    appointment_id INTEGER NOT NULL UNIQUE,
                    symptoms TEXT NOT NULL,
                    diagnosis TEXT NOT NULL,
                    treatment TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY (appointment_id) REFERENCES APPOINTMENTS(appointment_id)
                )
                """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS PRESCRIPTIONS (
                    prescription_id INTEGER PRIMARY KEY,
                    record_id INTEGER NOT NULL UNIQUE,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY (record_id) REFERENCES MEDICAL_RECORDS(record_id)
                )
                """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS PRESCRIPTION_ITEMS (
                    prescription_id INTEGER NOT NULL,
                    item_index INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    dosage TEXT NOT NULL,
                    frequency TEXT NOT NULL,
                    days INTEGER NOT NULL,
                    instructions TEXT,
                    PRIMARY KEY (prescription_id, item_index),
                    FOREIGN KEY (prescription_id) REFERENCES PRESCRIPTIONS(prescription_id)
                )
                """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS VACCINATIONS (
                    vaccination_id INTEGER PRIMARY KEY,
                    pet_id INTEGER NOT NULL,
                    vaccine_name TEXT NOT NULL,
                    vaccination_date TEXT NOT NULL,
                    next_due_date TEXT,
                    notes TEXT,
                    FOREIGN KEY (pet_id) REFERENCES PETS(pet_id)
                )
                """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS SERVICES (
                    service_id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL UNIQUE COLLATE NOCASE,
                    price INTEGER NOT NULL
                )
                """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS INVOICES (
                    invoice_id INTEGER PRIMARY KEY,
                    appointment_id INTEGER NOT NULL UNIQUE,
                    created_at TEXT NOT NULL,
                    status TEXT NOT NULL,
                    payment_method TEXT NOT NULL,
                    FOREIGN KEY (appointment_id) REFERENCES APPOINTMENTS(appointment_id)
                )
                """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS INVOICE_ITEMS (
                    invoice_id INTEGER NOT NULL,
                    item_index INTEGER NOT NULL,
                    service_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    unit_price INTEGER NOT NULL,
                    quantity INTEGER NOT NULL,
                    PRIMARY KEY (invoice_id, item_index),
                    FOREIGN KEY (invoice_id) REFERENCES INVOICES(invoice_id)
                )
                """);
            System.out.println("SQLite initialized successfully.");

        } catch (SQLException ex) {
            throw new IllegalStateException(
                "Cannot initialize VetCare database.", ex
            );
        }
    }

    public static void saveOwner(Owner owner) {
        String sql = """
            INSERT INTO OWNERS (owner_id, full_name, phone, email)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, owner.id);
            stmt.setString(2, owner.name);
            stmt.setString(3, owner.phone);
            stmt.setString(4, owner.email);
            stmt.executeUpdate();

        } catch (SQLException ex) {
            throw new IllegalStateException(
                "Cannot save owner: " + ex.getMessage(), ex
            );
        }
    }

    
    public static void savePet(Pet pet) {
        String sql = """
            INSERT INTO PETS
            (pet_id, owner_id, pet_name, species, breed, age)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = connect()) {

            // Enable foreign key constraints
            try (Statement pragma = conn.createStatement()) {
                pragma.execute("PRAGMA foreign_keys = ON");
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, pet.id);
                stmt.setInt(2, pet.ownerId);
                stmt.setString(3, pet.name);
                stmt.setString(4, pet.species);
                stmt.setString(5, pet.breed);
                stmt.setInt(6, pet.age);

                stmt.executeUpdate();
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                "Cannot save pet: " + ex.getMessage(), ex
            );
        }
    }


    public static List<Owner> loadOwners() {
        List<Owner> result = new ArrayList<>();

        String sql = """
            SELECT owner_id, full_name, phone, email
            FROM OWNERS ORDER BY owner_id
            """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(new Owner(
                    rs.getInt("owner_id"),
                    rs.getString("full_name"),
                    rs.getString("phone"),
                    rs.getString("email")
                ));
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                "Cannot load owners.", ex
            );
        }

        return result;
    }

    public static List<Pet> loadPets() {
        List<Pet> result = new ArrayList<>();

        String sql = """
            SELECT pet_id, owner_id, pet_name, species, breed, age
            FROM PETS ORDER BY pet_id
            """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(new Pet(
                    rs.getInt("pet_id"),
                    rs.getInt("owner_id"),
                    rs.getString("pet_name"),
                    rs.getString("species"),
                    rs.getString("breed"),
                    rs.getInt("age")
                ));
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                "Cannot load pets.", ex
            );
        }

        return result;
    }

    
    public static void saveAppointment(Appointment a) {
        String sql = """
            INSERT INTO APPOINTMENTS
            (appointment_id, pet_id, veterinarian,
            appointment_datetime, status, checked_in_at,
            weight_kg, check_in_notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = connect()) {
            try (Statement pragma = conn.createStatement()) {
                pragma.execute("PRAGMA foreign_keys = ON");
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, a.id);
                stmt.setInt(2, a.petId);
                stmt.setString(3, a.veterinarian);
                stmt.setString(4, a.dateTime.toString());
                stmt.setString(5, a.status);

                stmt.setString(6,
                    a.checkedInAt == null ? null : a.checkedInAt.toString());

                if (a.weightKg == null) {
                    stmt.setNull(7, Types.REAL);
                } else {
                    stmt.setDouble(7, a.weightKg);
                }

                stmt.setString(8, a.checkInNotes);
                stmt.executeUpdate();
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                "Cannot save appointment: " + ex.getMessage(), ex
            );
        }
    }

    public static List<Appointment> loadAppointments() {
        List<Appointment> result = new ArrayList<>();

        String sql = """
            SELECT * FROM APPOINTMENTS
            ORDER BY appointment_id
            """;

        try (Connection conn = connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Appointment a = new Appointment(
                    rs.getInt("appointment_id"),
                    rs.getInt("pet_id"),
                    rs.getString("veterinarian"),
                    java.time.LocalDateTime.parse(
                        rs.getString("appointment_datetime"))
                );

                a.status = rs.getString("status");

                String checkedIn = rs.getString("checked_in_at");
                a.checkedInAt = checkedIn == null
                    ? null
                    : java.time.LocalDateTime.parse(checkedIn);

                double weight = rs.getDouble("weight_kg");
                a.weightKg = rs.wasNull() ? null : weight;

                a.checkInNotes = rs.getString("check_in_notes");

                result.add(a);
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                "Cannot load appointments: " + ex.getMessage(), ex
            );
        }

        return result;
    }

    public static void updateAppointment(Appointment a) {
        String sql = """
            UPDATE APPOINTMENTS
            SET status = ?, checked_in_at = ?,
                weight_kg = ?, check_in_notes = ?
            WHERE appointment_id = ?
            """;

        try (Connection conn = connect();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, a.status);
            stmt.setString(2,
                a.checkedInAt == null ? null : a.checkedInAt.toString());

            if (a.weightKg == null) {
                stmt.setNull(3, Types.REAL);
            } else {
                stmt.setDouble(3, a.weightKg);
            }

            stmt.setString(4, a.checkInNotes);
            stmt.setInt(5, a.id);

            if (stmt.executeUpdate() != 1) {
                throw new IllegalStateException(
                    "Appointment not found in database."
                );
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                "Cannot update appointment: " + ex.getMessage(), ex
            );
        }
    }

    private static void foreignKeys(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
    }

    public static void saveMedicalRecord(MedicalRecord r, Appointment appointment) {
        try (Connection conn = connect()) {
            foreignKeys(conn);
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement st = conn.prepareStatement("""
                    INSERT INTO MEDICAL_RECORDS
                    (record_id, appointment_id, symptoms, diagnosis, treatment, created_at)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """)) {
                    st.setInt(1, r.id); st.setInt(2, r.appointmentId);
                    st.setString(3, r.symptoms); st.setString(4, r.diagnosis);
                    st.setString(5, r.treatment); st.setString(6, r.createdAt.toString());
                    st.executeUpdate();
                }
                try (PreparedStatement st = conn.prepareStatement("""
                    UPDATE APPOINTMENTS SET status = 'Completed'
                    WHERE appointment_id = ? AND status = 'Checked In'
                    """)) {
                    st.setInt(1, appointment.id);
                    if (st.executeUpdate() != 1) {
                        throw new SQLException("Appointment is not checked in.");
                    }
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot save medical record: " + ex.getMessage(), ex);
        }
    }

    public static List<MedicalRecord> loadMedicalRecords() {
        List<MedicalRecord> result = new ArrayList<>();
        try (Connection conn = connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM MEDICAL_RECORDS ORDER BY record_id")) {
            while (rs.next()) {
                result.add(new MedicalRecord(rs.getInt("record_id"),
                    rs.getInt("appointment_id"), rs.getString("symptoms"),
                    rs.getString("diagnosis"), rs.getString("treatment"),
                    java.time.LocalDateTime.parse(rs.getString("created_at"))));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot load medical records.", ex);
        }
        return result;
    }

    public static void savePrescription(Prescription rx) {
        try (Connection conn = connect()) {
            foreignKeys(conn);
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement st = conn.prepareStatement(
                        "INSERT INTO PRESCRIPTIONS (prescription_id, record_id, created_at) VALUES (?, ?, ?)")) {
                    st.setInt(1, rx.id); st.setInt(2, rx.medicalRecordId);
                    st.setString(3, rx.createdAt.toString()); st.executeUpdate();
                }
                try (PreparedStatement st = conn.prepareStatement("""
                    INSERT INTO PRESCRIPTION_ITEMS
                    (prescription_id, item_index, name, dosage, frequency, days, instructions)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """)) {
                    for (int i = 0; i < rx.medicines.size(); i++) {
                        Prescription.MedicineItem m = rx.medicines.get(i);
                        st.setInt(1, rx.id); st.setInt(2, i);
                        st.setString(3, m.name); st.setString(4, m.dosage);
                        st.setString(5, m.frequency); st.setInt(6, m.days);
                        st.setString(7, m.instructions); st.addBatch();
                    }
                    st.executeBatch();
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot save prescription: " + ex.getMessage(), ex);
        }
    }

    public static List<Prescription> loadPrescriptions() {
        List<Prescription> result = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement items = conn.prepareStatement("""
                 SELECT * FROM PRESCRIPTION_ITEMS
                 WHERE prescription_id = ? ORDER BY item_index
                 """);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM PRESCRIPTIONS ORDER BY prescription_id")) {
            while (rs.next()) {
                List<Prescription.MedicineItem> medicines = new ArrayList<>();
                items.setInt(1, rs.getInt("prescription_id"));
                try (ResultSet ir = items.executeQuery()) {
                    while (ir.next()) {
                        medicines.add(new Prescription.MedicineItem(ir.getString("name"),
                            ir.getString("dosage"), ir.getString("frequency"),
                            ir.getInt("days"), ir.getString("instructions")));
                    }
                }
                result.add(new Prescription(rs.getInt("prescription_id"),
                    rs.getInt("record_id"), medicines,
                    java.time.LocalDateTime.parse(rs.getString("created_at"))));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot load prescriptions.", ex);
        }
        return result;
    }

    public static void saveVaccination(Vaccination v) {
        try (Connection conn = connect()) {
            foreignKeys(conn);
            try (PreparedStatement st = conn.prepareStatement("""
                INSERT INTO VACCINATIONS
                (vaccination_id, pet_id, vaccine_name, vaccination_date, next_due_date, notes)
                VALUES (?, ?, ?, ?, ?, ?)
                """)) {
                st.setInt(1, v.id); st.setInt(2, v.petId);
                st.setString(3, v.vaccineName); st.setString(4, v.vaccinationDate.toString());
                st.setString(5, v.nextDueDate == null ? null : v.nextDueDate.toString());
                st.setString(6, v.notes); st.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot save vaccination: " + ex.getMessage(), ex);
        }
    }

    public static List<Vaccination> loadVaccinations() {
        List<Vaccination> result = new ArrayList<>();
        try (Connection conn = connect(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM VACCINATIONS ORDER BY vaccination_id")) {
            while (rs.next()) {
                String due = rs.getString("next_due_date");
                result.add(new Vaccination(rs.getInt("vaccination_id"), rs.getInt("pet_id"),
                    rs.getString("vaccine_name"), java.time.LocalDate.parse(rs.getString("vaccination_date")),
                    due == null ? null : java.time.LocalDate.parse(due), rs.getString("notes")));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot load vaccinations.", ex);
        }
        return result;
    }

    public static void saveService(ServiceItem s) {
        try (Connection conn = connect(); PreparedStatement st = conn.prepareStatement(
                "INSERT INTO SERVICES (service_id, name, price) VALUES (?, ?, ?)")) {
            st.setInt(1, s.id); st.setString(2, s.name);
            st.setLong(3, s.price); st.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot save service: " + ex.getMessage(), ex);
        }
    }

    public static List<ServiceItem> loadServices() {
        List<ServiceItem> result = new ArrayList<>();
        try (Connection conn = connect(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM SERVICES ORDER BY service_id")) {
            while (rs.next()) {
                result.add(new ServiceItem(rs.getInt("service_id"),
                    rs.getString("name"), rs.getLong("price")));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot load services.", ex);
        }
        return result;
    }

    public static void saveInvoice(Invoice invoice) {
        try (Connection conn = connect()) {
            foreignKeys(conn);
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement st = conn.prepareStatement("""
                    INSERT INTO INVOICES
                    (invoice_id, appointment_id, created_at, status, payment_method)
                    VALUES (?, ?, ?, ?, ?)
                    """)) {
                    st.setInt(1, invoice.id); st.setInt(2, invoice.appointmentId);
                    st.setString(3, invoice.createdAt.toString());
                    st.setString(4, invoice.status); st.setString(5, invoice.paymentMethod);
                    st.executeUpdate();
                }
                try (PreparedStatement st = conn.prepareStatement("""
                    INSERT INTO INVOICE_ITEMS
                    (invoice_id, item_index, service_id, name, unit_price, quantity)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """)) {
                    for (int i = 0; i < invoice.items.size(); i++) {
                        Invoice.LineItem item = invoice.items.get(i);
                        st.setInt(1, invoice.id); st.setInt(2, i);
                        st.setInt(3, item.serviceId); st.setString(4, item.name);
                        st.setLong(5, item.unitPrice); st.setInt(6, item.quantity);
                        st.addBatch();
                    }
                    st.executeBatch();
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot save invoice: " + ex.getMessage(), ex);
        }
    }

    public static List<Invoice> loadInvoices() {
        List<Invoice> result = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement items = conn.prepareStatement("""
                 SELECT * FROM INVOICE_ITEMS WHERE invoice_id = ? ORDER BY item_index
                 """);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM INVOICES ORDER BY invoice_id")) {
            while (rs.next()) {
                List<Invoice.LineItem> lines = new ArrayList<>();
                items.setInt(1, rs.getInt("invoice_id"));
                try (ResultSet ir = items.executeQuery()) {
                    while (ir.next()) {
                        lines.add(new Invoice.LineItem(ir.getInt("service_id"),
                            ir.getString("name"), ir.getLong("unit_price"), ir.getInt("quantity")));
                    }
                }
                Invoice invoice = new Invoice(rs.getInt("invoice_id"),
                    rs.getInt("appointment_id"), lines,
                    java.time.LocalDateTime.parse(rs.getString("created_at")));
                invoice.status = rs.getString("status");
                invoice.paymentMethod = rs.getString("payment_method");
                result.add(invoice);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot load invoices.", ex);
        }
        return result;
    }

    public static void updateInvoice(Invoice invoice) {
        try (Connection conn = connect(); PreparedStatement st = conn.prepareStatement("""
            UPDATE INVOICES SET status = ?, payment_method = ? WHERE invoice_id = ?
            """)) {
            st.setString(1, invoice.status); st.setString(2, invoice.paymentMethod);
            st.setInt(3, invoice.id);
            if (st.executeUpdate() != 1) {
                throw new IllegalStateException("Invoice not found in database.");
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot update invoice: " + ex.getMessage(), ex);
        }
    }
}
