package org.netflix.Controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.netflix.DAO.EpisodeDAO;
import org.netflix.DAO.SeasonDAO;
import org.netflix.DAO.WatchHistoryDAO;
import org.netflix.Models.Episode;
import org.netflix.Models.Movie;
import org.netflix.Models.Season;
import org.netflix.Models.User;
import org.netflix.Models.WatchHistory;
import org.netflix.Utils.SceneSwitcher;
import org.netflix.Utils.Session;
import org.netflix.Utils.TransferData;

import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.ResourceBundle;

public class VideoPlayerController implements Initializable {


    @FXML private Label      timeLabel;
    @FXML private MediaView  mediaView;
    @FXML private Button     playBtn;
    @FXML private Slider     timeSlider;
    @FXML private Slider     volumeSlider;
    @FXML private Label      videoTitle;
    @FXML private Label      episodeSubtitle;


    @FXML private Button     nextEpisodeBtn;
    @FXML private VBox       nextEpisodeOverlay;
    @FXML private Label      countdownLabel;
    @FXML private ProgressBar countdownBar;
    @FXML private Label      nextEpTitle;
    @FXML private Label      nextEpDesc;
    @FXML private ImageView  nextEpThumbnail;


    private MediaPlayer mediaPlayer;
    private org.netflix.Models.Media currentMedia;
    private Episode     currentEpisode;
    private Episode     nextEpisode;

    private Timeline    countdownTimeline;
    private int         countdownSeconds = 10;

    private static final int COUNTDOWN_DURATION = 10;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentMedia   = TransferData.getMedia();
        currentEpisode = TransferData.getEpisode();

