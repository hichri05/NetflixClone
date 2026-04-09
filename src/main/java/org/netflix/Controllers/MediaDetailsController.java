package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import org.netflix.DAO.*;
import org.netflix.Models.*;
import org.netflix.Services.RecommendationService;
import org.netflix.Utils.SceneSwitcher;
import org.netflix.Utils.Session;
import org.netflix.Utils.TransferData;

import java.io.IOException;
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
    @FXML public HBox castingContainer;
    @FXML public VBox episodesSection;

    Media media;
    List<Season> seasons;
    SeasonDAO seasonDAO = new SeasonDAO();

    @FXML
    public void initialize() {
        media = TransferData.getMedia();
        User user = Session.getUser();

        titleLabel.setText(media.getTitle());
        descriptionLabel.setText(media.getDescription());

        String imgUrl = media.getBackdropImageUrl();
        Image img = new Image(imgUrl, true);
        backgroundImage.setImage(img);
        backgroundImage.fitWidthProperty().bind(mainScroll.widthProperty().multiply(0.996));
        StackPane.setAlignment(backgroundImage, Pos.CENTER);
        mainScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        updateButtonUI(user, media);
        media.setCasting(ActeurDAO.getActeursByMedia(media.getIdMedia()));
        loadCasting();
        loadMoreLikeThis(user);

        // Show episodes section only for series
        if ("serie".equalsIgnoreCase(media.getType())) {
            episodesSection.setVisible(true);
            episodesSection.setManaged(true);
            loadSeasons();
        } else {
            episodesSection.setVisible(false);
            episodesSection.setManaged(false);
        }
    }

    // ── CASTING ──────────────────────────────────────────────────────

    private void loadCasting() {
        List<Acteur> cast = media.getCasting();
        if (cast == null || cast.isEmpty()) return;
        castingContainer.getChildren().clear();

        for (Acteur acteur : cast) {
            VBox card = new VBox(8);
            card.setAlignment(Pos.CENTER);
            card.setPrefWidth(90);

            ImageView photo = new ImageView();
            photo.setFitWidth(70);
            photo.setFitHeight(70);
            photo.setPreserveRatio(false);
            try {
                if (acteur.getActeurImageUrl() != null && !acteur.getActeurImageUrl().isEmpty()) {
                    photo.setImage(new Image(acteur.getActeurImageUrl(), true));
                }
            } catch (Exception ignored) {}

            Circle clip = new Circle(35, 35, 35);
            photo.setClip(clip);

            Label name = new Label(acteur.getNom());
            name.setStyle("-fx-text-fill: white; -fx-font-size: 12; -fx-font-weight: bold;");
            name.setWrapText(true);
            name.setMaxWidth(90);
            name.setAlignment(Pos.CENTER);

            Label role = new Label("Cast");
            role.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 11;");

            card.getChildren().addAll(photo, name, role);
            castingContainer.getChildren().add(card);
        }
    }

    // ── SEASONS & EPISODES ───────────────────────────────────────────

    private void loadSeasons() {
        seasons = seasonDAO.getSeasonsBySerie(media.getIdMedia());
        if (seasons == null || seasons.isEmpty()) return;

        seasonSelector.getItems().clear();
        for (Season s : seasons) {
            seasonSelector.getItems().add("Season " + s.getSeasonNumber());
        }

        // Load first season episodes by default
        seasonSelector.getSelectionModel().selectFirst();
        loadEpisodes(seasons.get(0));

        // On season change, reload episodes
        seasonSelector.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            int idx = newVal.intValue();
            if (idx >= 0 && idx < seasons.size()) {
                loadEpisodes(seasons.get(idx));
            }
        });
    }

    private void loadEpisodes(Season season) {
        episodeListContainer.getChildren().clear();
        List<Episode> episodes = EpisodeDAO.getEpisodesBySeason(season.getIdSeason());

        if (episodes == null || episodes.isEmpty()) {
            Label none = new Label("No episodes available.");
            none.setStyle("-fx-text-fill: #999; -fx-font-size: 13;");
            episodeListContainer.getChildren().add(none);
            return;
        }

        for (Episode ep : episodes) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 8; -fx-padding: 10;");
            row.setCursor(javafx.scene.Cursor.HAND);

            // Thumbnail
            ImageView thumb = new ImageView();
            thumb.setFitWidth(120);
            thumb.setFitHeight(70);
            thumb.setPreserveRatio(false);
            try {
                if (ep.getThumbnailPath() != null && !ep.getThumbnailPath().isEmpty()) {
                    thumb.setImage(new Image(ep.getThumbnailPath(), true));
                }
            } catch (Exception ignored) {}

            // Episode info
            VBox info = new VBox(5);
            Label epTitle = new Label("E" + ep.getEpisodeNumber() + " — " + ep.getTitle());
            epTitle.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");

            info.getChildren().add(epTitle);

            // Play button
            Button playBtn = new Button("▶");
            playBtn.setStyle("-fx-background-color: white; -fx-text-fill: black; " +
                    "-fx-background-radius: 50; -fx-padding: 5 12; -fx-cursor: hand;");

            HBox.setHgrow(info, Priority.ALWAYS);
            row.getChildren().addAll(thumb, info, playBtn);

            // Hover effect
            row.setOnMouseEntered(e ->
                    row.setStyle("-fx-background-color: #3a3a3a; -fx-background-radius: 8; -fx-padding: 10;"));
            row.setOnMouseExited(e ->
                    row.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 8; -fx-padding: 10;"));

            episodeListContainer.getChildren().add(row);
        }
    }

    // ── MORE LIKE THIS ───────────────────────────────────────────────

    private void loadMoreLikeThis(User user) {
        relatedGrid.getChildren().clear();

        List<Media> recommendations;
        if (user != null) {
            recommendations = RecommendationService.getRecommendations(user.getId(), 10);
        } else {
            recommendations = MediaDAO.getTopViews().stream()
                    .filter(m -> m.getIdMedia() != media.getIdMedia())
                    .limit(10)
                    .toList();
        }

        recommendations = recommendations.stream()
                .filter(m -> m.getIdMedia() != media.getIdMedia())
                .toList();

        for (Media related : recommendations) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/Views/MediaPoster.fxml"));
                Parent card = loader.load();
                MediaPosterController controller = loader.getController();
                controller.setData(related);
                relatedGrid.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ── BUTTON HANDLERS ──────────────────────────────────────────────

    @FXML
    public void handlePlay(ActionEvent actionEvent) {
        SceneSwitcher.goTo(actionEvent, "/org/Views/VideoPlayer.fxml");
    }

    public void handleAddToMyList(ActionEvent actionEvent) {
        User user = Session.getUser();
        if (user == null) {
            System.out.println("No user logged in!");
            return;
        }
        if (UserDAO.isFavorite(user.getId(), media.getIdMedia())) {
            RemoveFromList(media);
        } else {
            boolean success = MediaDAO.addToFavorites(user.getId(), media.getIdMedia());
            if (success) System.out.println("Successfully added to favorites");
            else System.err.println("Failed to add to list.");
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
        if (u == null) {
            mylistbtn.setText("+ My List");
            return;
        }
        if (UserDAO.isFavorite(u.getId(), m.getIdMedia())) {
            mylistbtn.setText("✓ In My List");
        } else {
            mylistbtn.setText("+ My List");
        }
    }

    public void handleBack(ActionEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/main.fxml");
    }
}