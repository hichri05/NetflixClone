package org.netflix.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import org.netflix.DAO.MediaDAO;
import org.netflix.DAO.MovieDAO;
import org.netflix.DAO.UserDAO;
import org.netflix.Models.Media;
import org.netflix.Models.Movie;

import org.netflix.Models.User;
import org.netflix.Utils.SceneSwitcher;
import org.netflix.Utils.Session;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML public ScrollPane searchContent;
    @FXML public FlowPane searchGrid;
    @FXML public ScrollPane mainScrollList;
    @FXML public FlowPane listGrid;
    @FXML private TextField searchField;
    @FXML private HBox actionRow;
    @FXML private HBox dramaRow;
    @FXML private Label mvTrendName;
    @FXML private Label mvTrendDesc;
    @FXML private ScrollPane mainScroll;
    @FXML private StackPane heroStack;
    @FXML private VBox mediaRows;
    @FXML private BorderPane navbar;
    @FXML private Label userinf;
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
        mainScrollList.setVisible(false);
        searchContent.setVisible(false);
        mainScroll.setVisible(true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= 2) {
                searchContent.setVisible(true);
                mainScroll.setVisible(false);
                mainScrollList.setVisible(false);

                performSearch(newValue);
            } else if (newValue.isEmpty()) {
                searchContent.setVisible(false);
                mainScroll.setVisible(true);
                mainScrollList.setVisible(false);
            }
        });
        User user = Session.getUser();
        userinf.setText(user.getUsername());

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
        searchContent.setVisible(false);
        mainScroll.setVisible(false);
        mainScrollList.setVisible(true);
        int userid = 1;
        List<Media> userFavorites = UserDAO.getUserFavorites(userid);
        displayMyList(userFavorites);
    }

    private void performSearch(String search) {
        List<Media> results = MediaDAO.searchMedia(search);
        if (results.isEmpty()) {
            Label noResults = new Label("Your search for '" + search + "' did not have any matches.");
            noResults.setStyle("-fx-text-fill: #999; -fx-font-size: 16px;");
            searchGrid.getChildren().add(noResults);
        }

        searchGrid.getChildren().clear();
        for (Media m : results) {
            loadMovieCard(m);
        }
    }

    private void loadMovieCard(Media m) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/Views/MediaPoster.fxml"));
            Parent card = loader.load();
            MediaPosterController controller = loader.getController();
            controller.setData(m);
            searchGrid.getChildren().add(card);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void displayMyList(List<Media> userFavorites) {
        listGrid.getChildren().clear();

        for (Media media : userFavorites) {
            try {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("org/netflix/Views/moviePoster.fxml"));
                StackPane posterNode = loader.load();

                MediaPosterController controller = loader.getController();
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
        searchContent.setVisible(false);
        mainScroll.setVisible(true);
        mainScrollList.setVisible(false);
    }
}