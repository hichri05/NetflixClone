package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.netflix.Utils.SceneSwitcher;
import org.netflix.Utils.Session;

import java.io.IOException;

public class AdminMainController {

    @FXML private StackPane contentArea;
    @FXML private Button btnCatalogue, btnCategories, btnUsers, logoutbtn;


    private static final String ACTIVE_STYLE   = "-fx-text-fill: white;-fx-background-color: transparent;";
    private static final String INACTIVE_STYLE = "-fx-text-fill: #aaa;-fx-background-color: transparent;";

    @FXML
    public void initialize() {

        showCatalogue(null);
    }

    public void showCatalogue(ActionEvent actionEvent) {
        loadView("/org/Views/CatalogueManagement.fxml");
        btnCatalogue.setStyle(ACTIVE_STYLE);
        btnCategories.setStyle(INACTIVE_STYLE);
        btnUsers.setStyle(INACTIVE_STYLE);
    }

    public void showCategories(ActionEvent actionEvent) {
        loadView("/org/Views/CategoryManagement.fxml");
        btnCatalogue.setStyle(INACTIVE_STYLE);
        btnCategories.setStyle(ACTIVE_STYLE);
        btnUsers.setStyle(INACTIVE_STYLE);
    }

    public void showUsers(ActionEvent actionEvent) {
        loadView("/org/Views/UserManagement.fxml");
        btnCatalogue.setStyle(INACTIVE_STYLE);
        btnCategories.setStyle(INACTIVE_STYLE);
        btnUsers.setStyle(ACTIVE_STYLE);
    }

    private void loadView(String fxmlPath) {
        try {
            Node node = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleLogout(ActionEvent actionEvent) {
        Session.logout();
        SceneSwitcher.goTo(actionEvent, "/org/Views/SignIn.fxml");
    }
}