package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.netflix.Services.AuthService;
import org.netflix.Utils.SceneSwitcher;

import java.lang.reflect.Field;

public class SignUpController {
    @FXML public TextField emailField;
    @FXML public TextField username;
    @FXML public PasswordField passwordField;
    @FXML public PasswordField passwordFieldConfirm;
    @FXML public Button loginButton;
    @FXML public ImageView background;
    @FXML public StackPane root;
    @FXML public Label errorLabel;
    @FXML public VBox container;

    @FXML
    public void initialize() {
        background.setManaged(false);
        background.fitWidthProperty().bind(root.widthProperty());
        background.fitHeightProperty().bind(root.heightProperty());
        background.setPreserveRatio(false);
    }

    public void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String userName = username.getText();
        String password = passwordField.getText();
        String passwordConfirm = passwordFieldConfirm.getText();
        if (email.isEmpty() || userName.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
            errorLabel.setText("Please enter a valid email or phone number.");
            errorLabel.setVisible(true);

            for (Node n: container.getChildren()) {
                if (n instanceof TextInputControl ti){
                    if (ti.getText().isEmpty()) ti.setStyle("-fx-border-color: #e87c03;-fx-border-width: 0 0 2 0;");
                    ti.textProperty().addListener((observable, oldValue, newValue) -> {
                        errorLabel.setVisible(false);
                        ti.setStyle("-fx-border-color: transparent;");
                    });
                }
            }
        } else if (AuthService.register(userName, email, password)) {
            SceneSwitcher.goTo(event, "/org/Views/main.fxml");
        }else{
            errorLabel.setText("Sorry, we can't find an account with this email address.");
            errorLabel.setVisible(true);
        }

        //SceneSwitcher.goTo(event, "/org/Views/main.fxml");
    }
}
