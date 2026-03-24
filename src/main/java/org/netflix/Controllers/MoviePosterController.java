package org.netflix.Controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.netflix.Models.Movie;

public class MoviePosterController {
    @FXML private StackPane rootPane;
    @FXML private ImageView posterImageView;

    public void setData(Movie movie) {
        String imgurl = movie.getCoverImageUrl();
        Image img = new Image(imgurl, true);
        posterImageView.setImage(img);
    }
    @FXML
    public void initialize() {
        Rectangle clip = new Rectangle(160, 240);
        clip.setArcWidth(15);
        clip.setArcHeight(15);
        posterImageView.setClip(clip);
    }
}
