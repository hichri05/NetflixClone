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
import org.netflix.DAO.*;
import org.netflix.Models.*;
import org.netflix.Utils.*;

import java.time.LocalDate;
import java.util.List;

public class MediaDetailsController {

    @FXML private ImageView backgroundImage;
    @FXML private Label titleLabel, descriptionLabel, ratingLabel, star1, star2, star3, star4, star5;
    @FXML private ScrollPane mainScroll;
    @FXML private Button mylistbtn;
    @FXML private HBox castingContainer;
    @FXML private FlowPane relatedGrid;
    @FXML private VBox commentsListContainer;
    @FXML private TextArea newCommentField;

    private List<Label> stars;
    private int currentRating = 0;
    private Media media;

    @FXML
    public void initialize() {
        media = TransferData.getMedia();
        User user = Session.getUser();
        stars = List.of(star1, star2, star3, star4, star5);

        mainScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setupUI(user);
        loadCast();
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
            img.setClip(new Circle(40, 40, 40)); // Rend l'image ronde
            Label name = new Label(a.getNom());
            name.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
            box.getChildren().addAll(img, name);
            castingContainer.getChildren().add(box);
        }
    }

    private void loadRelatedMedia() {
        relatedGrid.getChildren().clear();
        List<Movie> related = MovieDAO.getAllMovies(); // À adapter selon ton DAO
        for (int i = 0; i < Math.min(related.size(), 6); i++) {
            Media m = related.get(i);
            ImageView poster = new ImageView(new Image(m.getCoverImageUrl(), true));
            poster.setFitWidth(145); poster.setPreserveRatio(true);
            poster.setCursor(Cursor.HAND);
            poster.setOnMouseClicked(e -> {
                TransferData.setMedia(m);
                SceneSwitcher.goTo(e, "/org/Views/MediaDetails.fxml");
            });
            relatedGrid.getChildren().add(poster);
        }
    }

    private void loadComments() {
        commentsListContainer.getChildren().clear();
        // Utilise maintenant CommentDTO
        List<CommentDAO.CommentDTO> comments = CommentDAO.getCommentsByMedia(media.getIdMedia());

        for (CommentDAO.CommentDTO dto : comments) {
            VBox bubble = new VBox(5);
            bubble.getStyleClass().add("comment-bubble");

            // ✅ Affiche le nom au lieu de l'ID
            Label userLabel = new Label(dto.username);
            userLabel.setStyle("-fx-text-fill: #e50914; -fx-font-weight: bold;");

            Label contentLabel = new Label(dto.comment.getContent());
            contentLabel.setStyle("-fx-text-fill: white;");
            contentLabel.setWrapText(true);

            bubble.getChildren().addAll(userLabel, contentLabel);
            commentsListContainer.getChildren().add(bubble);
        }
    }
    @FXML
    public void handlePublishComment(ActionEvent event) {
        String txt = newCommentField.getText().trim();
        if (!txt.isEmpty()) {
            Comment c = new Comment(0, Session.getUser().getId(), media.getIdMedia(), txt, LocalDate.now(), 0);
            if (CommentDAO.addComment(c)) {
                newCommentField.clear();
                loadComments();
            }
        }
    }

    @FXML private void handleBack(ActionEvent e) { SceneSwitcher.goTo(e, "/org/Views/main.fxml"); }
    @FXML private void handlePlay(ActionEvent e) { SceneSwitcher.goTo(e, "/org/Views/VideoPlayer.fxml"); }

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
            s.setOnMouseExited(e -> fillStars(currentRating));
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