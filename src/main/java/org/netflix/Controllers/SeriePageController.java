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
import org.netflix.DAO.SerieDAO;
import org.netflix.Models.MediaGenre;
import org.netflix.Models.Movie;
import org.netflix.Models.Serie;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class SeriePageController implements Initializable {

    // ── Matches FilmPage.fxml ─────────────────────────────────────────
    @FXML private VBox      categoryContainer;
    @FXML private TextField searchField;

    // ── Hover popup shared across all cards ───────────────────────────
    private Popup             sharedPreviewPopup;
    private PauseTransition   showDelay;
    private PauseTransition   hideDelay;
    @FXML private VBox mediaRows;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup shared popup + delays
        sharedPreviewPopup = new Popup();
        sharedPreviewPopup.setAutoHide(false);
        sharedPreviewPopup.setHideOnEscape(true);

        showDelay = new PauseTransition(Duration.millis(300));
        hideDelay = new PauseTransition(Duration.millis(200));
        hideDelay.setOnFinished(ev -> sharedPreviewPopup.hide());

        sharedPreviewPopup.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> hideDelay.stop());
        sharedPreviewPopup.addEventFilter(MouseEvent.MOUSE_EXITED,  e -> hideDelay.playFromStart());

        // Load all genre rows
        categoryContainer.getChildren().clear();
        loadMovieCategories();
    }


    private void loadMovieCategories() {
        for (MediaGenre genre : MediaGenre.values()) {
            List<Serie> series = SerieDAO.findbyGenre(genre.name());
            if (series != null && !series.isEmpty()) {
                addGenreRow(genre.toString(), series);
            }
        }
    }

    private void addGenreRow(String title, List<Serie> series) {
        Label label = new Label(title);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 22; -fx-font-weight: bold;");
        label.setPadding(new Insets(0, 0, 10, 50));

        HBox movieRow = new HBox(10);
        movieRow.setPadding(new Insets(0, 50, 0, 20));
        movieRow.setAlignment(Pos.CENTER_LEFT);

        for (Serie serie : series) {
            movieRow.getChildren().add(createSerieCard(serie));
        }

        ScrollPane hScroll = new ScrollPane(movieRow);
        hScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        hScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        hScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        categoryContainer.getChildren().add(new VBox(label, hScroll));
    }

    // ═════════════════════════════════════════════════════════════════
    //  CARD BUILDER
    // ═════════════════════════════════════════════════════════════════

    private VBox createSerieCard(Serie serie) {
        // Poster thumbnail
        ImageView poster = new ImageView();
        try {
            String url = serie.getBackDropImageUrl();
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
                sharedPreviewPopup.getContent().setAll(buildHoverPopup(serie));
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


        Label title = new Label(serie.getTitle());
        title.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 12;");
        title.setMaxWidth(200);
        title.setAlignment(Pos.CENTER);

        VBox card = new VBox(5, poster, title);
        card.setAlignment(Pos.CENTER);
        card.setCursor(javafx.scene.Cursor.HAND);



        return card;
    }




    private VBox buildHoverPopup(Serie serie) {
        VBox preview = new VBox(10);
        preview.setStyle("-fx-background-color: #181818; -fx-background-radius: 10; " +
                "-fx-border-color: #333; -fx-border-radius: 10; -fx-padding: 0;");
        preview.setPrefWidth(300);

        // Backdrop image
        ImageView img = new ImageView(new Image(serie.getBackDropImageUrl(), true));
        img.setFitWidth(300);
        img.setFitHeight(160);
        img.setPreserveRatio(false);

        // Info section
        VBox info = new VBox(8);
        info.setPadding(new Insets(15));

        int match = 80 + (Math.abs(serie.getTitle().hashCode()) % 19);
        Label matchLbl = new Label(match + "% Match");
        matchLbl.setStyle("-fx-text-fill: #46d369; -fx-font-weight: bold; -fx-font-size: 14;");

        // Buttons
        String outlineBtn = "-fx-background-color: transparent; -fx-border-color: white; " +
                "-fx-border-radius: 50; -fx-text-fill: white; -fx-cursor: hand;";
        Button playBtn = new Button("▶");
        playBtn.setStyle("-fx-background-color: white; -fx-text-fill: black; " +
                "-fx-background-radius: 50; -fx-padding: 5 12; -fx-cursor: hand;");

        Button addBtn  = new Button("+"); addBtn.setStyle(outlineBtn);
        Button likeBtn = new Button("♥"); likeBtn.setStyle(outlineBtn);
        HBox buttons = new HBox(10, playBtn, addBtn, likeBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Label titleLbl = new Label(serie.getTitle());
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18;");

        Label desc = new Label(serie.getDescription());
        desc.setStyle("-fx-text-fill: #d2d2d2; -fx-font-size: 12;");
        desc.setWrapText(true);
        desc.setMaxWidth(270);
        desc.setMaxHeight(60);

        info.getChildren().addAll(matchLbl, buttons, titleLbl, desc);
        preview.getChildren().addAll(img, info);
        return preview;
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
    private void handleMoviesClick(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/Views/FilmPage.fxml"));
            Stage stage = (Stage) categoryContainer.getScene().getWindow(); // ← changed
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
