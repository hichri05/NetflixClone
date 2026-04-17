package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.netflix.DAO.*;
import org.netflix.Models.*;
import org.netflix.Utils.*;

import java.time.LocalDate;
import java.util.List;

public class MediaDetailsController {

    @FXML private ImageView backgroundImage;
    @FXML private Label titleLabel, descriptionLabel, ratingLabel;
    @FXML private Label star1, star2, star3, star4, star5;
    @FXML private ScrollPane mainScroll;
    @FXML private Button mylistbtn;
    @FXML private HBox castingContainer;
    @FXML private Button tabMoreLikeThis, tabEpisodes, tabComments;
    @FXML private VBox panelMoreLikeThis, panelEpisodes, panelComments;
    @FXML private FlowPane relatedGrid;
    @FXML private ComboBox<String> seasonComboBox;
    @FXML private VBox episodesContainer;
    @FXML private VBox commentsListContainer;
    @FXML private TextArea newCommentField;

    private List<Label> stars;
    private int currentRating = 0;
    private Media media;
    private List<Season> seasons;

    @FXML
    public void initialize() {
        media = TransferData.getMedia();
        User user = Session.getUser();
        stars = List.of(star1, star2, star3, star4, star5);
        mainScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setupUI(user);
        loadCast();
        setupEpisodesTab();
        loadRelatedMedia();
        loadComments();
    }

    private void setupUI(User user) {
        titleLabel.setText(media.getTitle());
        descriptionLabel.setText(media.getDescription());
        backgroundImage.setImage(new Image(media.getBackdropImageUrl(), true));
        backgroundImage.fitWidthProperty().bind(mainScroll.widthProperty().multiply(0.99));
        int saved = MediaDAO.getRating(user.getId(), media.getIdMedia());
        if (saved > 0) { currentRating = saved; fillStars(saved); }
        updateButtonUI(user, media);
        setupStarHover();
    }

    private void loadCast() {
        castingContainer.getChildren().clear();
        List<Acteur> acteurs = ActeurDAO.getActeursByMedia(media.getIdMedia());
        for (Acteur a : acteurs) {
            VBox box = new VBox(8);
            box.setAlignment(Pos.CENTER);
            ImageView img = new ImageView(new Image(a.getActeurImageUrl(), true));
            img.setFitWidth(80); img.setFitHeight(80);
            img.setClip(new Circle(40, 40, 40));
            Label name = new Label(a.getNom());
            name.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
            box.getChildren().addAll(img, name);
            castingContainer.getChildren().add(box);
        }
    }

    private void setupEpisodesTab() {
        boolean isSerie = (media instanceof Serie) || "serie".equalsIgnoreCase(media.getType());
        if (!isSerie) {
            tabEpisodes.setVisible(false);
            tabEpisodes.setManaged(false);
            return;
        }
        seasons = SeasonDAO.getSeasonsBySerie(media.getIdMedia());
        if (seasons == null || seasons.isEmpty()) return;
        for (Season s : seasons) {
            String label = (s.getTitle() != null && !s.getTitle().isBlank()) ? s.getTitle() : "Saison " + s.getSeasonNumber();
            seasonComboBox.getItems().add(label);
        }
        seasonComboBox.getSelectionModel().selectFirst();
        loadEpisodes(seasons.get(0).getIdSeason());
    }

    @FXML
    private void handleSeasonChange(ActionEvent event) {
        int idx = seasonComboBox.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && seasons != null && idx < seasons.size()) {
            loadEpisodes(seasons.get(idx).getIdSeason());
        }
    }

    private void loadEpisodes(int seasonId) {
        episodesContainer.getChildren().clear();
        List<Episode> episodes = EpisodeDAO.getEpisodesBySeason(seasonId);

        for (Episode ep : episodes) {
            HBox row = new HBox(20);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 15; -fx-cursor: hand; -fx-background-radius: 5;");

            // Photo/Thumbnail
            ImageView thumb = new ImageView();
            thumb.setFitWidth(180);
            thumb.setFitHeight(100);
            thumb.setPreserveRatio(false);

            if (ep.getThumbnailPath() != null && !ep.getThumbnailPath().isEmpty()) {
                try {
                    thumb.setImage(new Image(ep.getThumbnailPath(), true));
                } catch (Exception e) {
                    // Fallback if image fails to load
                }
            }

            Rectangle clip = new Rectangle(180, 100);
            clip.setArcWidth(8); clip.setArcHeight(8);
            thumb.setClip(clip);

            VBox info = new VBox(5);
            info.setAlignment(Pos.CENTER_LEFT);
            Label epTitle = new Label("Episode " + ep.getEpisodeNumber() + " - " + ep.getTitle());
            epTitle.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
            Label epDesc = new Label(ep.getDescription() != null ? ep.getDescription() : "");
            epDesc.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12px;");
            epDesc.setWrapText(true);
            epDesc.setMaxWidth(600);
            info.getChildren().addAll(epTitle, epDesc);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            row.getChildren().addAll(thumb, info, spacer);
            row.setOnMouseClicked(e -> {
                TransferData.setEpisode(ep);
                SceneSwitcher.goTo(e, "/org/Views/VideoPlayer.fxml");
            });
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #2a2a2a; -fx-padding: 15; -fx-cursor: hand; -fx-background-radius: 5; -fx-border-color: #e50914;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 15; -fx-cursor: hand; -fx-background-radius: 5;"));

            episodesContainer.getChildren().add(row);
        }
    }

    @FXML private void handleTabMoreLikeThis(ActionEvent event) { setTab(true, false, false); }
    @FXML private void handleTabEpisodes(ActionEvent event) { setTab(false, true, false); }
    @FXML private void handleTabComments(ActionEvent event) { setTab(false, false, true); }

    private void setTab(boolean more, boolean ep, boolean comm) {
        panelMoreLikeThis.setVisible(more); panelMoreLikeThis.setManaged(more);
        panelEpisodes.setVisible(ep); panelEpisodes.setManaged(ep);
        panelComments.setVisible(comm); panelComments.setManaged(comm);
    }

    private void loadRelatedMedia() { /* Existing logic */ }
    private void loadComments() { /* Existing logic */ }
    @FXML public void handlePublishComment(ActionEvent event) { /* Existing logic */ }
    @FXML private void handleBack(ActionEvent e) { SceneSwitcher.goTo(e, "/org/Views/main.fxml"); }
    @FXML private void handlePlay(ActionEvent e) { SceneSwitcher.goTo(e, "/org/Views/VideoPlayer.fxml"); }
    @FXML private void handleRate(MouseEvent event) { /* Star logic */ }
    private void fillStars(int count) { for (int i = 0; i < stars.size(); i++) stars.get(i).setStyle(i < count ? "-fx-text-fill: #e50914;" : "-fx-text-fill: #555;"); }
    private void setupStarHover() { /* Hover logic */ }
    private void updateButtonUI(User u, Media m) { mylistbtn.setText(UserDAO.isFavorite(u.getId(), m.getIdMedia()) ? "✓ In My List" : "+ My List"); }
    @FXML public void handleAddToMyList(ActionEvent actionEvent) { /* Favorite logic */ }
}