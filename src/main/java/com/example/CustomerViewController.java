package com.example;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Callback;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class CustomerViewController {
    private final CustomerManager customerManager;
    private final BorderPane view;
    private TableView<CustomerManager.Customer> customerTable;

    public CustomerViewController(CustomerManager customerManager) {
        this.customerManager = customerManager;
        this.view = new BorderPane();
        createCustomerView();
    }

    public BorderPane getView() {
        return view;
    }

    private void createCustomerView() {
        // Top section - search and add
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10));

        TextField searchField = new TextField();
        searchField.setPromptText("Search customers...");
        searchField.setPrefWidth(300);

        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                List<CustomerManager.Customer> results = customerManager.searchCustomers(query);
                refreshTable(results);
            } else {
                refreshTable(customerManager.getAllCustomers());
            }
        });

        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> {
            searchField.clear();
            refreshTable(customerManager.getAllCustomers());
        });

        Button addBtn = new Button("Add Customer");
        addBtn.setOnAction(e -> showCustomerDialog(null));

        topBar.getChildren().addAll(searchField, searchBtn, clearBtn, addBtn);

        // Center section - customer table
        customerTable = new TableView<>();
        customerTable.setPadding(new Insets(10));

        TableColumn<CustomerManager.Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setPrefWidth(150);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<CustomerManager.Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setPrefWidth(150);
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<CustomerManager.Customer, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setPrefWidth(120);
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<CustomerManager.Customer, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setPrefWidth(200);
        vehicleCol.setCellValueFactory(cellData -> {
            CustomerManager.Customer customer = cellData.getValue();
            String vehicle = String.format("%s %s %s - %s",
                    customer.getVehicleYear(),
                    customer.getVehicleMake(),
                    customer.getVehicleModel(),
                    customer.getVehicleColor());
            return javafx.beans.binding.Bindings.createStringBinding(() -> vehicle);
        });

        TableColumn<CustomerManager.Customer, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(createActionCellFactory());

        customerTable.getColumns().addAll(nameCol, emailCol, phoneCol, vehicleCol, actionsCol);
        refreshTable(customerManager.getAllCustomers());

        view.setTop(topBar);
        view.setCenter(customerTable);
    }

    private Callback<TableColumn<CustomerManager.Customer, Void>, TableCell<CustomerManager.Customer, Void>> createActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<CustomerManager.Customer, Void> call(TableColumn<CustomerManager.Customer, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    private final Button appointmentsBtn = new Button("Appointments");
                    private final Button invoicesBtn = new Button("Invoices");
                    private final HBox buttonBox = new HBox(5, editBtn, deleteBtn, appointmentsBtn, invoicesBtn);

                    {
                        editBtn.setOnAction(event -> {
                            CustomerManager.Customer customer = getTableView().getItems().get(getIndex());
                            showCustomerDialog(customer);
                        });

                        deleteBtn.setOnAction(event -> {
                            CustomerManager.Customer customer = getTableView().getItems().get(getIndex());
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Delete Customer");
                            alert.setHeaderText("Delete Customer: " + customer.getFullName());
                            alert.setContentText("Are you sure you want to delete this customer? This will also remove all associated appointments and invoices.");

                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() && result.get() == ButtonType.OK) {
                                customerManager.deleteCustomer(customer.getId());
                                refreshTable(customerManager.getAllCustomers());
                            }
                        });

                        appointmentsBtn.setOnAction(event -> {
                            CustomerManager.Customer customer = getTableView().getItems().get(getIndex());
                            showCustomerAppointments(customer);
                        });

                        invoicesBtn.setOnAction(event -> {
                            CustomerManager.Customer customer = getTableView().getItems().get(getIndex());
                            showCustomerInvoices(customer);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(buttonBox);
                        }
                    }
                };
            }
        };
    }

    private void showCustomerDialog(CustomerManager.Customer customer) {
        boolean isNewCustomer = customer == null;
        customer = isNewCustomer ? new CustomerManager.Customer() : customer;

        // Create the dialog
        Dialog<CustomerManager.Customer> dialog = new Dialog<>();
        dialog.setTitle(isNewCustomer ? "Add New Customer" : "Edit Customer");
        dialog.setHeaderText(isNewCustomer ? "Enter customer details" : "Edit customer details");

        // Set button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField(customer.getFirstName());
        firstNameField.setPromptText("First Name");

        TextField lastNameField = new TextField(customer.getLastName());
        lastNameField.setPromptText("Last Name");

        TextField emailField = new TextField(customer.getEmail());
        emailField.setPromptText("Email");

        TextField phoneField = new TextField(customer.getPhone());
        phoneField.setPromptText("Phone");

        TextField addressField = new TextField(customer.getAddress());
        addressField.setPromptText("Address");

        TextField vehicleMakeField = new TextField(customer.getVehicleMake());
        vehicleMakeField.setPromptText("Vehicle Make");

        TextField vehicleModelField = new TextField(customer.getVehicleModel());
        vehicleModelField.setPromptText("Vehicle Model");

        TextField vehicleYearField = new TextField(customer.getVehicleYear());
        vehicleYearField.setPromptText("Vehicle Year");

        TextField vehicleColorField = new TextField(customer.getVehicleColor());
        vehicleColorField.setPromptText("Vehicle Color");

        TextArea notesArea = new TextArea(customer.getNotes());
        notesArea.setPromptText("Notes");

        // Add fields to grid
        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Address:"), 0, 4);
        grid.add(addressField, 1, 4);
        grid.add(new Label("Vehicle Make:"), 0, 5);
        grid.add(vehicleMakeField, 1, 5);
        grid.add(new Label("Vehicle Model:"), 0, 6);
        grid.add(vehicleModelField, 1, 6);
        grid.add(new Label("Vehicle Year:"), 0, 7);
        grid.add(vehicleYearField, 1, 7);
        grid.add(new Label("Vehicle Color:"), 0, 8);
        grid.add(vehicleColorField, 1, 8);
        grid.add(new Label("Notes:"), 0, 9);
        grid.add(notesArea, 1, 9);

        dialog.getDialogPane().setContent(grid);

        // Convert result to customer when Save button is clicked
        CustomerManager.Customer finalCustomer = customer;
        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                finalCustomer.setFirstName(firstNameField.getText());
                finalCustomer.setLastName(lastNameField.getText());
                finalCustomer.setEmail(emailField.getText());
                finalCustomer.setPhone(phoneField.getText());
                finalCustomer.setAddress(addressField.getText());
                finalCustomer.setVehicleMake(vehicleMakeField.getText());
                finalCustomer.setVehicleModel(vehicleModelField.getText());
                finalCustomer.setVehicleYear(vehicleYearField.getText());
                finalCustomer.setVehicleColor(vehicleColorField.getText());
                finalCustomer.setNotes(notesArea.getText());
                return finalCustomer;
            }
            return null;
        });

        Optional<CustomerManager.Customer> result = dialog.showAndWait();

        result.ifPresent(c -> {
            if (isNewCustomer) {
                customerManager.addCustomer(c);
            } else {
                customerManager.updateCustomer(c);
            }
            refreshTable(customerManager.getAllCustomers());
        });
    }

    private void showCustomerAppointments(CustomerManager.Customer customer) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Appointments for " + customer.getFullName());

        // Set button types
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Create appointments table
        TableView<CustomerManager.Appointment> appointmentsTable = new TableView<>();

        TableColumn<CustomerManager.Appointment, String> dateCol = new TableColumn<>("Date/Time");
        dateCol.setPrefWidth(150);
        dateCol.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedDate = cellData.getValue().getDateTime().format(formatter);
            return javafx.beans.binding.Bindings.createStringBinding(() -> formattedDate);
        });

        TableColumn<CustomerManager.Appointment, String> locationCol = new TableColumn<>("Location");
        locationCol.setPrefWidth(200);
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));

        TableColumn<CustomerManager.Appointment, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(100);
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        appointmentsTable.getColumns().addAll(dateCol, locationCol, statusCol);

        // Populate table with customer's appointments
        List<CustomerManager.Appointment> appointments = customerManager.getAppointmentsByCustomer(customer.getId());
        appointmentsTable.setItems(FXCollections.observableArrayList(appointments));

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(
                new Label("Appointments for " + customer.getFullName()),
                appointmentsTable
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(500, 400);

        dialog.showAndWait();
    }

    private void showCustomerInvoices(CustomerManager.Customer customer) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Invoices for " + customer.getFullName());

        // Set button types
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Create invoices table
        TableView<CustomerManager.Invoice> invoicesTable = new TableView<>();

        TableColumn<CustomerManager.Invoice, String> dateCol = new TableColumn<>("Created Date");
        dateCol.setPrefWidth(150);
        dateCol.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = cellData.getValue().getCreationDate().format(formatter);
            return javafx.beans.binding.Bindings.createStringBinding(() -> formattedDate);
        });

        TableColumn<CustomerManager.Invoice, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setPrefWidth(100);
        amountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        amountCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });

        TableColumn<CustomerManager.Invoice, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(100);
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        invoicesTable.getColumns().addAll(dateCol, amountCol, statusCol);

        // Populate table with customer's invoices
        List<CustomerManager.Invoice> invoices = customerManager.getInvoicesByCustomer(customer.getId());
        invoicesTable.setItems(FXCollections.observableArrayList(invoices));

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(
                new Label("Invoices for " + customer.getFullName()),
                invoicesTable
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(500, 400);

        dialog.showAndWait();
    }

    private void refreshTable(List<CustomerManager.Customer> customers) {
        customerTable.setItems(FXCollections.observableArrayList(customers));
    }
}