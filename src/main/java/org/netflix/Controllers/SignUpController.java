package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.netflix.DAO.UserDAO;
import org.netflix.Models.User;
import org.netflix.Services.AuthService;
import org.netflix.Services.ValidationResult;
import org.netflix.Services.ValidationService;
import org.netflix.Utils.SceneSwitcher;
import org.netflix.Utils.Session;

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
        String email           = emailField.getText().trim();
        String userName        = username.getText().trim();
        String password        = passwordField.getText();
        String passwordConfirm = passwordFieldConfirm.getText();

        resetStyles();

        ValidationResult result = ValidationService.validate(email, userName, password, passwordConfirm);

        if (!result.isValid()) {
            showError(result.getMessage());
            highlightEmptyFields();
            return;
        }

        if (AuthService.register(userName, email, password)) {
            User registeredUser = UserDAO.findByEmail(email);
            Session.setUser(registeredUser);
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