package org.netflix.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.netflix.Models.Media;
import org.netflix.DAO.MediaDAO;

public class CatalogueController {

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

    private ObservableList<Media> mediaList = FXCollections.observableArrayList();
    private Media selectedMedia;

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colType.setCellValueFactory(new PropertyValueFactory<>("category"));

        refreshTable();

        mediaTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showMediaDetails(newSelection);
            }
        });

        typeCombo.setItems(FXCollections.observableArrayList("Movie", "Series"));
    }

    private void refreshTable() {
        //mediaList.setAll(MediaDAO.getAllMedia());
        mediaTable.setItems(mediaList);
    }

    private void showMediaDetails(Media media) {
        selectedMedia = media;
        titleField.setText(media.getTitle());
        descArea.setText(media.getDescription());
        posterUrlField.setText(media.getCoverImageUrl());
        
    }

    public void handlePrepareNew(ActionEvent actionEvent) {
        selectedMedia = null;
        clearFields();
        titleField.requestFocus();
    }

    public void handleOpenEpisodeManager(ActionEvent actionEvent) {
    }

    public void handleDelete(ActionEvent actionEvent) {
        if (selectedMedia != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + selectedMedia.getTitle() + "?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    //MediaDAO.deleteMedia(selectedMedia.getId());
                    refreshTable();
                    clearFields();
                }
            });
        }
    }

    public void handleSave(ActionEvent actionEvent) {
        if (selectedMedia == null) {
            //Media newMedia = new Media(titleField.getText(), descArea.getText(), posterUrlField.getText());
            //MediaDAO.addMedia(newMedia);
        } else {
            selectedMedia.setTitle(titleField.getText());
            selectedMedia.setDescription(descArea.getText());
            //MediaDAO.updateMedia(selectedMedia);
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
    }
}