package org.netflix.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.netflix.DAO.EpisodeDAO;
import org.netflix.DAO.SeasonDAO;
import org.netflix.Models.Episode;
import org.netflix.Models.Media;
import org.netflix.Models.Season;
import org.netflix.Utils.ConxDB;
import org.netflix.Utils.TransferData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EpisodeController {

    @FXML private Text seriesTitleHeader;


    @FXML private Spinner<Integer> seasonSpinner;
    @FXML private Spinner<Integer> episodeSpinner;
    @FXML private TextField epVideoUrl;


    @FXML private TableView<Episode> episodeTable;
    @FXML private TableColumn<Episode, Integer> colSeason;
    @FXML private TableColumn<Episode, Integer> colNumber;
    @FXML private TableColumn<Episode, String>  colUrl;

    private ObservableList<Episode> episodes = FXCollections.observableArrayList();
    private Media currentMedia;
    private SeasonDAO seasonDAO = new SeasonDAO();
    private Connection conn = ConxDB.getInstance();

    @FXML
    public void initialize() {
        currentMedia = TransferData.getMedia();

        if (currentMedia != null) {
            seriesTitleHeader.setText("Manage Episodes — " + currentMedia.getTitle());
        }


        seasonSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1));
        episodeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 200, 1));


        colSeason.setCellValueFactory(new PropertyValueFactory<>("seasonId"));
        colNumber.setCellValueFactory(new PropertyValueFactory<>("episodeNumber"));
        colUrl.setCellValueFactory(new PropertyValueFactory<>("filePath"));


        colSeason.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }

                Episode ep = getTableRow().getItem();
                setText(resolveSeasonNumber(ep.getSeasonId()));
            }
        });

        loadAllEpisodes();
    }

    private String resolveSeasonNumber(int seasonId) {
        String sql = "SELECT saisonNumber FROM saison WHERE id_Saison = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seasonId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return String.valueOf(rs.getInt("saisonNumber"));
        } catch (SQLException e) {
            System.err.println("Could not resolve season number: " + e.getMessage());
        }
        return String.valueOf(seasonId);
    }

    private void loadAllEpisodes() {
        episodes.clear();
        if (currentMedia == null) return;


        List<Season> seasons = seasonDAO.getSeasonsBySerie(currentMedia.getIdMedia());
        for (Season s : seasons) {
            List<Episode> eps = EpisodeDAO.getEpisodesBySeason(s.getIdSeason());
            episodes.addAll(eps);
        }
        episodeTable.setItems(episodes);
    }

    @FXML
    public void handleAddEpisode(ActionEvent actionEvent) {
        if (currentMedia == null) {
            showError("No series selected.");
            return;
        }

        String videoUrl = epVideoUrl.getText().trim();
        if (videoUrl.isEmpty()) {
            showError("Please enter a video URL.");
            return;
        }

        int seasonNumber  = seasonSpinner.getValue();
        int episodeNumber = episodeSpinner.getValue();


        int seasonId = getOrCreateSeason(currentMedia.getIdMedia(), seasonNumber);
        if (seasonId == -1) {
            showError("Failed to find or create season " + seasonNumber + ".");
            return;
        }


        if (episodeExistsInSeason(seasonId, episodeNumber)) {
            showError("Episode " + episodeNumber + " already exists in Season " + seasonNumber + ".");
            return;
        }

        Episode episode = new Episode(
                0,
                seasonId,
                episodeNumber,
                "S" + seasonNumber + "E" + episodeNumber, // default title
                videoUrl,
                null
        );

        boolean ok = EpisodeDAO.addEpisode(episode);
        if (ok) {
            epVideoUrl.clear();
            loadAllEpisodes();
            showInfo("Episode S" + seasonNumber + "E" + episodeNumber + " added.");
        } else {
            showError("Failed to add episode.");
        }
    }

    private boolean episodeExistsInSeason(int seasonId, int episodeNumber) {
        String sql = "SELECT COUNT(*) FROM episode WHERE id_Saison = ? AND episodeNumber = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seasonId);
            ps.setInt(2, episodeNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Duplicate check failed: " + e.getMessage());
        }
        return false;
    }

    private int getOrCreateSeason(int serieId, int seasonNumber) {

        String sql = "SELECT id_Saison FROM saison WHERE id_Serie = ? AND saisonNumber = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, serieId);
            ps.setInt(2, seasonNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_Saison");
        } catch (SQLException e) {
            System.err.println("Season lookup failed: " + e.getMessage());
        }


        Season newSeason = new Season(0, serieId, seasonNumber,
                "Season " + seasonNumber, "");
        boolean created = SeasonDAO.addSeason(newSeason);
        if (!created) return -1;

        // Retrieve the new ID
        String getId = "SELECT id_Saison FROM saison WHERE id_Serie = ? AND saisonNumber = ?";
        try (PreparedStatement ps = conn.prepareStatement(getId)) {
            ps.setInt(1, serieId);
            ps.setInt(2, seasonNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_Saison");
        } catch (SQLException e) {
            System.err.println("Season ID retrieval failed: " + e.getMessage());
        }
        return -1;
    }

    @FXML
    public void handleDeleteEpisode(ActionEvent actionEvent) {
        Episode selected = episodeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select an episode to remove.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete " + selected.getTitle() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean ok = EpisodeDAO.deleteEpisode(selected.getId());
                if (ok) {
                    loadAllEpisodes();
                } else {
                    showError("Failed to delete episode.");
                }
            }
        });
    }

    @FXML
    public void handleClose(ActionEvent actionEvent) {
        Stage stage = (Stage) episodeTable.getScene().getWindow();
        stage.close();
    }


    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }
}