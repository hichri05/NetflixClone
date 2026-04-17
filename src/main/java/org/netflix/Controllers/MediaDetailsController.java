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
    @FXML private HBox episodesContainer;

    // ── Tabs ──────────────────────────────────────────────────────────────────
    @FXML private Button tabMoreLikeThis, tabComments;
    @FXML private VBox panelMoreLikeThis, panelComments;
    @FXML private FlowPane relatedGrid;

    // ── Comments ──────────────────────────────────────────────────────────────
    @FXML private VBox commentsListContainer;
    @FXML private TextArea newCommentField;

    private List<Label> stars;
    private int currentRating = 0;
    private Media media;
    private List<Season> seasons;

    private static final String TAB_ACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: white;" +
                    "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand;" +
                    "-fx-border-color: #e50914; -fx-border-width: 0 0 3 0;" +
                    "-fx-padding: 10 20 10 20;";

    private static final String TAB_ACTIVE_FIRST =
            "-fx-background-color: transparent; -fx-text-fill: white;" +
                    "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand;" +
                    "-fx-border-color: #e50914; -fx-border-width: 0 0 3 0;" +
                    "-fx-padding: 10 20 10 0;";

    private static final String TAB_INACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: #aaa;" +
                    "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand;" +
                    "-fx-border-color: transparent; -fx-border-width: 0 0 3 0;" +
                    "-fx-padding: 10 20 10 20;";

    private static final String TAB_INACTIVE_FIRST =
            "-fx-background-color: transparent; -fx-text-fill: #aaa;" +
                    "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand;" +
                    "-fx-border-color: transparent; -fx-border-width: 0 0 3 0;" +
                    "-fx-padding: 10 20 10 0;";

    private static final String STYLE_ACTIVE_FIRST = TAB_ACTIVE_FIRST;
    private static final String STYLE_INACTIVE_FIRST = TAB_INACTIVE_FIRST;
    private static final String STYLE_ACTIVE = TAB_ACTIVE;
    private static final String STYLE_INACTIVE = TAB_INACTIVE;

    @FXML
    public void initialize() {
        media = TransferData.getMedia();
        User user = Session.getUser();
        stars = List.of(star1, star2, star3, star4, star5);

        mainScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setupUI(user);
        loadCast();
        loadSeasonEpisodeBar();
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

    // ── Season / Episode bar ──────────────────────────────────────────────────
    private void loadSeasonEpisodeBar() {
        boolean isSerie = (media instanceof Serie) ||
                "serie".equalsIgnoreCase(media.getType()) ||
                "series".equalsIgnoreCase(media.getType());

        if (!isSerie) return;

        seasons = SeasonDAO.getSeasonsBySerie(media.getIdMedia());
        if (seasons == null || seasons.isEmpty()) return;

        seasonEpisodeBar.setVisible(true);
        seasonEpisodeBar.setManaged(true);

        for (Season s : seasons) {
            String label = (s.getTitle() != null && !s.getTitle().isBlank())
                    ? s.getTitle()
                    : "Season " + s.getSeasonNumber();
            seasonComboBox.getItems().add(label);
        }
        seasonComboBox.getSelectionModel().selectFirst();
        loadEpisodes(seasons.get(0).getIdSeason());
    }

    @FXML
    private void handleSeasonChange(ActionEvent event) {
        int idx = seasonComboBox.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < seasons.size()) {
            loadEpisodes(seasons.get(idx).getIdSeason());
        }
    }

    private void loadEpisodes(int seasonId) {
        episodesContainer.getChildren().clear();
        List<Episode> episodes = EpisodeDAO.getEpisodesBySeason(seasonId);
        for (Episode ep : episodes) {
            VBox card = new VBox(6);
            card.setAlignment(Pos.TOP_LEFT);
            card.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 8; -fx-cursor: hand;");
            card.setPrefWidth(160);

            Label numLbl = new Label(String.valueOf(ep.getEpisodeNumber()));
            numLbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 18px; -fx-min-width: 30;");

            StackPane thumbBox = new StackPane();
            ImageView thumb = new ImageView();
            thumb.setFitWidth(160);
            thumb.setFitHeight(90);
            thumb.setPreserveRatio(false);
            if (ep.getThumbnailPath() != null && !ep.getThumbnailPath().isBlank()) {
                thumb.setImage(new Image(ep.getThumbnailPath(), true));
            }
            Rectangle clip = new Rectangle(160, 90);
            clip.setArcWidth(8); clip.setArcHeight(8);
            thumb.setClip(clip);
            thumbBox.getChildren().add(thumb);

            Label epTitle = new Label("Ep " + ep.getEpisodeNumber() + " · " + ep.getTitle());
            epTitle.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
            epTitle.setWrapText(true);
            epTitle.setMaxWidth(145);

            Label epDesc = new Label(ep.getDescription() != null ? ep.getDescription() : "");
            epDesc.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");
            epDesc.setWrapText(true);
            epDesc.setMaxWidth(145);

            card.getChildren().addAll(thumb, epTitle, epDesc);

            card.setOnMouseClicked(e -> {
                TransferData.setEpisode(ep);
                SceneSwitcher.goTo(e, "/org/Views/VideoPlayer.fxml");
            });
            card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #2a2a2a; -fx-padding: 8; -fx-cursor: hand;"));
            card.setOnMouseExited(e ->  card.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 8; -fx-cursor: hand;"));

            episodesContainer.getChildren().add(card);
        }
    }

    // ── Tab switching ─────────────────────────────────────────────────────────
    @FXML
    private void handleTabMoreLikeThis(ActionEvent event) {
        tabMoreLikeThis.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white;" +
                        "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand;" +
                        "-fx-border-color: #e50914; -fx-border-width: 0 0 3 0;" +
                        "-fx-padding: 10 20 10 0;");
        tabComments.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #aaa;" +
                        "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand;" +
                        "-fx-border-color: transparent; -fx-border-width: 0 0 3 0;" +
                        "-fx-padding: 10 20 10 20;");
        panelMoreLikeThis.setVisible(true);
        panelMoreLikeThis.setManaged(true);
        panelComments.setVisible(false);
        panelComments.setManaged(false);
    }

    @FXML
    private void handleTabComments(ActionEvent event) {
        tabComments.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white;" +
                        "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand;" +
                        "-fx-border-color: #e50914; -fx-border-width: 0 0 3 0;" +
                        "-fx-padding: 10 20 10 20;");
        tabMoreLikeThis.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #aaa;" +
                        "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand;" +
                        "-fx-border-color: transparent; -fx-border-width: 0 0 3 0;" +
                        "-fx-padding: 10 20 10 0;");
        panelComments.setVisible(true);
        panelComments.setManaged(true);
        panelMoreLikeThis.setVisible(false);
        panelMoreLikeThis.setManaged(false);
    }

    private void loadRelatedMedia() {
        relatedGrid.getChildren().clear();
        List<Media> recommendations = new java.util.ArrayList<>();

        if (media.getGenres() != null && !media.getGenres().isEmpty()) {
            for (Genre g : media.getGenres()) {
                if (g.getName() == null) continue; // ← FIX
                String genreStr = g.getName().toString();
                List<Movie> byGenre = MovieDAO.findbyGenre(genreStr);
                for (Movie m : byGenre) {
                    if (m.getIdMedia() != media.getIdMedia() && !recommendations.contains(m))
                        recommendations.add(m);
                }

                List<Serie> seriesByGenre = SerieDAO.findbyGenre(genreStr);
                for (Serie s : seriesByGenre) {
                    if (s.getIdMedia() != media.getIdMedia() && !recommendations.contains(s))
                        recommendations.add(s);
                }
                if (recommendations.size() >= 12) break;
            }
        }

        // Fallback
        if (recommendations.isEmpty()) {
            List<Movie> all = MovieDAO.getAllMovies();
            for (Movie m : all) {
                if (m.getIdMedia() != media.getIdMedia()) recommendations.add(m);
                if (recommendations.size() >= 12) break;
            }
        }

        for (Media m : recommendations) {
            VBox card = new VBox(6);
            card.setAlignment(Pos.TOP_LEFT);
            card.setCursor(Cursor.HAND);

            ImageView poster = new ImageView(new Image(m.getCoverImageUrl(), true));
            poster.setFitWidth(145);
            poster.setFitHeight(210);
            poster.setPreserveRatio(false);

            Rectangle clip = new Rectangle(145, 210);
            clip.setArcWidth(8); clip.setArcHeight(8);
            poster.setClip(clip);

            Label titleLbl = new Label(m.getTitle());
            titleLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
            titleLbl.setMaxWidth(145);
            titleLbl.setWrapText(true);

            HBox genreTags = new HBox(4);
            if (m.getGenres() != null) {
                for (int i = 0; i < Math.min(2, m.getGenres().size()); i++) {
                    MediaGenre genreName = m.getGenres().get(i).getName(); // ← FIX
                    if (genreName == null) continue;                        // ← FIX
                    Label tag = new Label(genreName.toString());
                    tag.setStyle("-fx-background-color: #333; -fx-text-fill: #aaa;" +
                            "-fx-font-size: 9px; -fx-padding: 2 5 2 5;");
                    genreTags.getChildren().add(tag);
                }
            }

            card.getChildren().addAll(poster, titleLbl, genreTags);
            card.setOnMouseEntered(e -> poster.setOpacity(0.75));
            card.setOnMouseExited(e ->  poster.setOpacity(1.0));

            final Media chosen = m;
            card.setOnMouseClicked(e -> {
                TransferData.setMedia(chosen);
                SceneSwitcher.goTo(e, "/org/Views/MediaDetails.fxml");
            });

            relatedGrid.getChildren().add(card);
        }
    }

    // ── Comments Section ──
    private void loadComments() {
        commentsListContainer.getChildren().clear();
        List<CommentDAO.CommentDTO> comments = CommentDAO.getCommentsByMedia(media.getIdMedia());
        User currentUser = Session.getUser();

        for (CommentDAO.CommentDTO dto : comments) {
            VBox bubble = new VBox(5);
            bubble.getStyleClass().add("comment-bubble");

            HBox header = new HBox(8);
            header.setAlignment(Pos.CENTER_LEFT);

            Label userLabel = new Label(dto.username);
            userLabel.setStyle("-fx-text-fill: #e50914; -fx-font-weight: bold;");

            Label dateLabel = new Label(dto.comment.getCreated_at() != null
                    ? dto.comment.getCreated_at().toString() : "");
            dateLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 10px;");

            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            Button reportBtn = new Button();
            boolean isReported = dto.comment.getIs_reported() == 1;

            if (isReported) {
                reportBtn.setText("🚨 Reported");
                reportBtn.setStyle("-fx-background-color: #3a0a0a; -fx-text-fill: #e50914; -fx-font-size: 10px; -fx-padding: 2 8 2 8; -fx-background-radius: 4; -fx-border-color: transparent;");

                if (currentUser.getRole().equalsIgnoreCase("admin")) {
                    reportBtn.setCursor(Cursor.HAND);
                    reportBtn.setOnAction(e -> handleUnreportComment(dto.comment.getId_Comment()));
                    reportBtn.setOnMouseEntered(e -> reportBtn.setText("Undo Report?"));
                    reportBtn.setOnMouseExited(e -> reportBtn.setText("🚨 Reported"));
                } else {
                    reportBtn.setDisable(true);
                }
            } else {
                reportBtn.setText("🚩 Report");
                reportBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-font-size: 10px; -fx-padding: 2 8 2 8; -fx-cursor: hand; -fx-border-color: #333; -fx-background-radius: 4;");
                reportBtn.setOnAction(e -> handleReportComment(dto.comment.getId_Comment()));
            }

            header.getChildren().addAll(userLabel, dateLabel, spacer, reportBtn);

            if (currentUser.getRole().equalsIgnoreCase("admin") || currentUser.getId() == dto.comment.getId_User()) {
                Button deleteBtn = new Button("🗑 Delete");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #888; -fx-font-size: 10px; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> handleDeleteComment(dto.comment.getId_Comment()));
                header.getChildren().add(deleteBtn);
            }

            Label contentLabel = new Label(dto.comment.getContent());
            contentLabel.setStyle("-fx-text-fill: white;");
            contentLabel.setWrapText(true);

            bubble.getChildren().addAll(header, contentLabel);
            commentsListContainer.getChildren().add(bubble);
        }
    }

    private void handleReportComment(int commentId) {
        if (CommentDAO.reportComment(commentId)) {
            loadComments();
        }
    }

    private void handleUnreportComment(int commentId) {
        if (CommentDAO.UnreportComment(commentId)) { // Make sure this method exists in your DAO!
            loadComments();
        }
    }

    private void handleDeleteComment(int commentId) {
        if (CommentDAO.deleteComment(commentId)) {
            loadComments();
        }
    }

    @FXML
    public void handlePublishComment(ActionEvent event) {
        String txt = newCommentField.getText().trim();
        if (!txt.isEmpty()) {
            Comment c = new Comment(0, Session.getUser().getId(), media.getIdMedia(),
                    txt, LocalDate.now(), 0);
            if (CommentDAO.addComment(c)) {
                newCommentField.clear();
                loadComments();
            }
        }
    }

    @FXML private void handleBack(ActionEvent e) { SceneSwitcher.goTo(e, "/org/Views/main.fxml"); }
    @FXML private void handlePlay(ActionEvent e) { SceneSwitcher.goTo(e, "/org/Views/VideoPlayer.fxml"); }

    // ── Rating ────────────────────────────────────────────────────────────────
    @FXML
    private void handleRate(MouseEvent event) {
        int rating = Integer.parseInt((String) ((Label) event.getSource()).getUserData());
        currentRating = rating;
        fillStars(rating);
        MediaDAO.saveRating(Session.getUser().getId(), media.getIdMedia(), rating);
    }

    private void fillStars(int count) {
        for (int i = 0; i < stars.size(); i++) {
            stars.get(i).setStyle(i < count ? "-fx-text-fill: #e50914;" : "-fx-text-fill: #555;");
        }
    }

    private void setupStarHover() {
        for (Label s : stars) {
            int idx = stars.indexOf(s) + 1;
            s.setOnMouseEntered(e -> fillStars(idx));
            s.setOnMouseExited(e ->  fillStars(currentRating));
        }
    }

    private void updateButtonUI(User u, Media m) {
        mylistbtn.setText(UserDAO.isFavorite(u.getId(), m.getIdMedia()) ? "✓ In My List" : "+ My List");
    }

    public void handleAddToMyList(ActionEvent actionEvent) {
        User u = Session.getUser();
        if (UserDAO.isFavorite(u.getId(), media.getIdMedia())) {
            MediaDAO.removeFromFavorites(u.getId(), media.getIdMedia());
        } else {
            MediaDAO.addToFavorites(u.getId(), media.getIdMedia());
        }
        updateButtonUI(u, media);
    }
}