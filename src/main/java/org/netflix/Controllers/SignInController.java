package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.netflix.DAO.UserDAO;
import org.netflix.Services.AuthService;
import org.netflix.Utils.SceneSwitcher;

public class SignInController {
    @FXML public TextField emailField;
    @FXML public PasswordField passwordField;
    @FXML public Button loginButton;
    @FXML public Label signupLabel;
    @FXML public Label errorLabel;
    @FXML private ImageView background;
    @FXML private StackPane root;
    @FXML public VBox container;

    @FXML
    public void initialize() {
        background.setManaged(false);
        background.fitWidthProperty().bind(root.widthProperty());
        background.fitHeightProperty().bind(root.heightProperty());
        background.setPreserveRatio(false);
    }

    public void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        if (email.isEmpty() || password.isEmpty()) {
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
        }else if (AuthService.login(email, password)) {
            SceneSwitcher.goTo(event, "/org/Views/main.fxml");
        }
        else {
            errorLabel.setText("Sorry, we can't find an account with this email address.");
            errorLabel.setVisible(true);
        }
    }


    public void goToSignUp(MouseEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/SignUp.fxml");
    }
}
