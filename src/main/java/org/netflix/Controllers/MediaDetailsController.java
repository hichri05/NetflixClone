package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.netflix.DAO.*;
import org.netflix.Models.*;
import org.netflix.Utils.*;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class MediaDetailsController {

    @FXML public StackPane detailHero;
    @FXML public ImageView backgroundImage;
    @FXML public Label titleLabel, descriptionLabel;
    @FXML public ChoiceBox<String> seasonSelector;
    @FXML public VBox episodeListContainer;
    @FXML public FlowPane relatedGrid;
    @FXML public ScrollPane mainScroll;
    @FXML public Button mylistbtn;
    @FXML private HBox ratingBox;
    @FXML private Label star1, star2, star3, star4, star5, ratingLabel;

    private List<Label> stars;
    private int currentRating = 0;
    private Media media;
    private SeasonDAO seasonDAO = new SeasonDAO();

    @FXML
    public void initialize() {
        media = TransferData.getMedia();
        User user = Session.getUser();
        stars = List.of(star1, star2, star3, star4, star5);
        setupStarHover();

        if (user != null) {
            int saved = MediaDAO.getRating(user.getId(), media.getIdMedia());
            if (saved > 0) {
                currentRating = saved;
                fillStars(saved);
                ratingLabel.setText("Your rating: " + saved + "/5");
            }
        }

        titleLabel.setText(media.getTitle());
        descriptionLabel.setText(media.getDescription());

        String imgUrl = media.getBackdropImageUrl();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            Image img = new Image(imgUrl, true);
            backgroundImage.setImage(img);
        }
        backgroundImage.fitWidthProperty().bind(mainScroll.widthProperty().multiply(0.996));
        mainScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        if (user != null) updateButtonUI(user, media);


        if ("Serie".equalsIgnoreCase(media.getType())) {
            loadSeasonsAndEpisodes();
        } else {

            seasonSelector.setVisible(false);
            seasonSelector.setManaged(false);
        }

        loadRelatedMedia();


        if (user != null) {
            WatchHistory wh = new WatchHistory(
                    user.getId(),
                    media.getIdMedia(),
                    0.0,
                    Timestamp.from(Instant.now()),
                    0
            );
            WatchHistoryDAO.addToHistory(wh);
        }
    }



    private void loadSeasonsAndEpisodes() {
        List<Season> seasons = seasonDAO.getSeasonsBySerie(media.getIdMedia());
        if (seasons == null || seasons.isEmpty()) return;

        seasonSelector.getItems().clear();
        for (Season s : seasons) {
            seasonSelector.getItems().add("Season " + s.getSeasonNumber());
        }
        seasonSelector.setValue(seasonSelector.getItems().get(0));

        loadEpisodesForSeason(seasons.get(0));

        seasonSelector.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            if (newIdx.intValue() >= 0 && newIdx.intValue() < seasons.size()) {
                loadEpisodesForSeason(seasons.get(newIdx.intValue()));
            }
        });
    }

    private void loadEpisodesForSeason(Season season) {
        episodeListContainer.getChildren().clear();
        List<Episode> episodes = EpisodeDAO.getEpisodesBySeason(season.getIdSeason());
        if (episodes == null || episodes.isEmpty()) {
            Label none = new Label("No episodes available.");
            none.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");
            episodeListContainer.getChildren().add(none);
            return;
        }

        for (Episode ep : episodes) {
            HBox row = buildEpisodeRow(ep);
            episodeListContainer.getChildren().add(row);
        }
    }

    private HBox buildEpisodeRow(Episode ep) {
        HBox row = new HBox(15);
        row.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 8; -fx-padding: 10;");
        row.setAlignment(Pos.CENTER_LEFT);

        ImageView thumb = new ImageView();
        thumb.setFitWidth(120);
        thumb.setFitHeight(68);
        thumb.setPreserveRatio(false);
        if (ep.getThumbnailPath() != null && !ep.getThumbnailPath().isEmpty()) {
            try { thumb.setImage(new Image(ep.getThumbnailPath(), true)); }
            catch (Exception ignored) {}
        } else {
            thumb.setStyle("-fx-background-color: #444;");
        }

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label epTitle = new Label("Ep " + ep.getEpisodeNumber() + " — " + ep.getTitle());
        epTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        info.getChildren().add(epTitle);

        Button playBtn = new Button("▶ Play");
        playBtn.setStyle("-fx-background-color: white; -fx-text-fill: black; " +
                "-fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
        playBtn.setOnAction(e -> playEpisode(ep));

        row.getChildren().addAll(thumb, info, playBtn);
        row.setOnMouseClicked(e -> playEpisode(ep));
        row.setStyle(row.getStyle() + "-fx-cursor: hand;");
        return row;
    }

    private void playEpisode(Episode ep) {
        TransferData.setMedia(media);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/Views/VideoPlayer.fxml"));
            Stage stage = (Stage) mainScroll.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }

        User user = Session.getUser();
        if (user != null) {
            WatchHistory wh = new WatchHistory(
                    user.getId(),
                    null,
                    ep.getId(),
                    0.0,
                    Timestamp.from(Instant.now()),
                    0
            );
            WatchHistoryDAO.addToHistory(wh);
        }
    }


    private void loadRelatedMedia() {
        relatedGrid.getChildren().clear();

        // Find media with same genre
        List<Genre> genres = media.getGenres();
        if (genres == null || genres.isEmpty()) return;

        String genreName = genres.get(0).getName() != null
                ? genres.get(0).getName().name()
                : null;
        if (genreName == null) return;

        List<Media> related = MediaDAO.getMediasByGenre(genreName);
        int count = 0;
        for (Media m : related) {
            if (m.getIdMedia() == media.getIdMedia()) continue; // skip self
            if (count >= 8) break;
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/Views/MediaPoster.fxml"));
                Parent card = loader.load();
                MediaPosterController controller = loader.getController();
                controller.setData(m);
                relatedGrid.getChildren().add(card);
                count++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @FXML
    public void handlePlay(ActionEvent actionEvent) {
        SceneSwitcher.goTo(actionEvent, "/org/Views/VideoPlayer.fxml");
    }

    @FXML
    public void handleAddToMyList(ActionEvent actionEvent) {
        User user = Session.getUser();
        if (user == null) return;

        if (UserDAO.isFavorite(user.getId(), media.getIdMedia())) {
            RemoveFromList(media);
        } else {
            MediaDAO.addToFavorites(user.getId(), media.getIdMedia());
        }
        updateButtonUI(user, media);
    }

    @FXML
    private void RemoveFromList(Media media) {
        if (media == null) return;
        User user = Session.getUser();
        MediaDAO.removeFromFavorites(user.getId(), media.getIdMedia());
    }

    private void updateButtonUI(User u, Media m) {
        if (UserDAO.isFavorite(u.getId(), m.getIdMedia())) {
            mylistbtn.setText("✓ In My List");
        } else {
            mylistbtn.setText("+ My List");
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/main.fxml");
    }


    @FXML
    private void handleRate(MouseEvent event) {
        Label clicked = (Label) event.getSource();
        int rating = Integer.parseInt((String) clicked.getUserData());
        currentRating = rating;
        fillStars(rating);
        ratingLabel.setText("Your rating: " + rating + "/5");

        User user = Session.getUser();
        if (user != null) {
            MediaDAO.saveRating(user.getId(), media.getIdMedia(), rating);
        }
    }

    private void fillStars(int count) {
        for (int i = 0; i < stars.size(); i++) {
            if (i < count) {
                stars.get(i).getStyleClass().add("star-filled");
            } else {
                stars.get(i).getStyleClass().remove("star-filled");
            }
        }
    }

    private void setupStarHover() {
        for (Label star : stars) {
            int idx = stars.indexOf(star) + 1;
            star.setOnMouseEntered(e -> fillStars(idx));
            star.setOnMouseExited(e -> fillStars(currentRating));
        }
    }
}