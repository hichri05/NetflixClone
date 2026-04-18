package org.netflix.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import org.netflix.DAO.MediaDAO;
import org.netflix.Models.Genre;
import org.netflix.Models.Media;
import org.netflix.Models.MediaGenre;
import org.netflix.Utils.ConxDB;
import org.netflix.Utils.SceneSwitcher;
import org.netflix.Utils.TransferData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CatalogueController {

    @FXML private ComboBox<String> genreSelectionCombo;
    @FXML private TableView<Media> mediaTable;
    @FXML private TableColumn<Media, String> colTitle;
    @FXML private TableColumn<Media, String> colType;

    @FXML private TextField titleField, yearField, durationField, videoUrlField, posterUrlField;
    @FXML private TextArea descArea, actorsArea;
    @FXML private ComboBox<String> typeCombo, filterTypeCombo;
    @FXML private FlowPane genreFlowPane;
    @FXML private Button manageEpisodesBtn;

    private final ObservableList<Media> mediaList = FXCollections.observableArrayList();
    private Media selectedMedia;
    private final Connection conn = ConxDB.getInstance();

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        typeCombo.setItems(FXCollections.observableArrayList("Movie", "Serie"));
        filterTypeCombo.setItems(FXCollections.observableArrayList("All", "Movie", "Serie"));
        filterTypeCombo.setValue("All");

        filterTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(newVal));

        mediaTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) showMediaDetails(newSel);
        });

        genreSelectionCombo.setItems(FXCollections.observableArrayList(
                Arrays.stream(MediaGenre.values()).map(Enum::name).toList()
        ));

        refreshTable();
    }

    private void refreshTable() {
        mediaList.setAll(MediaDAO.getAllMedia());
        mediaTable.setItems(mediaList);
    }

    private void applyFilter(String type) {
        if (type == null || type.equalsIgnoreCase("All")) {
            mediaTable.setItems(mediaList);
        } else {
            mediaTable.setItems(mediaList.filtered(m ->
                    type.equalsIgnoreCase(m.getType())
            ));
        }
    }

    private void showMediaDetails(Media media) {
        clearFields();
        selectedMedia = media;

        titleField.setText(safe(media.getTitle()));
        descArea.setText(safe(media.getDescription()));
        posterUrlField.setText(safe(media.getCoverImageUrl()));
        typeCombo.setValue(media.getType());

        yearField.setText(
                media.getReleaseYear() > 0
                        ? String.valueOf(media.getReleaseYear())
                        : ""
        );

        loadMovieExtras(media);
        loadGenres(media);

        boolean isSerie = "Serie".equalsIgnoreCase(media.getType());
        manageEpisodesBtn.setVisible(isSerie);
        manageEpisodesBtn.setManaged(isSerie);
    }

    private void loadMovieExtras(Media media) {
        if (!"Movie".equalsIgnoreCase(media.getType())) return;

        String sql = "SELECT videoUrl, duration_minutes FROM movie WHERE id_Media = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, media.getIdMedia());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                videoUrlField.setText(safe(rs.getString("videoUrl")));

                int dur = rs.getInt("duration_minutes");
                if (dur > 0) {
                    durationField.setText(String.valueOf(dur));
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void loadGenres(Media media) {
        if (media.getGenres() == null) return;

        media.getGenres().stream()
                .map(Genre::getName)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .forEach(this::addGenreTag);
    }

    @FXML
    public void handlePrepareNew(ActionEvent e) {
        clearFields();
        selectedMedia = null;

        titleField.requestFocus();
        manageEpisodesBtn.setVisible(false);
        manageEpisodesBtn.setManaged(false);
    }

    @FXML
    public void handleSave(ActionEvent e) {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showError("Title is required.");
            return;
        }

        String type = typeCombo.getValue();
        if (type == null) {
            showError("Select type.");
            return;
        }

        int year = parseInt(yearField.getText(), "Year must be a number.");
        int duration = parseInt(durationField.getText(), "Duration must be a number.");

        if (year == -1 || duration == -1) return;

        String description = descArea.getText().trim();
        String posterUrl = posterUrlField.getText().trim();
        String videoUrl = videoUrlField.getText().trim();

        List<String> genres = genreFlowPane.getChildren().stream()
                .filter(n -> n instanceof Label)
                .map(n -> ((Label) n).getText())
                .toList();

        if (selectedMedia == null) {
            createMedia(title, description, posterUrl, year, type, videoUrl, duration, genres);
        } else {
            updateMedia(title, description, posterUrl, year, type, videoUrl, duration, genres);
        }

        refreshTable();
        clearFields();
        showInfo("Saved successfully.");
    }

    private void createMedia(String title, String desc, String poster, int year,
                             String type, String videoUrl, int duration, List<String> genres) {

        Media media = new Media();
        media.setTitle(title);
        media.setDescription(desc);
        media.setCoverImageUrl(poster);
        media.setReleaseYear(year);
        media.setType(type);

        if (!MediaDAO.addMedia(media)) {
            showError("Failed to add media.");
            return;
        }

        int id = media.getIdMedia();

        if ("Movie".equalsIgnoreCase(type)) {
            insertMovieRow(id, videoUrl, duration);
        } else {
            insertSerieRow(id);
        }

        saveGenreLinks(id, genres);
    }

    private void updateMedia(String title, String desc, String poster, int year,
                             String type, String videoUrl, int duration, List<String> genres) {

        selectedMedia.setTitle(title);
        selectedMedia.setDescription(desc);
        selectedMedia.setCoverImageUrl(poster);
        selectedMedia.setReleaseYear(year);
        selectedMedia.setType(type);

        MediaDAO.updateMedia(selectedMedia);

        int id = selectedMedia.getIdMedia();

        if ("Movie".equalsIgnoreCase(type)) {
            updateMovieRow(id, videoUrl, duration);
        }

        deleteGenreLinks(id);
        saveGenreLinks(id, genres);
    }

    @FXML
    public void handleDelete(ActionEvent e) {
        if (selectedMedia == null) {
            showError("Select a media item first.");
            return;
        }

        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Delete \"" + selectedMedia.getTitle() + "\"?"
        );

        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                MediaDAO.deleteMedia(selectedMedia.getIdMedia());
                refreshTable();
                clearFields();
            }
        });
    }

    @FXML
    public void handleOpenEpisodeManager(ActionEvent e) {
        Media media = mediaTable.getSelectionModel().getSelectedItem();

        if (media == null || !"Serie".equalsIgnoreCase(media.getType())) {
            showError("Select a Series first.");
            return;
        }

        TransferData.setMedia(media);
        SceneSwitcher.goTo(e, "/org/Views/EpisodeManager.fxml");
    }

    @FXML
    public void handleAddGenreFromCombo(ActionEvent e) {
        String val = genreSelectionCombo.getValue();

        if (val != null && !val.isEmpty()) {
            addGenreTag(val);
            genreSelectionCombo.getSelectionModel().clearSelection();
        }
    }

    private void addGenreTag(String name) {
        boolean exists = genreFlowPane.getChildren().stream()
                .anyMatch(n -> n instanceof Label &&
                        ((Label) n).getText().equals(name));

        if (exists) return;

        Label tag = new Label(name);
        tag.setStyle(
                "-fx-background-color: #333; -fx-text-fill: white;" +
                        "-fx-padding: 5 10; -fx-background-radius: 15; -fx-cursor: hand;"
        );

        tag.setOnMouseClicked(e -> genreFlowPane.getChildren().remove(tag));
        genreFlowPane.getChildren().add(tag);
    }

    private void insertMovieRow(int id, String url, int duration) {
        String sql = "INSERT IGNORE INTO movie (id_Media, videoUrl, duration_minutes) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, url);
            ps.setInt(3, duration);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void updateMovieRow(int id, String url, int duration) {
        String sql = "UPDATE movie SET videoUrl = ?, duration_minutes = ? WHERE id_Media = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, url);
            ps.setInt(2, duration);
            ps.setInt(3, id);

            if (ps.executeUpdate() == 0) {
                insertMovieRow(id, url, duration);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void insertSerieRow(int id) {
        String sql = "INSERT IGNORE INTO serie (id_Media, nbrSaison) VALUES (?, 0)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void saveGenreLinks(int mediaId, List<String> names) {
        String getId = "SELECT id_Genre FROM genres WHERE name = ?";
        String insert = "INSERT IGNORE INTO media_genres (id_Media, id_Genre) VALUES (?, ?)";

        for (String name : names) {
            try (PreparedStatement ps = conn.prepareStatement(getId)) {
                ps.setString(1, name);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    try (PreparedStatement ins = conn.prepareStatement(insert)) {
                        ins.setInt(1, mediaId);
                        ins.setInt(2, rs.getInt("id_Genre"));
                        ins.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private void deleteGenreLinks(int mediaId) {
        String sql = "DELETE FROM media_genres WHERE id_Media = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mediaId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private int parseInt(String value, String errorMsg) {
        if (value == null || value.trim().isEmpty()) return 0;

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            showError(errorMsg);
            return -1;
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void clearFields() {
        titleField.clear();
        yearField.clear();
        durationField.clear();
        videoUrlField.clear();
        posterUrlField.clear();
        descArea.clear();
        actorsArea.clear();
        genreFlowPane.getChildren().clear();
        selectedMedia = null;
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }
}