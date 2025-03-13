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
import javafx.util.StringConverter;

import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;

public class ServiceViewController {
    private CustomerManager customerManager;
    private BorderPane view;
    private TableView<CustomerManager.Service> serviceTable;
    private ObservableList<CustomerManager.Service> serviceData;

    public ServiceViewController(CustomerManager customerManager) {
        this.customerManager = customerManager;
        createView();
        loadServices();
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

        Label titleLabel = new Label("Services Management");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Button addServiceBtn = new Button("Add New Service");
        addServiceBtn.setOnAction(e -> showServiceDialog(null));

        topSection.getChildren().addAll(titleLabel, addServiceBtn);

        // Create table for services
        serviceTable = new TableView<>();
        serviceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<CustomerManager.Service, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));

        TableColumn<CustomerManager.Service, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<CustomerManager.Service, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<CustomerManager.Service, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setCellFactory(column -> new TableCell<CustomerManager.Service, Double>() {
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

        TableColumn<CustomerManager.Service, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(120);
        actionsCol.setCellFactory(param -> new TableCell<CustomerManager.Service, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setOnAction(event -> {
                    CustomerManager.Service service = getTableView().getItems().get(getIndex());
                    showServiceDialog(service);
                });

                deleteBtn.setOnAction(event -> {
                    CustomerManager.Service service = getTableView().getItems().get(getIndex());
                    confirmAndDeleteService(service);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        serviceTable.getColumns().addAll(codeCol, nameCol, descriptionCol, priceCol, actionsCol);

        // Create search box
        HBox searchBox = new HBox(10);
        searchBox.setPadding(new Insets(10, 0, 10, 0));

        TextField searchField = new TextField();
        searchField.setPromptText("Search services...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterServices(newValue);
        });

        searchBox.getChildren().add(searchField);

        // Add components to view
        VBox centerContent = new VBox(10);
        centerContent.getChildren().addAll(searchBox, serviceTable);
        VBox.setVgrow(serviceTable, Priority.ALWAYS);

        view.setTop(topSection);
        view.setCenter(centerContent);
    }

    private void loadServices() {
        List<CustomerManager.Service> services = customerManager.getAllServices();
        serviceData = FXCollections.observableArrayList(services);
        serviceTable.setItems(serviceData);
    }

    private void filterServices(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            loadServices();
            return;
        }

        String lowerCaseSearch = searchText.toLowerCase();
        ObservableList<CustomerManager.Service> filteredData = FXCollections.observableArrayList();

        for (CustomerManager.Service service : customerManager.getAllServices()) {
            if (service.getCode().toLowerCase().contains(lowerCaseSearch) ||
                    service.getName().toLowerCase().contains(lowerCaseSearch) ||
                    service.getDescription().toLowerCase().contains(lowerCaseSearch)) {
                filteredData.add(service);
            }
        }

        serviceTable.setItems(filteredData);
    }

    private void showServiceDialog(CustomerManager.Service service) {
        boolean isNewService = (service == null);
        Dialog<CustomerManager.Service> dialog = new Dialog<>();
        dialog.setTitle(isNewService ? "Add New Service" : "Edit Service");
        dialog.setHeaderText(isNewService ? "Create a new service" : "Edit service details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create form fields
        TextField codeField = new TextField();
        codeField.setPromptText("Code");

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        TextField priceField = new TextField();
        priceField.setPromptText("Price");

        // Add fields to grid
        grid.add(new Label("Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionField, 1, 2);
        grid.add(new Label("Price:"), 0, 3);
        grid.add(priceField, 1, 3);

        // Fill with existing data if editing
        if (!isNewService) {
            codeField.setText(service.getCode());
            nameField.setText(service.getName());
            descriptionField.setText(service.getDescription());
            priceField.setText(String.valueOf(service.getPrice()));
        }

        dialog.getDialogPane().setContent(grid);

        // Request focus on the code field by default
        codeField.requestFocus();

        // Convert the result to a Service when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    CustomerManager.Service result = isNewService ? new CustomerManager.Service() : service;
                    result.setCode(codeField.getText());
                    result.setName(nameField.getText());
                    result.setDescription(descriptionField.getText());
                    result.setPrice(Double.parseDouble(priceField.getText()));
                    return result;
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Input");
                    alert.setHeaderText("Please enter a valid price");
                    alert.setContentText("The price must be a number.");
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });

        // Process the result
        Optional<CustomerManager.Service> result = dialog.showAndWait();
        result.ifPresent(serviceResult -> {
            if (isNewService) {
                customerManager.addService(serviceResult);
            } else {
                customerManager.updateService(serviceResult);
            }
            loadServices();
        });
    }

    private void confirmAndDeleteService(CustomerManager.Service service) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete Service");
        confirmDialog.setContentText("Are you sure you want to delete the service: " + service.getName() + "?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            customerManager.deleteService(service.getId());
            loadServices();
        }
    }

    // Method to refresh data if needed
    public void refreshData() {
        loadServices();
    }
}