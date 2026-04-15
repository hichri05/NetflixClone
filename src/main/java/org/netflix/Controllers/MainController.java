package org.netflix.Controllers;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.*;
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
import org.netflix.DAO.*;
import org.netflix.Models.*;
import org.netflix.Utils.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MainController implements Initializable {

    @FXML private ScrollPane searchContent, mainScrollList, mainScroll;
    @FXML private ScrollPane moviesScroll, seriesScroll;
    @FXML private ScrollPane filterContent;
    @FXML private FlowPane   searchGrid;
    @FXML private FlowPane   listGrid;
    @FXML private FlowPane   filterGrid;
    @FXML private VBox       moviesRows, seriesRows;
    @FXML private TextField  searchField;
    @FXML private Label      mvTrendName, mvTrendDesc, userinf;
    @FXML private StackPane  heroStack;
    @FXML private VBox       mediaRows;
    @FXML private Button     playbtn, mylistbtn, adminBtn;

    // ── Filter bar controls ───────────────────────────────────────────
    @FXML private ComboBox<String> genreFilterCombo;
    @FXML private ComboBox<String> yearFilterCombo;
    @FXML private HBox             activeFilterChips;
    @FXML private Label            filterHeadingLabel;

    private User user;

    private List<Media> heroMedias;
    private int         currentHeroIndex = 0;
    private Timeline    heroTimeline;
    private Media       currentHeroMedia;

    private boolean moviesLoaded = false;
    private boolean seriesLoaded = false;

    // Shared popup for movies tab
    private Popup           moviesPopup;
    private PauseTransition moviesShowDelay;
    private PauseTransition moviesHideDelay;

    // Shared popup for series tab
    private Popup           seriesPopup;
    private PauseTransition seriesShowDelay;
    private PauseTransition seriesHideDelay;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        searchField.setFocusTraversable(false);
        user = Session.getUser();

        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        adminBtn.setVisible(isAdmin);
        adminBtn.setManaged(isAdmin);

        // Setup shared popups for movies and series tabs
        moviesPopup = buildSharedPopup(moviesHideDelay = new PauseTransition(Duration.millis(200)));
        moviesShowDelay = new PauseTransition(Duration.millis(300));
        moviesHideDelay.setOnFinished(ev -> moviesPopup.hide());
        moviesPopup.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> moviesHideDelay.stop());
        moviesPopup.addEventFilter(MouseEvent.MOUSE_EXITED,  e -> moviesHideDelay.playFromStart());

        seriesPopup = buildSharedPopup(seriesHideDelay = new PauseTransition(Duration.millis(200)));
        seriesShowDelay = new PauseTransition(Duration.millis(300));
        seriesHideDelay.setOnFinished(ev -> seriesPopup.hide());
        seriesPopup.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> seriesHideDelay.stop());
        seriesPopup.addEventFilter(MouseEvent.MOUSE_EXITED,  e -> seriesHideDelay.playFromStart());

        setupHeroSize();
        loadMediaRows();
        setupSearch();
        setupUser();
        setupInitialView();
        setupFilterBar();
        getTrendMovie();
    }

    private Popup buildSharedPopup(PauseTransition hideDelay) {
        Popup popup = new Popup();
        popup.setAutoHide(false);
        popup.setHideOnEscape(true);
        return popup;
    }

    // ── FILTER BAR SETUP ─────────────────────────────────────────────

    private void setupFilterBar() {
        // Populate genre combo
        List<String> genres = new ArrayList<>();
        genres.add("All Genres");
        for (MediaGenre g : MediaGenre.values()) {
            genres.add(g.toString().replace("_", " "));
        }
        genreFilterCombo.setItems(FXCollections.observableArrayList(genres));
        genreFilterCombo.setValue("All Genres");

        // Populate year combo — gather distinct years from DB, fallback to range
        List<String> years = new ArrayList<>();
        years.add("All Years");
        List<Media> all = MediaDAO.getAllMedia();
        Set<Integer> distinctYears = new TreeSet<>(Comparator.reverseOrder());
        for (Media m : all) {
            if (m.getReleaseYear() > 1900) distinctYears.add(m.getReleaseYear());
        }
        for (int y : distinctYears) years.add(String.valueOf(y));
        // Fallback range if DB is empty
        if (distinctYears.isEmpty()) {
            for (int y = 2025; y >= 1980; y--) years.add(String.valueOf(y));
        }
        yearFilterCombo.setItems(FXCollections.observableArrayList(years));
        yearFilterCombo.setValue("All Years");
    }

    @FXML
    public void handleApplyFilter(ActionEvent event) {
        String selectedGenre = genreFilterCombo.getValue();
        String selectedYear  = yearFilterCombo.getValue();

        boolean genreActive = selectedGenre != null && !selectedGenre.equals("All Genres");
        boolean yearActive  = selectedYear  != null && !selectedYear.equals("All Years");

        if (!genreActive && !yearActive) {
            // Nothing selected — just go home
            showHomeView();
            return;
        }

        // Build filter heading
        StringBuilder heading = new StringBuilder("Results");
        if (genreActive && yearActive)
            heading = new StringBuilder(selectedGenre + "  ·  " + selectedYear);
        else if (genreActive)
            heading = new StringBuilder(selectedGenre);
        else
            heading = new StringBuilder(selectedYear);
        filterHeadingLabel.setText(heading.toString());

        // Fetch and filter media
        List<Media> results = MediaDAO.getAllMediaWithViews();

        if (genreActive) {
            String genreKey = selectedGenre.replace(" ", "_");
            results = results.stream()
                    .filter(m -> m.getGenres() != null && m.getGenres().stream()
                            .anyMatch(g -> g.toString().replace("_", " ").equalsIgnoreCase(selectedGenre)
                                    || g.toString().equalsIgnoreCase(genreKey)))
                    .collect(Collectors.toList());
        }

        if (yearActive) {
            int year = Integer.parseInt(selectedYear);
            results = results.stream()
                    .filter(m -> m.getReleaseYear() == year)
                    .collect(Collectors.toList());
        }

        // Render chips showing active filters
        renderFilterChips(genreActive ? selectedGenre : null, yearActive ? selectedYear : null);

        // Show results
        filterGrid.getChildren().clear();
        if (results.isEmpty()) {
            Label empty = new Label("No results found.");
            empty.setStyle("-fx-text-fill: #666; -fx-font-size: 16px;");
            filterGrid.getChildren().add(empty);
        } else {
            for (Media m : results) loadFilterCard(m);
        }

        showFilterView();
    }

    private void renderFilterChips(String genre, String year) {
        activeFilterChips.getChildren().clear();
        if (genre != null) activeFilterChips.getChildren().add(buildChip("🎭 " + genre));
        if (year  != null) activeFilterChips.getChildren().add(buildChip("📅 " + year));
    }

    private Label buildChip(String text) {
        Label chip = new Label(text);
        chip.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: #e5e5e5; " +
                "-fx-font-size: 11px; -fx-padding: 3 10; " +
                "-fx-background-radius: 20; -fx-border-radius: 20; " +
                "-fx-border-color: #444;");
        return chip;
    }

    private void loadFilterCard(Media media) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/Views/MediaPoster.fxml"));
            Parent card = loader.load();
            MediaPosterController controller = loader.getController();
            controller.setData(media);
            filterGrid.getChildren().add(card);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleClearFilter(ActionEvent event) {
        genreFilterCombo.setValue("All Genres");
        yearFilterCombo.setValue("All Years");
        activeFilterChips.getChildren().clear();
        showHomeView();
    }

    // ── SETUP ────────────────────────────────────────────────────────

    private void setupHeroSize() {
        heroStack.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                heroStack.prefHeightProperty().bind(newScene.heightProperty().multiply(0.55));
            }
        });
    }

    private void loadMediaRows() {
        loadRow("Top 10 Views on Netflix", MediaDAO.getTopViews());
        loadRow("Action",      MediaDAO.getMediasByGenre("Action"));
        loadRow("Drama",       MediaDAO.getMediasByGenre("Drame"));
        loadRow("Comedy",      MediaDAO.getMediasByGenre("Comedie"));
        loadRow("Sci-Fi",      MediaDAO.getMediasByGenre("Science_Fiction"));
        loadRow("Thriller",    MediaDAO.getMediasByGenre("Thriller"));
        loadRow("Romance",     MediaDAO.getMediasByGenre("Romance"));
        loadRow("Historique",  MediaDAO.getMediasByGenre("Historique"));
        loadRow("Crime",       MediaDAO.getMediasByGenre("Crime"));
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() >= 2) {
                showSearchView();
                performSearch(newVal);
            } else if (newVal.isEmpty()) {
                showHomeView();
            }
        });
    }

    private void setupUser() {
        if (user != null) userinf.setText(user.getUsername());
    }

    private void setupInitialView() {
        searchContent.setVisible(false);
        mainScrollList.setVisible(false);
        moviesScroll.setVisible(false);
        seriesScroll.setVisible(false);
        filterContent.setVisible(false);
        mainScroll.setVisible(true);
    }

    // ── VIEW SWITCHING ───────────────────────────────────────────────

    private void hideAll() {
        mainScroll.setVisible(false);
        searchContent.setVisible(false);
        mainScrollList.setVisible(false);
        moviesScroll.setVisible(false);
        seriesScroll.setVisible(false);
        filterContent.setVisible(false);
    }

    private void showHomeView()   { hideAll(); mainScroll.setVisible(true); }
    private void showSearchView() { hideAll(); searchContent.setVisible(true); }
    private void showMyListView() { hideAll(); mainScrollList.setVisible(true); }
    private void showFilterView() { hideAll(); filterContent.setVisible(true); }

    private void showMoviesView() {
        hideAll();
        moviesScroll.setVisible(true);
        if (!moviesLoaded) {
            moviesLoaded = true;
            for (MediaGenre genre : MediaGenre.values()) {
                List<Movie> movies = MovieDAO.findbyGenre(genre.name());
                if (movies != null && !movies.isEmpty()) {
                    addMovieGenreRow(genre.toString().replace("_", " "), movies);
                }
            }
        }
    }

    private void showSeriesView() {
        hideAll();
        seriesScroll.setVisible(true);
        if (!seriesLoaded) {
            seriesLoaded = true;
            for (MediaGenre genre : MediaGenre.values()) {
                List<Serie> series = SerieDAO.findbyGenre(genre.name());
                if (series != null && !series.isEmpty()) {
                    addSerieGenreRow(genre.toString().replace("_", " "), series);
                }
            }
        }
    }

    // ── MOVIES TAB ──────────────────────────────────────────────────

    private void addMovieGenreRow(String title, List<Movie> movies) {
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

        moviesRows.getChildren().add(new VBox(label, hScroll));
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
            moviesHideDelay.stop();
            poster.setScaleX(1.1);
            poster.setScaleY(1.1);
            poster.setStyle("-fx-border-color: white; -fx-border-width: 2;");
            moviesShowDelay.setOnFinished(ev -> {
                moviesPopup.getContent().setAll(buildMovieHoverPopup(movie));
                javafx.geometry.Point2D p = poster.localToScreen(0, 0);
                moviesPopup.show(poster, p.getX() - 50, p.getY() + poster.getFitHeight() + 5);
            });
            moviesShowDelay.playFromStart();
        });
        poster.setOnMouseExited(e -> {
            moviesShowDelay.stop();
            poster.setScaleX(1.0);
            poster.setScaleY(1.0);
            poster.setStyle("-fx-border-width: 0;");
            moviesHideDelay.playFromStart();
        });
        poster.setOnMouseClicked(e -> {
            if (moviesPopup != null) moviesPopup.hide();
            moviesShowDelay.stop();
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
            if (moviesPopup != null) moviesPopup.hide();
            moviesShowDelay.stop();
            openMediaDetails(movie);
        });

        return card;
    }

    private VBox buildMovieHoverPopup(Movie movie) {
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
        playBtn.setOnAction(e -> { moviesPopup.hide(); openMediaDetails(movie); });

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

    // ── SERIES TAB ──────────────────────────────────────────────────

    private void addSerieGenreRow(String title, List<Serie> series) {
        Label label = new Label(title);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 22; -fx-font-weight: bold;");
        label.setPadding(new Insets(0, 0, 10, 50));

        HBox serieRow = new HBox(10);
        serieRow.setPadding(new Insets(0, 50, 0, 20));
        serieRow.setAlignment(Pos.CENTER_LEFT);

        for (Serie serie : series) {
            serieRow.getChildren().add(createSerieCard(serie));
        }

        ScrollPane hScroll = new ScrollPane(serieRow);
        hScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        hScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        hScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        seriesRows.getChildren().add(new VBox(label, hScroll));
    }

    private VBox createSerieCard(Serie serie) {
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
            seriesHideDelay.stop();
            poster.setScaleX(1.1);
            poster.setScaleY(1.1);
            poster.setStyle("-fx-border-color: white; -fx-border-width: 2;");
            seriesShowDelay.setOnFinished(ev -> {
                seriesPopup.getContent().setAll(buildSerieHoverPopup(serie));
                javafx.geometry.Point2D p = poster.localToScreen(0, 0);
                seriesPopup.show(poster, p.getX() - 50, p.getY() + poster.getFitHeight() + 5);
            });
            seriesShowDelay.playFromStart();
        });
        poster.setOnMouseExited(e -> {
            seriesShowDelay.stop();
            poster.setScaleX(1.0);
            poster.setScaleY(1.0);
            poster.setStyle("-fx-border-width: 0;");
            seriesHideDelay.playFromStart();
        });
        poster.setOnMouseClicked(e -> {
            if (seriesPopup != null) seriesPopup.hide();
            seriesShowDelay.stop();
            openMediaDetails(serie);
        });

        Label title = new Label(serie.getTitle());
        title.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 12;");
        title.setMaxWidth(200);
        title.setAlignment(Pos.CENTER);

        VBox card = new VBox(5, poster, title);
        card.setAlignment(Pos.CENTER);
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(e -> {
            if (seriesPopup != null) seriesPopup.hide();
            seriesShowDelay.stop();
            openMediaDetails(serie);
        });

        return card;
    }

    private VBox buildSerieHoverPopup(Serie serie) {
        VBox preview = new VBox(10);
        preview.setStyle("-fx-background-color: #181818; -fx-background-radius: 10; " +
                "-fx-border-color: #333; -fx-border-radius: 10; -fx-padding: 0;");
        preview.setPrefWidth(300);

        ImageView img = new ImageView(new Image(serie.getBackDropImageUrl(), true));
        img.setFitWidth(300);
        img.setFitHeight(160);
        img.setPreserveRatio(false);

        VBox info = new VBox(8);
        info.setPadding(new Insets(15));

        int match = 80 + (Math.abs(serie.getTitle().hashCode()) % 19);
        Label matchLbl = new Label(match + "% Match");
        matchLbl.setStyle("-fx-text-fill: #46d369; -fx-font-weight: bold; -fx-font-size: 14;");

        String outlineBtn = "-fx-background-color: transparent; -fx-border-color: white; " +
                "-fx-border-radius: 50; -fx-text-fill: white; -fx-cursor: hand;";
        Button playBtn = new Button("▶");
        playBtn.setStyle("-fx-background-color: white; -fx-text-fill: black; " +
                "-fx-background-radius: 50; -fx-padding: 5 12; -fx-cursor: hand;");
        playBtn.setOnAction(e -> { seriesPopup.hide(); openMediaDetails(serie); });

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

    // ── SHARED NAVIGATION ────────────────────────────────────────────

    private void openMediaDetails(Media media) {
        TransferData.setMedia(media);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/Views/MediaDetails.fxml"));
            Stage stage = (Stage) mediaRows.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── HOME ROW LOADER ──────────────────────────────────────────────

    private void loadRow(String title, List<Media> medias) {
        if (medias == null || medias.isEmpty()) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/Views/MediaRow.fxml"));
            Parent row = loader.load();
            MediaRowController controller = loader.getController();
            controller.setData(title, medias);
            mediaRows.getChildren().add(row);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── HERO ─────────────────────────────────────────────────────────

    public void getTrendMovie() {
        heroMedias = MediaDAO.getTopViews();
        if (heroMedias == null || heroMedias.isEmpty()) return;

        updateHero(heroMedias.get(0));

        heroTimeline = new Timeline(new KeyFrame(Duration.seconds(6), e -> {
            currentHeroIndex = (currentHeroIndex + 1) % heroMedias.size();
            updateHero(heroMedias.get(currentHeroIndex));
        }));
        heroTimeline.setCycleCount(Timeline.INDEFINITE);
        heroTimeline.play();
    }

    private void updateHero(Media media) {
        currentHeroMedia = media;
        mvTrendName.setText(media.getTitle());

        String desc = media.getDescription();
        mvTrendDesc.setText(desc != null && desc.length() > 120
                ? desc.substring(0, 120) + "..." : desc);

        heroStack.prefWidthProperty().bind(mediaRows.widthProperty());

        String url = media.getBackdropImageUrl();
        if (url != null && !url.isEmpty()) {
            url = url.replace("\\", "/");
            heroStack.setStyle(
                    "-fx-background-image: url('" + url + "');" +
                            "-fx-background-size: 110%;" +
                            "-fx-background-position: center;"
            );
        }

        updateButtonUI(user, media);
    }

    private void updateButtonUI(User u, Media m) {
        if (u == null) { mylistbtn.setText("+ My List"); return; }
        mylistbtn.setText(UserDAO.isFavorite(u.getId(), m.getIdMedia())
                ? "✓ In My List" : "+ My List");
    }

    // ── SEARCH ───────────────────────────────────────────────────────

    private void performSearch(String search) {
        List<Media> results = MediaDAO.searchMedia(search);
        searchGrid.getChildren().clear();

        if (results.isEmpty()) {
            Label noResults = new Label("No results for '" + search + "'");
            noResults.setStyle("-fx-text-fill: #999; -fx-font-size: 16px;");
            searchGrid.getChildren().add(noResults);
            return;
        }
        results.forEach(this::loadMovieCard);
    }

    private void loadMovieCard(Media media) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/Views/MediaPoster.fxml"));
            Parent card = loader.load();
            MediaPosterController controller = loader.getController();
            controller.setData(media);
            searchGrid.getChildren().add(card);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── MY LIST ──────────────────────────────────────────────────────

    public void displayMyList(List<Media> favorites) {
        listGrid.getChildren().clear();
        for (Media media : favorites) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        MainController.class.getResource("/org/Views/MediaPoster.fxml"));
                Parent poster = loader.load();
                MediaPosterController controller = loader.getController();
                controller.showRemoveButton(true);
                controller.setData(media);
                listGrid.getChildren().add(poster);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ── HANDLERS ─────────────────────────────────────────────────────

    @FXML private void handleHomeClick(MouseEvent event)  { showHomeView(); }
    @FXML private void handleMoviesClick(MouseEvent event) { showMoviesView(); }
    @FXML private void handleSerieClick(MouseEvent event)  { showSeriesView(); }

    @FXML
    private void handleMyListClick(MouseEvent event) {
        showMyListView();
        if (user != null) {
            List<Media> favorites = UserDAO.getUserFavorites(user.getId());
            displayMyList(favorites);
        }
    }

    @FXML
    public void handlePlay(ActionEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/VideoPlayer.fxml");
    }

    @FXML
    public void handleAddToMyList(ActionEvent event) {
        if (user == null || currentHeroMedia == null) return;
        if (UserDAO.isFavorite(user.getId(), currentHeroMedia.getIdMedia())) {
            MediaDAO.removeFromFavorites(user.getId(), currentHeroMedia.getIdMedia());
        } else {
            MediaDAO.addToFavorites(user.getId(), currentHeroMedia.getIdMedia());
        }
        updateButtonUI(user, currentHeroMedia);
    }

    @FXML
    public void handleOpenDashboard(ActionEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/MainDashboard.fxml");
    }
}