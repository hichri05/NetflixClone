package org.netflix.Controllers;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.util.Duration;
import org.netflix.DAO.MediaDAO;
import org.netflix.DAO.UserDAO;
import org.netflix.Models.Media;
import org.netflix.Models.User;
import org.netflix.Utils.SceneSwitcher;
import org.netflix.Utils.Session;
import org.netflix.Utils.TransferData;

import java.util.List;

public class MediaPosterController {

    //
    @FXML private StackPane rootPane;
    @FXML private ImageView posterImageView;
    @FXML private Button removeBtn;

    //
    private Media media;

    // Hover
    private Popup           hoverPopup;
    private PauseTransition showDelay;
    private PauseTransition hideDelay;

    //
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

    // ── HOVER SETUP ──────────────────────────────────────────────────

    private void setupHover() {
        hoverPopup = new Popup();
        hoverPopup.setAutoHide(false);
        hoverPopup.setHideOnEscape(true);

        showDelay = new PauseTransition(Duration.millis(300));
        hideDelay = new PauseTransition(Duration.millis(200));
        hideDelay.setOnFinished(e -> hoverPopup.hide());

        // Keep popup alive when mouse is over it
        hoverPopup.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> hideDelay.stop());
        hoverPopup.addEventFilter(MouseEvent.MOUSE_EXITED,  e -> hideDelay.playFromStart());

        rootPane.setOnMouseEntered(e -> {
            hideDelay.stop();
            showDelay.setOnFinished(ev -> {
                if (media == null) return;
                hoverPopup.getContent().setAll(buildHoverPopup());
                javafx.geometry.Point2D p = rootPane.localToScreen(0, 0);
                hoverPopup.show(rootPane,
                        p.getX() - 70,
                        p.getY() + rootPane.getHeight() + 5);
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

        // Backdrop image
        String imgUrl = media.getBackDropImageUrl();
        ImageView img = new ImageView(new Image(imgUrl, true));
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
        playBtn.setOnAction(e -> {
            hoverPopup.hide();
            TransferData.setMedia(media);
            // reuse existing navigation
            rootPane.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED,
                    0, 0, 0, 0, javafx.scene.input.MouseButton.PRIMARY,
                    1, false, false, false, false,
                    true, false, false, true, false, false, null));
        });

        Button addBtn  = new Button("+"); addBtn.setStyle(outlineBtn);
        Button likeBtn = new Button("♥"); likeBtn.setStyle(outlineBtn);

        // Add to list from popup
        addBtn.setOnAction(e -> {
            User user = Session.getUser();
            if (user != null) MediaDAO.addToFavorites(user.getId(), media.getIdMedia());
        });

        HBox buttons = new HBox(10, playBtn, addBtn, likeBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Label titleLbl = new Label(media.getTitle());
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18;");

        Label desc = new Label(media.getDescription());
        desc.setStyle("-fx-text-fill: #d2d2d2; -fx-font-size: 12;");
        desc.setWrapText(true);
        desc.setMaxWidth(270);
        desc.setMaxHeight(60);

        info.getChildren().addAll(matchLbl, buttons, titleLbl, desc);
        preview.getChildren().addAll(img, info);
        return preview;
    }

    //
    public void setData(Media media) {
        if (media == null) return;
        this.media = media;
        String imageUrl = media.getCoverImageUrl();
        Image image = new Image(imageUrl, true);
        posterImageView.setImage(image);
    }

    //
    public void showRemoveButton(boolean show) {
        removeBtn.setVisible(show);
        removeBtn.setManaged(show);
    }

    //
    @FXML
    private void handlePosterClick(MouseEvent event) {
        if (media == null) return;
        TransferData.setMedia(media);
        SceneSwitcher.goTo(event, "/org/Views/MediaDetails.fxml");
    }

    @FXML
    private void handleRemoveFromList(ActionEvent event) {
        if (media == null) return;
        System.out.println("Removing: " + media.getTitle());
        User user = Session.getUser();
        MediaDAO.removeFromFavorites(user.getId(), media.getIdMedia());
        List<Media> favorites = UserDAO.getUserFavorites(user.getId());
        rootPane.getChildren().clear();
    }
}