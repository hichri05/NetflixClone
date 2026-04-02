package org.netflix.Controllers;

import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.netflix.Models.Acteur;
import org.netflix.Models.Genre;
import org.netflix.Models.Movie;

public class NetflixController {

    @FXML private ImageView backgroundImage;
    @FXML private Label titleLabel;
    @FXML private Label taglineLabel;
    @FXML private Label matchLabel;
    @FXML private Label yearLabel;
    @FXML private Label durationLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label castLabel;
    @FXML private Label genreLabel;
    @FXML private Label tagsLabel;
    @FXML private Label episodeTitleLabel;
    @FXML private Label quoteLabel;
    @FXML private ProgressBar progressBar;

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/netflix/Views/FilmPage.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMovieData(Movie movie) {
        // Hero backdrop image
        try {
            String backdropUrl = movie.getBackDropImageUrl(); // from Media class
            if (backdropUrl != null && !backdropUrl.isEmpty()) {
                backgroundImage.setImage(new Image(backdropUrl, true));
            }
        } catch (Exception e) {
            System.out.println("Backdrop image error: " + e.getMessage());
        }

        // Title
        if (titleLabel != null)
            titleLabel.setText(movie.getTitle() != null ? movie.getTitle().toUpperCase() : "");

        // Tagline — empty unless your model has one
        if (taglineLabel != null)
            taglineLabel.setText(movie.getDirector() != null ? "Directed by " + movie.getDirector() : "");

        // Match score based on title hash
        if (matchLabel != null) {
            int match = 80 + (Math.abs(movie.getTitle().hashCode()) % 19);
            matchLabel.setText(match + "% Match");
        }

        // Year
        if (yearLabel != null)
            yearLabel.setText(String.valueOf(movie.getReleaseYear()));

        // Duration — Movie uses getDurationMinutes()
        if (durationLabel != null)
            durationLabel.setText(movie.getDurationMinutes() + " min");

        // Description
        if (descriptionLabel != null)
            descriptionLabel.setText(movie.getDescription() != null ? movie.getDescription() : "");

        // Genres
        if (genreLabel != null && movie.getGenres() != null) {
            String genres = movie.getGenres().stream()
                    .map(g -> g.getName().name())
                    .collect(Collectors.joining(", "));
            genreLabel.setText(genres);
        }

        // Cast
        if (castLabel != null && movie.getCasting() != null && !movie.getCasting().isEmpty()) {
            String cast = movie.getCasting().stream()
                    .map(Acteur::getNom)
                    .collect(Collectors.joining(", "));
            castLabel.setText(cast);
        } else if (castLabel != null) {
            castLabel.setText("N/A");
        }

        // Optional fields
        if (episodeTitleLabel != null) episodeTitleLabel.setText("");
        if (quoteLabel != null) quoteLabel.setText("");
        if (tagsLabel != null) tagsLabel.setText("");
        if (progressBar != null) progressBar.setProgress(0);
    }
}