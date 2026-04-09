package org.netflix.Controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.netflix.DAO.MovieDAO;
import org.netflix.Models.MediaGenre;
import org.netflix.Models.Movie;
import org.netflix.Utils.TransferData;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FilmPageController implements Initializable {

    @FXML private VBox      categoryContainer;
    @FXML private TextField searchField;

    private Popup             sharedPreviewPopup;
    private PauseTransition   showDelay;
    private PauseTransition   hideDelay;
    @FXML private VBox mediaRows;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sharedPreviewPopup = new Popup();
        sharedPreviewPopup.setAutoHide(false);
        sharedPreviewPopup.setHideOnEscape(true);

        showDelay = new PauseTransition(Duration.millis(300));
        hideDelay = new PauseTransition(Duration.millis(200));
        hideDelay.setOnFinished(ev -> sharedPreviewPopup.hide());

        sharedPreviewPopup.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> hideDelay.stop());
        sharedPreviewPopup.addEventFilter(MouseEvent.MOUSE_EXITED,  e -> hideDelay.playFromStart());

        categoryContainer.getChildren().clear();
        loadMovieCategories();
    }

    private void loadMovieCategories() {
        for (MediaGenre genre : MediaGenre.values()) {
            List<Movie> movies = MovieDAO.findbyGenre(genre.name());
            if (movies != null && !movies.isEmpty()) {
                addGenreRow(genre.toString(), movies);
            }
        }
    }

    private void addGenreRow(String title, List<Movie> movies) {
        Label label = new Label(title);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 22; -fx-font-weight: bold;");
        label.setPadding(new Insets(0, 0, 10, 50));

        HBox movieRow = new HBox(10);
        movieRow.setPadding(new Insets(0, 50, 0, 20));
        movieRow.setAlignment(Pos.CENTER_LEFT);

        for (Movie movie : movies) {
            movieRow.getChildren().add(createMovieCard(movie));
        }

        ScrollPane hScroll = new ScrollPane(movieRow);
        hScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        hScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        hScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        categoryContainer.getChildren().add(new VBox(label, hScroll));
    }

    private VBox createMovieCard(Movie movie) {
        ImageView poster = new ImageView();
        try {
            String url = movie.getBackDropImageUrl();
            if (url != null && !url.isEmpty())
                poster.setImage(new Image(url, true));
        } catch (Exception ignored) {}
        poster.setFitWidth(200);
        poster.setFitHeight(112);
        poster.setPreserveRatio(false);

        poster.setOnMouseEntered(e -> {
            hideDelay.stop();
            poster.setScaleX(1.1);
            poster.setScaleY(1.1);
            poster.setStyle("-fx-border-color: white; -fx-border-width: 2;");
            showDelay.setOnFinished(ev -> {
                sharedPreviewPopup.getContent().setAll(buildHoverPopup(movie));
                javafx.geometry.Point2D p = poster.localToScreen(0, 0);
                sharedPreviewPopup.show(poster, p.getX() - 50, p.getY() + poster.getFitHeight() + 5);
            });
            showDelay.playFromStart();
        });
        poster.setOnMouseExited(e -> {
            showDelay.stop();
            poster.setScaleX(1.0);
            poster.setScaleY(1.0);
            poster.setStyle("-fx-border-width: 0;");
            hideDelay.playFromStart();
        });
        poster.setOnMouseClicked(e -> {
            if (sharedPreviewPopup != null) sharedPreviewPopup.hide();
            showDelay.stop();
            openMediaDetails(movie);
        });

        Label title = new Label(movie.getTitle());
        title.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 12;");
        title.setMaxWidth(200);
        title.setAlignment(Pos.CENTER);

        VBox card = new VBox(5, poster, title);
        card.setAlignment(Pos.CENTER);
        card.setCursor(javafx.scene.Cursor.HAND);

        card.setOnMouseClicked(e -> {
            if (sharedPreviewPopup != null) sharedPreviewPopup.hide();
            showDelay.stop();
            openMediaDetails(movie);
        });

        return card;
    }
    private VBox buildHoverPopup(Movie movie) {
        VBox preview = new VBox(10);
        preview.setStyle("-fx-background-color: #181818; -fx-background-radius: 10; " +
                "-fx-border-color: #333; -fx-border-radius: 10; -fx-padding: 0;");
        preview.setPrefWidth(300);

        ImageView img = new ImageView(new Image(movie.getBackDropImageUrl(), true));
        img.setFitWidth(300);
        img.setFitHeight(160);
        img.setPreserveRatio(false);

        VBox info = new VBox(8);
        info.setPadding(new Insets(15));

        int match = 80 + (Math.abs(movie.getTitle().hashCode()) % 19);
        Label matchLbl = new Label(match + "% Match");
        matchLbl.setStyle("-fx-text-fill: #46d369; -fx-font-weight: bold; -fx-font-size: 14;");

        String outlineBtn = "-fx-background-color: transparent; -fx-border-color: white; " +
                "-fx-border-radius: 50; -fx-text-fill: white; -fx-cursor: hand;";
        Button playBtn = new Button("▶");
        playBtn.setStyle("-fx-background-color: white; -fx-text-fill: black; " +
                "-fx-background-radius: 50; -fx-padding: 5 12; -fx-cursor: hand;");
        playBtn.setOnAction(e -> {
            if (sharedPreviewPopup != null) sharedPreviewPopup.hide();
            openMediaDetails(movie);
        });
        Button addBtn  = new Button("+"); addBtn.setStyle(outlineBtn);
        Button likeBtn = new Button("♥"); likeBtn.setStyle(outlineBtn);
        HBox buttons = new HBox(10, playBtn, addBtn, likeBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Label titleLbl = new Label(movie.getTitle());
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18;");

        Label desc = new Label(movie.getDescription());
        desc.setStyle("-fx-text-fill: #d2d2d2; -fx-font-size: 12;");
        desc.setWrapText(true);
        desc.setMaxWidth(270);
        desc.setMaxHeight(60);

        info.getChildren().addAll(matchLbl, buttons, titleLbl, desc);
        preview.getChildren().addAll(img, info);
        return preview;
    }

    private void openMediaDetails(Movie movie) {
        try {
            TransferData.setMedia(movie);
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/Views/MediaDetails.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage =
                    (javafx.stage.Stage) categoryContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleMainClick(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/Views/main.fxml"));
            Stage stage = (Stage) categoryContainer.getScene().getWindow(); // ← changed
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSerieClick(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/Views/SeriePage.fxml"));
            Stage stage = (Stage) categoryContainer.getScene().getWindow(); // ← changed
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}