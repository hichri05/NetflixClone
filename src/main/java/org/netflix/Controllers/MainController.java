package org.netflix.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.netflix.DAO.MovieDAO;
import org.netflix.Models.Movie;
import org.netflix.Services.MovieService;
import org.netflix.Utils.SceneSwitcher;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML private HBox actionRow;
    @FXML private HBox dramaRow;
    @FXML private Label mvTrendName;
    @FXML private Label mvTrendDesc;
    @FXML private ImageView mvTrendImg;
    @FXML private ScrollPane mainScroll;
    @FXML private StackPane heroStack;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mvTrendImg.fitWidthProperty().bind(heroStack.widthProperty());
        mvTrendImg.fitHeightProperty().bind(heroStack.heightProperty());

        List<Movie> actionMovies = MovieService.getMoviesByGenre("Action");
        List<Movie> DramaMovies = MovieService.getMoviesByGenre("Drama");
        try {
            fillRow(actionRow);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            fillRow(dramaRow);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getTrendMovie();

    }

    public void fillRow(HBox row) throws IOException {
        List<Movie> movies = MovieDAO.getAllMovies();
        for(Movie movie : movies){
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/Views/moviePoster.fxml"));
            StackPane poster = loader.load();
            MoviePosterController controller = loader.getController();
            controller.setData(movie);
            row.getChildren().add(poster);
        }
    }
    public void getTrendMovie(){

        Movie movie = MovieDAO.getTrendMovie();
        if (movie != null) {
            String highResUrl = movie.getCoverImageUrl().replace("/w500/", "/original/");

            heroStack.setStyle(heroStack.getStyle() + "-fx-background-image: url('" + highResUrl + "');");
        }
        mvTrendName.setText(movie.getTitle());
        mvTrendDesc.setText(movie.getDescription());
        String imgurl = movie.getCoverImageUrl();
        String highResUrl = imgurl.replace("/w500/", "/original/");
        Image img = new Image(highResUrl, true);
        mvTrendImg.setImage(img);

    }
    @FXML
    private void handleMyListClick(MouseEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/MyList.fxml");
    }
}