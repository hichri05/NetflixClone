package org.netflix.Controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import org.netflix.Main.App;

public class PrimaryController {

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
}
