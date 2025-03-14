package com.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InvoiceViewController {
    private CustomerManager customerManager;
    private BorderPane view;
    private TableView<InvoiceDisplayItem> invoiceTable;
    private ObservableList<InvoiceDisplayItem> invoiceData;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

    public InvoiceViewController(CustomerManager customerManager) {
        this.customerManager = customerManager;
        createView();
        loadInvoices();
    }

    public BorderPane getView() {
        return view;
    }

    private void createView() {
        view = new BorderPane();
        view.setPadding(new Insets(10));

        // Create top section with title and add button
        HBox topSection = new HBox(10);
        topSection.setPadding(new Insets(0, 0, 10, 0));

        Label titleLabel = new Label("Invoice Management");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Button createInvoiceBtn = new Button("Create New Invoice");
        createInvoiceBtn.setOnAction(e -> showCreateInvoiceDialog());

        topSection.getChildren().addAll(titleLabel, createInvoiceBtn);

        // Create filter buttons
        HBox filterBox = new HBox(10);
        filterBox.setPadding(new Insets(0, 0, 10, 0));

        Button allInvoicesBtn = new Button("All Invoices");
        allInvoicesBtn.setOnAction(e -> loadInvoices());

        Button pendingInvoicesBtn = new Button("Pending Invoices");
        pendingInvoicesBtn.setOnAction(e -> loadPendingInvoices());

        TextField customerSearchField = new TextField();
        customerSearchField.setPromptText("Search by customer name...");
        customerSearchField.setPrefWidth(250);

        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> searchInvoicesByCustomer(customerSearchField.getText()));

        filterBox.getChildren().addAll(allInvoicesBtn, pendingInvoicesBtn, customerSearchField, searchBtn);

        // Create table for invoices
        invoiceTable = new TableView<>();
        invoiceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<InvoiceDisplayItem, String> invoiceIdCol = new TableColumn<>("Invoice #");
        invoiceIdCol.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        invoiceIdCol.setPrefWidth(100);

        TableColumn<InvoiceDisplayItem, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerCol.setPrefWidth(150);

        TableColumn<InvoiceDisplayItem, String> dateCol = new TableColumn<>("Created Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        dateCol.setPrefWidth(150);

        TableColumn<InvoiceDisplayItem, String> servicesCol = new TableColumn<>("Services");
        servicesCol.setCellValueFactory(new PropertyValueFactory<>("services"));
        servicesCol.setPrefWidth(200);

        TableColumn<InvoiceDisplayItem, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(100);
        amountCol.setCellFactory(column -> new TableCell<InvoiceDisplayItem, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                    setText(currencyFormat.format(price));
                }
            }
        });

        TableColumn<InvoiceDisplayItem, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(column -> new TableCell<InvoiceDisplayItem, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "PENDING":
                            setStyle("-fx-text-fill: orange;");
                            break;
                        case "PAID":
                            setStyle("-fx-text-fill: green;");
                            break;
                        case "CANCELLED":
                            setStyle("-fx-text-fill: red;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });

        TableColumn<InvoiceDisplayItem, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(param -> new TableCell<InvoiceDisplayItem, Void>() {
            private final Button viewBtn = new Button("View");
            private final Button markPaidBtn = new Button("Mark Paid");
            private final Button cancelBtn = new Button("Cancel");
            private final HBox pane = new HBox(5, viewBtn, markPaidBtn, cancelBtn);

            {
                viewBtn.setOnAction(event -> {
                    InvoiceDisplayItem invoiceItem = getTableView().getItems().get(getIndex());
                    showInvoiceDetailsDialog(invoiceItem.getInvoiceId());
                });

                markPaidBtn.setOnAction(event -> {
                    InvoiceDisplayItem invoiceItem = getTableView().getItems().get(getIndex());
                    if ("PENDING".equals(invoiceItem.getStatus())) {
                        customerManager.updateInvoiceStatus(invoiceItem.getInvoiceId(), "PAID");
                        refreshData();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Status Update");
                        alert.setHeaderText(null);
                        alert.setContentText("Only pending invoices can be marked as paid.");
                        alert.showAndWait();
                    }
                });

                cancelBtn.setOnAction(event -> {
                    InvoiceDisplayItem invoiceItem = getTableView().getItems().get(getIndex());
                    if (!"PAID".equals(invoiceItem.getStatus())) {
                        customerManager.updateInvoiceStatus(invoiceItem.getInvoiceId(), "CANCELLED");
                        refreshData();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Status Update");
                        alert.setHeaderText(null);
                        alert.setContentText("Paid invoices cannot be cancelled.");
                        alert.showAndWait();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                InvoiceDisplayItem invoiceItem = getTableView().getItems().get(getIndex());

                // Show/hide buttons based on invoice status
                if ("PAID".equals(invoiceItem.getStatus())) {
                    markPaidBtn.setVisible(false);
                    cancelBtn.setVisible(false);
                    setGraphic(new HBox(5, viewBtn));
                } else if ("CANCELLED".equals(invoiceItem.getStatus())) {
                    markPaidBtn.setVisible(false);
                    cancelBtn.setVisible(false);
                    setGraphic(new HBox(5, viewBtn));
                } else {
                    markPaidBtn.setVisible(true);
                    cancelBtn.setVisible(true);
                    setGraphic(pane);
                }
            }
        });

        invoiceTable.getColumns().addAll(
                invoiceIdCol, customerCol, dateCol, servicesCol,
                amountCol, statusCol, actionsCol
        );

        // Add components to view
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(topSection, filterBox, invoiceTable);
        VBox.setVgrow(invoiceTable, Priority.ALWAYS);

        view.setCenter(vbox);
    }

    private void loadInvoices() {
        List<CustomerManager.Invoice> invoices = customerManager.getAllInvoices();
        updateInvoiceTableData(invoices);
    }

    private void loadPendingInvoices() {
        List<CustomerManager.Invoice> pendingInvoices = customerManager.getPendingInvoices();
        updateInvoiceTableData(pendingInvoices);
    }

    private void searchInvoicesByCustomer(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadInvoices();
            return;
        }

        String searchLower = searchText.toLowerCase();
        List<CustomerManager.Invoice> allInvoices = customerManager.getAllInvoices();
        List<CustomerManager.Invoice> filteredInvoices = new ArrayList<>();

        for (CustomerManager.Invoice invoice : allInvoices) {
            CustomerManager.Customer customer = customerManager.getCustomer(invoice.getCustomerId());
            if (customer != null &&
                    (customer.getFirstName().toLowerCase().contains(searchLower) ||
                            customer.getLastName().toLowerCase().contains(searchLower))) {
                filteredInvoices.add(invoice);
            }
        }

        updateInvoiceTableData(filteredInvoices);
    }

    private void updateInvoiceTableData(List<CustomerManager.Invoice> invoices) {
        invoiceData = FXCollections.observableArrayList();

        for (CustomerManager.Invoice invoice : invoices) {
            CustomerManager.Customer customer = customerManager.getCustomer(invoice.getCustomerId());
            String customerName = customer != null ? customer.getFullName() : "Unknown Customer";

            // Get service names
            StringBuilder serviceNames = new StringBuilder();
            for (String serviceId : invoice.getServiceIds()) {
                CustomerManager.Service service = customerManager.getService(serviceId);
                if (service != null) {
                    if (serviceNames.length() > 0) {
                        serviceNames.append(", ");
                    }
                    serviceNames.append(service.getName());
                }
            }

            String formattedDate = invoice.getCreationDate() != null
                    ? invoice.getCreationDate().format(dateFormatter)
                    : "N/A";

            invoiceData.add(new InvoiceDisplayItem(
                    invoice.getId(),
                    customerName,
                    formattedDate,
                    serviceNames.toString(),
                    invoice.getTotalAmount(),
                    invoice.getStatus()
            ));
        }

        invoiceTable.setItems(invoiceData);
    }

    private void showCreateInvoiceDialog() {
        Dialog<InvoiceCreateRequest> dialog = new Dialog<>();
        dialog.setTitle("Create New Invoice");
        dialog.setHeaderText("Create a new invoice");

        // Set the button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create form fields
        ComboBox<CustomerManager.Customer> customerComboBox = new ComboBox<>();
        customerComboBox.setPromptText("Select Customer");
        customerComboBox.setPrefWidth(300);

        ObservableList<CustomerManager.Customer> customerList =
                FXCollections.observableArrayList(customerManager.getAllCustomers());
        customerComboBox.setItems(customerList);

        customerComboBox.setCellFactory(param -> new ListCell<CustomerManager.Customer>() {
            @Override
            protected void updateItem(CustomerManager.Customer customer, boolean empty) {
                super.updateItem(customer, empty);
                setText(empty || customer == null ? "" : customer.getFullName());
            }
        });

        customerComboBox.setButtonCell(new ListCell<CustomerManager.Customer>() {
            @Override
            protected void updateItem(CustomerManager.Customer customer, boolean empty) {
                super.updateItem(customer, empty);
                setText(empty || customer == null ? "" : customer.getFullName());
            }
        });

        // Service selection list view
        ListView<ServiceCheckItem> serviceListView = new ListView<>();
        serviceListView.setPrefHeight(200);

        List<CustomerManager.Service> services = customerManager.getAllServices();
        ObservableList<ServiceCheckItem> serviceItems = FXCollections.observableArrayList();

        for (CustomerManager.Service service : services) {
            ServiceCheckItem item = new ServiceCheckItem(service.getId(), service.getName(), service.getPrice(), false);
            serviceItems.add(item);
        }

        serviceListView.setItems(serviceItems);
        serviceListView.setCellFactory(param -> new ServiceCheckCell());

        // Add fields to grid
        grid.add(new Label("Customer:"), 0, 0);
        grid.add(customerComboBox, 1, 0);
        grid.add(new Label("Services:"), 0, 1);
        grid.add(serviceListView, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the customer combo box by default
        customerComboBox.requestFocus();

        // Convert the result when the create button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                CustomerManager.Customer selectedCustomer = customerComboBox.getValue();
                if (selectedCustomer == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Customer Required");
                    alert.setContentText("Please select a customer for this invoice.");
                    alert.showAndWait();
                    return null;
                }

                // Get selected services
                List<String> selectedServiceIds = serviceItems.stream()
                        .filter(ServiceCheckItem::isSelected)
                        .map(ServiceCheckItem::getId)
                        .collect(Collectors.toList());

                if (selectedServiceIds.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Service Required");
                    alert.setContentText("Please select at least one service for this invoice.");
                    alert.showAndWait();
                    return null;
                }

                return new InvoiceCreateRequest(selectedCustomer.getId(), selectedServiceIds);
            }
            return null;
        });

        // Process the result
        Optional<InvoiceCreateRequest> result = dialog.showAndWait();
        result.ifPresent(request -> {
            // Create the invoice
            String invoiceId = customerManager.createInvoice(request.customerId, request.serviceIds, null);

            if (invoiceId != null) {
                refreshData();

                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Invoice created successfully!");
                alert.showAndWait();
            }
        });
    }

    private void showInvoiceDetailsDialog(String invoiceId) {
        CustomerManager.Invoice invoice = customerManager.getInvoice(invoiceId);
        if (invoice == null) {
            return;
        }

        CustomerManager.Customer customer = customerManager.getCustomer(invoice.getCustomerId());

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Invoice Details");
        dialog.setHeaderText("Invoice #" + invoiceId);

        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        // Create content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        int row = 0;

        grid.add(new Label("Customer:"), 0, row);
        grid.add(new Label(customer != null ? customer.getFullName() : "Unknown"), 1, row++);

        grid.add(new Label("Status:"), 0, row);
        Label statusLabel = new Label(invoice.getStatus());
        statusLabel.setStyle(getStatusStyle(invoice.getStatus()));
        grid.add(statusLabel, 1, row++);

        grid.add(new Label("Created:"), 0, row);
        grid.add(new Label(formatDateTime(invoice.getCreationDate())), 1, row++);

        if (invoice.getPaymentDate() != null) {
            grid.add(new Label("Paid:"), 0, row);
            grid.add(new Label(formatDateTime(invoice.getPaymentDate())), 1, row++);
        }

        grid.add(new Label("Services:"), 0, row++);

        VBox servicesBox = new VBox(5);
        double total = 0;

        for (String serviceId : invoice.getServiceIds()) {
            CustomerManager.Service service = customerManager.getService(serviceId);
            if (service != null) {
                HBox serviceRow = new HBox(10);
                Label nameLabel = new Label(service.getName());
                nameLabel.setPrefWidth(200);

                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                Label priceLabel = new Label(currencyFormat.format(service.getPrice()));

                serviceRow.getChildren().addAll(nameLabel, priceLabel);
                servicesBox.getChildren().add(serviceRow);

                total += service.getPrice();
            }
        }

        grid.add(servicesBox, 0, row++, 2, 1);

        Separator separator = new Separator();
        grid.add(separator, 0, row++, 2, 1);

        HBox totalRow = new HBox(10);
        Label totalLabel = new Label("Total:");
        totalLabel.setStyle("-fx-font-weight: bold;");
        totalLabel.setPrefWidth(200);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        Label totalAmountLabel = new Label(currencyFormat.format(invoice.getTotalAmount()));
        totalAmountLabel.setStyle("-fx-font-weight: bold;");

        totalRow.getChildren().addAll(totalLabel, totalAmountLabel);
        grid.add(totalRow, 0, row++, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(dateFormatter) : "N/A";
    }

    private String getStatusStyle(String status) {
        switch (status) {
            case "PENDING": return "-fx-text-fill: orange;";
            case "PAID": return "-fx-text-fill: green;";
            case "CANCELLED": return "-fx-text-fill: red;";
            default: return "";
        }
    }

    public void refreshData() {
        loadInvoices();
    }

    // Helper classes for the invoice management
    public static class InvoiceDisplayItem {
        private final String invoiceId;
        private final String customerName;
        private final String creationDate;
        private final String services;
        private final double amount;
        private final String status;

        public InvoiceDisplayItem(String invoiceId, String customerName, String creationDate,
                                  String services, double amount, String status) {
            this.invoiceId = invoiceId;
            this.customerName = customerName;
            this.creationDate = creationDate;
            this.services = services;
            this.amount = amount;
            this.status = status;
        }

        public String getInvoiceId() { return invoiceId; }
        public String getCustomerName() { return customerName; }
        public String getCreationDate() { return creationDate; }
        public String getServices() { return services; }
        public double getAmount() { return amount; }
        public String getStatus() { return status; }
    }

    private static class InvoiceCreateRequest {
        private final String customerId;
        private final List<String> serviceIds;

        public InvoiceCreateRequest(String customerId, List<String> serviceIds) {
            this.customerId = customerId;
            this.serviceIds = serviceIds;
        }
    }

    private static class ServiceCheckItem {
        private final String id;
        private final String name;
        private final double price;
        private boolean selected;

        public ServiceCheckItem(String id, String name, double price, boolean selected) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.selected = selected;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }

        @Override
        public String toString() {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            return name + " - " + currencyFormat.format(price);
        }
    }

    private class ServiceCheckCell extends ListCell<ServiceCheckItem> {
        private final CheckBox checkBox = new CheckBox();
        private final Label priceLabel = new Label();
        private final HBox layout = new HBox(10);

        public ServiceCheckCell() {
            layout.setStyle("-fx-alignment: center-left;");
            HBox.setHgrow(checkBox, Priority.ALWAYS);
            layout.getChildren().addAll(checkBox, priceLabel);

            checkBox.setOnAction(e -> {
                ServiceCheckItem item = getItem();
                if (item != null) {
                    item.setSelected(checkBox.isSelected());
                }
            });
        }

        @Override
        protected void updateItem(ServiceCheckItem item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                checkBox.setText(item.getName());
                checkBox.setSelected(item.isSelected());

                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                priceLabel.setText(currencyFormat.format(item.getPrice()));

                setGraphic(layout);
            }
        }
    }
}