        resolveNextEpisode();
        setupPlayer();
        setupTitle();
    }


    private void setupPlayer() {
        String videoSource = resolveVideoSource();

        try {
            Media media = new Media(videoSource);
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
        } catch (Exception e) {
            System.err.println("Could not load video: " + videoSource);
            try {
                String fallback = getClass().getResource("/org/Videos/WAR_MACHINE.mp4").toExternalForm();
                mediaPlayer = new MediaPlayer(new Media(fallback));
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
            if (nextEpisode != null) {
                showNextEpisodeOverlay();
            }
        });


        seekToSavedPosition();
    }

    private void seekToSavedPosition() {
        User user = Session.getUser();
        if (user == null || currentEpisode == null) return;

        List<WatchHistory> history = new WatchHistoryDAO().findByUser(user.getId());
        for (WatchHistory wh : history) {
            if (wh.getEpisodeId() != null
                    && wh.getEpisodeId() == currentEpisode.getId()
                    && wh.getCompleted() == 0
                    && wh.getStoppedAtTime() > 5) {
                mediaPlayer.setOnReady(() -> {
                    timeSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
                    mediaPlayer.volumeProperty().bind(volumeSlider.valueProperty().divide(100));
                    mediaPlayer.seek(Duration.seconds(wh.getStoppedAtTime()));
                    mediaPlayer.play();
                    playBtn.setText("⏸");
                });
                return;
            }
        }
    }


    private void resolveNextEpisode() {
        nextEpisode = null;

        if (currentEpisode == null || currentMedia == null) {
            setupNextEpisodeButton();
            return;
        }

        int seasonId = currentEpisode.getSeasonId();
        List<Episode> episodes = EpisodeDAO.getEpisodesBySeason(seasonId);

        boolean foundCurrent = false;
        for (Episode ep : episodes) {
            if (foundCurrent) {
                nextEpisode = ep;
                break;
            }
            if (ep.getId() == currentEpisode.getId()) {
                foundCurrent = true;
            }
        }

        if (nextEpisode == null) {
            List<Season> seasons = SeasonDAO.getSeasonsBySerie(currentMedia.getIdMedia());
            boolean foundCurrentSeason = false;
            for (Season season : seasons) {
                if (foundCurrentSeason) {
                    List<Episode> nextSeasonEps = EpisodeDAO.getEpisodesBySeason(season.getIdSeason());
                    if (!nextSeasonEps.isEmpty()) {
                        nextEpisode = nextSeasonEps.get(0);
                    }
                    break;
                }
                if (season.getIdSeason() == seasonId) {
                    foundCurrentSeason = true;
                }
            }
        }

        setupNextEpisodeButton();
    }

    private void setupNextEpisodeButton() {
        if (nextEpisode != null) {
            nextEpisodeBtn.setVisible(true);
            nextEpisodeBtn.setManaged(true);
        }
    }

    private void setupTitle() {
        if (currentMedia != null) {
            videoTitle.setText(currentMedia.getTitle());
        }
        if (currentEpisode != null) {
            episodeSubtitle.setText(
                    "S" + resolveSeasonNumber(currentEpisode.getSeasonId())
                            + " · E" + currentEpisode.getEpisodeNumber()
                            + " — " + currentEpisode.getTitle()
            );
        }
    }

    private int resolveSeasonNumber(int seasonId) {
        if (currentMedia == null) return 1;
        List<Season> seasons = SeasonDAO.getSeasonsBySerie(currentMedia.getIdMedia());
        for (Season s : seasons) {
            if (s.getIdSeason() == seasonId) return s.getSeasonNumber();
        }
        return seasonId;
    }



    private void showNextEpisodeOverlay() {
        if (nextEpisode == null) return;

        nextEpTitle.setText(
                "E" + nextEpisode.getEpisodeNumber() + " — " + nextEpisode.getTitle()
        );
        nextEpDesc.setText(
                nextEpisode.getDescription() != null ? nextEpisode.getDescription() : ""
        );

        if (nextEpisode.getThumbnailPath() != null && !nextEpisode.getThumbnailPath().isBlank()) {
            nextEpThumbnail.setImage(new Image(nextEpisode.getThumbnailPath(), true));
        }

        nextEpisodeOverlay.setVisible(true);
        nextEpisodeOverlay.setManaged(true);

        countdownSeconds = COUNTDOWN_DURATION;
        countdownLabel.setText(String.valueOf(countdownSeconds));
        countdownBar.setProgress(1.0);

        countdownTimeline = new Timeline();
        for (int i = 0; i <= COUNTDOWN_DURATION; i++) {
            final int remaining = COUNTDOWN_DURATION - i;
            final double progress = remaining / (double) COUNTDOWN_DURATION;
            countdownTimeline.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(i), ev -> {
                        countdownLabel.setText(String.valueOf(remaining));
                        countdownBar.setProgress(progress);
                        if (remaining == 0) {
                            playNextEpisode();
                        }
                    })
            );
        }
        countdownTimeline.play();
    }

    private void hideNextEpisodeOverlay() {
        if (countdownTimeline != null) countdownTimeline.stop();
        nextEpisodeOverlay.setVisible(false);
        nextEpisodeOverlay.setManaged(false);
    }

    @FXML
    public void handlePlayNext(ActionEvent event) {
        hideNextEpisodeOverlay();
        playNextEpisode();
    }

    @FXML
    public void handleCancelNext(ActionEvent event) {
        hideNextEpisodeOverlay();
    }

    @FXML
    public void handleNextEpisode(ActionEvent event) {
        if (nextEpisode == null) return;
        saveWatchHistory(false);
        if (mediaPlayer != null) mediaPlayer.stop();
        hideNextEpisodeOverlay();
        TransferData.setEpisode(nextEpisode);
        SceneSwitcher.goTo(event, "/org/Views/VideoPlayer.fxml");
    }

    private void playNextEpisode() {
        if (nextEpisode == null) return;
        if (mediaPlayer != null) mediaPlayer.stop();

        // Navigate: set next episode in TransferData then reload
        TransferData.setEpisode(nextEpisode);
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/org/Views/VideoPlayer.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) mediaView.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String resolveVideoSource() {
        if (currentEpisode != null) {
            String path = currentEpisode.getFilePath();
            if (path != null && !path.isBlank()) {
                if (!path.startsWith("http") && !path.startsWith("file:")) {
                    java.io.File f = new java.io.File(path);
                    if (f.exists()) return f.toURI().toString();
                }
                return path;
            }
        }

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

        Integer episodeId = (currentEpisode != null) ? currentEpisode.getId() : null;

        WatchHistory wh = new WatchHistory(
                user.getId(),
                currentMedia.getIdMedia(),
                episodeId,
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
        if (countdownTimeline != null) countdownTimeline.stop();
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
                    elapsedMinutes, elapsedSeconds, totalMinutes, totalSeconds);
        }
    }
}