package org.netflix.Controllers;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Duration;
import org.netflix.Models.MediaGenre;
import org.netflix.Models.Movie;
import org.netflix.DAO.MovieDAO;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FilmPageController implements Initializable {

    @FXML private VBox categoryContainer;

    private Popup sharedPreviewPopup;
    private PauseTransition showDelay;  // delay BEFORE showing
    private PauseTransition hideDelay;  // delay BEFORE hiding

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sharedPreviewPopup = new Popup();
        sharedPreviewPopup.setAutoHide(false);
        sharedPreviewPopup.setHideOnEscape(true);

        // Wait 300ms before showing — eliminates flicker from accidental hover
        showDelay = new PauseTransition(Duration.millis(300));

        // Wait 200ms before hiding — bridges the gap between poster and popup
        hideDelay = new PauseTransition(Duration.millis(200));
        hideDelay.setOnFinished(ev -> sharedPreviewPopup.hide());

        sharedPreviewPopup.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> hideDelay.stop());
        sharedPreviewPopup.addEventFilter(MouseEvent.MOUSE_EXITED, e -> hideDelay.playFromStart());

        if (categoryContainer != null) {
            categoryContainer.getChildren().clear();
            loadMovieCategories();
        }
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

        VBox section = new VBox(label, hScroll);
        categoryContainer.getChildren().add(section);
    }

    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);

        ImageView poster = new ImageView();
        try {
            String imageUrl = movie.getBackDropImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                poster.setImage(new Image(imageUrl, true));
            }
        } catch (Exception e) {
            System.out.println("Image non trouvable");
        }

        poster.setFitWidth(200);
        poster.setFitHeight(112);
        poster.setPreserveRatio(false);

        poster.setOnMouseEntered(e -> {
            hideDelay.stop();

            poster.setScaleX(1.1);
            poster.setScaleY(1.1);
            poster.setStyle("-fx-border-color: white; -fx-border-width: 2;");

            // Wait 300ms before showing — if mouse leaves before that, we never show
            showDelay.setOnFinished(ev -> {
                sharedPreviewPopup.getContent().clear();
                sharedPreviewPopup.getContent().add(createPreviewPopup(movie));

                // Position BELOW the poster so popup never overlaps it
                // This is the key fix — no overlap = no flicker loop
                javafx.geometry.Point2D p = poster.localToScreen(0, 0);
                double popupX = p.getX() - 50;
                double popupY = p.getY() + poster.getFitHeight() + 5; // below the poster

                sharedPreviewPopup.show(poster, popupX, popupY);
            });
            showDelay.playFromStart();
        });

        poster.setOnMouseExited(e -> {
            showDelay.stop(); // if mouse left before 300ms, never show
            poster.setScaleX(1.0);
            poster.setScaleY(1.0);
            poster.setStyle("-fx-border-width: 0;");
            hideDelay.playFromStart();
        });

        Label title = new Label(movie.getTitle());
        title.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 12;");
        title.setMaxWidth(200);
        title.setAlignment(Pos.CENTER);

        card.getChildren().addAll(poster, title);
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(event -> {
            // Hide the popup immediately so it doesn't stay on screen
            if (sharedPreviewPopup != null) sharedPreviewPopup.hide();

            // Call the navigation method
            navigateToDetails(movie);
        });
        return card;
    }

    private VBox createPreviewPopup(Movie movie) {
        VBox preview = new VBox(10);
        // On ajoute une bordure légère pour que la popup ressorte sur le fond noir
        preview.setStyle("-fx-background-color: #181818; -fx-background-radius: 10; -fx-border-color: #333; -fx-border-radius: 10; -fx-padding: 0;");
        preview.setPrefWidth(300);

        // Image du haut (Affiche plus grande)
        ImageView topImage = new ImageView(new Image(movie.getBackDropImageUrl(), true));
        topImage.setFitWidth(300); // Ajusté à la largeur de la preview
        topImage.setFitHeight(160);
        topImage.setPreserveRatio(false);

        VBox infoContainer = new VBox(8);
        infoContainer.setPadding(new javafx.geometry.Insets(15));

        // 1. Calcul du Match Dynamique
        // On utilise l'ID ou le Hash du titre pour que le score reste le même pour un film donné
        int randomMatch = 80 + (Math.abs(movie.getTitle().hashCode()) % 19); // Génère un score entre 80% et 98%
        Label matchLabel = new Label(randomMatch + "% Match");
        matchLabel.setStyle("-fx-text-fill: #46d369; -fx-font-weight: bold; -fx-font-size: 14;");

        // 2. Boutons
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_LEFT);
        String btnStyle = "-fx-background-color: transparent; -fx-border-color: white; -fx-border-radius: 50; -fx-text-fill: white; -fx-cursor: hand;";

        Button playBtn = new Button("▶");
        playBtn.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-background-radius: 50; -fx-padding: 5 12 5 12;");

        Button addBtn = new Button("+");
        addBtn.setStyle(btnStyle);

        Button likeBtn = new Button("♥");
        likeBtn.setStyle(btnStyle);

        buttons.getChildren().addAll(playBtn, addBtn, likeBtn);

        // 3. Titre
        Label title = new Label(movie.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18;");

        // 4. Description (Ajoutée ici)
        Label description = new Label(movie.getDescription());
        description.setStyle("-fx-text-fill: #d2d2d2; -fx-font-size: 12;");
        description.setWrapText(true); // Permet de passer à la ligne automatiquement
        description.setMaxWidth(270); // Un peu moins que la largeur de la VBox
        description.setMaxHeight(60); // Limite la hauteur pour ne pas que la fenêtre soit trop longue

        // Assemblage
        infoContainer.getChildren().addAll(matchLabel, buttons, title, description);
        preview.getChildren().addAll(topImage, infoContainer);

        return preview;
    }
    private void navigateToDetails(Movie movie) {
        try {
            // Try multiple possible paths
            URL fxmlUrl = getClass().getResource("/org/netflix/Views/FilmDetail.fxml");
            if (fxmlUrl == null) fxmlUrl = getClass().getResource("/Views/FilmDetail.fxml");
            if (fxmlUrl == null) fxmlUrl = getClass().getResource("FilmDetail.fxml");
            if (fxmlUrl == null) {
                System.out.println("STILL NULL - doing manual file load");
                java.io.File f = new java.io.File("src/main/resources/org/netflix/Views/FilmDetail.fxml");
                System.out.println("File exists: " + f.exists() + " at " + f.getAbsolutePath());
                fxmlUrl = f.toURI().toURL();
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            javafx.scene.Parent detailRoot = loader.load();

            NetflixController controller = loader.getController();
            controller.setMovieData(movie);

            javafx.stage.Stage stage = (javafx.stage.Stage) categoryContainer.getScene().getWindow();
            stage.getScene().setRoot(detailRoot);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }
    }
}