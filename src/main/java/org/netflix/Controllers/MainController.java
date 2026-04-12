package org.netflix.Controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.netflix.DAO.*;
import org.netflix.Models.*;
import org.netflix.Utils.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private ScrollPane searchContent, mainScrollList, mainScroll;
    @FXML private FlowPane   searchGrid;
    @FXML private FlowPane   listGrid;
    @FXML private TextField  searchField;
    @FXML private Label      mvTrendName, mvTrendDesc, userinf;
    @FXML private StackPane  heroStack;
    @FXML private VBox       mediaRows;
    @FXML private Button     playbtn, mylistbtn, adminBtn;

    User user;

    private List<Media> heroMedias;
    private int         currentHeroIndex = 0;
    private Timeline    heroTimeline;
    private Media       currentHeroMedia;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        searchField.setFocusTraversable(false);
        user = Session.getUser();

        // Auth guard — redirect to sign-in if not logged in
        /*if (user == null) {
            javafx.application.Platform.runLater(() -> {
                AuthGuard.requireLogin(searchField);
            });
            return;
        }*/


        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        adminBtn.setVisible(isAdmin);
        adminBtn.setManaged(isAdmin);

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
                heroStack.prefHeightProperty().bind(newScene.heightProperty().multiply(0.55));
            }
        });
    }

    private void loadMediaRows() {
        loadRow("Top 10 Views on Netflix", MediaDAO.getTopViews());
        loadRow("Action",      MediaDAO.getMediasByGenre("Action"));
        loadRow("Drama",       MediaDAO.getMediasByGenre("Drame"));
        loadRow("Comedy",      MediaDAO.getMediasByGenre("Comedie"));
        loadRow("Sci-Fi",      MediaDAO.getMediasByGenre("Science_Fiction"));
        loadRow("Thriller",    MediaDAO.getMediasByGenre("Thriller"));
        loadRow("Romance",     MediaDAO.getMediasByGenre("Romance"));
        loadRow("Historique",  MediaDAO.getMediasByGenre("Historique"));
        loadRow("Crime",       MediaDAO.getMediasByGenre("Crime"));
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
        if (user != null) userinf.setText(user.getUsername());
    }

    private void setupInitialView() {
        searchContent.setVisible(false);
        mainScrollList.setVisible(false);
        mainScroll.setVisible(true);
    }

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

    // ── HERO ─────────────────────────────────────────────────────────

    public void getTrendMovie() {
        heroMedias = MediaDAO.getTopViews();
        if (heroMedias == null || heroMedias.isEmpty()) return;

        updateHero(heroMedias.get(0));

        heroTimeline = new Timeline(new KeyFrame(Duration.seconds(6), e -> {
            currentHeroIndex = (currentHeroIndex + 1) % heroMedias.size();
            updateHero(heroMedias.get(currentHeroIndex));
        }));
        heroTimeline.setCycleCount(Timeline.INDEFINITE);
        heroTimeline.play();
    }

    private void updateHero(Media media) {
        currentHeroMedia = media;
        mvTrendName.setText(media.getTitle());

        String desc = media.getDescription();
        mvTrendDesc.setText(desc != null && desc.length() > 120
                ? desc.substring(0, 120) + "..." : desc);

        heroStack.prefWidthProperty().bind(mediaRows.widthProperty());

        String url = media.getBackdropImageUrl();
        if (url != null && !url.isEmpty()) {
            url = url.replace("\\", "/");
            heroStack.setStyle(
                    "-fx-background-image: url('" + url + "');" +
                            "-fx-background-size: 110%;" +
                            "-fx-background-position: center;"
            );
        }

        updateButtonUI(user, media);
    }

    private void updateButtonUI(User u, Media m) {
        if (u == null) { mylistbtn.setText("+ My List"); return; }
        mylistbtn.setText(UserDAO.isFavorite(u.getId(), m.getIdMedia())
                ? "✓ In My List" : "+ My List");
    }

    // ── SEARCH ───────────────────────────────────────────────────────

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

    // ── MY LIST ──────────────────────────────────────────────────────

    public void displayMyList(List<Media> favorites) {
        listGrid.getChildren().clear();
        for (Media media : favorites) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        MainController.class.getResource("/org/Views/MediaPoster.fxml"));
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

    // ── VIEW SWITCHING ───────────────────────────────────────────────

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

    // ── HANDLERS ─────────────────────────────────────────────────────

    @FXML private void handleHomeClick(MouseEvent event)    { showHomeView(); }

    @FXML
    private void handleMyListClick(MouseEvent event) {
        showMyListView();
        if (user != null) {
            List<Media> favorites = UserDAO.getUserFavorites(user.getId());
            displayMyList(favorites);
        }
    }

    @FXML
    public void handlePlay(ActionEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/VideoPlayer.fxml");
    }

    @FXML
    public void handleAddToMyList(ActionEvent event) {
        if (user == null || currentHeroMedia == null) return;

        if (UserDAO.isFavorite(user.getId(), currentHeroMedia.getIdMedia())) {
            MediaDAO.removeFromFavorites(user.getId(), currentHeroMedia.getIdMedia());
        } else {
            MediaDAO.addToFavorites(user.getId(), currentHeroMedia.getIdMedia());
        }
        updateButtonUI(user, currentHeroMedia);
    }

    @FXML
    private void RemoveFromList(Media media) {
        if (media == null || user == null) return;
        MediaDAO.removeFromFavorites(user.getId(), media.getIdMedia());
    }

    @FXML
    public void handleOpenDashboard(ActionEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/MainDashboard.fxml");
    }

    @FXML
    private void handleMoviesClick(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/Views/FilmPage.fxml"));
            Stage stage = (Stage) mediaRows.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleSerieClick(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/Views/SeriePage.fxml"));
            Stage stage = (Stage) mediaRows.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // kept for internal use — not used in UI directly
    private VBox buildHoverPopup(Serie serie) {
        VBox preview = new VBox(10);
        preview.setStyle("-fx-background-color: #181818; -fx-background-radius: 10; " +
                "-fx-border-color: #333; -fx-border-radius: 10; -fx-padding: 0;");
        preview.setPrefWidth(300);

        ImageView img = new ImageView(new Image(serie.getBackDropImageUrl(), true));
        img.setFitWidth(300); img.setFitHeight(160); img.setPreserveRatio(false);

        VBox info = new VBox(8);
        info.setPadding(new Insets(15));

        int match = 80 + (Math.abs(serie.getTitle().hashCode()) % 19);
        Label matchLbl = new Label(match + "% Match");
        matchLbl.setStyle("-fx-text-fill: #46d369; -fx-font-weight: bold; -fx-font-size: 14;");

        String outlineBtn = "-fx-background-color: transparent; -fx-border-color: white; " +
                "-fx-border-radius: 50; -fx-text-fill: white; -fx-cursor: hand;";
        Button playBtn = new Button("▶");
        playBtn.setStyle("-fx-background-color: white; -fx-text-fill: black; " +
                "-fx-background-radius: 50; -fx-padding: 5 12; -fx-cursor: hand;");
        Button addBtn  = new Button("+"); addBtn.setStyle(outlineBtn);
        Button likeBtn = new Button("♥"); likeBtn.setStyle(outlineBtn);
        HBox buttons = new HBox(10, playBtn, addBtn, likeBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Label titleLbl = new Label(serie.getTitle());
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18;");
        Label desc = new Label(serie.getDescription());
        desc.setStyle("-fx-text-fill: #d2d2d2; -fx-font-size: 12;");
        desc.setWrapText(true); desc.setMaxWidth(270); desc.setMaxHeight(60);

        info.getChildren().addAll(matchLbl, buttons, titleLbl, desc);
        preview.getChildren().addAll(img, info);
        return preview;
    }
}