package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import org.netflix.DAO.EpisodeDAO;
import org.netflix.DAO.MediaDAO;
import org.netflix.DAO.WatchHistoryDAO;
import org.netflix.Models.*;
import org.netflix.Utils.SceneSwitcher;
import org.netflix.Utils.Session;
import org.netflix.Utils.TransferData;

import java.util.*;

public class WatchHistoryController {

    @FXML private HBox    inProgressRow;
    @FXML private FlowPane completedGrid;
    @FXML private VBox    inProgressSection;
    @FXML private VBox    emptyState;
    @FXML private Label   subtitleLabel;

    @FXML
    public void initialize() {
        User user = Session.getUser();
        if (user == null) return;

        List<WatchHistory> history = new WatchHistoryDAO().findByUser(user.getId());

        Map<Integer, WatchHistory> inProgressMap  = new LinkedHashMap<>();
        Map<Integer, WatchHistory> completedMap   = new LinkedHashMap<>();

        for (WatchHistory wh : history) {
            int key = wh.getMediaId() != null ? wh.getMediaId() : -1;
            if (wh.getCompleted() == 1) {
                completedMap.putIfAbsent(key, wh);
            } else if (wh.getStoppedAtTime() > 5.0) {
                inProgressMap.putIfAbsent(key, wh);
            }
        }

        for (int key : inProgressMap.keySet()) completedMap.remove(key);

        boolean hasAnything = !inProgressMap.isEmpty() || !completedMap.isEmpty();
        emptyState.setVisible(!hasAnything);
        emptyState.setManaged(!hasAnything);

        if (!hasAnything) return;

        inProgressSection.setVisible(!inProgressMap.isEmpty());
        inProgressSection.setManaged(!inProgressMap.isEmpty());

        for (WatchHistory wh : inProgressMap.values()) {
            if (wh.getMediaId() == null) continue;
            Media media = resolveMedia(wh.getMediaId());
            if (media == null) continue;


            double totalSecs = 0;
            String progressText = "";

            if (wh.getEpisodeId() != null) {

                List<Episode> allEps = findEpisodesForMedia(media);
                for (Episode ep : allEps) {
                    if (ep.getId() == wh.getEpisodeId()) {
                        totalSecs   = ep.getDuration() * 60.0;
                        progressText = "E" + ep.getEpisodeNumber() + " — ";
                        break;
                    }
                }
            }

            double ratio = totalSecs > 0
                    ? Math.min(wh.getStoppedAtTime() / totalSecs, 1.0)
                    : 0.5;

            int mins = (int)(wh.getStoppedAtTime() / 60);
            progressText += mins + " min watched";

            VBox card = buildInProgressCard(media, ratio, progressText, wh);
            inProgressRow.getChildren().add(card);
        }


        subtitleLabel.setText(inProgressMap.size() + " in progress · " +
                completedMap.size() + " watched");

        for (WatchHistory wh : completedMap.values()) {
            if (wh.getMediaId() == null) continue;
            Media media = resolveMedia(wh.getMediaId());
            if (media == null) continue;
            completedGrid.getChildren().add(buildCompletedCard(media));
        }
    }


