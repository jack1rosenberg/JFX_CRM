package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AutoDetailCRM extends Application {
    private CustomerManager customerManager;
    private Stage primaryStage;

    // Controllers for different sections
    private CustomerViewController customerViewController;
    private AppointmentViewController appointmentViewController;
    private ServiceViewController serviceViewController;
    private InvoiceViewController invoiceViewController;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        customerManager = new CustomerManager();

        // Add sample data for testing
        customerManager.addSampleData();

        // Initialize controllers
        customerViewController = new CustomerViewController(customerManager);
        appointmentViewController = new AppointmentViewController(customerManager);
        serviceViewController = new ServiceViewController(customerManager);
        invoiceViewController = new InvoiceViewController(customerManager);

        // Create the main UI layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(createNavigationMenu());

        // Start with customer view
        mainLayout.setCenter(customerViewController.getView());

        Scene scene = new Scene(mainLayout, 1200, 700);
        primaryStage.setTitle("Auto Detailing CRM");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createNavigationMenu() {
        VBox navigationMenu = new VBox(10);
        navigationMenu.setPadding(new Insets(10));
        navigationMenu.setPrefWidth(150);
        navigationMenu.setStyle("-fx-background-color: #f0f0f0;");

        Button customersBtn = new Button("Customers");
        customersBtn.setPrefWidth(130);
        customersBtn.setOnAction(e -> switchView("customers"));

        Button appointmentsBtn = new Button("Appointments");
        appointmentsBtn.setPrefWidth(130);
        appointmentsBtn.setOnAction(e -> switchView("appointments"));

        Button servicesBtn = new Button("Services");
        servicesBtn.setPrefWidth(130);
        servicesBtn.setOnAction(e -> switchView("services"));

        Button invoicesBtn = new Button("Invoices");
        invoicesBtn.setPrefWidth(130);
        invoicesBtn.setOnAction(e -> switchView("invoices"));

        navigationMenu.getChildren().addAll(
                new Label("Auto Detailing CRM"),
                new Separator(),
                customersBtn,
                appointmentsBtn,
                servicesBtn,
                invoicesBtn
        );

        return navigationMenu;
    }

    private void switchView(String viewName) {
        BorderPane mainLayout = (BorderPane) primaryStage.getScene().getRoot();

        switch (viewName) {
            case "customers":
                mainLayout.setCenter(customerViewController.getView());
                break;
            case "appointments":
                mainLayout.setCenter(appointmentViewController.getView());
                appointmentViewController.refreshData(); // Refresh appointment data
                break;
            case "services":
                mainLayout.setCenter(serviceViewController.getView());
                break;
            case "invoices":
                mainLayout.setCenter(invoiceViewController.getView());
                invoiceViewController.refreshData(); // Refresh invoice data
                break;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}