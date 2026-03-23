package org.netflix.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.netflix.DAO.MovieDAO;
import org.netflix.Models.Movie;

import javax.print.DocFlavor;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML private HBox movieRow1;
    @FXML private HBox movieRow2;
    @FXML private Label mvTrendName;
    @FXML private Label mvTrendDesc;
    @FXML private ImageView mvTrendImg;
    @FXML private StackPane mainStack;
    @FXML private ScrollPane heroStackPane;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mvTrendImg.fitWidthProperty().bind(mainStack.widthProperty());
        mvTrendImg.fitHeightProperty().bind(mainStack.heightProperty());
        
        getTrendMovie();
        getMovies();
        for (Node node : movieRow1.getChildren()) {
            ImageView iv = (ImageView) node;
            iv.setFitHeight(250);
            iv.setFitWidth(180);
            iv.setPreserveRatio(false);
        }
        for (Node node : movieRow2.getChildren()) {
            ImageView iv = (ImageView) node;
            iv.setFitHeight(250);
            iv.setFitWidth(180);
            iv.setPreserveRatio(false);
        }
    }
    public void getMovies(){
        List<Movie> movies = MovieDAO.getAllMovies();
        for(Movie movie : movies){
            String imgurl = movie.getCoverImageUrl();
            Image img = new Image(imgurl, true);

            ImageView iv1 = new ImageView(img);
            iv1.setFitHeight(250);
            iv1.setFitWidth(180);
            iv1.setPreserveRatio(false);
            iv1.getStyleClass().add("movie-poster");
            movieRow1.getChildren().add(iv1);

            ImageView iv2 = new ImageView(img);
            iv2.setFitHeight(250);
            iv2.setFitWidth(180);
            iv2.setPreserveRatio(false);
            iv2.getStyleClass().add("movie-poster");
            movieRow2.getChildren().add(iv2);
        }
    }
    public void getTrendMovie(){
        Movie movie = MovieDAO.getTrendMovie();
        mvTrendName.setText(movie.getTitle());
        mvTrendDesc.setText(movie.getDescription());
        String imgurl = movie.getCoverImageUrl();
        Image img = new Image(imgurl);
        mvTrendImg.setImage(img);

    }
}
