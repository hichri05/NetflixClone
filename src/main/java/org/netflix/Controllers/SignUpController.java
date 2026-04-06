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

        errorLabel.setVisible(false);
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String email    = emailField.getText().trim();
        String userName = username.getText().trim();
        String password = passwordField.getText();
        String passwordConfirm = passwordFieldConfirm.getText();


        resetStyles();


        if (email.isEmpty() || userName.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
            showError("Please fill in all fields.");
            highlightEmptyFields();
            return;
        }


        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Please enter a valid email address.");
            highlight(emailField);
            return;
        }


        if (userName.length() < 2) {
            showError("Username must be at least 2 characters.");
            highlight(username);
            return;
        }

        if (password.length() < 8) {
            showError("Password must be at least 8 characters.");
            highlight(passwordField);
            return;
        }

        if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")) {
            showError("Password must contain: uppercase, lowercase, digit, and special character (@#$%^&+=).");
            highlight(passwordField);
            return;
        }


        if (!password.equals(passwordConfirm)) {
            showError("Passwords do not match.");
            highlight(passwordField);
            highlight(passwordFieldConfirm);
            return;
        }


        if (AuthService.register(userName, email, password)) {
            SceneSwitcher.goTo(event, "/org/Views/main.fxml");
        } else {
            showError("An account with this email already exists.");
            highlight(emailField);
        }
    }



    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void highlight(TextInputControl field) {
        field.setStyle(field.getStyle() + "; -fx-border-color: #e87c03; -fx-border-width: 0 0 2 0;");
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            field.setStyle(field.getStyle()
                    .replace("; -fx-border-color: #e87c03; -fx-border-width: 0 0 2 0;", ""));
            errorLabel.setVisible(false);
        });
    }

    private void highlightEmptyFields() {
        for (Node n : container.getChildren()) {
            if (n instanceof TextInputControl ti && ti.getText().trim().isEmpty()) {
                highlight(ti);
            }
        }
    }

    private void resetStyles() {
        for (Node n : container.getChildren()) {
            if (n instanceof TextInputControl ti) {
                ti.setStyle(ti.getStyle()
                        .replace("; -fx-border-color: #e87c03; -fx-border-width: 0 0 2 0;", ""));
            }
        }
        errorLabel.setVisible(false);
    }
}