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
import java.util.ArrayList;
import java.util.List;

public class CatalogueController {

    @FXML private ComboBox<String> genreSelectionCombo;
    @FXML private TableView<Media> mediaTable;
    @FXML private TableColumn<Media, String> colTitle;
    @FXML private TableColumn<Media, String> colType;

    @FXML private TextField titleField, yearField, durationField, videoUrlField, posterUrlField;
    @FXML private TextArea  descArea, actorsArea;
    @FXML private ComboBox<String> typeCombo, filterTypeCombo;
    @FXML private FlowPane genreFlowPane;
    @FXML private Button manageEpisodesBtn;

    private ObservableList<Media> mediaList = FXCollections.observableArrayList();
    private Media selectedMedia;
    private Connection conn = ConxDB.getInstance();

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));


        refreshTable();

        mediaTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSel, newSel) -> { if (newSel != null) showMediaDetails(newSel); });

        typeCombo.setItems(FXCollections.observableArrayList("Movie", "Serie"));
        filterTypeCombo.setItems(FXCollections.observableArrayList("All", "Movie", "Serie"));
        filterTypeCombo.setValue("All");
        filterTypeCombo.valueProperty().addListener((obs, o, n) -> applyFilter(n));

        ObservableList<String> genreOptions = FXCollections.observableArrayList();
        for (MediaGenre g : MediaGenre.values()) genreOptions.add(g.name());
        genreSelectionCombo.setItems(genreOptions);


    }


    private void refreshTable() {
        mediaList.setAll(MediaDAO.getAllMedia());
        mediaTable.setItems(mediaList);
    }

    private void applyFilter(String type) {
        if (type == null || type.equals("All")) {
            mediaTable.setItems(mediaList);
        } else {
            ObservableList<Media> filtered = mediaList.filtered(
                    m -> type.equalsIgnoreCase(m.getType()));
            mediaTable.setItems(filtered);
        }
    }


    private void showMediaDetails(Media media) {
        clearFields();
        selectedMedia = media;
        titleField.setText(media.getTitle() != null ? media.getTitle() : "");
        descArea.setText(media.getDescription() != null ? media.getDescription() : "");
        posterUrlField.setText(media.getCoverImageUrl() != null ? media.getCoverImageUrl() : "");
        typeCombo.setValue(media.getType());
        yearField.setText(media.getReleaseYear() > 0
                ? String.valueOf(media.getReleaseYear()) : "");


        loadMovieExtras(media);

        List<Genre> genreList = media.getGenres();
        if (genreList != null) {
            for (Genre genre : genreList) {
                if (genre.getName() != null) addGenreTag(genre.getName().toString());
            }
        }


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
                String url = rs.getString("videoUrl");
                int dur = rs.getInt("duration_minutes");
                if (url != null) videoUrlField.setText(url);
                if (dur > 0) durationField.setText(String.valueOf(dur));
            }
        } catch (SQLException e) {
            System.err.println("Error loading movie extras: " + e.getMessage());
        }
    }



    @FXML
    public void handlePrepareNew(ActionEvent actionEvent) {
        selectedMedia = null;
        clearFields();
        titleField.requestFocus();
        manageEpisodesBtn.setVisible(false);
        manageEpisodesBtn.setManaged(false);
    }

    @FXML
    public void handleSave(ActionEvent actionEvent) {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showError("Title is required.");
            return;
        }

        String type = typeCombo.getValue();
        if (type == null) {
            showError("Please select a type (Movie or Serie).");
            return;
        }

        int year = 0;
        if (!yearField.getText().trim().isEmpty()) {
            try { year = Integer.parseInt(yearField.getText().trim()); }
            catch (NumberFormatException e) { showError("Year must be a number."); return; }
        }

        int duration = 0;
        if (!durationField.getText().trim().isEmpty()) {
            try { duration = Integer.parseInt(durationField.getText().trim()); }
            catch (NumberFormatException e) { showError("Duration must be a number."); return; }
        }

        String description = descArea.getText().trim();
        String posterUrl   = posterUrlField.getText().trim();
        String videoUrl    = videoUrlField.getText().trim();


        List<String> selectedGenres = new ArrayList<>();
        genreFlowPane.getChildren().forEach(node -> {
            if (node instanceof Label) selectedGenres.add(((Label) node).getText());
        });

        if (selectedMedia == null) {

            Media newMedia = new Media();
            newMedia.setTitle(title);
            newMedia.setDescription(description);
            newMedia.setCoverImageUrl(posterUrl);
            newMedia.setReleaseYear(year);
            newMedia.setType(type);

            boolean ok = MediaDAO.addMedia(newMedia);
            if (!ok) { showError("Failed to add media."); return; }

            int newId = newMedia.getIdMedia();


            if ("Movie".equalsIgnoreCase(type)) {
                insertMovieRow(newId, videoUrl, duration);
            } else {
                insertSerieRow(newId);
            }


            saveGenreLinks(newId, selectedGenres);

        } else {

            selectedMedia.setTitle(title);
            selectedMedia.setDescription(description);
            selectedMedia.setCoverImageUrl(posterUrl);
            selectedMedia.setReleaseYear(year);
            selectedMedia.setType(type);

            MediaDAO.updateMedia(selectedMedia);

            int id = selectedMedia.getIdMedia();


            if ("Movie".equalsIgnoreCase(type)) {
                updateMovieRow(id, videoUrl, duration);
            }


            deleteGenreLinks(id);
            saveGenreLinks(id, selectedGenres);
        }

        refreshTable();
        clearFields();
        showInfo("Saved successfully.");
    }

    @FXML
    public void handleDelete(ActionEvent actionEvent) {
        if (selectedMedia == null) { showError("Select a media item first."); return; }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + selectedMedia.getTitle() + "\"?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                MediaDAO.deleteMedia(selectedMedia.getIdMedia());
                refreshTable();
                clearFields();
            }
        });
    }

    @FXML
    public void handleOpenEpisodeManager(ActionEvent actionEvent) {
        selectedMedia = mediaTable.getSelectionModel().getSelectedItem();
        if (selectedMedia == null || !"Serie".equalsIgnoreCase(selectedMedia.getType())) {
            showError("Please select a Series first.");
            return;
        }
        TransferData.setMedia(selectedMedia);
        SceneSwitcher.goTo(actionEvent, "/org/Views/EpisodeManager.fxml");
    }



    @FXML
    public void handleAddGenreFromCombo(ActionEvent event) {
        Object val = genreSelectionCombo.getValue();
        if (val == null) return;
        String selected = val.toString();
        if (!selected.isEmpty()) {
            addGenreTag(selected);
            genreSelectionCombo.getSelectionModel().clearSelection();
        }
    }

    private void addGenreTag(String genreName) {
        boolean exists = genreFlowPane.getChildren().stream()
                .anyMatch(node -> node instanceof Label
                        && ((Label) node).getText().equals(genreName));
        if (exists) return;

        Label tag = new Label(genreName);
        tag.setStyle("-fx-background-color: #333; -fx-text-fill: white; " +
                "-fx-padding: 5 10; -fx-background-radius: 15; -fx-cursor: hand;");
        tag.setOnMouseClicked(e -> genreFlowPane.getChildren().remove(tag));
        genreFlowPane.getChildren().add(tag);
    }



    private void insertMovieRow(int mediaId, String videoUrl, int duration) {
        String sql = "INSERT IGNORE INTO movie (id_Media, videoUrl, duration_minutes) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mediaId);
            ps.setString(2, videoUrl);
            ps.setInt(3, duration);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting movie row: " + e.getMessage());
        }
    }

    private void updateMovieRow(int mediaId, String videoUrl, int duration) {
        String sql = "UPDATE movie SET videoUrl = ?, duration_minutes = ? WHERE id_Media = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, videoUrl);
            ps.setInt(2, duration);
            ps.setInt(3, mediaId);
            int rows = ps.executeUpdate();
            if (rows == 0) insertMovieRow(mediaId, videoUrl, duration);
        } catch (SQLException e) {
            System.err.println("Error updating movie row: " + e.getMessage());
        }
    }

    private void insertSerieRow(int mediaId) {
        String sql = "INSERT IGNORE INTO serie (id_Media, nbrSaison) VALUES (?, 0)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mediaId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting serie row: " + e.getMessage());
        }
    }

    private void saveGenreLinks(int mediaId, List<String> genreNames) {
        String getIdSql  = "SELECT id_Genre FROM genres WHERE name = ?";
        String insertSql = "INSERT IGNORE INTO media_genres (id_Media, id_Genre) VALUES (?, ?)";
        for (String name : genreNames) {
            try (PreparedStatement ps = conn.prepareStatement(getIdSql)) {
                ps.setString(1, name);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int genreId = rs.getInt("id_Genre");
                    try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                        ins.setInt(1, mediaId);
                        ins.setInt(2, genreId);
                        ins.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error saving genre link: " + e.getMessage());
            }
        }
    }

    private void deleteGenreLinks(int mediaId) {
        String sql = "DELETE FROM media_genres WHERE id_Media = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mediaId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting genre links: " + e.getMessage());
        }
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