package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.netflix.Models.Acteur;
import org.netflix.Models.Genre;
import org.netflix.Models.Movie;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class NetflixController implements Initializable {

    @FXML private ImageView heroImageView;
    @FXML private Label     movieTitleLabel;
    @FXML private Label     directorLabel;
    @FXML private Label     yearLabel;
    @FXML private Label     ratingLabel;
    @FXML private Label     durationLabel;
    @FXML private Label     typeLabel;
    @FXML private Label     descriptionLabel;
    @FXML private Label     castLabel;
    @FXML private Label     genreLabel;
    @FXML private Label     viewsLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Nothing to do — setMovie() is called right after load()
    }


    public void setMovie(Movie movie) {
        if (movie == null) return;

        String backdropUrl = movie.getBackDropImageUrl();
        if (backdropUrl != null && !backdropUrl.isEmpty()) {
            try { heroImageView.setImage(new Image(backdropUrl, true)); }
            catch (Exception ignored) {}
        }


        movieTitleLabel.setText(movie.getTitle().toUpperCase());


        String dir = movie.getDirector();
        directorLabel.setText("Directed by " + (dir != null && !dir.isEmpty() ? dir : "Unknown"));


        yearLabel.setText(String.valueOf(movie.getReleaseYear()));

        int match = 80 + (Math.abs(movie.getTitle().hashCode()) % 19);
        ratingLabel.setText(match + "% Match");


        int mins = movie.getDurationMinutes();
        durationLabel.setText(mins > 0 ? (mins / 60) + "h " + (mins % 60) + "m" : "—");

        typeLabel.setText("MOVIE");

        String desc = movie.getDescription();
        descriptionLabel.setText(desc != null && !desc.isEmpty() ? desc : "No synopsis available.");

        List<Acteur> casting = movie.getCasting();
        if (casting != null && !casting.isEmpty()) {
            String castStr = casting.stream()
                    .map(a -> a.getNom() + " " + a.getNom())
                    .collect(Collectors.joining(", "));
            castLabel.setText("Cast: " + castStr);
        } else {
            castLabel.setText("Cast: —");
        }

        List<Genre> genres = movie.getGenres();
        if (genres != null && !genres.isEmpty()) {
            String genreStr = genres.stream()
                    .map(Genre::toString)
                    .collect(Collectors.joining(", "));
            genreLabel.setText("Genres: " + genreStr);
        } else {
            genreLabel.setText("Genres: —");
        }

        viewsLabel.setText("Views: " + movie.getViews());
    }


    @FXML
    private void handleClose() {
        try {
            javafx.scene.Parent filmPage = FXMLLoader.load(
                    getClass().getResource("/org/Views/FilmPage.fxml"));
            javafx.stage.Stage stage =
                    (javafx.stage.Stage) movieTitleLabel.getScene().getWindow();
            stage.getScene().setRoot(filmPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleLike(ActionEvent actionEvent) {
    }

    public void handleDislike(ActionEvent actionEvent) {
    }

    public void handleAddToList(ActionEvent actionEvent) {
    }

    public void handleResume(ActionEvent actionEvent) {
    }
}
