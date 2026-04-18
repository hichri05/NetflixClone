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
import java.util.*;

public class MediaDetailsController {

    // ── Hero ──────────────────────────────────────────────────────────────────
    @FXML private ImageView  backgroundImage;
    @FXML private Label      titleLabel, descriptionLabel, ratingLabel;
    @FXML private Label      typeBadgeLabel, yearLabel, ratingAvgLabel, durationLabel;
    @FXML private Label      star1, star2, star3, star4, star5;
    @FXML private ScrollPane mainScroll;
    @FXML private Button     mylistbtn, playbtn;

    // ── Cast ──────────────────────────────────────────────────────────────────
    @FXML private HBox castingContainer;

    // ── Season / Episode ──────────────────────────────────────────────────────
    @FXML private ComboBox<String> seasonComboBox;
    @FXML private VBox             episodesContainer;

    // ── Tabs ──────────────────────────────────────────────────────────────────
    @FXML private Button   tabMoreLikeThis, tabEpisodes, tabComments;
    @FXML private VBox     panelMoreLikeThis, panelEpisodes, panelComments;
    @FXML private FlowPane relatedGrid;

    // ── Comments ──────────────────────────────────────────────────────────────
    @FXML private VBox     commentsListContainer;
    @FXML private TextArea newCommentField;

    // ── State ─────────────────────────────────────────────────────────────────
    private List<Label> stars;
    private int   currentRating = 0;
    private Media media;
    private List<Season> seasons;

    private Map<Integer, Double> resumeMap    = new HashMap<>();
    private Set<Integer>         completedSet = new HashSet<>();

    private Episode smartResumeEpisode     = null;
    private int     smartResumeSeasonIndex = 0;

    // Tab style constants
    private static final String TAB_ACTIVE_FIRST =
            "-fx-background-color: transparent; -fx-text-fill: white;" +
                    "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand;" +
                    "-fx-border-color: #e50914; -fx-border-width: 0 0 3 0; -fx-padding: 10 20 10 0;";
    private static final String TAB_INACTIVE_FIRST =
            "-fx-background-color: transparent; -fx-text-fill: #aaa;" +
                    "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand;" +
                    "-fx-border-color: transparent; -fx-border-width: 0 0 3 0; -fx-padding: 10 20 10 0;";
    private static final String TAB_ACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: white;" +
                    "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand;" +
                    "-fx-border-color: #e50914; -fx-border-width: 0 0 3 0; -fx-padding: 10 20 10 20;";
    private static final String TAB_INACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: #aaa;" +
                    "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand;" +
                    "-fx-border-color: transparent; -fx-border-width: 0 0 3 0; -fx-padding: 10 20 10 20;";

    // ─────────────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        media = TransferData.getMedia();
        User user = Session.getUser();
        stars = List.of(star1, star2, star3, star4, star5);

        mainScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        buildWatchMaps(user);
        setupHeroUI(user);
        loadCast();
        loadSeasonEpisodeBar();
        loadRelatedMedia();
        loadComments();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  WATCH HISTORY MAPS
    // ══════════════════════════════════════════════════════════════════════════

    private void buildWatchMaps(User user) {
        if (user == null) return;
        List<WatchHistory> history = new WatchHistoryDAO().findByUser(user.getId());
        for (WatchHistory wh : history) {
            if (wh.getEpisodeId() == null) continue;
            if (wh.getCompleted() == 1) {
                completedSet.add(wh.getEpisodeId());
            } else if (wh.getStoppedAtTime() > 5.0) {
                resumeMap.put(wh.getEpisodeId(), wh.getStoppedAtTime());
            }
        }
    }

