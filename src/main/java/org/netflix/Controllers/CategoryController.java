package org.netflix.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.netflix.Models.MediaGenre;
import org.netflix.Utils.ConxDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryController {

    @FXML private ListView<String> categoryListView;
    @FXML private TextField genreNameField;
    @FXML private TextField displayField;

    private ObservableList<String> genreNames = FXCollections.observableArrayList();
    private Connection conn = ConxDB.getInstance();

    @FXML
    public void initialize() {
        loadGenres();


        categoryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                genreNameField.setText(newVal);

                displayField.setText(newVal.replace("_", " "));
            }
        });
    }

    private void loadGenres() {
        genreNames.clear();
        List<String> genres = getGenresFromDB();
        if (genres.isEmpty()) {

            for (MediaGenre g : MediaGenre.values()) {
                genreNames.add(g.name());
            }
        } else {
            genreNames.addAll(genres);
        }
        categoryListView.setItems(genreNames);
    }

    private List<String> getGenresFromDB() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT name FROM genres ORDER BY name ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Error loading genres: " + e.getMessage());
        }
        return list;
    }

    @FXML
    public void handleSave(ActionEvent actionEvent) {
        String technicalName = genreNameField.getText().trim().toUpperCase().replace(" ", "_");
        String displayName   = displayField.getText().trim();

        if (technicalName.isEmpty()) {
            showError("Technical name cannot be empty.");
            return;
        }

        String selected = categoryListView.getSelectionModel().getSelectedItem();

        if (selected != null && selected.equalsIgnoreCase(technicalName)) {

            showInfo("Genre '" + technicalName + "' already exists — no changes needed.");
            return;
        }


        String sql = "INSERT IGNORE INTO genres (name) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, technicalName);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                showInfo("Genre '" + technicalName + "' added successfully.");
            } else {
                showInfo("Genre '" + technicalName + "' already exists.");
            }
            loadGenres();
            clearFields();
        } catch (SQLException e) {
            showError("Failed to save genre: " + e.getMessage());
        }
    }

    @FXML
    public void handleDelete(ActionEvent actionEvent) {
        String selected = categoryListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a genre to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete genre '" + selected + "'?\nThis will also remove it from all associated media.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (deleteGenreFromDB(selected)) {
                    showInfo("Genre '" + selected + "' deleted.");
                    loadGenres();
                    clearFields();
                } else {
                    showError("Failed to delete genre '" + selected + "'.");
                }
            }
        });
    }

    private boolean deleteGenreFromDB(String genreName) {

        String deleteLinks = "DELETE FROM media_genres WHERE id_Genre = " +
                "(SELECT id_Genre FROM genres WHERE name = ?)";
        String deleteGenre = "DELETE FROM genres WHERE name = ?";
        try {
            try (PreparedStatement ps = conn.prepareStatement(deleteLinks)) {
                ps.setString(1, genreName);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(deleteGenre)) {
                ps.setString(1, genreName);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting genre: " + e.getMessage());
            return false;
        }
    }

    private void clearFields() {
        genreNameField.clear();
        displayField.clear();
        categoryListView.getSelectionModel().clearSelection();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }
}