    private VBox buildInProgressCard(Media media, double ratio, String progressText,
                                     WatchHistory wh) {
        VBox card = new VBox(0);
        card.setPrefWidth(210);
        card.setCursor(Cursor.HAND);
        card.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 8;");


        StackPane thumbBox = new StackPane();
        ImageView poster = new ImageView();
        poster.setFitWidth(210); poster.setFitHeight(118);
        poster.setPreserveRatio(false);
        try {
            String url = media.getBackdropImageUrl() != null
                    ? media.getBackdropImageUrl() : media.getCoverImageUrl();
            if (url != null) poster.setImage(new Image(url, true));
        } catch (Exception ignored) {}

        Rectangle clip = new Rectangle(210, 118);
        clip.setArcWidth(8); clip.setArcHeight(8);
        poster.setClip(clip);


        ProgressBar bar = new ProgressBar(ratio);
        bar.setPrefWidth(210); bar.setPrefHeight(4);
        bar.setStyle("-fx-accent: #e50914; -fx-background-color: rgba(0,0,0,0.6);");
        StackPane.setAlignment(bar, Pos.BOTTOM_CENTER);


        Label playIcon = new Label("▶");
        playIcon.setStyle("-fx-text-fill: white; -fx-font-size: 28px;" +
                "-fx-background-color: rgba(0,0,0,0.55); -fx-background-radius: 24;" +
                "-fx-padding: 8 12 8 12;");
        playIcon.setOpacity(0);

        thumbBox.getChildren().addAll(poster, bar, playIcon);


        card.setOnMouseEntered(e -> { playIcon.setOpacity(1); poster.setOpacity(0.75); });
        card.setOnMouseExited(e  -> { playIcon.setOpacity(0); poster.setOpacity(1.0);  });


        VBox info = new VBox(4);
        info.setStyle("-fx-padding: 10 12 12 12;");

        Label titleLbl = new Label(media.getTitle());
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        titleLbl.setMaxWidth(186); titleLbl.setWrapText(false);

        Label progLbl = new Label(progressText);
        progLbl.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

        HBox bottomRow = new HBox(6);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        Label continueLbl = new Label("▶ Continue");
        continueLbl.setStyle("-fx-text-fill: #e50914; -fx-font-size: 11px; -fx-font-weight: bold;");
        bottomRow.getChildren().add(continueLbl);

        info.getChildren().addAll(titleLbl, progLbl, bottomRow);
        card.getChildren().addAll(thumbBox, info);


        card.setOnMouseClicked(e -> {
            TransferData.setMedia(media);
            if (wh.getEpisodeId() != null) {

                for (Episode ep : findEpisodesForMedia(media)) {
                    if (ep.getId() == wh.getEpisodeId()) {
                        TransferData.setEpisode(ep);
                        break;
                    }
                }
                SceneSwitcher.goTo(e, "/org/Views/VideoPlayer.fxml");
            } else {
                SceneSwitcher.goTo(e, "/org/Views/MediaDetails.fxml");
            }
        });

        return card;
    }

    private VBox buildCompletedCard(Media media) {
        VBox card = new VBox(0);
        card.setPrefWidth(145);
        card.setCursor(Cursor.HAND);

        StackPane thumbBox = new StackPane();
        ImageView poster = new ImageView();
        poster.setFitWidth(145); poster.setFitHeight(210);
        poster.setPreserveRatio(false);
        try {
            if (media.getCoverImageUrl() != null)
                poster.setImage(new Image(media.getCoverImageUrl(), true));
        } catch (Exception ignored) {}

        Rectangle clip = new Rectangle(145, 210);
        clip.setArcWidth(6); clip.setArcHeight(6);
        poster.setClip(clip);

        Label vuBadge = new Label("✓");
        vuBadge.setStyle("-fx-background-color: rgba(70,211,105,0.85); -fx-text-fill: white;" +
                "-fx-font-size: 13px; -fx-font-weight: bold;" +
                "-fx-padding: 4 8 4 8; -fx-background-radius: 0 0 0 6;");
        StackPane.setAlignment(vuBadge, Pos.TOP_RIGHT);

        thumbBox.getChildren().addAll(poster, vuBadge);

        card.setOnMouseEntered(e -> poster.setOpacity(0.75));
        card.setOnMouseExited(e  -> poster.setOpacity(1.0));

        Label titleLbl = new Label(media.getTitle());
        titleLbl.setStyle("-fx-text-fill: #ccc; -fx-font-size: 11px;");
        titleLbl.setMaxWidth(145); titleLbl.setWrapText(true);
        titleLbl.setStyle("-fx-text-fill: #ccc; -fx-font-size: 11px; -fx-padding: 6 0 0 0;");

        card.getChildren().addAll(thumbBox, titleLbl);
        card.setOnMouseClicked(e -> {
            TransferData.setMedia(media);
            SceneSwitcher.goTo(e, "/org/Views/MediaDetails.fxml");
        });

        return card;
    }

    private Media resolveMedia(int mediaId) {
        try {
            return MediaDAO.getAllMedia().stream()
                    .filter(m -> m.getIdMedia() == mediaId)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) { return null; }
    }

    private List<Episode> findEpisodesForMedia(Media media) {
        List<Episode> all = new ArrayList<>();
        try {
            List<Season> seasons = org.netflix.DAO.SeasonDAO.getSeasonsBySerie(media.getIdMedia());
            for (Season s : seasons) all.addAll(EpisodeDAO.getEpisodesBySeason(s.getIdSeason()));
        } catch (Exception ignored) {}
        return all;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/main.fxml");
    }
}