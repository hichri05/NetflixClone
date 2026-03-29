package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
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
    //
    @FXML
    public void initialize() {
        applyRoundedCorners();
    }

    private void applyRoundedCorners() {
        Rectangle clip = new Rectangle(160, 240);
        clip.setArcWidth(15);
        clip.setArcHeight(15);
        posterImageView.setClip(clip);
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
        MediaDAO.removeFromFavorites(user.getId(), media.getIdMedia());
        List<Media> favorites = UserDAO.getUserFavorites(user.getId());
        //MainController.displayMyList(favorites);
    }
}