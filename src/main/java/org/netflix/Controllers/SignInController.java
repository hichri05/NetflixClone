package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.netflix.Utils.SceneSwitcher;

public class SignInController {
    @FXML private ImageView background;
    @FXML private StackPane root;
    @FXML
    public void initialize() {
        background.setManaged(false);
        background.fitWidthProperty().bind(root.widthProperty());
        background.fitHeightProperty().bind(root.heightProperty());
        background.setPreserveRatio(false);
    }

    public void handleLogin(ActionEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/main.fxml");
    }

    public void goToSignUp(MouseEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/SignUp.fxml");
    }
}