    private void resolveSmartResume(List<Season> seasons) {
        for (int si = 0; si < seasons.size(); si++) {
            List<Episode> eps = EpisodeDAO.getEpisodesBySeason(seasons.get(si).getIdSeason());
            for (Episode ep : eps) {
                if (!completedSet.contains(ep.getId())) {
                    smartResumeEpisode     = ep;
                    smartResumeSeasonIndex = si;
                    return;
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HERO UI
    // ══════════════════════════════════════════════════════════════════════════

    private void setupHeroUI(User user) {
        titleLabel.setText(media.getTitle());
        descriptionLabel.setText(media.getDescription());

        String bdUrl = media.getBackdropImageUrl();
        if (bdUrl != null && !bdUrl.isBlank()) {
            backgroundImage.setImage(new Image(bdUrl, true));
        }
        backgroundImage.fitWidthProperty().bind(mainScroll.widthProperty().multiply(0.99));

        boolean isSerie = "serie".equalsIgnoreCase(media.getType()) ||
                "series".equalsIgnoreCase(media.getType());
        typeBadgeLabel.setText(isSerie ? "SERIES" : "MOVIE");
        typeBadgeLabel.setStyle(typeBadgeLabel.getStyle() +
                (isSerie ? "-fx-background-color: #1a76d2;" : "-fx-background-color: #e50914;"));

        if (media.getReleaseYear() > 0)
            yearLabel.setText(String.valueOf(media.getReleaseYear()));

        double avg = media.getAverageRating();
        if (avg > 0)
            ratingAvgLabel.setText(String.format("★ %.1f", avg));

        if (user != null) {
            int saved = MediaDAO.getRating(user.getId(), media.getIdMedia());
            if (saved > 0) { currentRating = saved; fillStars(saved); }
        }

        updateMyListButton(user, media);
        setupStarHover();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CAST
    // ══════════════════════════════════════════════════════════════════════════

    private void loadCast() {
        castingContainer.getChildren().clear();
        List<Acteur> acteurs = ActeurDAO.getActeursByMedia(media.getIdMedia());
        for (Acteur a : acteurs) {
            VBox box = new VBox(8);
            box.setAlignment(Pos.CENTER);

            ImageView img = new ImageView();
            try {
                if (a.getActeurImageUrl() != null && !a.getActeurImageUrl().isBlank())
                    img.setImage(new Image(a.getActeurImageUrl(), true));
            } catch (Exception ignored) {}
            img.setFitWidth(80); img.setFitHeight(80);
            img.setClip(new Circle(40, 40, 40));

            Label name = new Label(a.getNom());
            name.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
            name.setMaxWidth(90);
            name.setWrapText(true);
            name.setAlignment(Pos.CENTER);

            box.getChildren().addAll(img, name);
            castingContainer.getChildren().add(box);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SEASON / EPISODE BAR
    // ══════════════════════════════════════════════════════════════════════════

    private void loadSeasonEpisodeBar() {
        boolean isSerie = (media instanceof Serie) ||
                "serie".equalsIgnoreCase(media.getType()) ||
                "series".equalsIgnoreCase(media.getType());
        if (!isSerie) return;

        seasons = SeasonDAO.getSeasonsBySerie(media.getIdMedia());
        if (seasons == null || seasons.isEmpty()) return;

        // Show the Episodes tab only for series
        tabEpisodes.setVisible(true);
        tabEpisodes.setManaged(true);

        resolveSmartResume(seasons);

        for (Season s : seasons) {
            String label = (s.getTitle() != null && !s.getTitle().isBlank())
                    ? s.getTitle() : "Season " + s.getSeasonNumber();
            seasonComboBox.getItems().add(label);
        }

        seasonComboBox.getSelectionModel().select(smartResumeSeasonIndex);
        loadEpisodes(seasons.get(smartResumeSeasonIndex).getIdSeason());
    }

    @FXML
    private void handleSeasonChange(ActionEvent event) {
        int idx = seasonComboBox.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && seasons != null && idx < seasons.size()) {
            loadEpisodes(seasons.get(idx).getIdSeason());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  EPISODE CARDS
    // ══════════════════════════════════════════════════════════════════════════

    private void loadEpisodes(int seasonId) {
        episodesContainer.getChildren().clear();
        List<Episode> episodes = EpisodeDAO.getEpisodesBySeason(seasonId);

        for (Episode ep : episodes) {
            boolean isCompleted   = completedSet.contains(ep.getId());
            boolean isInProgress  = resumeMap.containsKey(ep.getId());
            boolean isSmartResume = smartResumeEpisode != null
                    && ep.getId() == smartResumeEpisode.getId();

            HBox row = new HBox(18);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setCursor(Cursor.HAND);

            String defaultStyle = isSmartResume
                    ? "-fx-background-color: #1e1212; -fx-padding: 14; -fx-cursor: hand;" +
                    "-fx-border-color: #e50914; -fx-border-width: 0 0 0 3;" +
                    "-fx-background-radius: 6;"
                    : "-fx-background-color: #1a1a1a; -fx-padding: 14; -fx-cursor: hand;" +
                    "-fx-border-color: #2a2a2a; -fx-border-width: 0 0 1 0;" +
                    "-fx-background-radius: 0;";
            String hoverStyle = isSmartResume
                    ? "-fx-background-color: #2a1515; -fx-padding: 14; -fx-cursor: hand;" +
                    "-fx-border-color: #e50914; -fx-border-width: 0 0 0 3; -fx-background-radius: 6;"
                    : "-fx-background-color: #222222; -fx-padding: 14; -fx-cursor: hand;" +
                    "-fx-border-color: #333; -fx-border-width: 0 0 1 0; -fx-background-radius: 0;";

            row.setStyle(defaultStyle);
            row.setOnMouseEntered(e -> row.setStyle(hoverStyle));
            row.setOnMouseExited(e  -> row.setStyle(defaultStyle));

            Label numLbl = new Label(String.format("%02d", ep.getEpisodeNumber()));
            numLbl.setStyle("-fx-text-fill: #555; -fx-font-size: 20px; -fx-font-weight: bold; -fx-min-width: 36;");

            StackPane thumbBox = new StackPane();
            thumbBox.setMinWidth(180); thumbBox.setMaxWidth(180);

            ImageView thumb = new ImageView();
            thumb.setFitWidth(180); thumb.setFitHeight(101);
            thumb.setPreserveRatio(false);
            if (ep.getThumbnailPath() != null && !ep.getThumbnailPath().isBlank()) {
                try { thumb.setImage(new Image(ep.getThumbnailPath(), true)); }
                catch (Exception ignored) {}
            } else {
                thumb.setStyle("-fx-background-color: #2a2a2a;");
            }
            Rectangle thumbClip = new Rectangle(180, 101);
            thumbClip.setArcWidth(6); thumbClip.setArcHeight(6);
            thumb.setClip(thumbClip);

            if (isInProgress) {
                double stopped  = resumeMap.get(ep.getId());
                double duration = ep.getDuration() * 60.0;
                double ratio    = duration > 0 ? Math.min(stopped / duration, 1.0) : 0.5;

                ProgressBar progBar = new ProgressBar(ratio);
                progBar.setPrefWidth(180); progBar.setPrefHeight(3);
                progBar.setStyle("-fx-accent: #e50914; -fx-background-color: rgba(0,0,0,0.5);");
                StackPane.setAlignment(progBar, Pos.BOTTOM_CENTER);
                thumbBox.getChildren().addAll(thumb, progBar);
            } else {
                thumbBox.getChildren().add(thumb);
            }

            VBox info = new VBox(6);
            info.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(info, Priority.ALWAYS);

            HBox titleLine = new HBox(12);
            titleLine.setAlignment(Pos.BOTTOM_LEFT);

            Label titleLbl = new Label(ep.getTitle() != null ? ep.getTitle() : "Episode " + ep.getEpisodeNumber());
            titleLbl.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");

            Label durLbl = new Label(ep.getDuration() > 0 ? ep.getDuration() + " min" : "");
            durLbl.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

            titleLine.getChildren().addAll(titleLbl, durLbl);

            Label descLbl = new Label(ep.getDescription() != null && !ep.getDescription().isBlank()
                    ? ep.getDescription() : "No summary available.");
            descLbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 13px;");
            descLbl.setWrapText(true);
            descLbl.setMaxWidth(550);

            info.getChildren().addAll(titleLine, descLbl);

            VBox badgeCol = new VBox(6);
            badgeCol.setAlignment(Pos.CENTER_RIGHT);
            badgeCol.setMinWidth(100);

            if (isCompleted) {
                Label vuBadge = new Label("✓ Vu");
                vuBadge.setStyle("-fx-background-color: #0d2b16; -fx-text-fill: #46d369;" +
                        "-fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-padding: 4 10 4 10; -fx-background-radius: 12;");
                badgeCol.getChildren().add(vuBadge);

            } else if (isInProgress) {
                Label progressBadge = new Label("▶ En cours");
                progressBadge.setStyle("-fx-background-color: #2a1a00; -fx-text-fill: #f5a623;" +
                        "-fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-padding: 4 10 4 10; -fx-background-radius: 12;");

                double stopped  = resumeMap.get(ep.getId());
                int remainSecs  = (int) Math.max(0, ep.getDuration() * 60 - stopped);
                int remainMin   = remainSecs / 60;
                Label timeLbl   = new Label(remainMin + " min left");
                timeLbl.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");
                badgeCol.getChildren().addAll(progressBadge, timeLbl);

            } else if (isSmartResume) {
                Label startBadge = new Label("▶ Start here");
                startBadge.setStyle("-fx-background-color: #3a0a0a; -fx-text-fill: #e50914;" +
                        "-fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-padding: 4 10 4 10; -fx-background-radius: 12;");
                badgeCol.getChildren().add(startBadge);
            }

            row.getChildren().addAll(numLbl, thumbBox, info, badgeCol);

            row.setOnMouseClicked(e -> {
                TransferData.setEpisode(ep);
                SceneSwitcher.goTo(e, "/org/Views/VideoPlayer.fxml");
            });

            episodesContainer.getChildren().add(row);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TABS
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleTabMoreLikeThis(ActionEvent event) {
        tabMoreLikeThis.setStyle(TAB_ACTIVE_FIRST);
        tabEpisodes.setStyle(TAB_INACTIVE);
        tabComments.setStyle(TAB_INACTIVE);
        panelMoreLikeThis.setVisible(true);  panelMoreLikeThis.setManaged(true);
        panelEpisodes.setVisible(false);     panelEpisodes.setManaged(false);
        panelComments.setVisible(false);     panelComments.setManaged(false);
    }

    @FXML
    private void handleTabEpisodes(ActionEvent event) {
        tabMoreLikeThis.setStyle(TAB_INACTIVE_FIRST);
        tabEpisodes.setStyle(TAB_ACTIVE);
        tabComments.setStyle(TAB_INACTIVE);
        panelMoreLikeThis.setVisible(false); panelMoreLikeThis.setManaged(false);
        panelEpisodes.setVisible(true);      panelEpisodes.setManaged(true);
        panelComments.setVisible(false);     panelComments.setManaged(false);
    }

    @FXML
    private void handleTabComments(ActionEvent event) {
        tabMoreLikeThis.setStyle(TAB_INACTIVE_FIRST);
        tabEpisodes.setStyle(TAB_INACTIVE);
        tabComments.setStyle(TAB_ACTIVE);
        panelMoreLikeThis.setVisible(false); panelMoreLikeThis.setManaged(false);
        panelEpisodes.setVisible(false);     panelEpisodes.setManaged(false);
        panelComments.setVisible(true);      panelComments.setManaged(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MORE LIKE THIS
    // ══════════════════════════════════════════════════════════════════════════

    private void loadRelatedMedia() {
        relatedGrid.getChildren().clear();
        List<Media> recommendations = new ArrayList<>();

        if (media.getGenres() != null) {
            for (Genre g : media.getGenres()) {
                if (g.getName() == null) continue;
                String genreStr = g.getName().toString();
                for (Movie m : MovieDAO.findbyGenre(genreStr))
                    if (m.getIdMedia() != media.getIdMedia() && !recommendations.contains(m))
                        recommendations.add(m);
                for (Serie s : SerieDAO.findbyGenre(genreStr))
                    if (s.getIdMedia() != media.getIdMedia() && !recommendations.contains(s))
                        recommendations.add(s);
                if (recommendations.size() >= 12) break;
            }
        }

        if (recommendations.isEmpty()) {
            for (Movie m : MovieDAO.getAllMovies()) {
                if (m.getIdMedia() != media.getIdMedia()) recommendations.add(m);
                if (recommendations.size() >= 12) break;
            }
        }

        for (Media m : recommendations.stream().limit(12).toList()) {
            VBox card = new VBox(6);
            card.setCursor(Cursor.HAND);

            ImageView poster = new ImageView();
            try {
                if (m.getCoverImageUrl() != null)
                    poster.setImage(new Image(m.getCoverImageUrl(), true));
            } catch (Exception ignored) {}
            poster.setFitWidth(145); poster.setFitHeight(210);
            poster.setPreserveRatio(false);
            Rectangle clip = new Rectangle(145, 210);
            clip.setArcWidth(8); clip.setArcHeight(8);
            poster.setClip(clip);

            Label titleLbl = new Label(m.getTitle());
            titleLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
            titleLbl.setMaxWidth(145); titleLbl.setWrapText(true);

            card.getChildren().addAll(poster, titleLbl);
            card.setOnMouseEntered(e -> poster.setOpacity(0.75));
            card.setOnMouseExited(e  -> poster.setOpacity(1.0));

            final Media chosen = m;
            card.setOnMouseClicked(e -> {
                TransferData.setMedia(chosen);
                SceneSwitcher.goTo(e, "/org/Views/MediaDetails.fxml");
            });
            relatedGrid.getChildren().add(card);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  COMMENTS
    // ══════════════════════════════════════════════════════════════════════════


    private void loadComments() {
        commentsListContainer.getChildren().clear();
        List<CommentDAO.CommentDTO> comments = CommentDAO.getCommentsByMedia(media.getIdMedia());
        User currentUser = Session.getUser();

        for (CommentDAO.CommentDTO dto : comments) {
            VBox bubble = new VBox(5);
            bubble.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 12; -fx-background-radius: 8;");

            HBox header = new HBox(8);
            header.setAlignment(Pos.CENTER_LEFT);


            Label avatar = new Label(dto.username.isEmpty() ? "?" : String.valueOf(dto.username.charAt(0)).toUpperCase());
            avatar.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-min-width: 28; -fx-min-height: 28; -fx-background-radius: 14; -fx-alignment: center;");

            Label userLbl = new Label(dto.username);
            userLbl.setStyle("-fx-text-fill: #e50914; -fx-font-weight: bold;");

            Label dateLbl = new Label(dto.comment.getCreated_at() != null ? " · " + dto.comment.getCreated_at() : "");
            dateLbl.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);


            boolean alreadyReported = dto.comment.getIs_reported() == 1;
            Button reportBtn = new Button(alreadyReported ? "🚨 Unreport" : "🚩 Report");


            reportBtn.setStyle(alreadyReported
                    ? "-fx-background-color: transparent; -fx-text-fill: #e50914; -fx-font-size: 10px; -fx-border-color: #e50914; -fx-background-radius: 3; -fx-cursor: hand;"
                    : "-fx-background-color: transparent; -fx-text-fill: #555; -fx-font-size: 10px; -fx-border-color: #333; -fx-background-radius: 3; -fx-cursor: hand;");

            final int cId = dto.comment.getId_Comment();

            reportBtn.setOnAction(e -> {
                if (alreadyReported) {

                    if (CommentDAO.UnreportComment(cId)) loadComments();
                } else {

                    if (CommentDAO.reportComment(cId)) loadComments();
                }
            });

            header.getChildren().addAll(avatar, userLbl, dateLbl, spacer, reportBtn);

            // --- Option: Delete (Admin ou Propriétaire) ---
            if (currentUser != null && ("ADMIN".equalsIgnoreCase(currentUser.getRole()) || currentUser.getId() == dto.comment.getId_User())) {
                Button delBtn = new Button("🗑");
                delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-font-size: 12px; -fx-cursor: hand;");
                delBtn.setOnAction(e -> { if (CommentDAO.deleteComment(cId)) loadComments(); });
                header.getChildren().add(delBtn);
            }


            Label content = new Label(dto.comment.getContent());
            content.setStyle("-fx-text-fill: #e5e5e5; -fx-font-size: 13px;");
            content.setWrapText(true);

            bubble.getChildren().addAll(header, content);
            commentsListContainer.getChildren().add(bubble);
        }
    }
    @FXML
    public void handlePublishComment(ActionEvent event) {
        String txt = newCommentField.getText().trim();
        if (txt.isEmpty()) return;
        Comment c = new Comment(0, Session.getUser().getId(), media.getIdMedia(),
                txt, LocalDate.now(), 0);
        if (CommentDAO.addComment(c)) {
            newCommentField.clear();
            loadComments();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  NAVIGATION & ACTIONS
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleBack(ActionEvent e) {
        SceneSwitcher.goTo(e, "/org/Views/main.fxml");
    }

    @FXML
    private void handlePlay(ActionEvent e) {
        if (smartResumeEpisode != null) {
            TransferData.setEpisode(smartResumeEpisode);
        } else {
            TransferData.setEpisode(null);
        }
        SceneSwitcher.goTo(e, "/org/Views/VideoPlayer.fxml");
    }

    // ── Rating ────────────────────────────────────────────────────────────────
    @FXML
    private void handleRate(MouseEvent event) {
        int rating = Integer.parseInt((String) ((Label) event.getSource()).getUserData());
        currentRating = rating;
        fillStars(rating);
        MediaDAO.saveRating(Session.getUser().getId(), media.getIdMedia(), rating);
    }

    private void fillStars(int count) {
        for (int i = 0; i < stars.size(); i++)
            stars.get(i).setStyle(i < count ? "-fx-text-fill: #e50914;" : "-fx-text-fill: #555;");
    }

    private void setupStarHover() {
        for (Label s : stars) {
            int idx = stars.indexOf(s) + 1;
            s.setOnMouseEntered(e -> fillStars(idx));
            s.setOnMouseExited(e  -> fillStars(currentRating));
        }
    }

    // ── My List ───────────────────────────────────────────────────────────────
    private void updateMyListButton(User u, Media m) {
        if (u == null) return;
        mylistbtn.setText(UserDAO.isFavorite(u.getId(), m.getIdMedia())
                ? "✓ In My List" : "+ My List");
    }

    @FXML
    public void handleAddToMyList(ActionEvent event) {
        User u = Session.getUser();
        if (u == null) return;
        if (UserDAO.isFavorite(u.getId(), media.getIdMedia())) {
            MediaDAO.removeFromFavorites(u.getId(), media.getIdMedia());
        } else {
            MediaDAO.addToFavorites(u.getId(), media.getIdMedia());
        }
        updateMyListButton(u, media);
    }
}