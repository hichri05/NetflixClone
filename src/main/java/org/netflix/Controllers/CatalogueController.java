package org.netflix.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import org.netflix.Models.Genre;
import org.netflix.Models.Media;
import org.netflix.DAO.MediaDAO;
import org.netflix.Models.MediaGenre;
import org.netflix.Utils.SceneSwitcher;

import java.util.List;

public class CatalogueController {

    @FXML private ComboBox genreSelectionCombo;
    @FXML
    private TableView<Media> mediaTable;
    @FXML
    private TableColumn<Media, String> colTitle;
    @FXML
    private TableColumn<Media, String> colType;

    @FXML
    private TextField titleField, yearField, durationField, videoUrlField, posterUrlField;
    @FXML
    private TextArea descArea, actorsArea;
    @FXML
    private ComboBox<String> typeCombo, filterTypeCombo;
    @FXML
    private FlowPane genreFlowPane;

    private ObservableList<Media> mediaList = FXCollections.observableArrayList();
    private Media selectedMedia;

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        refreshTable();

        mediaTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showMediaDetails(newSelection);
            }
        });

        typeCombo.setItems(FXCollections.observableArrayList("Movie", "Series"));

        ObservableList<String> genreOptions = FXCollections.observableArrayList();

        for (MediaGenre g : MediaGenre.values()) {
            genreOptions.add(g.name());
        }

        genreSelectionCombo.setItems(genreOptions);

    }

    private void refreshTable() {
        mediaList.setAll(MediaDAO.getAllMedia());
        mediaTable.setItems(mediaList);
    }

    private void showMediaDetails(Media media) {
        clearFields();
        selectedMedia = media;
        titleField.setText(media.getTitle());
        descArea.setText(media.getDescription());
        posterUrlField.setText(media.getCoverImageUrl());
        typeCombo.setValue(media.getType());
        List<Genre> genreList = media.getGenres();
        for (Genre genre : genreList) {
            if (genre.getName() != null) addGenreTag(genre.getName().toString());
        }

    }

    public void handlePrepareNew(ActionEvent actionEvent) {
        selectedMedia = null;
        clearFields();
        titleField.requestFocus();
    }

    public void handleOpenEpisodeManager(ActionEvent actionEvent) {
        selectedMedia = mediaTable.getSelectionModel().getSelectedItem();
        if (selectedMedia != null) {
            SceneSwitcher.goTo(actionEvent, "/org/Views/EpisodeManager.fxml");
        }else{
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a Series first!");
            alert.show();
            return;
        }
    }

    public void handleDelete(ActionEvent actionEvent) {
        if (selectedMedia != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + selectedMedia.getTitle() + "?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    MediaDAO.deleteMedia(selectedMedia.getIdMedia());
                    refreshTable();
                    clearFields();
                }
            });
        }
    }

    public void handleSave(ActionEvent actionEvent) {
        if (selectedMedia == null) {
            Media newMedia = new Media(titleField.getText(), descArea.getText(), posterUrlField.getText());
            MediaDAO.addMedia(newMedia);
        } else {
            selectedMedia.setTitle(titleField.getText());
            selectedMedia.setDescription(descArea.getText());
            MediaDAO.updateMedia(selectedMedia);
        }
        refreshTable();
        clearFields();
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
    }
    private void addGenreTag(String genreName) {
        boolean exists = genreFlowPane.getChildren().stream()
                .anyMatch(node -> node instanceof Label && ((Label) node).getText().equals(genreName));

        if (!exists) {

            Label tag = new Label(genreName);
            tag.getStyleClass().add("genre-tag");
            tag.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 15; -fx-cursor: hand;");

            tag.setOnMouseClicked(event -> {
                genreFlowPane.getChildren().remove(tag);
            });

            genreFlowPane.getChildren().add(tag);
        }
    }

    public void handleAddGenreFromCombo(ActionEvent event) {
        String selectedGenre = genreSelectionCombo.getValue().toString();
        if (selectedGenre != null && !selectedGenre.isEmpty()) {
            addGenreTag(selectedGenre);
            genreSelectionCombo.getSelectionModel().clearSelection();
        }
    }
}