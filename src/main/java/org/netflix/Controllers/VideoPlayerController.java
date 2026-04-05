package org.netflix.Controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;

import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.netflix.Models.Movie;
import org.netflix.Utils.SceneSwitcher;
import org.netflix.Utils.TransferData;


import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class VideoPlayerController implements Initializable {
    @FXML private Label timeLabel;
    @FXML private MediaView mediaView;
    @FXML private Button playBtn;
    @FXML private Slider timeSlider;
    @FXML private Slider volumeSlider;
    @FXML private Label videoTitle;
    private MediaPlayer mediaPlayer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String vd = getClass().getResource("/org/Videos/WAR_MACHINE.mp4").toExternalForm();
        Media media = new Media(vd);
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
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

        timeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (timeSlider.isValueChanging()) {
                mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
            }
        });
        timeSlider.setOnMouseClicked(event -> {
            mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
        });



        org.netflix.Models.Media m = TransferData.getMedia();
        videoTitle.setText(m.getTitle());


    }

    @FXML
    public void handleBack(ActionEvent event) {
        mediaPlayer.stop();
        SceneSwitcher.goTo(event, "/org/Views/MediaDetails.fxml");
    }

    public void handleRewind(ActionEvent actionEvent) {
        mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(10)));
    }

    public void handleForward(ActionEvent actionEvent) {
        mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(10)));
    }

    public void togglePlay(ActionEvent actionEvent) {
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
        int elapsedHours = intElapsed / (60 * 60);
        int elapsedMinutes = (intElapsed - elapsedHours * 60 * 60) / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

        int intTotal = (int) Math.floor(total.toSeconds());
        int totalHours = intTotal / (60 * 60);
        int totalMinutes = (intTotal - totalHours * 60 * 60) / 60;
        int totalSeconds = intTotal - totalHours * 60 * 60 - totalMinutes * 60;

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