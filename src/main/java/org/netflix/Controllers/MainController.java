package org.netflix.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.netflix.DAO.MediaDAO;
import org.netflix.DAO.MovieDAO;
import org.netflix.Models.Media;
import org.netflix.Models.Movie;

import org.netflix.Utils.SceneSwitcher;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML private HBox actionRow;
    @FXML private HBox dramaRow;
    @FXML private Label mvTrendName;
    @FXML private Label mvTrendDesc;
    @FXML private ScrollPane mainScroll;
    @FXML private StackPane heroStack;
    @FXML private VBox mediaRows;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        List<Media> trendingMedias = MediaDAO.getTrendingMedias();
        List<Media> actionMedias = MediaDAO.getMediasByGenre("Action");
        List<Media> dramaMedias = MediaDAO.getMediasByGenre("Drama");
        //List<Media> horrorMedias = MediaDAO.getMediasByGenre("Horror");
        List<Media> comedyMedias = MediaDAO.getMediasByGenre("Comedy");
        List<Media> scifiMedias = MediaDAO.getMediasByGenre("Scifi");
        try {
            fillRow("Action", actionMedias);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            fillRow("Drama", dramaMedias);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
       /* try {
            fillRow("Horror", horrorMedias);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
        try {
            fillRow("Comedy", comedyMedias);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            fillRow("Sci-Fi", scifiMedias);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getTrendMovie();

    }

    public void fillRow(String Title, List<Media> medias) throws IOException {
        if(medias.isEmpty()) {return;}
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/Views/MediaRow.fxml"));
        Parent row = loader.load();
        MediaRowController controller = loader.getController();
        controller.setData(Title, medias);
        mediaRows.getChildren().add(row);

    }
    public void getTrendMovie(){

        Movie movie = MovieDAO.getTrendMovie();

        mvTrendName.setText(movie.getTitle());
        mvTrendDesc.setText(movie.getDescription());
        String imgurl = movie.getBackdropImageUrl();
        heroStack.setStyle(
                        "-fx-background-image: url('" + imgurl + "'); " +
                        "-fx-background-size: cover; " +
                        "-fx-background-position: center center; " +
                        "-fx-background-repeat: no-repeat;" +
                                "-fx-background-color: linear-gradient(to bottom, rgba(0,0,0,0.7) 0%, transparent 20%);"
        );
    }
    @FXML
    private void handleMyListClick(MouseEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/MyList.fxml");
    }
}