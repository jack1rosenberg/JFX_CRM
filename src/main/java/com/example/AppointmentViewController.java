package com.example;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AppointmentViewController {
    private CustomerManager customerManager;
    private BorderPane mainView;
    private TableView<CustomerManager.Appointment> appointmentTable;
    private ObservableList<CustomerManager.Appointment> appointmentList;
    private ComboBox<LocalDate> dateFilterComboBox;
    private ComboBox<CustomerManager.Customer> customerFilterComboBox;

    public AppointmentViewController(CustomerManager customerManager) {
        this.customerManager = customerManager;
        this.appointmentList = FXCollections.observableArrayList();
        createView();
        refreshData();
    }

    public BorderPane getView() {
        return mainView;
    }

    private void createView() {
        mainView = new BorderPane();
        mainView.setPadding(new Insets(10));

        // Top section with filter controls
        HBox filterBox = new HBox(10);
        filterBox.setPadding(new Insets(0, 0, 10, 0));

        // Date filter
        dateFilterComboBox = new ComboBox<>();
        dateFilterComboBox.setPromptText("Filter by Date");
        dateFilterComboBox.setCellFactory(getDateCellFactory());
        dateFilterComboBox.setButtonCell(getDateCellFactory().call(null));

        // Add next 30 days to date filter
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 30; i++) {
            dateFilterComboBox.getItems().add(today.plusDays(i));
        }

        dateFilterComboBox.setOnAction(e -> {
            if (dateFilterComboBox.getValue() != null) {
                customerFilterComboBox.setValue(null);
                filterAppointmentsByDate(dateFilterComboBox.getValue());
            }
        });

        // Customer filter
        customerFilterComboBox = new ComboBox<>();
        customerFilterComboBox.setPromptText("Filter by Customer");
        refreshCustomerFilter(); // Load customer data

        customerFilterComboBox.setOnAction(e -> {
            if (customerFilterComboBox.getValue() != null) {
                dateFilterComboBox.setValue(null);
                filterAppointmentsByCustomer(customerFilterComboBox.getValue().getId());
            }
        });

        // Reset button
        Button resetFilterBtn = new Button("Reset Filters");
        resetFilterBtn.setOnAction(e -> {
            dateFilterComboBox.setValue(null);
            customerFilterComboBox.setValue(null);
            refreshData();
        });

        filterBox.getChildren().addAll(
                new Label("Filter:"),
                dateFilterComboBox,
                customerFilterComboBox,
                resetFilterBtn
        );

        // Create the appointment table
        appointmentTable = new TableView<>();
        appointmentTable.setPlaceholder(new Label("No appointments available"));

        // Define columns
        TableColumn<CustomerManager.Appointment, String> customerColumn = new TableColumn<>("Customer");
        customerColumn.setCellValueFactory(cellData -> {
            CustomerManager.Customer customer = customerManager.getCustomer(cellData.getValue().getCustomerId());
            return new SimpleStringProperty(customer != null ? customer.getFullName() : "Unknown");
        });

        TableColumn<CustomerManager.Appointment, LocalDateTime> dateTimeColumn = new TableColumn<>("Date & Time");
        dateTimeColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDateTime()));
        dateTimeColumn.setCellFactory(column -> new TableCell<CustomerManager.Appointment, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        TableColumn<CustomerManager.Appointment, String> locationColumn = new TableColumn<>("Location");
        locationColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLocation()));

        TableColumn<CustomerManager.Appointment, String> servicesColumn = new TableColumn<>("Services");
        servicesColumn.setCellValueFactory(cellData -> {
            List<String> serviceNames = cellData.getValue().getServiceIds().stream()
                    .map(id -> {
                        CustomerManager.Service service = customerManager.getService(id);
                        return service != null ? service.getName() : "Unknown";
                    })
                    .collect(Collectors.toList());
            return new SimpleStringProperty(String.join(", ", serviceNames));
        });

        TableColumn<CustomerManager.Appointment, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()));

        // Add columns to table
        appointmentTable.getColumns().addAll(
                customerColumn,
                dateTimeColumn,
                locationColumn,
                servicesColumn,
                statusColumn
        );

        appointmentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        appointmentTable.setItems(appointmentList);

        // Create bottom action buttons
        HBox actionBox = new HBox(10);
        actionBox.setPadding(new Insets(10, 0, 0, 0));

        Button addBtn = new Button("Add Appointment");
        addBtn.setOnAction(e -> showAppointmentDialog(null));

        Button editBtn = new Button("Edit Appointment");
        editBtn.setOnAction(e -> {
            CustomerManager.Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showAppointmentDialog(selected);
            } else {
                showAlert(Alert.AlertType.WARNING, "No Selection",
                        "Please select an appointment to edit.");
            }
        });

        Button deleteBtn = new Button("Delete Appointment");
        deleteBtn.setOnAction(e -> {
            CustomerManager.Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (showConfirmation("Delete Appointment",
                        "Are you sure you want to delete this appointment?")) {
                    customerManager.deleteAppointment(selected.getId());
                    refreshData();
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "No Selection",
                        "Please select an appointment to delete.");
            }
        });

        Button changeStatusBtn = new Button("Change Status");
        changeStatusBtn.setOnAction(e -> {
            CustomerManager.Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showStatusChangeDialog(selected);
            } else {
                showAlert(Alert.AlertType.WARNING, "No Selection",
                        "Please select an appointment to change status.");
            }
        });

        actionBox.getChildren().addAll(addBtn, editBtn, deleteBtn, changeStatusBtn);

        // Assemble main view
        mainView.setTop(filterBox);
        mainView.setCenter(appointmentTable);
        mainView.setBottom(actionBox);
    }

    public void refreshData() {
        appointmentList.clear();
        appointmentList.addAll(customerManager.getAllAppointments());
        refreshCustomerFilter();
    }

    private void refreshCustomerFilter() {
        customerFilterComboBox.getItems().clear();
        customerFilterComboBox.getItems().addAll(customerManager.getAllCustomers());

        // Setup cell factory to display customer names
        customerFilterComboBox.setCellFactory(new Callback<ListView<CustomerManager.Customer>, ListCell<CustomerManager.Customer>>() {
            @Override
            public ListCell<CustomerManager.Customer> call(ListView<CustomerManager.Customer> param) {
                return new ListCell<CustomerManager.Customer>() {
                    @Override
                    protected void updateItem(CustomerManager.Customer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getFullName());
                        }
                    }
                };
            }
        });

        // Set button cell to display selected customer name
        customerFilterComboBox.setButtonCell(new ListCell<CustomerManager.Customer>() {
            @Override
            protected void updateItem(CustomerManager.Customer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Filter by Customer");
                } else {
                    setText(item.getFullName());
                }
            }
        });
    }

    private Callback<ListView<LocalDate>, ListCell<LocalDate>> getDateCellFactory() {
        return new Callback<ListView<LocalDate>, ListCell<LocalDate>>() {
            @Override
            public ListCell<LocalDate> call(ListView<LocalDate> param) {
                return new ListCell<LocalDate>() {
                    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy");

                    @Override
                    protected void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(formatter.format(item));
                        }
                    }
                };
            }
        };
    }

    private void filterAppointmentsByDate(LocalDate date) {
        appointmentList.clear();
        appointmentList.addAll(customerManager.getAppointmentsByDate(date));
    }

    private void filterAppointmentsByCustomer(String customerId) {
        appointmentList.clear();
        appointmentList.addAll(customerManager.getAppointmentsByCustomer(customerId));
    }

    private void showAppointmentDialog(CustomerManager.Appointment appointment) {
        // Create a dialog for adding/editing appointments
        Dialog<CustomerManager.Appointment> dialog = new Dialog<>();
        dialog.setTitle(appointment == null ? "Add New Appointment" : "Edit Appointment");
        dialog.setHeaderText(appointment == null ?
                "Enter details for new appointment" : "Edit appointment details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Customer selection
        ComboBox<CustomerManager.Customer> customerCombo = new ComboBox<>();
        customerCombo.getItems().addAll(customerManager.getAllCustomers());
        customerCombo.setCellFactory(new Callback<ListView<CustomerManager.Customer>, ListCell<CustomerManager.Customer>>() {
            @Override
            public ListCell<CustomerManager.Customer> call(ListView<CustomerManager.Customer> param) {
                return new ListCell<CustomerManager.Customer>() {
                    @Override
                    protected void updateItem(CustomerManager.Customer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getFullName());
                        }
                    }
                };
            }
        });


        // Date picker
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());

        // Time picker (ComboBox with hour options)
        ComboBox<LocalTime> timePicker = new ComboBox<>();
        // Add time slots from 8 AM to 6 PM at hourly intervals
        for (int hour = 8; hour <= 18; hour++) {
            timePicker.getItems().add(LocalTime.of(hour, 0));
            timePicker.getItems().add(LocalTime.of(hour, 30));
        }
        timePicker.setValue(LocalTime.of(10, 0));
        timePicker.setCellFactory(new Callback<ListView<LocalTime>, ListCell<LocalTime>>() {
            @Override
            public ListCell<LocalTime> call(ListView<LocalTime> param) {
                return new ListCell<LocalTime>() {
                    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

                    @Override
                    protected void updateItem(LocalTime item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(formatter.format(item));
                        }
                    }
                };
            }
        });
        timePicker.setButtonCell(new ListCell<LocalTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        // Location
        TextField locationField = new TextField();
        locationField.setPromptText("Address or location");

        // Services selection
        ListView<CustomerManager.Service> servicesListView = new ListView<>();
        servicesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        servicesListView.setCellFactory(new Callback<ListView<CustomerManager.Service>, ListCell<CustomerManager.Service>>() {
            @Override
            public ListCell<CustomerManager.Service> call(ListView<CustomerManager.Service> param) {
                return new ListCell<CustomerManager.Service>() {
                    @Override
                    protected void updateItem(CustomerManager.Service item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getName() + " - $" + String.format("%.2f", item.getPrice()));
                        }
                    }
                };
            }
        });
        servicesListView.getItems().addAll(customerManager.getAllServices());
        servicesListView.setPrefHeight(150);

        // Notes
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Enter any additional notes here");

        // Add fields to grid
        grid.add(new Label("Customer:"), 0, 0);
        grid.add(customerCombo, 1, 0);
        grid.add(new Label("Date:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Time:"), 0, 2);
        grid.add(timePicker, 1, 2);
        grid.add(new Label("Location:"), 0, 3);
        grid.add(locationField, 1, 3);
        grid.add(new Label("Services:"), 0, 4);
        grid.add(servicesListView, 1, 4);
        grid.add(new Label("Notes:"), 0, 5);
        grid.add(notesArea, 1, 5);

        // Set initial values if editing existing appointment
        if (appointment != null) {
            // Find and select the customer
            for (CustomerManager.Customer customer : customerCombo.getItems()) {
                if (customer.getId().equals(appointment.getCustomerId())) {
                    customerCombo.setValue(customer);
                    break;
                }
            }

            datePicker.setValue(appointment.getDateTime().toLocalDate());
            timePicker.setValue(appointment.getDateTime().toLocalTime());
            locationField.setText(appointment.getLocation());
            notesArea.setText(appointment.getNotes());

            // Select services
            for (CustomerManager.Service service : servicesListView.getItems()) {
                if (appointment.getServiceIds().contains(service.getId())) {
                    servicesListView.getSelectionModel().select(service);
                }
            }
        }

        dialog.getDialogPane().setContent(grid);

        // Convert the result to appointment when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (customerCombo.getValue() == null) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a customer.");
                    return null;
                }

                if (servicesListView.getSelectionModel().getSelectedItems().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select at least one service.");
                    return null;
                }

                CustomerManager.Appointment result = appointment == null ?
                        new CustomerManager.Appointment() : appointment;

                result.setCustomerId(customerCombo.getValue().getId());
                result.setDateTime(LocalDateTime.of(datePicker.getValue(), timePicker.getValue()));
                result.setLocation(locationField.getText());
                result.setNotes(notesArea.getText());

                // Set selected services
                List<String> selectedServiceIds = new ArrayList<>();
                for (CustomerManager.Service service : servicesListView.getSelectionModel().getSelectedItems()) {
                    selectedServiceIds.add(service.getId());
                }
                result.setServiceIds(selectedServiceIds);

                return result;
            }
            return null;
        });


        // Show the dialog and process the result
        Optional<CustomerManager.Appointment> result = dialog.showAndWait();
        result.ifPresent(newAppointment -> {
            if (appointment == null) {
                customerManager.addAppointment(newAppointment);
            } else {
                customerManager.updateAppointment(newAppointment);
            }
            refreshData();
        });
    }

    private void showStatusChangeDialog(CustomerManager.Appointment appointment) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Change Appointment Status");
        dialog.setHeaderText("Select new status for appointment");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create status selection
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("SCHEDULED", "COMPLETED", "CANCELLED");
        statusCombo.setValue(appointment.getStatus());

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(new Label("Status:"), statusCombo);

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return statusCombo.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newStatus -> {
            appointment.setStatus(newStatus);
            customerManager.updateAppointment(appointment);
            refreshData();
        });
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}