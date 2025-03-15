package com.example;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Backend for my Auto Detailing CRM application, and IB CS2 IA
 * Manages customers, appointments, services, and invoices
 */
public class CustomerManager {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private Map<String, Customer> customers;
    private Map<String, Appointment> appointments;
    private Map<String, Service> services;
    private Map<String, Invoice> invoices;

    private static final String DATA_DIRECTORY = "data";
    private static final String CUSTOMERS_FILE = DATA_DIRECTORY + "/customers.txt";
    private static final String APPOINTMENTS_FILE = DATA_DIRECTORY + "/appointments.txt";
    private static final String SERVICES_FILE = DATA_DIRECTORY + "/services.txt";
    private static final String INVOICES_FILE = DATA_DIRECTORY + "/invoices.txt";


    public CustomerManager() {
        customers = new HashMap<>();
        appointments = new HashMap<>();
        services = new HashMap<>();
        invoices = new HashMap<>();

        // Create data directory if it doesn't exist
        File directory = new File(DATA_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdir();
        }

        // Load existing data or initialize with defaults
        if (!loadData()) {
            // Initialize with default services if loading failed
            initializeDefaultServices();
        }
    }

    private void initializeDefaultServices() {
        addService(new Service("BASIC_WASH", "Basic Wash & Vacuum", "Exterior wash and interior vacuum", 49.99));
        addService(new Service("PREMIUM_WASH", "Premium Wash", "Exterior wash, wax, and interior detailing", 89.99));
        addService(new Service("FULL_DETAIL", "Full Detail Package", "Complete interior and exterior detailing", 149.99));
        addService(new Service("CLAY_POLISH", "Clay Bar & Polish", "Paint correction and polish", 129.99));
        addService(new Service("CERAMIC_COAT", "Ceramic Coating", "Professional ceramic coating application", 299.99));
    }

    // Customer methods
    public String addCustomer(Customer customer) {
        if (customer.getId() == null || customer.getId().isEmpty()) {
            customer.setId(UUID.randomUUID().toString());
        }
        customers.put(customer.getId(), customer);
        saveData(); // Save after modification
        return customer.getId();
    }

    public boolean updateCustomer(Customer customer) {
        if (customers.containsKey(customer.getId())) {
            customers.put(customer.getId(), customer);
            saveData(); // Save after modification
            return true;
        }
        return false;
    }

    public boolean deleteCustomer(String customerId) {
        if (customers.containsKey(customerId)) {
            customers.remove(customerId);

            // Remove related appointments
            List<String> appointmentsToRemove = appointments.values().stream()
                    .filter(a -> a.getCustomerId().equals(customerId))
                    .map(Appointment::getId)
                    .collect(Collectors.toList());

            appointmentsToRemove.forEach(appointments::remove);

            saveData();
            return true;
        }
        return false;
    }

