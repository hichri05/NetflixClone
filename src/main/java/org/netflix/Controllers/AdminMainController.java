package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class AdminMainController {
    @FXML private StackPane contentArea;
    @FXML private Button btnCatalogue, btnCategories, btnUsers;

    public void showCatalogue(ActionEvent actionEvent) {
        contentArea.getChildren().clear();
        try {

            Node node = FXMLLoader.load(getClass().getResource("/org/Views/CatalogueManagement.fxml"));

            contentArea.getChildren().add(node);
            btnCatalogue.setStyle("-fx-text-fill: white;-fx-background-color: transparent;");
            btnCategories.setStyle("-fx-text-fill: #aaa;-fx-background-color: transparent;");
            btnUsers.setStyle("-fx-text-fill: #aaa;-fx-background-color: transparent;");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showCategories(ActionEvent actionEvent) {
        contentArea.getChildren().clear();
        try {

            Node node = FXMLLoader.load(getClass().getResource("/org/Views/CategoryManagement.fxml"));

            contentArea.getChildren().add(node);
            btnCatalogue.setStyle("-fx-text-fill: #aaa;-fx-background-color: transparent;");
            btnCategories.setStyle("-fx-text-fill: white;-fx-background-color: transparent;");
            btnUsers.setStyle("-fx-text-fill: #aaa;-fx-background-color: transparent;");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showUsers(ActionEvent actionEvent) {
        contentArea.getChildren().clear();
        try {

            Node node = FXMLLoader.load(getClass().getResource("/org/Views/UserManagement.fxml"));

            contentArea.getChildren().add(node);
            btnCatalogue.setStyle("-fx-text-fill: #aaa;-fx-background-color: transparent;");
            btnCategories.setStyle("-fx-text-fill: #aaa;-fx-background-color: transparent;");
            btnUsers.setStyle("-fx-text-fill: white;-fx-background-color: transparent;");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void handleLogout(ActionEvent actionEvent) {

    }
}
