package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.netflix.Models.Media;
import org.netflix.Models.Movie;
import org.netflix.Utils.SceneSwitcher;
import org.netflix.Utils.TransferData;

public class MoviePosterController {
    @FXML private StackPane rootPane;
    @FXML private ImageView posterImageView;
    @FXML private Button removeBtn;

    private Media  media;

    @FXML
    public void initialize() {
        Rectangle clip = new Rectangle(160, 240);
        clip.setArcWidth(15);
        clip.setArcHeight(15);
        posterImageView.setClip(clip);
    }
    public void setData(Media media) {
        String imgurl = media.getCoverImageUrl();
        Image img = new Image(imgurl, true);
        posterImageView.setImage(img);
        this.media = media;
    }


    public void showRemoveButton(boolean show) {
        removeBtn.setVisible(show);
        removeBtn.setManaged(show);
    }
    public void handleRemoveFromList(ActionEvent actionEvent) {
        System.out.println("Removing item from favorites...");
    }

    @FXML
    private void handlePosterClick(MouseEvent event) {
        TransferData.setMedia(media);
        SceneSwitcher.goTo(event, "/org/Views/MediaDetails.fxml");
    }
}
