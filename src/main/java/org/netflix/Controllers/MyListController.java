package org.netflix.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.netflix.DAO.UserDAO;
import org.netflix.Models.Media;
import org.netflix.Utils.SceneSwitcher;

import java.io.IOException;
import java.util.List;

public class MyListController {
    @FXML private FlowPane listGrid;
    @FXML
    public void initialize() throws IOException {
        //List<Media> userFavorites = UserDAO.getUserFavorites();
        //displayMyList(userFavorites);
    }
    public void displayMyList(List<Media> userFavorites) {
        listGrid.getChildren().clear();

        for (Media media : userFavorites) {
            try {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("org/netflix/Views/moviePoster.fxml"));
                StackPane posterNode = loader.load();

                MoviePosterController controller = loader.getController();
                controller.showRemoveButton(true);
                controller.setData(media);

                listGrid.getChildren().add(posterNode);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void handleHomeClick(MouseEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/main.fxml");
    }
}
