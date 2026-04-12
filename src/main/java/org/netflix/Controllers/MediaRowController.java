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
        @FXML private Label rowTitleLabel;
    @FXML private HBox  posterContainer;
        private final MediaServiceImpl mediaService =
                new MediaServiceImpl(new MediaDAO(), new RatingDAO());

        public void initData(String genre) {
            rowTitleLabel.setText(genre);

            List<Media> mediaList = mediaService.filterByGenre(genre);

            loadPosters(mediaList);
        }

        public void initFeatured() {
            rowTitleLabel.setText("À la une");

            List<Media> featured = mediaService.getFeaturedMedia();

            loadPosters(featured);
        }
        public void initTop5() {
            rowTitleLabel.setText("Les plus regardés");

            List<Media> top5 = mediaService.getTop5MostWatched();

            loadPosters(top5);
        }
        private void loadPosters(List<Media> mediaList) {
            posterContainer.getChildren().clear();

            for (Media media : mediaList) {
                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/org/netflix/Views/MediaPoster.fxml"));
                    Node posterNode = loader.load();

                    MediaPosterController pc = loader.getController();
                    pc.initData(media);

                    posterContainer.getChildren().add(posterNode);

                } catch (Exception e) {
                    System.err.println("Erreur chargement poster : " + e.getMessage());
                }
            }
        }
    }
