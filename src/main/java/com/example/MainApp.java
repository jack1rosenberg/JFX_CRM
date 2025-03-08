package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends javafx.application.Application {
    //@Override
    public void start(javafx.stage.Stage primaryStage) {
        javafx.scene.control.Label label = new javafx.scene.control.Label("Hello, JavaFX!");
        javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane(label);
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 400, 300);
        
        primaryStage.setTitle("JavaFX App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        javafx.application.Application.launch(args);
    }
}

