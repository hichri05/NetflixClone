package org.netflix.Controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.netflix.DAO.WatchHistoryDAO;
import org.netflix.Models.Movie;
import org.netflix.Models.User;
import org.netflix.Models.WatchHistory;
import org.netflix.Utils.SceneSwitcher;
import org.netflix.Utils.Session;
import org.netflix.Utils.TransferData;

import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ResourceBundle;

public class VideoPlayerController implements Initializable {

    @FXML private Label     timeLabel;
    @FXML private MediaView mediaView;
    @FXML private Button    playBtn;
    @FXML private Slider    timeSlider;
    @FXML private Slider    volumeSlider;
    @FXML private Label     videoTitle;

    private MediaPlayer mediaPlayer;
    private org.netflix.Models.Media currentMedia;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentMedia = TransferData.getMedia();

        String videoSource = resolveVideoSource();

        try {
            Media media = new Media(videoSource);
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
        } catch (Exception e) {
            System.err.println("Could not load video: " + videoSource);
            e.printStackTrace();

            try {
                String fallback = getClass().getResource("/org/Videos/WAR_MACHINE.mp4").toExternalForm();
                Media media = new Media(fallback);
                mediaPlayer = new MediaPlayer(media);
                mediaView.setMediaPlayer(mediaPlayer);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        }


        Platform.runLater(() -> {
            if (mediaView.getScene() != null) {
                mediaView.fitWidthProperty().bind(mediaView.getScene().widthProperty());
                mediaView.fitHeightProperty().bind(mediaView.getScene().heightProperty());
            }
        });



        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!timeSlider.isValueChanging()) {
                timeSlider.setValue(newTime.toSeconds());
            }
            timeLabel.setText(formatTime(newTime, mediaPlayer.getTotalDuration()));
        });


        mediaPlayer.setOnReady(() -> {
            timeSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
            mediaPlayer.volumeProperty().bind(volumeSlider.valueProperty().divide(100));
            mediaPlayer.play();
            playBtn.setText("⏸");
        });


        timeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (timeSlider.isValueChanging()) {
                mediaPlayer.seek(Duration.seconds(newVal.doubleValue()));
            }
        });
        timeSlider.setOnMouseClicked(e -> mediaPlayer.seek(Duration.seconds(timeSlider.getValue())));


        mediaPlayer.setOnEndOfMedia(() -> {
            saveWatchHistory(true);
            playBtn.setText("▶");
        });

        // Title
        if (currentMedia != null) {
            videoTitle.setText(currentMedia.getTitle());
        }
    }


    private String resolveVideoSource() {
        if (currentMedia instanceof Movie) {
            String url = ((Movie) currentMedia).getVideoUrl();
            if (url != null && !url.isBlank()) {
                if (!url.startsWith("http") && !url.startsWith("file:")) {
                    java.io.File f = new java.io.File(url);
                    if (f.exists()) return f.toURI().toString();
                }
                return url;
            }
        }

        URL res = getClass().getResource("/org/Videos/WAR_MACHINE.mp4");
        if (res != null) return res.toExternalForm();
        return "";
    }



    private void saveWatchHistory(boolean completed) {
        User user = Session.getUser();
        if (user == null || currentMedia == null) return;
        double stoppedAt = mediaPlayer != null
                ? mediaPlayer.getCurrentTime().toSeconds() : 0.0;
        WatchHistory wh = new WatchHistory(
                user.getId(),
                currentMedia.getIdMedia(),
                stoppedAt,
                Timestamp.from(Instant.now()),
                completed ? 1 : 0
        );
        WatchHistoryDAO.addToHistory(wh);
    }



    @FXML
    public void handleBack(ActionEvent event) {
        if (mediaPlayer != null) {
            saveWatchHistory(false);
            mediaPlayer.stop();
        }
        SceneSwitcher.goTo(event, "/org/Views/MediaDetails.fxml");
    }

    @FXML
    public void handleRewind(ActionEvent actionEvent) {
        if (mediaPlayer != null)
            mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(10)));
    }

    @FXML
    public void handleForward(ActionEvent actionEvent) {
        if (mediaPlayer != null)
            mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(10)));
    }

    @FXML
    public void togglePlay(ActionEvent actionEvent) {
        if (mediaPlayer == null) return;
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playBtn.setText("▶");
        } else {
            mediaPlayer.play();
            playBtn.setText("⏸");
        }
    }



    private String formatTime(Duration elapsed, Duration total) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours   = intElapsed / 3600;
        int elapsedMinutes = (intElapsed % 3600) / 60;
        int elapsedSeconds = intElapsed % 60;

        int intTotal = (int) Math.floor(total.toSeconds());
        int totalHours   = intTotal / 3600;
        int totalMinutes = (intTotal % 3600) / 60;
        int totalSeconds = intTotal % 60;

        if (totalHours > 0) {
            return String.format("%d:%02d:%02d / %d:%02d:%02d",
                    elapsedHours, elapsedMinutes, elapsedSeconds,
                    totalHours, totalMinutes, totalSeconds);
        } else {
            return String.format("%02d:%02d / %02d:%02d",
                    elapsedMinutes, elapsedSeconds,
                    totalMinutes, totalSeconds);
        }
    }
}