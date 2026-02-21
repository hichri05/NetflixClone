package org.netflix.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javax.print.DocFlavor;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private HBox movieRow;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        for (Node node : movieRow.getChildren()) {
            ImageView iv = (ImageView) node;
            iv.setFitHeight(250);
            iv.setFitWidth(180);
            iv.setPreserveRatio(false);

        }
    }
}
