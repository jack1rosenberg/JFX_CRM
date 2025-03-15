package com.example;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private Map<String, Customer> customers;
    private Map<String, Appointment> appointments;
    private Map<String, Service> services;
    private Map<String, Invoice> invoices;

    public CustomerManager() {
        customers = new HashMap<>();
        appointments = new HashMap<>();
        services = new HashMap<>();
        invoices = new HashMap<>();

        // Initialize with default services
        initializeDefaultServices();
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
        return customer.getId();
    }

    public boolean updateCustomer(Customer customer) {
        if (customers.containsKey(customer.getId())) {
            customers.put(customer.getId(), customer);
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
        return appointment.getId();
    }

    public boolean updateAppointment(Appointment appointment) {
        if (appointments.containsKey(appointment.getId())) {
            appointments.put(appointment.getId(), appointment);
            return true;
        }
        return false;
    }

    public boolean deleteAppointment(String appointmentId) {
        if (appointments.containsKey(appointmentId)) {
            appointments.remove(appointmentId);
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
        return service.getId();
    }

    public boolean updateService(Service service) {
        if (services.containsKey(service.getId())) {
            services.put(service.getId(), service);
            return true;
        }
        return false;
    }

    public boolean deleteService(String serviceId) {
        if (services.containsKey(serviceId)) {
            services.remove(serviceId);
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

        return invoice.getId();
    }

    public boolean updateInvoiceStatus(String invoiceId, String status) {
        Invoice invoice = invoices.get(invoiceId);
        if (invoice != null) {
            invoice.setStatus(status);
            if (status.equals("PAID")) {
                invoice.setPaymentDate(LocalDateTime.now());
            }
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