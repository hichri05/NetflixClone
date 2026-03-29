package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import org.netflix.DAO.*;
import org.netflix.Models.*;
import org.netflix.Utils.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    //
    @FXML private ScrollPane searchContent, mainScrollList, mainScroll;
    @FXML private FlowPane searchGrid;
    @FXML private FlowPane listGrid;
    @FXML private TextField searchField;
    @FXML private Label mvTrendName, mvTrendDesc, userinf;
    @FXML private StackPane heroStack;
    @FXML private VBox mediaRows;
    @FXML private Button playbtn, mylistbtn;

    //
    User user;
    //
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupHeroSize();
        loadMediaRows();
        setupSearch();
        setupUser();
        setupInitialView();
        getTrendMovie();
    }
    private void setupHeroSize() {

        heroStack.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // Hero = 55% of window height
                heroStack.prefHeightProperty().bind(newScene.heightProperty().multiply(0.55));
            }
        });
    }
    private void loadMediaRows() {
        loadRow("Action", MediaDAO.getMediasByGenre("Action"));
        loadRow("Drama", MediaDAO.getMediasByGenre("Drama"));
        loadRow("Comedy", MediaDAO.getMediasByGenre("Comedy"));
        loadRow("Sci-Fi", MediaDAO.getMediasByGenre("Scifi"));
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() >= 2) {
                showSearchView();
                performSearch(newVal);
            } else if (newVal.isEmpty()) {
                showHomeView();
            }
        });
    }

    private void setupUser() {
        user = Session.getUser();
        if (user != null) {
            userinf.setText(user.getUsername());
        }
    }

    private void setupInitialView() {
        searchContent.setVisible(false);
        mainScrollList.setVisible(false);
        mainScroll.setVisible(true);
    }

    //
    private void loadRow(String title, List<Media> medias) {
        if (medias == null || medias.isEmpty()) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/Views/MediaRow.fxml"));
            Parent row = loader.load();

            MediaRowController controller = loader.getController();
            controller.setData(title, medias);

            mediaRows.getChildren().add(row);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getTrendMovie() {
        Movie movie = MovieDAO.getTrendMovie();

        mvTrendName.setText(movie.getTitle());
        mvTrendDesc.setText(movie.getDescription());

        heroStack.getChildren().removeIf(n -> n instanceof ImageView);

        String url = movie.getBackdropImageUrl().replace("\\", "/");

        heroStack.setStyle(
                "-fx-background-image: url('" + url + "');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;" +
                        "-fx-background-repeat: no-repeat;"
        );

        updateButtonUI(user, movie);
    }

    private void updateButtonUI(User u,  Media m) {
        if (UserDAO.isFavorite(u.getId(), m.getIdMedia())) {
            mylistbtn.setText("✓ In My List");
        } else {
            mylistbtn.setText("+ My List");
        }
    }

    //
    private void performSearch(String search) {
        List<Media> results = MediaDAO.searchMedia(search);

        searchGrid.getChildren().clear();

        if (results.isEmpty()) {
            Label noResults = new Label("No results for '" + search + "'");
            noResults.setStyle("-fx-text-fill: #999; -fx-font-size: 16px;");
            searchGrid.getChildren().add(noResults);
            return;
        }

        results.forEach(this::loadMovieCard);
    }

    private void loadMovieCard(Media media) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/Views/MediaPoster.fxml"));
            Parent card = loader.load();

            MediaPosterController controller = loader.getController();
            controller.setData(media);

            searchGrid.getChildren().add(card);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //
    public void displayMyList(List<Media> favorites) {
        listGrid.getChildren().clear();

        for (Media media : favorites) {
            try {
                FXMLLoader loader = new FXMLLoader(MainController.class.getResource("/org/Views/MediaPoster.fxml"));
                Parent poster = loader.load();

                MediaPosterController controller = loader.getController();
                controller.showRemoveButton(true);
                controller.setData(media);

                listGrid.getChildren().add(poster);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //
    private void showHomeView() {
        searchContent.setVisible(false);
        mainScroll.setVisible(true);
        mainScrollList.setVisible(false);
    }

    private void showSearchView() {
        searchContent.setVisible(true);
        mainScroll.setVisible(false);
        mainScrollList.setVisible(false);
    }

    private void showMyListView() {
        searchContent.setVisible(false);
        mainScroll.setVisible(false);
        mainScrollList.setVisible(true);
    }

    //

    @FXML
    private void handleHomeClick(MouseEvent event) {
        showHomeView();
    }

    @FXML
    private void handleMyListClick(MouseEvent event) {
        showMyListView();

        User user = Session.getUser();
        List<Media> favorites = UserDAO.getUserFavorites(user.getId());

        displayMyList(favorites);
    }

    @FXML
    public void handlePlay(ActionEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/VideoPlayer.fxml");
    }

    @FXML
    public void handleAddToMyList(ActionEvent event) {
        User user = Session.getUser();
        if (user == null) {
            System.out.println("No user logged in!");
            return;
        }

        Media media = MovieDAO.getTrendMovie();
        if (UserDAO.isFavorite(user.getId(), media.getIdMedia())) {
            RemoveFromList(media);

        }else{
            boolean success = MediaDAO.addToFavorites(user.getId(), media.getIdMedia());
            if (success) {
                System.out.println("Successfully added to favorites");
            } else {
                System.err.println("Failed to add to list.");
            }
        }
        updateButtonUI(user, media);
    }
    @FXML
    private void RemoveFromList(Media media) {
        if (media == null) return;
        System.out.println("Removing: " + media.getTitle());

        User user = Session.getUser();
        MediaDAO.removeFromFavorites(user.getId(), media.getIdMedia());
    }
}