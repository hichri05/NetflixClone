package org.netflix.Controllers;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import org.netflix.DAO.WatchHistoryDAO;
import org.netflix.Models.WatchHistory;

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

    @FXML private ComboBox<String> genreFilterCombo;
    @FXML private ComboBox<String> yearFilterCombo;
    @FXML private HBox activeFilter, filterBar;
    @FXML private Label filterHeadingLabel;

    @FXML private VBox continueWatchingSection;
    @FXML private HBox continueWatchingRow;

    private User user;

    private List<Media> heroMedias;
    private int         currentHeroIndex = 0;
    private Timeline    heroTimeline;
    private Media       currentHeroMedia;

    private boolean moviesLoaded = false;
    private boolean seriesLoaded = false;
    private Popup           moviesPopup;
    private PauseTransition moviesShowDelay;
    private PauseTransition moviesHideDelay;

    private Popup           seriesPopup;
    private PauseTransition seriesShowDelay;
    private PauseTransition seriesHideDelay;

    private Popup           userPopup;
    private PauseTransition userShowDelay;
    private PauseTransition userHideDelay;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        filterBar.setVisible(false);
        searchField.setFocusTraversable(false);
        user = Session.getUser();

        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        adminBtn.setVisible(isAdmin);
        adminBtn.setManaged(isAdmin);

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
        loadContinueWatching();
        setupSearch();
        setupUser();
        setupUserPopup();
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


    private void setupUserPopup() {
        userPopup = new Popup();
        userPopup.setAutoHide(true);
        userPopup.setHideOnEscape(true);

        userHideDelay = new PauseTransition(Duration.millis(250));
        userHideDelay.setOnFinished(ev -> userPopup.hide());

        userShowDelay = new PauseTransition(Duration.millis(180));

        Label avatar = new Label(user != null
                ? String.valueOf(user.getUsername().charAt(0)).toUpperCase() : "?");
        avatar.setStyle(
                "-fx-background-color: #e50914;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-min-width: 42px; -fx-min-height: 42px;" +
                        "-fx-max-width: 42px; -fx-max-height: 42px;" +
                        "-fx-background-radius: 4;" +
                        "-fx-alignment: center;"
        );

        Label nameLbl = new Label(user != null ? user.getUsername() : "");
        nameLbl.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;"
        );

        VBox topRow = new VBox(6, avatar, nameLbl);
        topRow.setAlignment(Pos.CENTER);
        topRow.setPadding(new Insets(14, 16, 12, 16));

        Pane divider1 = new Pane();
        divider1.setPrefHeight(1);
        divider1.setStyle("-fx-background-color: #333;");

        Button profileBtn = new Button("👤  My Profile");
        profileBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #e5e5e5;" +
                        "-fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 10 16;" +
                        "-fx-alignment: CENTER_LEFT; -fx-border-width: 0;"
        );
        profileBtn.setMaxWidth(Double.MAX_VALUE);
        profileBtn.setOnMouseEntered(e -> profileBtn.setStyle(
                "-fx-background-color: #1a1a1a; -fx-text-fill: white; -fx-font-size: 13px;" +
                        "-fx-cursor: hand; -fx-padding: 10 16; -fx-alignment: CENTER_LEFT; -fx-border-width: 0;"));
        profileBtn.setOnMouseExited(e -> profileBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #e5e5e5; -fx-font-size: 13px;" +
                        "-fx-cursor: hand; -fx-padding: 10 16; -fx-alignment: CENTER_LEFT; -fx-border-width: 0;"));
        profileBtn.setOnAction(e -> {
            userPopup.hide();
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/org/Views/Profile.fxml"));
                Stage stage = (Stage) mediaRows.getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        Pane divider2 = new Pane();
        divider2.setPrefHeight(1);
        divider2.setStyle("-fx-background-color: #222;");

        Button logoutBtn = new Button("Sign out of Netflix");
        logoutBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #e5e5e5;" +
                        "-fx-font-size: 13px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 10 16;" +
                        "-fx-alignment: center;" +
                        "-fx-border-width: 0;"
        );
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(
                "-fx-background-color: #1a1a1a;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 10 16;" +
                        "-fx-alignment: center;" +
                        "-fx-border-width: 0;"
        ));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #e5e5e5;" +
                        "-fx-font-size: 13px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 10 16;" +
                        "-fx-alignment: center;" +
                        "-fx-border-width: 0;"
        ));
        logoutBtn.setOnAction(e -> handleLogout());

        Label caret = new Label("▲");
        caret.setStyle(
                "-fx-text-fill: #333;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 0;"
        );
        caret.setAlignment(Pos.CENTER);

        VBox card = new VBox(topRow, divider1, profileBtn, divider2, logoutBtn);
        card.setStyle(
                "-fx-background-color: #141414;" +
                        "-fx-border-color: #333;" +
                        "-fx-border-width: 1;" +
                        "-fx-background-radius: 4;" +
                        "-fx-border-radius: 4;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.85), 16, 0, 0, 5);"
        );
        card.setPrefWidth(190);

        VBox wrapper = new VBox(0, caret, card);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setStyle("-fx-background-color: transparent;");

        userPopup.getContent().add(wrapper);

        wrapper.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> userHideDelay.stop());
        wrapper.addEventFilter(MouseEvent.MOUSE_EXITED,  e -> userHideDelay.playFromStart());

        userinf.setCursor(Cursor.HAND);
        userinf.setOnMouseEntered(e -> {
            userHideDelay.stop();
            userShowDelay.setOnFinished(ev -> {
                Point2D p = userinf.localToScreen(0, 0);
                userPopup.show(userinf,
                        p.getX() - card.getPrefWidth() / 2 + userinf.getWidth() / 2,
                        p.getY() + userinf.getHeight() + 4);
            });
            userShowDelay.playFromStart();
        });
        userinf.setOnMouseExited(e -> {
            userShowDelay.stop();
            userHideDelay.playFromStart();
        });
    }

    private void handleLogout() {
        if (userPopup != null) userPopup.hide();
        Session.logout();
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/org/Views/SignIn.fxml"));
            Stage stage = (Stage) mediaRows.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupFilterBar() {
        List<String> genres = new ArrayList<>();
        genres.add("All Genres");
        for (MediaGenre g : MediaGenre.values()) {
            genres.add(g.toString().replace("_", " "));
        }
        genreFilterCombo.setItems(FXCollections.observableArrayList(genres));
        genreFilterCombo.setValue("All Genres");

        List<String> years = new ArrayList<>();
        years.add("All Years");
        List<Media> all = MediaDAO.getAllMedia();
        Set<Integer> distinctYears = new TreeSet<>(Comparator.reverseOrder());
        for (Media m : all) {
            if (m.getReleaseYear() > 1900) distinctYears.add(m.getReleaseYear());
        }
        for (int y : distinctYears) years.add(String.valueOf(y));
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
            showHomeView();
            return;
        }

        StringBuilder heading = new StringBuilder("Results");
        if (genreActive && yearActive)
            heading = new StringBuilder(selectedGenre + "  ·  " + selectedYear);
        else if (genreActive)
            heading = new StringBuilder(selectedGenre);
        else
            heading = new StringBuilder(selectedYear);
        filterHeadingLabel.setText(heading.toString());

        List<Media> results = new ArrayList<>();
        if (genreActive) {
            String genreKey = selectedGenre.replace(" ", "_");
            results = MediaDAO.getMediasByGenre(genreKey);
            results = results.stream()
                    .filter(m -> m.getGenres() != null && m.getGenres().stream()
                            .anyMatch(g -> g.toString().equalsIgnoreCase(genreKey)))
                    .collect(Collectors.toList());
        } else {
            results = MediaDAO.getAllMedia();
        }

        if (yearActive) {
            int year = Integer.parseInt(selectedYear);
            results = results.stream()
                    .filter(m -> m.getReleaseYear() == year)
                    .collect(Collectors.toList());
        }

        renderFilterChips(genreActive ? selectedGenre : null, yearActive ? selectedYear : null);

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
        activeFilter.getChildren().clear();
        if (genre != null) activeFilter.getChildren().add(buildChip("🎭 " + genre));
        if (year  != null) activeFilter.getChildren().add(buildChip("📅 " + year));
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
        activeFilter.getChildren().clear();
        showHomeView();
    }

    private void setupHeroSize() {
        heroStack.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                heroStack.prefHeightProperty().bind(newScene.heightProperty().multiply(0.55));
            }
        });
    }

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

    public void hideAll() {
        mainScroll.setVisible(false);
        searchContent.setVisible(false);
        mainScrollList.setVisible(false);
        moviesScroll.setVisible(false);
        seriesScroll.setVisible(false);
        filterContent.setVisible(false);
        filterBar.setVisible(false);
    }

    private void showHomeView()   { hideAll(); mainScroll.setVisible(true); }
    private void showSearchView() { hideAll(); searchContent.setVisible(true); filterBar.setVisible(true);}
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
                Point2D p = poster.localToScreen(0, 0);
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
        card.setCursor(Cursor.HAND);
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
                Point2D p = poster.localToScreen(0, 0);
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
        card.setCursor(Cursor.HAND);
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


    private void loadContinueWatching() {
        if (user == null) return;
        List<WatchHistory> history = new WatchHistoryDAO().findByUser(user.getId());

        java.util.LinkedHashMap<Integer, WatchHistory> inProgressMap = new java.util.LinkedHashMap<>();
        for (WatchHistory wh : history) {
            if (wh.getMediaId() == null) continue;
            if (wh.getCompleted() == 0 && wh.getStoppedAtTime() > 5.0) {
                inProgressMap.putIfAbsent(wh.getMediaId(), wh);
            }
        }

        if (inProgressMap.isEmpty()) return;

        for (WatchHistory wh : inProgressMap.values()) {
            Media media = MediaDAO.getAllMedia().stream()
                    .filter(m -> m.getIdMedia() == wh.getMediaId())
                    .findFirst().orElse(null);
            if (media == null) continue;

            double[] pd = WatchHistoryDAO.getProgressAndDuration(user.getId(), wh.getMediaId());
            double ratio = pd[1] > 0 ? Math.min(pd[0] / pd[1], 1.0) : 0.5;

            continueWatchingRow.getChildren().add(buildContinueCard(media, ratio, wh));
        }

        continueWatchingSection.setVisible(true);
        continueWatchingSection.setManaged(true);
    }

    private VBox buildContinueCard(Media media, double ratio, WatchHistory wh) {
        VBox card = new VBox(0);
        card.setPrefWidth(210);
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 8;");

        javafx.scene.layout.StackPane thumbBox = new javafx.scene.layout.StackPane();
        ImageView poster = new ImageView();
        poster.setFitWidth(210); poster.setFitHeight(118);
        poster.setPreserveRatio(false);
        try {
            String url = media.getBackdropImageUrl() != null
                    ? media.getBackdropImageUrl() : media.getCoverImageUrl();
            if (url != null) poster.setImage(new Image(url, true));
        } catch (Exception ignored) {}

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(210, 118);
        clip.setArcWidth(8); clip.setArcHeight(8);
        poster.setClip(clip);

        javafx.scene.control.ProgressBar bar = new javafx.scene.control.ProgressBar(ratio);
        bar.setPrefWidth(210); bar.setPrefHeight(4);
        bar.setStyle("-fx-accent: #e50914; -fx-background-color: rgba(0,0,0,0.55);");
        javafx.scene.layout.StackPane.setAlignment(bar, javafx.geometry.Pos.BOTTOM_CENTER);

        Label playIcon = new Label("▶");
        playIcon.setStyle("-fx-text-fill: white; -fx-font-size: 26px;" +
                "-fx-background-color: rgba(0,0,0,0.55); -fx-background-radius: 22;" +
                "-fx-padding: 8 12;");
        playIcon.setOpacity(0);

        thumbBox.getChildren().addAll(poster, bar, playIcon);

        card.setOnMouseEntered(e -> { playIcon.setOpacity(1); poster.setOpacity(0.75); });
        card.setOnMouseExited(e  -> { playIcon.setOpacity(0); poster.setOpacity(1.0);  });

        VBox info = new VBox(4);
        info.setStyle("-fx-padding: 10 12 12 12;");

        Label titleLbl = new Label(media.getTitle());
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        titleLbl.setMaxWidth(186); titleLbl.setWrapText(false);

        int mins = (int)(wh.getStoppedAtTime() / 60);
        Label progLbl = new Label(mins + " min watched");
        progLbl.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

        Label resumeLbl = new Label("▶ Continue");
        resumeLbl.setStyle("-fx-text-fill: #e50914; -fx-font-size: 11px; -fx-font-weight: bold;");

        info.getChildren().addAll(titleLbl, progLbl, resumeLbl);
        card.getChildren().addAll(thumbBox, info);

        final Media chosen = media;
        card.setOnMouseClicked(e -> {
            TransferData.setMedia(chosen);
            try {
                javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(
                        getClass().getResource("/org/Views/MediaDetails.fxml"));
                javafx.stage.Stage stage = (javafx.stage.Stage) mediaRows.getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        return card;
    }


    @FXML private void handleHomeClick(MouseEvent event)   { showHomeView(); }
    @FXML private void handleMoviesClick(MouseEvent event)  { showMoviesView(); }
    @FXML private void handleSerieClick(MouseEvent event)   { showSeriesView(); }

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
        if (currentHeroMedia != null) TransferData.setMedia(currentHeroMedia);
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

    @FXML
    public void handleProfileClick(javafx.scene.input.MouseEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/Profile.fxml");
    }

    @FXML
    public void handleViewHistory(ActionEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/WatchHistory.fxml");
    }
}