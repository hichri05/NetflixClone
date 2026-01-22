package org.netflix.Controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import org.netflix.Main.App;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}