    public Customer getCustomer(String id) {
        return customers.get(id);
    }

    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers.values());
    }

    public List<Customer> searchCustomers(String query) {
        query = query.toLowerCase();
        String finalQuery = query;
        return customers.values().stream()
                .filter(c -> c.getFirstName().toLowerCase().contains(finalQuery) ||
                        c.getLastName().toLowerCase().contains(finalQuery) ||
                        c.getEmail().toLowerCase().contains(finalQuery) ||
                        c.getPhone().toLowerCase().contains(finalQuery))
                .collect(Collectors.toList());
    }

    // Appointment methods
    public String addAppointment(Appointment appointment) {
        if (appointment.getId() == null || appointment.getId().isEmpty()) {
            appointment.setId(UUID.randomUUID().toString());
        }
        appointments.put(appointment.getId(), appointment);
        saveData();
        return appointment.getId();
    }

    public boolean updateAppointment(Appointment appointment) {
        if (appointments.containsKey(appointment.getId())) {
            appointments.put(appointment.getId(), appointment);
            saveData();
            return true;
        }
        return false;
    }

    public boolean deleteAppointment(String appointmentId) {
        if (appointments.containsKey(appointmentId)) {
            appointments.remove(appointmentId);
            saveData();
            return true;
        }
        return false;
    }

    public Appointment getAppointment(String id) {
        return appointments.get(id);
    }

    public List<Appointment> getAllAppointments() {
        return new ArrayList<>(appointments.values());
    }

    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        return appointments.values().stream()
                .filter(a -> a.getDateTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<Appointment> getAppointmentsByCustomer(String customerId) {
        return appointments.values().stream()
                .filter(a -> a.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }

    // Service methods
    public String addService(Service service) {
        if (service.getId() == null || service.getId().isEmpty()) {
            service.setId(UUID.randomUUID().toString());
        }
        services.put(service.getId(), service);
        saveData();
        return service.getId();
    }

    public boolean updateService(Service service) {
        if (services.containsKey(service.getId())) {
            services.put(service.getId(), service);
            saveData();
            return true;
        }
        return false;
    }

    public boolean deleteService(String serviceId) {
        if (services.containsKey(serviceId)) {
            services.remove(serviceId);
            saveData();
            return true;
        }
        return false;
    }

    public Service getService(String id) {
        return services.get(id);
    }

    public List<Service> getAllServices() {
        return new ArrayList<>(services.values());
    }

    // Invoice methods
    public String createInvoice(String customerId, List<String> serviceIds, String appointmentId) {
        Invoice invoice = new Invoice();
        invoice.setId(UUID.randomUUID().toString());
        invoice.setCustomerId(customerId);
        invoice.setAppointmentId(appointmentId);
        invoice.setCreationDate(LocalDateTime.now());
        invoice.setStatus("PENDING");

        double total = 0;
        for (String serviceId : serviceIds) {
            Service service = services.get(serviceId);
            if (service != null) {
                invoice.getServiceIds().add(serviceId);
                total += service.getPrice();
            }
        }

        invoice.setTotalAmount(total);
        invoices.put(invoice.getId(), invoice);

        saveData();
        return invoice.getId();
    }

    public boolean updateInvoiceStatus(String invoiceId, String status) {
        Invoice invoice = invoices.get(invoiceId);
        if (invoice != null) {
            invoice.setStatus(status);
            if (status.equals("PAID")) {
                invoice.setPaymentDate(LocalDateTime.now());
            }
            saveData();
            return true;
        }
        return false;
    }

    public Invoice getInvoice(String id) {
        return invoices.get(id);
    }

    public List<Invoice> getAllInvoices() {
        return new ArrayList<>(invoices.values());
    }

    public List<Invoice> getInvoicesByCustomer(String customerId) {
        return invoices.values().stream()
                .filter(i -> i.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }

    public List<Invoice> getPendingInvoices() {
        return invoices.values().stream()
                .filter(i -> i.getStatus().equals("PENDING"))
                .collect(Collectors.toList());
    }


    public boolean saveData() {
        try {
            saveCustomers();
            saveAppointments();
            saveServices();
            saveInvoices();
            return true;
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadData() {
        try {
            boolean customersLoaded = loadCustomers();
            boolean appointmentsLoaded = loadAppointments();
            boolean servicesLoaded = loadServices();
            boolean invoicesLoaded = loadInvoices();

            // Return true if at least services were loaded successfully
            return servicesLoaded;
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    private void saveCustomers() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CUSTOMERS_FILE))) {
            for (Customer customer : customers.values()) {
                StringBuilder sb = new StringBuilder();
                sb.append(customer.getId()).append("|");
                sb.append(customer.getFirstName()).append("|");
                sb.append(customer.getLastName()).append("|");
                sb.append(customer.getEmail()).append("|");
                sb.append(customer.getPhone()).append("|");
                sb.append(escapeField(customer.getAddress())).append("|");
                sb.append(customer.getVehicleMake()).append("|");
                sb.append(customer.getVehicleModel()).append("|");
                sb.append(customer.getVehicleYear()).append("|");
                sb.append(customer.getVehicleColor()).append("|");
                sb.append(escapeField(customer.getNotes())).append("|");
                sb.append(customer.getCreatedAt().format(DATE_FORMATTER));

                writer.println(sb.toString());
            }
        }
    }

    private boolean loadCustomers() throws IOException {
        File file = new File(CUSTOMERS_FILE);
        if (!file.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            customers.clear();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 12) {
                    Customer customer = new Customer();
                    customer.setId(parts[0]);
                    customer.setFirstName(parts[1]);
                    customer.setLastName(parts[2]);
                    customer.setEmail(parts[3]);
                    customer.setPhone(parts[4]);
                    customer.setAddress(unescapeField(parts[5]));
                    customer.setVehicleMake(parts[6]);
                    customer.setVehicleModel(parts[7]);
                    customer.setVehicleYear(parts[8]);
                    customer.setVehicleColor(parts[9]);
                    customer.setNotes(unescapeField(parts[10]));
                    customer.setCreatedAt(LocalDateTime.parse(parts[11], DATE_FORMATTER));

                    customers.put(customer.getId(), customer);
                }
            }
            return true;
        }
    }

    // Appointment persistence
    private void saveAppointments() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(APPOINTMENTS_FILE))) {
            for (Appointment appointment : appointments.values()) {
                StringBuilder sb = new StringBuilder();
                sb.append(appointment.getId()).append("|");
                sb.append(appointment.getCustomerId()).append("|");
                sb.append(appointment.getDateTime().format(DATE_FORMATTER)).append("|");
                sb.append(escapeField(appointment.getLocation())).append("|");

                // Save service IDs as comma-separated values
                sb.append(appointment.getServiceIds().stream()
                        .collect(Collectors.joining(","))).append("|");

                sb.append(appointment.getStatus()).append("|");
                sb.append(escapeField(appointment.getNotes()));

                writer.println(sb.toString());
            }
        }
    }

    private boolean loadAppointments() throws IOException {
        File file = new File(APPOINTMENTS_FILE);
        if (!file.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            appointments.clear();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6) {
                    Appointment appointment = new Appointment();
                    appointment.setId(parts[0]);
                    appointment.setCustomerId(parts[1]);
                    appointment.setDateTime(LocalDateTime.parse(parts[2], DATE_FORMATTER));
                    appointment.setLocation(unescapeField(parts[3]));

                    // Parse service IDs
                    List<String> serviceIds = new ArrayList<>();
                    if (parts[4] != null && !parts[4].isEmpty()) {
                        String[] serviceIdArray = parts[4].split(",");
                        for (String serviceId : serviceIdArray) {
                            serviceIds.add(serviceId);
                        }
                    }
                    appointment.setServiceIds(serviceIds);

                    appointment.setStatus(parts[5]);

                    if (parts.length > 6) {
                        appointment.setNotes(unescapeField(parts[6]));
                    }

                    appointments.put(appointment.getId(), appointment);
                }
            }
            return true;
        }
    }

    // Service persistence
    private void saveServices() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SERVICES_FILE))) {
            for (Service service : services.values()) {
                StringBuilder sb = new StringBuilder();
                sb.append(service.getId()).append("|");
                sb.append(service.getCode()).append("|");
                sb.append(service.getName()).append("|");
                sb.append(escapeField(service.getDescription())).append("|");
                sb.append(service.getPrice());

                writer.println(sb.toString());
            }
        }
    }

    private boolean loadServices() throws IOException {
        File file = new File(SERVICES_FILE);
        if (!file.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            services.clear();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    Service service = new Service();
                    service.setId(parts[0]);
                    service.setCode(parts[1]);
                    service.setName(parts[2]);
                    service.setDescription(unescapeField(parts[3]));
                    service.setPrice(Double.parseDouble(parts[4]));

                    services.put(service.getId(), service);
                }
            }
            return true;
        }
    }

    // Invoice persistence
    private void saveInvoices() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(INVOICES_FILE))) {
            for (Invoice invoice : invoices.values()) {
                StringBuilder sb = new StringBuilder();
                sb.append(invoice.getId()).append("|");
                sb.append(invoice.getCustomerId()).append("|");
                sb.append(invoice.getAppointmentId()).append("|");

                // Save service IDs as comma-separated values
                sb.append(invoice.getServiceIds().stream()
                        .collect(Collectors.joining(","))).append("|");

                sb.append(invoice.getTotalAmount()).append("|");
                sb.append(invoice.getStatus()).append("|");
                sb.append(invoice.getCreationDate().format(DATE_FORMATTER)).append("|");

                // Payment date might be null
                sb.append(invoice.getPaymentDate() != null ?
                        invoice.getPaymentDate().format(DATE_FORMATTER) : "");

                writer.println(sb.toString());
            }
        }
    }

    private boolean loadInvoices() throws IOException {
        File file = new File(INVOICES_FILE);
        if (!file.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            invoices.clear();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 7) {
                    Invoice invoice = new Invoice();
                    invoice.setId(parts[0]);
                    invoice.setCustomerId(parts[1]);
                    invoice.setAppointmentId(parts[2]);

                    // Parse service IDs
                    List<String> serviceIds = new ArrayList<>();
                    if (parts[3] != null && !parts[3].isEmpty()) {
                        String[] serviceIdArray = parts[3].split(",");
                        for (String serviceId : serviceIdArray) {
                            serviceIds.add(serviceId);
                        }
                    }
                    invoice.setServiceIds(serviceIds);

                    invoice.setTotalAmount(Double.parseDouble(parts[4]));
                    invoice.setStatus(parts[5]);
                    invoice.setCreationDate(LocalDateTime.parse(parts[6], DATE_FORMATTER));

                    // Handle payment date if present
                    if (parts.length > 7 && !parts[7].isEmpty()) {
                        invoice.setPaymentDate(LocalDateTime.parse(parts[7], DATE_FORMATTER));
                    }

                    invoices.put(invoice.getId(), invoice);
                }
            }
            return true;
        }
    }


    /**
     * Helper method to escape pipe characters in fields
     * so they don't interfere with parsing
     */
    private String escapeField(String field) {
        if (field == null) return "";
        return field.replace("|", "\\|").replace("\n", "\\n");
    }

    /**
     * Helper method to unescape pipe characters in fields
     */
    private String unescapeField(String field) {
        if (field == null) return "";
        return field.replace("\\|", "|").replace("\\n", "\n");
    }



    public boolean backupData(String backupSuffix) {
        try {
            copyFile(CUSTOMERS_FILE, CUSTOMERS_FILE + "." + backupSuffix);
            copyFile(APPOINTMENTS_FILE, APPOINTMENTS_FILE + "." + backupSuffix);
            copyFile(SERVICES_FILE, SERVICES_FILE + "." + backupSuffix);
            copyFile(INVOICES_FILE, INVOICES_FILE + "." + backupSuffix);
            return true;
        } catch (IOException e) {
            System.err.println("Error creating backup: " + e.getMessage());
            return false;
        }
    }

    private void copyFile(String source, String destination) throws IOException {
        File sourceFile = new File(source);
        if (!sourceFile.exists()) return;

        try (
                FileInputStream in = new FileInputStream(sourceFile);
                FileOutputStream out = new FileOutputStream(new File(destination))
        ) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }



    // Data classes
    public static class Customer {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String address;
        private String vehicleMake;
        private String vehicleModel;
        private String vehicleYear;
        private String vehicleColor;
        private String notes;
        private LocalDateTime createdAt;

        public Customer() {
            this.createdAt = LocalDateTime.now();
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getVehicleMake() { return vehicleMake; }
        public void setVehicleMake(String vehicleMake) { this.vehicleMake = vehicleMake; }

        public String getVehicleModel() { return vehicleModel; }
        public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

        public String getVehicleYear() { return vehicleYear; }
        public void setVehicleYear(String vehicleYear) { this.vehicleYear = vehicleYear; }

        public String getVehicleColor() { return vehicleColor; }
        public void setVehicleColor(String vehicleColor) { this.vehicleColor = vehicleColor; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public String getFullName() {
            return firstName + " " + lastName;
        }
    }

    public static class Appointment {
        private String id;
        private String customerId;
        private LocalDateTime dateTime;
        private String location;
        private List<String> serviceIds;
        private String status; // SCHEDULED, COMPLETED, CANCELLED
        private String notes;

        public Appointment() {
            serviceIds = new ArrayList<>();
            status = "SCHEDULED";
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }

        public LocalDateTime getDateTime() { return dateTime; }
        public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public List<String> getServiceIds() { return serviceIds; }
        public void setServiceIds(List<String> serviceIds) { this.serviceIds = serviceIds; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class Service {
        private String id;
        private String code;
        private String name;
        private String description;
        private double price;

        public Service() {}

        public Service(String code, String name, String description, double price) {
            this.id = UUID.randomUUID().toString();
            this.code = code;
            this.name = name;
            this.description = description;
            this.price = price;
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }

    public static class Invoice {
        private String id;
        private String customerId;
        private String appointmentId;
        private List<String> serviceIds;
        private double totalAmount;
        private String status; // PENDING, PAID, CANCELLED
        private LocalDateTime creationDate;
        private LocalDateTime paymentDate;

        public Invoice() {
            serviceIds = new ArrayList<>();
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }

        public String getAppointmentId() { return appointmentId; }
        public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

        public List<String> getServiceIds() { return serviceIds; }
        public void setServiceIds(List<String> serviceIds) { this.serviceIds = serviceIds; }

        public double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDateTime getCreationDate() { return creationDate; }
        public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

        public LocalDateTime getPaymentDate() { return paymentDate; }
        public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    }

    // For demo/testing purposes
    public void addSampleData() {
        // Add sample customers
        Customer c1 = new Customer();
        c1.setFirstName("John");
        c1.setLastName("Smith");
        c1.setEmail("john.smith@example.com");
        c1.setPhone("555-123-4567");
        c1.setAddress("123 Main St, Anytown, USA");
        c1.setVehicleMake("Honda");
        c1.setVehicleModel("Accord");
        c1.setVehicleYear("2019");
        c1.setVehicleColor("Black");
        String c1Id = addCustomer(c1);

        Customer c2 = new Customer();
        c2.setFirstName("Emily");
        c2.setLastName("Johnson");
        c2.setEmail("emily.j@example.com");
        c2.setPhone("555-987-6543");
        c2.setAddress("456 Oak Ave, Somewhere, USA");
        c2.setVehicleMake("Toyota");
        c2.setVehicleModel("Camry");
        c2.setVehicleYear("2021");
        c2.setVehicleColor("Silver");
        String c2Id = addCustomer(c2);

        // Add sample appointments
        Appointment a1 = new Appointment();
        a1.setCustomerId(c1Id);
        a1.setDateTime(LocalDateTime.now().plusDays(2).withHour(10).withMinute(0));
        a1.setLocation("123 Main St, Anytown, USA");
        a1.getServiceIds().add(services.values().stream()
                .filter(s -> s.getCode().equals("FULL_DETAIL"))
                .findFirst().get().getId());
        String a1Id = addAppointment(a1);

        Appointment a2 = new Appointment();
        a2.setCustomerId(c2Id);
        a2.setDateTime(LocalDateTime.now().plusDays(3).withHour(14).withMinute(30));
        a2.setLocation("456 Oak Ave, Somewhere, USA");
        a2.getServiceIds().add(services.values().stream()
                .filter(s -> s.getCode().equals("BASIC_WASH"))
                .findFirst().get().getId());
        a2.getServiceIds().add(services.values().stream()
                .filter(s -> s.getCode().equals("CLAY_POLISH"))
                .findFirst().get().getId());
        String a2Id = addAppointment(a2);

        // Create invoices
        createInvoice(c1Id, a1.getServiceIds(), a1Id);
        createInvoice(c2Id, a2.getServiceIds(), a2Id);
    }
}