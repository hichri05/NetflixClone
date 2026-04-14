package org.netflix.Controllers;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.netflix.DAO.MediaDAO;
import org.netflix.DAO.UserDAO;
import org.netflix.Models.Media;
import org.netflix.Models.User;
import org.netflix.Utils.Session;
import org.netflix.Utils.TransferData;

import java.io.IOException;
import java.util.List;

public class MediaPosterController {

    @FXML private StackPane rootPane;
    @FXML private ImageView posterImageView;
    @FXML private Button removeBtn;

    private Media media;
    private Popup hoverPopup;
    private PauseTransition showDelay;
    private PauseTransition hideDelay;

    // Safety placeholder
    private final String PLACEHOLDER = "https://via.placeholder.com/300x450?text=No+Image";

    @FXML
    public void initialize() {
        applyRoundedCorners();
        setupHover();
    }

    private void applyRoundedCorners() {
        Rectangle clip = new Rectangle(160, 240);
        clip.setArcWidth(15);
        clip.setArcHeight(15);
        posterImageView.setClip(clip);
    }

    // Safety helper to prevent the "Invalid URL" crash
    private Image safeLoadImage(String url) {
        if (url == null || url.trim().isEmpty()) {
            return new Image(PLACEHOLDER, true);
        }
        try {
            return new Image(url, true);
        } catch (Exception e) {
            return new Image(PLACEHOLDER, true);
        }
    }

    private void setupHover() {
        hoverPopup = new Popup();
        hoverPopup.setAutoHide(false);
        hoverPopup.setHideOnEscape(true);

        showDelay = new PauseTransition(Duration.millis(300));
        hideDelay = new PauseTransition(Duration.millis(200));
        hideDelay.setOnFinished(e -> hoverPopup.hide());

        hoverPopup.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> hideDelay.stop());
        hoverPopup.addEventFilter(MouseEvent.MOUSE_EXITED,  e -> hideDelay.playFromStart());

        rootPane.setOnMouseEntered(e -> {
            hideDelay.stop();
            showDelay.setOnFinished(ev -> {
                if (media == null) return;
                hoverPopup.getContent().setAll(buildHoverPopup());
                javafx.geometry.Point2D p = rootPane.localToScreen(0, 0);
                hoverPopup.show(rootPane, p.getX() - 70, p.getY() + rootPane.getHeight() + 5);
            });
            showDelay.playFromStart();
        });

        rootPane.setOnMouseExited(e -> {
            showDelay.stop();
            hideDelay.playFromStart();
        });
    }

    private VBox buildHoverPopup() {
        VBox preview = new VBox(10);
        preview.setStyle("-fx-background-color: #181818; -fx-background-radius: 10; " +
                "-fx-border-color: #333; -fx-border-radius: 10; -fx-padding: 0;");
        preview.setPrefWidth(300);

        // Backdrop image (Safely loaded)
        ImageView img = new ImageView(safeLoadImage(media.getBackDropImageUrl()));
        img.setFitWidth(300);
        img.setFitHeight(160);
        img.setPreserveRatio(false);

        // Info section
        VBox info = new VBox(8);
        info.setPadding(new Insets(15));

        int match = 80 + (Math.abs(media.getTitle().hashCode()) % 19);
        Label matchLbl = new Label(match + "% Match");
        matchLbl.setStyle("-fx-text-fill: #46d369; -fx-font-weight: bold; -fx-font-size: 14;");

        // Buttons
        String outlineBtn = "-fx-background-color: transparent; -fx-border-color: white; " +
                "-fx-border-radius: 50; -fx-text-fill: white; -fx-cursor: hand;";

        Button playBtn = new Button("▶");
        playBtn.setStyle("-fx-background-color: white; -fx-text-fill: black; " +
                "-fx-background-radius: 50; -fx-padding: 5 12; -fx-cursor: hand;");
        playBtn.setOnAction(e -> handlePosterClick(null));

        Button addBtn  = new Button("+"); addBtn.setStyle(outlineBtn);
        Button likeBtn = new Button("♥"); likeBtn.setStyle(outlineBtn);

        addBtn.setOnAction(e -> {
            User user = Session.getUser();
            if (user != null) MediaDAO.addToFavorites(user.getId(), media.getIdMedia());
        });

        HBox buttons = new HBox(10, playBtn, addBtn, likeBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Label titleLbl = new Label(media.getTitle());
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18;");

        // Description Restored
        Label desc = new Label(media.getDescription());
        desc.setStyle("-fx-text-fill: #d2d2d2; -fx-font-size: 12;");
        desc.setWrapText(true);
        desc.setMaxWidth(270);
        desc.setMaxHeight(60);

        info.getChildren().addAll(matchLbl, buttons, titleLbl, desc);
        preview.getChildren().addAll(img, info);
        return preview;
    }

    public void setData(Media media) {
        if (media == null) return;
        this.media = media;

        // Poster (Safely loaded)
        posterImageView.setImage(safeLoadImage(media.getCoverImageUrl()));

        rootPane.getChildren().removeIf(n -> "typeBadge".equals(n.getId()));
        Label badge = new Label(media.getType());
        badge.setId("typeBadge");
        badge.getStyleClass().add("type-badge");
        badge.getStyleClass().add("Movie".equalsIgnoreCase(media.getType()) ? "type-badge-film" : "type-badge-serie");
        StackPane.setAlignment(badge, Pos.TOP_LEFT);
        StackPane.setMargin(badge, new Insets(10, 0, 0, 10));
        rootPane.getChildren().add(badge);
    }

    public void showRemoveButton(boolean show) {
        removeBtn.setVisible(show);
        removeBtn.setManaged(show);
    }

    @FXML
    private void handlePosterClick(MouseEvent event) {
        if (media == null) return;
        if (hoverPopup != null) hoverPopup.hide();
        TransferData.setMedia(media);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/Views/MediaDetails.fxml"));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRemoveFromList(ActionEvent event) {
        if (media == null) return;
        User user = Session.getUser();
        MediaDAO.removeFromFavorites(user.getId(), media.getIdMedia());
        rootPane.setOpacity(0.5);
    }
}