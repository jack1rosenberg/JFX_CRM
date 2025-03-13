package com.example;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.CustomerManager;
import com.example.CustomerManager.Appointment;
import com.example.CustomerManager.Customer;
import com.example.CustomerManager.Invoice;
import com.example.CustomerManager.Service;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class AutoDetailCRM extends Application {

    private CustomerManager manager;
    private static final String APP_TITLE = "Auto Detail Pro CRM";
    // Dark theme colors
    private static final String DARK_BG = "#1E1E1E";
    private static final String DARKER_BG = "#121212";
    private static final String PRIMARY_COLOR = "#3498db";
    private static final String SECONDARY_COLOR = "#2980b9";
    private static final String TEXT_COLOR = "#EEEEEE";
    private static final String CARD_BG = "#252525";

    @Override
    public void start(Stage primaryStage) {
        manager = new CustomerManager();
        manager.addSampleData(); // Add sample data for demonstration

        // Create the main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: " + DARK_BG + ";");

        // Create a header
        HBox header = createHeader();
        mainLayout.setTop(header);

        // Create a tab pane for different sections
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: " + DARK_BG + "; -fx-tab-min-width: 120px;");

        // Create tabs
        Tab dashboardTab = new Tab("Dashboard");
        dashboardTab.setContent(createDashboardView());

        Tab customersTab = new Tab("Customers");
        customersTab.setContent(createCustomersView());

        Tab appointmentsTab = new Tab("Appointments");
        appointmentsTab.setContent(createAppointmentsView());

        Tab servicesTab = new Tab("Services");
        servicesTab.setContent(createServicesView());

        Tab invoicesTab = new Tab("Invoices");
        invoicesTab.setContent(createInvoicesView());

        tabPane.getTabs().addAll(dashboardTab, customersTab, appointmentsTab, servicesTab, invoicesTab);
        mainLayout.setCenter(tabPane);

        // Apply stylesheet
        Scene scene = new Scene(mainLayout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // Set the scene and show the stage
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setSpacing(10);
        header.setStyle("-fx-background-color: " + DARKER_BG + ";");

        Text title = new Text(APP_TITLE);
        title.setFill(Color.web(TEXT_COLOR));
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        MenuButton userMenu = new MenuButton("Admin");
        userMenu.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white;");

        MenuItem profileItem = new MenuItem("Profile Settings");
        MenuItem logoutItem = new MenuItem("Logout");
        userMenu.getItems().addAll(profileItem, logoutItem);

        header.getChildren().addAll(title, spacer, userMenu);

        return header;
    }

    private VBox createDashboardView() {
        VBox dashboard = new VBox();
        dashboard.setSpacing(20);
        dashboard.setPadding(new Insets(20));
        dashboard.setStyle("-fx-background-color: " + DARK_BG + ";");

        // Welcome message
        Text welcomeText = new Text("Welcome to Auto Detail Pro CRM");
        welcomeText.setFill(Color.web(TEXT_COLOR));
        welcomeText.setFont(Font.font("System", FontWeight.BOLD, 20));

        // Stats cards
        HBox statsCards = new HBox();
        statsCards.setSpacing(15);
        statsCards.setAlignment(Pos.CENTER);

        statsCards.getChildren().addAll(
                createStatCard("Customers", Integer.toString(manager.getAllCustomers().size()), "users"),
                createStatCard("Appointments Today", Integer.toString(manager.getAppointmentsByDate(LocalDate.now()).size()), "calendar"),
                createStatCard("Pending Invoices", Integer.toString(manager.getPendingInvoices().size()), "dollar-sign"),
                createStatCard("Services", Integer.toString(manager.getAllServices().size()), "tools")
        );

        // Charts and upcoming appointments
        HBox chartsRow = new HBox();
        chartsRow.setSpacing(15);

        // Service popularity pie chart
        VBox chartBox = new VBox();
        chartBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 5;");
        chartBox.setPadding(new Insets(15));
        chartBox.setSpacing(10);

        Label chartTitle = new Label("Service Popularity");
        chartTitle.setTextFill(Color.web(TEXT_COLOR));
        chartTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        PieChart pieChart = new PieChart();
        pieChart.setTitle("");
        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(true);
        pieChart.setStyle("-fx-pie-label-visible: true; -fx-pie-label-text-fill: white;");

        // Count services in appointments
        PieChart.Data slice1 = new PieChart.Data("Basic Wash", 35);
        PieChart.Data slice2 = new PieChart.Data("Premium Wash", 25);
        PieChart.Data slice3 = new PieChart.Data("Full Detail", 20);
        PieChart.Data slice4 = new PieChart.Data("Clay & Polish", 15);
        PieChart.Data slice5 = new PieChart.Data("Ceramic Coating", 5);

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                slice1, slice2, slice3, slice4, slice5
        );
        pieChart.setData(pieChartData);

        chartBox.getChildren().addAll(chartTitle, pieChart);

        // Upcoming appointments
        VBox upcomingBox = new VBox();
        upcomingBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 5;");
        upcomingBox.setPadding(new Insets(15));
        upcomingBox.setSpacing(10);

        Label upcomingTitle = new Label("Upcoming Appointments");
        upcomingTitle.setTextFill(Color.web(TEXT_COLOR));
        upcomingTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        ListView<Appointment> appointmentsList = new ListView<>();
        appointmentsList.setStyle("-fx-background-color: " + CARD_BG + "; -fx-control-inner-background: " + CARD_BG + "; -fx-text-fill: " + TEXT_COLOR + ";");

        // Get upcoming appointments (next 5 days)
        List<Appointment> upcomingAppointments = manager.getAllAppointments()
                .stream()
                .filter(a -> a.getDateTime().isAfter(LocalDateTime.now()) &&
                        a.getDateTime().isBefore(LocalDateTime.now().plusDays(5)))
                .sorted((a1, a2) -> a1.getDateTime().compareTo(a2.getDateTime()))
                .collect(Collectors.toList());

        ObservableList<Appointment> appointmentsData = FXCollections.observableArrayList(upcomingAppointments);
        appointmentsList.setItems(appointmentsData);

        // Custom cell factory for appointments
        appointmentsList.setCellFactory(param -> new ListCell<Appointment>() {
            @Override
            protected void updateItem(Appointment appointment, boolean empty) {
                super.updateItem(appointment, empty);

                if (empty || appointment == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Customer customer = manager.getCustomer(appointment.getCustomerId());
                    String customerName = customer != null ? customer.getFullName() : "Unknown Customer";

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm");
                    String dateTimeStr = appointment.getDateTime().format(formatter);

                    String serviceNames = appointment.getServiceIds().stream()
                            .map(id -> manager.getService(id))
                            .filter(s -> s != null)
                            .map(Service::getName)
                            .collect(Collectors.joining(", "));

                    VBox vbox = new VBox();
                    vbox.setSpacing(5);

                    Label nameLabel = new Label(customerName);
                    nameLabel.setTextFill(Color.web(TEXT_COLOR));
                    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

                    Label dateLabel = new Label(dateTimeStr);
                    dateLabel.setTextFill(Color.web(TEXT_COLOR));

                    Label serviceLabel = new Label(serviceNames);
                    serviceLabel.setTextFill(Color.web(PRIMARY_COLOR));

                    vbox.getChildren().addAll(nameLabel, dateLabel, serviceLabel);
                    setGraphic(vbox);

                    setStyle("-fx-background-color: " + CARD_BG + ";");
                }
            }
        });

        Button addAppointmentBtn = new Button("+ New Appointment");
        addAppointmentBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white;");
        addAppointmentBtn.setOnAction(e -> showAppointmentDialog(null));

        upcomingBox.getChildren().addAll(upcomingTitle, appointmentsList, addAppointmentBtn);

        chartsRow.getChildren().addAll(chartBox, upcomingBox);
        HBox.setHgrow(chartBox, Priority.ALWAYS);
        HBox.setHgrow(upcomingBox, Priority.ALWAYS);

        dashboard.getChildren().addAll(welcomeText, statsCards, chartsRow);

        return dashboard;
    }

    private VBox createStatCard(String title, String value, String iconName) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(25, 40, 25, 40));
        card.setSpacing(10);
        card.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 5;");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.web(TEXT_COLOR));

        Label valueLabel = new Label(value);
        valueLabel.setTextFill(Color.web(PRIMARY_COLOR));
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 28));

        card.getChildren().addAll(titleLabel, valueLabel);

        return card;
    }

    private VBox createCustomersView() {
        VBox customersView = new VBox();
        customersView.setSpacing(15);
        customersView.setPadding(new Insets(20));
        customersView.setStyle("-fx-background-color: " + DARK_BG + ";");

        // Header section with title and add button
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(15);

        Text title = new Text("Customers");
        title.setFill(Color.web(TEXT_COLOR));
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        TextField searchField = new TextField();
        searchField.setPromptText("Search customers...");
        searchField.setStyle("-fx-background-color: " + CARD_BG + "; -fx-text-fill: " + TEXT_COLOR + "; -fx-prompt-text-fill: #888888;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addCustomerBtn = new Button("+ Add Customer");
        addCustomerBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white;");
        addCustomerBtn.setOnAction(e -> showCustomerDialog(null));

        header.getChildren().addAll(title, searchField, spacer, addCustomerBtn);

        // Customers table
        TableView<Customer> customersTable = new TableView<>();
        customersTable.setStyle("-fx-background-color: " + CARD_BG + "; -fx-control-inner-background: " + CARD_BG + ";");

        // Create table columns
        TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(200);

        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(150);

        TableColumn<Customer, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(data -> {
            Customer customer = data.getValue();
            String vehicle = customer.getVehicleYear() + " " + customer.getVehicleMake() + " " + customer.getVehicleModel();
            return javafx.beans.property.SimpleStringProperty.valueOf(vehicle);
        });
        vehicleCol.setPrefWidth(200);

        TableColumn<Customer, String> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(param -> new TableCell<Customer, String>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");

            {
                viewBtn.setStyle("-fx-background-color: " + SECONDARY_COLOR + "; -fx-text-fill: white;");
                editBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white;");

                viewBtn.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    showCustomerDetailsDialog(customer);
                });

                editBtn.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    showCustomerDialog(customer);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.getChildren().addAll(viewBtn, editBtn);
                    setGraphic(buttons);
                }
            }
        });

        customersTable.getColumns().addAll(nameCol, emailCol, phoneCol, vehicleCol, actionCol);

        // Load customer data
        ObservableList<Customer> customersData = FXCollections.observableArrayList(manager.getAllCustomers());
        customersTable.setItems(customersData);

        customersView.getChildren().addAll(header, customersTable);
        VBox.setVgrow(customersTable, Priority.ALWAYS);

        // Implement search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                customersTable.setItems(FXCollections.observableArrayList(manager.getAllCustomers()));
            } else {
                customersTable.setItems(FXCollections.observableArrayList(manager.searchCustomers(newValue)));
            }
        });

        return customersView;
    }

    private VBox createAppointmentsView() {
        VBox appointmentsView = new VBox();
        appointmentsView.setSpacing(15);
        appointmentsView.setPadding(new Insets(20));
        appointmentsView.setStyle("-fx-background-color: " + DARK_BG + ";");

        // Header section with title and add button
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(15);

        Text title = new Text("Appointments");
        title.setFill(Color.web(TEXT_COLOR));
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle("-fx-background-color: " + CARD_BG + "; -fx-text-fill: " + TEXT_COLOR + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addAppointmentBtn = new Button("+ Add Appointment");
        addAppointmentBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white;");
        addAppointmentBtn.setOnAction(e -> showAppointmentDialog(null));

        header.getChildren().addAll(title, datePicker, spacer, addAppointmentBtn);

        // Appointments table
        TableView<Appointment> appointmentsTable = new TableView<>();
        appointmentsTable.setStyle("-fx-background-color: " + CARD_BG + "; -fx-control-inner-background: " + CARD_BG + ";");

        // Create table columns
        TableColumn<Appointment, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(data -> {
            Customer customer = manager.getCustomer(data.getValue().getCustomerId());
            String customerName = customer != null ? customer.getFullName() : "Unknown";
            return javafx.beans.property.SimpleStringProperty.valueOf(customerName);
        });
        customerCol.setPrefWidth(200);

        TableColumn<Appointment, String> dateCol = new TableColumn<>("Date & Time");
        dateCol.setCellValueFactory(data -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm");
            return javafx.beans.property.SimpleStringProperty.valueOf(data.getValue().getDateTime().format(formatter));
        });
        dateCol.setPrefWidth(200);

        TableColumn<Appointment, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        locationCol.setPrefWidth(200);

        TableColumn<Appointment, String> servicesCol = new TableColumn<>("Services");
        servicesCol.setCellValueFactory(data -> {
            List<String> serviceIds = data.getValue().getServiceIds();
            String serviceNames = serviceIds.stream()
                    .map(id -> manager.getService(id))
                    .filter(s -> s != null)
                    .map(Service::getName)
                    .collect(Collectors.joining(", "));
            return javafx.beans.property.SimpleStringProperty.valueOf(serviceNames);
        });
        servicesCol.setPrefWidth(250);

        TableColumn<Appointment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        TableColumn<Appointment, String> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(param -> new TableCell<Appointment, String>() {
            private final Button editBtn = new Button("Edit");
            private final Button completeBtn = new Button("Complete");

            {
                editBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white;");
                completeBtn.setStyle("-fx-background-color: " + SECONDARY_COLOR + "; -fx-text-fill: white;");

                editBtn.setOnAction(event -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    showAppointmentDialog(appointment);
                });

                completeBtn.setOnAction(event -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    appointment.setStatus("COMPLETED");
                    manager.updateAppointment(appointment);

                    // Create invoice
                    manager.createInvoice(appointment.getCustomerId(), appointment.getServiceIds(), appointment.getId());

                    // Refresh table
                    appointmentsTable.refresh();
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    HBox buttons = new HBox(5);

                    if ("COMPLETED".equals(appointment.getStatus())) {
                        Button invoiceBtn = new Button("Invoice");
                        invoiceBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                        buttons.getChildren().add(invoiceBtn);
                    } else {
                        buttons.getChildren().addAll(editBtn, completeBtn);
                    }

                    setGraphic(buttons);
                }
            }
        });

        appointmentsTable.getColumns().addAll(customerCol, dateCol, locationCol, servicesCol, statusCol, actionCol);

        // Load appointment data for the selected date
        ObservableList<Appointment> appointmentsData =
                FXCollections.observableArrayList(manager.getAppointmentsByDate(datePicker.getValue()));
        appointmentsTable.setItems(appointmentsData);

        // Update table when date is changed
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            appointmentsTable.setItems(FXCollections.observableArrayList(manager.getAppointmentsByDate(newValue)));
        });

        appointmentsView.getChildren().addAll(header, appointmentsTable);
        VBox.setVgrow(appointmentsTable, Priority.ALWAYS);

        return appointmentsView;
    }

    private VBox createServicesView() {
        VBox servicesView = new VBox();
        servicesView.setSpacing(15);
        servicesView.setPadding(new Insets(20));
        servicesView.setStyle("-fx-background-color: " + DARK_BG + ";");

        // Header section with title and add button
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(15);

        Text title = new Text("Services");
        title.setFill(Color.web(TEXT_COLOR));
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addServiceBtn = new Button("+ Add Service");
        addServiceBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white;");
        addServiceBtn.setOnAction(e -> showServiceDialog(null));

        header.getChildren().addAll(title, spacer, addServiceBtn);

        // Services table
        TableView<Service> servicesTable = new TableView<>();
        servicesTable.setStyle("-fx-background-color: " + CARD_BG + "; -fx-control-inner-background: " + CARD_BG + ";");

        // Create table columns
        TableColumn<Service, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeCol.setPrefWidth(150);

        TableColumn<Service, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<Service, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionCol.setPrefWidth(300);

        TableColumn<Service, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(150);
        priceCol.setCellFactory(col -> new TableCell<Service, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price));
                }
            }
        });

        TableColumn<Service, String> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(param -> new TableCell<Service, String>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                editBtn.setOnAction(event -> {
                    Service service = getTableView().getItems().get(getIndex());
                    showServiceDialog(service);
                });

                deleteBtn.setOnAction(event -> {
                    Service service = getTableView().getItems().get(getIndex());

                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle("Delete Service");
                    alert.setHeaderText("Delete " + service.getName());
                    alert.setContentText("Are you sure you want to delete this service?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        manager.deleteService(service.getId());
                        servicesTable.getItems().remove(service);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.getChildren().addAll(editBtn, deleteBtn);
                    setGraphic(buttons);
                }
            }
        });

        servicesTable.getColumns().addAll(codeCol, nameCol, descriptionCol, priceCol, actionCol);

        // Load service data
        ObservableList<Service> servicesData = FXCollections.observableArrayList(manager.getAllServices());
        servicesTable.setItems(servicesData);

        servicesView.getChildren().addAll(header, servicesTable);
        VBox.setVgrow(servicesTable, Priority.ALWAYS);

        return servicesView;
    }

    private VBox createInvoicesView() {
        VBox invoicesView = new VBox();
        invoicesView.setSpacing(15);
        invoicesView.setPadding(new Insets(20));
        invoicesView.setStyle("-fx-background-color: " + DARK_BG + ";");

        // Header section with title and filters
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(15);

        Text title = new Text("Invoices");
        title.setFill(Color.web(TEXT_COLOR));
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "Pending", "Paid", "Cancelled");
        statusFilter.setValue("All");
        statusFilter.setStyle("-fx-background-color: " + CARD_BG + "; -fx-text-fill: " + TEXT_COLOR + ";");

        DatePicker startDate = new DatePicker(LocalDate.now().minusMonths(1));
        startDate.setPromptText("Start Date");
        startDate.setStyle("-fx-background-color: " + CARD_BG + "; -fx-text-fill: " + TEXT_COLOR + ";");

        DatePicker endDate = new DatePicker(LocalDate.now());
        endDate.setPromptText("End Date");
        endDate.setStyle("-fx-background-color: " + CARD_BG + "; -fx-text-fill: " + TEXT_COLOR + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button createInvoiceBtn = new Button("+ Create Invoice");
        createInvoiceBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white;");
        createInvoiceBtn.setOnAction(e -> showCreateInvoiceDialog());

        header.getChildren().addAll(title, statusFilter, startDate, endDate, spacer, createInvoiceBtn);

        // Invoices table
        TableView<Invoice> invoicesTable = new TableView<>();
        invoicesTable.setStyle("-fx-background-color: " + CARD_BG + "; -fx-control-inner-background: " + CARD_BG + ";");

        // Create table columns
        TableColumn<Invoice, String> invoiceNumberCol = new TableColumn<>("Invoice #");
        invoiceNumberCol.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        invoiceNumberCol.setPrefWidth(100);

        TableColumn<Invoice, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(data -> {
            Customer customer = manager.getCustomer(data.getValue().getCustomerId());
            String customerName = customer != null ? customer.getFullName() : "Unknown";
            return javafx.beans.property.SimpleStringProperty.valueOf(customerName);
        });
        customerCol.setPrefWidth(200);

        TableColumn<Invoice, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        dateCol.setPrefWidth(120);
        dateCol.setCellFactory(col -> new TableCell<Invoice, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
                }
            }
        });

        TableColumn<Invoice, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        amountCol.setPrefWidth(120);
        amountCol.setCellFactory(col -> new TableCell<Invoice, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });

        TableColumn<Invoice, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<Invoice, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(status);
                    statusLabel.setPadding(new Insets(2, 8, 2, 8));
                    statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 3;");

                    switch (status) {
                        case "PAID":
                            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #2ecc71;");
                            break;
                        case "PENDING":
                            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #f39c12;");
                            break;
                        case "CANCELLED":
                            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #e74c3c;");
                            break;
                        default:
                            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #7f8c8d;");
                            break;
                    }

                    setGraphic(statusLabel);
                }
            }
        });

        TableColumn<Invoice, String> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(200);
        actionCol.setCellFactory(param -> new TableCell<Invoice, String>() {
            private final Button viewBtn = new Button("View");
            private final Button payBtn = new Button("Mark Paid");
            private final Button printBtn = new Button("Print");

            {
                viewBtn.setStyle("-fx-background-color: " + SECONDARY_COLOR + "; -fx-text-fill: white;");
                payBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                printBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");

                viewBtn.setOnAction(event -> {
                    Invoice invoice = getTableView().getItems().get(getIndex());
                    showInvoiceDetailsDialog(invoice);
                });

                payBtn.setOnAction(event -> {
                    Invoice invoice = getTableView().getItems().get(getIndex());
                    invoice.setStatus("PAID");
                    invoice.setPaymentDate(LocalDate.now());
                    manager.updateInvoice(invoice);
                    invoicesTable.refresh();
                });

                printBtn.setOnAction(event -> {
                    Invoice invoice = getTableView().getItems().get(getIndex());
                    printInvoice(invoice);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Invoice invoice = getTableView().getItems().get(getIndex());
                    HBox buttons = new HBox(5);

                    buttons.getChildren().add(viewBtn);

                    if ("PENDING".equals(invoice.getStatus())) {
                        buttons.getChildren().add(payBtn);
                    }

                    buttons.getChildren().add(printBtn);
                    setGraphic(buttons);
                }
            }
        });

        invoicesTable.getColumns().addAll(invoiceNumberCol, customerCol, dateCol, amountCol, statusCol, actionCol);

        // Load invoice data with default filters
        ObservableList<Invoice> invoicesData = FXCollections.observableArrayList(manager.getAllInvoices());
        invoicesTable.setItems(invoicesData);

        // Apply filters when changed
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyInvoiceFilters(invoicesTable, statusFilter.getValue(), startDate.getValue(), endDate.getValue());
        });

        startDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyInvoiceFilters(invoicesTable, statusFilter.getValue(), startDate.getValue(), endDate.getValue());
        });

        endDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyInvoiceFilters(invoicesTable, statusFilter.getValue(), startDate.getValue(), endDate.getValue());
        });

        invoicesView.getChildren().addAll(header, invoicesTable);
        VBox.setVgrow(invoicesTable, Priority.ALWAYS);

        return invoicesView;
    }

    private void applyInvoiceFilters(TableView<Invoice> table, String status, LocalDate startDate, LocalDate endDate) {
        List<Invoice> allInvoices = manager.getAllInvoices();
        List<Invoice> filteredInvoices = allInvoices.stream()
                .filter(invoice -> {
                    // Apply status filter
                    if (!"All".equals(status)) {
                        if (!status.equalsIgnoreCase(invoice.getStatus())) {
                            return false;
                        }
                    }

                    // Apply date filters
                    if (startDate != null && endDate != null) {
                        return !invoice.getCreationDate().isBefore(startDate) &&
                                !invoice.getCreationDate().isAfter(endDate);
                    }

                    return true;
                })
                .collect(Collectors.toList());

        table.setItems(FXCollections.observableArrayList(filteredInvoices));
    }

    private void showCustomerDialog(Customer customer) {
        // Create the custom dialog
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle(customer == null ? "Add Customer" : "Edit Customer");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: " + DARK_BG + ";");

        // Create the grid pane
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.setStyle("-fx-background-color: " + DARK_BG + ";");

        // Create text fields
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        firstNameField.setStyle("-fx-background-color: " + CARD_BG + "; -fx-text-fill: " + TEXT_COLOR + ";");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        lastNameField.setStyle("-fx-background-color: " + CARD_BG + "; -fx-text-fill: " + TEXT_COLOR + ";");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setStyle("-fx-background-color: " + CARD_BG + "; -fx-text-fill: " + TEXT_COLOR + ";");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");
        phoneField.setStyle("-fx-background-color: " + CARD_BG + "; -fx-text-fill: " + TEXT_COLOR + ";");

        TextField addressField = new TextField();
        addressField.setPromptText("Address");
        addressField.setStyle("-fx-background-color: " + CARD_BG + "; -fx-text-fill: " + TEXT_COLOR + ";");

        TextField vehicleYearField = new TextField();
        vehicleYearField.setPromptText("Vehicle Year");
        vehicleYearField.setStyle("-fx-background-color: " + CARD_BG + "; -fx-text-fill: " + TEXT_COLOR + ";");

        TextField vehicleMakeField = new TextField();
        vehicleMakeField.setPromptText("Vehicle Make");
        vehicleMakeField.setStyle("-fx-background-color: " + CARD_BG + "; -fx-text-fill: " + TEXT_COLOR + ";");

        TextField vehicleModelField = new TextField();
        vehicleModelField.setPromptText("Vehicle Model");
        vehicleModelField.setStyle("-fx-background-color: " + CARD_BG + "; -fx-text-fill: " + TEXT_COLOR + ";");

        TextField vehicleColorField = new TextField();
        vehicleColorField.setPromptText("Vehicle Color");
        vehicleColorField.setStyle("-fx-background-color: " + CARD_BG + "; -fx-text-fill: " + TEXT_COLOR + ";");

        // Add labels and fields to the grid
        addLabelAndField(grid, "First Name:", firstNameField, 0);
        addLabelAndField(grid, "Last Name:", lastNameField, 1);
        addLabelAndField(grid, "Email:", emailField, 2);
        addLabelAndField(grid, "Phone:", phoneField, 3);
        addLabelAndField(grid, "Address:", addressField, 4);
        addLabelAndField(grid, "Vehicle Year:", vehicleYearField, 5);
        addLabelAndField(grid, "Vehicle Make:", vehicleMakeField, 6);
        addLabelAndField(grid, "Vehicle Model:", vehicleModelField, 7);
        addLabelAndField(grid, "Vehicle Color:", vehicleColorField, 8);

        // Pre-fill if editing
        if (customer != null) {
            firstNameField.setText(customer.getFirstName());
            lastNameField.setText(customer.getLastName());
            emailField.setText(customer.getEmail());
            phoneField.setText(customer.getPhone());
            addressField.setText(customer.getAddress());
            vehicleYearField.setText(customer.getVehicleYear());
            vehicleMakeField.setText(customer.getVehicleMake());
            vehicleModelField.setText(customer.getVehicleModel());
            vehicleColorField.setText(customer.getVehicleColor());
        }

        dialog.getDialogPane().setContent(grid);

        // Request focus on the first field
        Platform.runLater(() -> firstNameField.requestFocus());

        // Convert the result to customer when the OK button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                if (customer == null) {
                    return new Customer(
                            UUID.randomUUID().toString(),
                            firstNameField.getText(),
                            lastNameField.getText(),
                            emailField.getText(),
                            phoneField.getText(),
                            addressField.getText(),
                            vehicleYearField.getText(),
                            vehicleMakeField.getText(),
                            vehicleModelField.getText(),
                            vehicleColorField.getText()
                    );
                } else {
                    customer.setFirstName(firstNameField.getText());
                    customer.setLastName(lastNameField.getText());
                    customer.setEmail(emailField.getText());
                    customer.setPhone(phoneField.getText());
                    customer.setAddress(addressField.getText());
                    customer.setVehicleYear(vehicleYearField.getText());
                    customer.setVehicleMake(vehicleMakeField.getText());
                    customer.setVehicleModel(vehicleModelField.getText());
                    customer.setVehicleColor(vehicleColorField.getText());
                    return customer;
                }
            }
            return null;
        });

        Optional<Customer> result = dialog.showAndWait();

        result.ifPresent(newCustomer -> {
            if (customer == null) {
                manager.addCustomer(newCustomer);
            } else {
                manager.updateCustomer(newCustomer);
            }

            // Refresh the view
            TabPane tabPane = (TabPane) ((BorderPane) dialog.getDialogPane().getScene().getRoot()).getCenter();
            VBox customersView = (VBox) tabPane.getTabs().get(1).getContent();
            TableView<Customer> customersTable = (TableView<Customer>) customersView.getChildren().get(1);
            customersTable.setItems(FXCollections.observableArrayList(manager.getAllCustomers()));
        });
    }

    private void showCustomerDetailsDialog(Customer customer) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Customer Details");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle("-fx-background-color: " + DARK_BG + ";");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: " + DARK_BG + ";");

        // Customer info section
        Text nameText = new Text(customer.getFullName());
        nameText.setFill(Color.web(TEXT_COLOR));
        nameText.setFont(Font.font("System", FontWeight.BOLD, 18));

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(15);
        detailsGrid.setVgap(8);
        detailsGrid.setPadding(new Insets(10, 0, 20, 0));

        addLabelAndText(detailsGrid, "Email:", customer.getEmail(), 0);
        addLabelAndText(detailsGrid, "Phone:", customer.getPhone(), 1);
        addLabelAndText(detailsGrid, "Address:", customer.getAddress(), 2);
        addLabelAndText(detailsGrid, "Vehicle:",
                customer.getVehicleYear() + " " + customer.getVehicleMake() + " " +
                        customer.getVehicleModel() + " (" + customer.getVehicleColor() + ")", 3);

        // Appointment history section
        Text appointmentsTitle = new Text("Appointment History");
        appointmentsTitle.setFill(Color.web(TEXT_COLOR));
        appointmentsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        ListView<Appointment> appointmentsList = new ListView<>();
        appointmentsList.setStyle("-fx-background-color: " + CARD_BG + "; -fx-control-inner-background: " + CARD_BG + ";");
        appointmentsList.setPrefHeight(150);

        // Get customer appointments
        List<Appointment> customerAppointments = manager.getAppointmentsByCustomer(customer.getId());

        appointmentsList.setItems(FXCollections.observableArrayList(customerAppointments));
        appointmentsList.setCellFactory(param -> new ListCell<Appointment>() {
            @Override
            protected void updateItem(Appointment appointment, boolean empty) {
                super.updateItem(appointment, empty);

                if (empty || appointment == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm");
                    String dateTime = appointment.getDateTime().format(formatter);

                    String services = appointment.getServiceIds().stream()
                            .map(id -> manager.getService(id))
                            .filter(s -> s != null)
                            .map(Service::getName)
                            .collect(Collectors.joining(", "));

                    setText(dateTime + " - " + services + " (" + appointment.getStatus() + ")");
                    setTextFill(Color.web(TEXT_COLOR));
                }
            }
        });

        // Invoice history section
        Text invoicesTitle = new Text("Invoice History");
        invoicesTitle.setFill(Color.web(TEXT_COLOR));
        invoicesTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        ListView<Invoice> invoicesList = new ListView<>();
        invoicesList.setStyle("-fx-background-color: " + CARD_BG + "; -fx-control-inner-background: " + CARD_BG + ";");
        invoicesList.setPrefHeight(150);

        // Get customer invoices
        List<Invoice> customerInvoices = manager.getInvoicesByCustomer(customer.getId());

        invoicesList.setItems(FXCollections.observableArrayList(customerInvoices));
        invoicesList.setCellFactory(param -> new ListCell<Invoice>() {
            @Override
            protected void updateItem(Invoice invoice, boolean empty) {
                super.updateItem(invoice, empty);

                if (empty || invoice == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                    String date = invoice.getCreationDate().format(formatter);
                    String paid = invoice.getPaymentDate() != null ?
                            " (Paid on " + invoice.getPaymentDate().format(formatter) + ")" : "";

                    setText("Invoice #" + invoice.getInvoiceNumber() + " - " + date +
                            " - $" + String.format("%.2f", invoice.getTotalAmount()) +
                            " - " + invoice.getStatus() + paid);
                    setTextFill(Color.web(TEXT_COLOR));
                }
            }
        });

        // Notes section
        Text notesTitle = new Text("Notes");
        notesTitle.setFill(Color.web(TEXT_COLOR));
        notesTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        TextArea notesArea = new TextArea(customer.getNotes());
        notesArea.setStyle("-fx-background-color: " + CARD_BG + "; -fx-text-fill: " + TEXT_COLOR + ";");
        notesArea.setPrefHeight(100);

        Button saveNotesBtn = new Button("Save Notes");
        saveNotesBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white;");
        saveNotesBtn.setOnAction(e -> {
            customer.setNotes(notesArea.getText());
            manager.updateCustomer(customer);

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Notes Saved");
            alert.setHeaderText(null);
            alert.setContentText("Customer notes have been saved successfully.");
            alert.showAndWait();
        });

        content.getChildren().addAll(
                nameText, detailsGrid,
                appointmentsTitle, appointmentsList,
                invoicesTitle, invoicesList,
                notesTitle, notesArea, saveNotesBtn
        );

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void showAppointmentDialog(Appointment appointment) {
        Dialog<Appointment> dialog = new Dialog<>();
        dialog.setTitle(appointment == null ? "Add Appointment" : "Edit Appointment");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: " + DARK_BG + ";");

        // Fetch customer list
        ComboBox<Customer> customerCombo = new ComboBox<>(FXCollections.observableArrayList(manager.getAllCustomers()));
        customerCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Customer customer) {
                return customer != null ? customer.getFullName() : "";
            }

            @Override
            public Customer fromString(String string) {
                return null;
            }
        });

        // Fetch service list
        ListView<Service> servicesListView = new ListView<>(FXCollections.observableArrayList(manager.getAllServices()));
        servicesListView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);

        // Date picker & time
        DatePicker datePicker = new DatePicker(LocalDate.now());
        ComboBox<String> timeCombo = new ComboBox<>(FXCollections.observableArrayList("09:00", "10:00", "11:00", "12:00"));
        timeCombo.setValue("09:00");

        // Status
        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("SCHEDULED", "CONFIRMED", "COMPLETED"));
        statusCombo.setValue("SCHEDULED");

        // Notes field
        TextArea notesArea = new TextArea();

        // Pre-fill if editing
        if (appointment != null) {
            customerCombo.setValue(manager.getCustomer(appointment.getCustomerId()));
            datePicker.setValue(appointment.getDateTime().toLocalDate());
            timeCombo.setValue(appointment.getDateTime().toLocalTime().toString());
            statusCombo.setValue(appointment.getStatus());
            notesArea.setText(appointment.getNotes());
        }

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Customer:"), 0, 0);
        grid.add(customerCombo, 1, 0);
        grid.add(new Label("Date:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Time:"), 0, 2);
        grid.add(timeCombo, 1, 2);
        grid.add(new Label("Services:"), 0, 3);
        grid.add(servicesListView, 1, 3);
        grid.add(new Label("Status:"), 0, 4);
        grid.add(statusCombo, 1, 4);
        grid.add(new Label("Notes:"), 0, 5);
        grid.add(notesArea, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                Customer selectedCustomer = customerCombo.getValue();
                LocalDate date = datePicker.getValue();
                LocalTime time = LocalTime.parse(timeCombo.getValue());
                List<String> serviceIds = servicesListView.getSelectionModel().getSelectedItems().stream().map(Service::getId).collect(Collectors.toList());
                if (selectedCustomer != null && date != null && !serviceIds.isEmpty()) {
                    LocalDateTime dateTime = LocalDateTime.of(date, time);
                    if (appointment == null) {
                        return new Appointment(selectedCustomer.getId(), dateTime, serviceIds, statusCombo.getValue(), notesArea.getText());
                    } else {
                        appointment.setCustomerId(selectedCustomer.getId());
                        appointment.setDateTime(dateTime);
                        appointment.setServiceIds(serviceIds);
                        appointment.setStatus(statusCombo.getValue());
                        appointment.setNotes(notesArea.getText());
                        return appointment;
                    }
                }
            }
            return null;
        });

        Optional<Appointment> result = dialog.showAndWait();
        result.ifPresent(newAppointment -> {
            if (appointment == null) {
                manager.addAppointment(newAppointment);
            } else {
                manager.updateAppointment(newAppointment);
            }
        });
    }
}
