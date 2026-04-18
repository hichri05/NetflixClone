package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.netflix.DAO.UserDAO;
import org.netflix.Models.User;
import org.netflix.Services.AuthService;
import org.netflix.Utils.SceneSwitcher;
import org.netflix.Utils.Session;

public class SignInController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField     passwordVisible;
    @FXML private Button        togglePassword;
    @FXML private Label         errorLabel;
    @FXML private ImageView     background;
    @FXML private StackPane     root;

    private boolean   isPasswordVisible = false;
    @FXML
    private ImageView eyeIcon;

    private Image imgEyeOpen;
    private Image imgEyeClosed;

    private static final String STYLE_NORMAL =
            "-fx-background-color: none;" +
                    "-fx-border-color: #fff;" +
                    "-fx-border-radius: 3px;" +
                    "-fx-border-width: 0.5px;" +
                    "-fx-text-fill: white;" +
                    "-fx-prompt-text-fill: #8c8c8c;";

    private static final String STYLE_ERROR =
            "-fx-background-color: none;" +
                    "-fx-border-color: #e87c03;" +
                    "-fx-border-radius: 3px;" +
                    "-fx-border-width: 0 0 2 0;" +
                    "-fx-text-fill: white;" +
                    "-fx-prompt-text-fill: #8c8c8c;";

    private static final String STYLE_Banned =
            "-fx-background-color: none;" +
                    "-fx-border-color: #FF0000;" +
                    "-fx-border-radius: 3px;" +
                    "-fx-border-width: 0 0 2 0;" +
                    "-fx-text-fill: white;" +
                    "-fx-prompt-text-fill: #8c8c8c;";

    @FXML
    public void initialize() {

        background.setManaged(false);
        background.fitWidthProperty().bind(root.widthProperty());
        background.fitHeightProperty().bind(root.heightProperty());
        background.setPreserveRatio(false);

        imgEyeOpen   = new Image(getClass().getResourceAsStream("/org/Images/oeil__fermé.png"));
        imgEyeClosed = new Image(getClass().getResourceAsStream("/org/Images/oeil_ouvert.png"));

        // ✅ FIXED: don't reassign eyeIcon, just configure the one injected by FXML
        eyeIcon.setImage(imgEyeOpen);
        eyeIcon.setFitWidth(22);
        eyeIcon.setFitHeight(22);
        eyeIcon.setPreserveRatio(true);
        makeWhite(eyeIcon);

        togglePassword.setText(null);
        togglePassword.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: transparent;" +
                        "-fx-padding: 5px;"
        );

        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        emailField.setStyle(STYLE_NORMAL);
        passwordField.setStyle(STYLE_NORMAL);
        passwordVisible.setStyle(STYLE_NORMAL);

        passwordVisible.setVisible(false);
        passwordVisible.setManaged(false);

        passwordField.textProperty().addListener((obs, o, n) -> {
            if (!n.equals(passwordVisible.getText())) passwordVisible.setText(n);
        });
        passwordVisible.textProperty().addListener((obs, o, n) -> {
            if (!n.equals(passwordField.getText())) passwordField.setText(n);
        });

        emailField.textProperty().addListener((obs, o, n) -> {
            emailField.setStyle(STYLE_NORMAL);
            hideError();
        });
        passwordField.textProperty().addListener((obs, o, n) -> {
            passwordField.setStyle(STYLE_NORMAL);
            passwordVisible.setStyle(STYLE_NORMAL);
            hideError();
        });
        passwordVisible.textProperty().addListener((obs, o, n) -> {
            passwordField.setStyle(STYLE_NORMAL);
            passwordVisible.setStyle(STYLE_NORMAL);
            hideError();
        });
    }

    private void makeWhite(ImageView iv) {
        ColorAdjust ca = new ColorAdjust();
        ca.setBrightness(1.0);
        iv.setEffect(ca);
    }

    @FXML
    public void togglePasswordVisibility(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            passwordVisible.setText(passwordField.getText());
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordVisible.requestFocus();
            passwordVisible.positionCaret(passwordVisible.getText().length());
            eyeIcon.setImage(imgEyeClosed);
            makeWhite(eyeIcon);

        } else {
            passwordField.setText(passwordVisible.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
            eyeIcon.setImage(imgEyeOpen);
            makeWhite(eyeIcon);
        }
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String email    = emailField.getText().trim();
        String password = isPasswordVisible
                ? passwordVisible.getText()
                : passwordField.getText();

        emailField.setStyle(STYLE_NORMAL);
        passwordField.setStyle(STYLE_NORMAL);
        passwordVisible.setStyle(STYLE_NORMAL);
        hideError();

        if (email.isEmpty() && password.isEmpty()) {
            emailField.setStyle(STYLE_ERROR);
            passwordField.setStyle(STYLE_ERROR);
            passwordVisible.setStyle(STYLE_ERROR);
            showError("Veuillez saisir votre email et mot de passe.");
            emailField.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            emailField.setStyle(STYLE_ERROR);
            showError("Veuillez saisir votre adresse email.");
            emailField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordField.setStyle(STYLE_ERROR);
            passwordVisible.setStyle(STYLE_ERROR);
            showError("Veuillez saisir votre mot de passe.");
            if (isPasswordVisible) passwordVisible.requestFocus();
            else passwordField.requestFocus();
            return;
        }

        User user = UserDAO.findByEmail(email);
        if (user == null) {
            emailField.setStyle(STYLE_ERROR);
            showError("Aucun compte trouvé avec cette adresse email.");
            emailField.requestFocus();
            emailField.selectAll();
            return;
        }

        if ("BANNED".equalsIgnoreCase(user.getRole())) {
            emailField.setStyle(STYLE_Banned);
            passwordField.setStyle(STYLE_Banned);
            passwordVisible.setStyle(STYLE_Banned);
            showError1("Votre compte a été suspendu. Veuillez contacter le support.");
            emailField.requestFocus();
            return;
        }

        if (!AuthService.login(email, password)) {
            passwordField.setStyle(STYLE_ERROR);
            passwordVisible.setStyle(STYLE_ERROR);
            showError("Mot de passe incorrect. Veuillez réessayer.");
            if (isPasswordVisible) { passwordVisible.requestFocus(); passwordVisible.clear(); }
            else                   { passwordField.requestFocus();   passwordField.clear();   }
            return;
        }

        Session.setUser(user);
        System.out.println("Connecté : " + user.getUsername());
        SceneSwitcher.goTo(event, "/org/Views/main.fxml");
    }

    @FXML
    public void goToSignUp(MouseEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/SignUp.fxml");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void showError1(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}