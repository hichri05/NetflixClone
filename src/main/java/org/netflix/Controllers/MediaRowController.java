package org.netflix.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.netflix.DAO.MediaDAO;
import org.netflix.DAO.*;
import org.netflix.Models.Media;
import org.netflix.Models.Movie;
import org.netflix.Services.MediaServiceImpl;

import java.io.IOException;
import java.util.List;

public class MediaRowController {
    @FXML private Label rowTitle;
    @FXML
    private HBox movieContainer;

    public void setData(String title, List<Media> medias) {
        rowTitle.setText(title);
        for (Media media : medias) {
            loadMovieCard(media);
        }
    }

    private void loadMovieCard(Media media) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/Views/MediaPoster.fxml"));
            Parent card = loader.load();
            MediaPosterController controller = loader.getController();
            controller.setData(media);
            movieContainer.getChildren().add(card);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    }
