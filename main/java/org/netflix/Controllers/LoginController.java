package org.netflix.Controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.netflix.Main.App;

public class LoginController {
    @FXML private ImageView background;
    @FXML private StackPane root;
    @FXML
    public void initialize() {
        background.setManaged(false);
        background.fitWidthProperty().bind(root.widthProperty());
        background.fitHeightProperty().bind(root.heightProperty());
        background.setPreserveRatio(false);
    }
}
