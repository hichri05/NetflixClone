package org.netflix.Controllers;

import javafx.animation.KeyFrame;
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
import javafx.stage.Stage;
import javafx.util.Duration;
import org.netflix.DAO.EpisodeDAO;
import org.netflix.DAO.SeasonDAO;
import org.netflix.DAO.WatchHistoryDAO;
import org.netflix.Models.*;
import org.netflix.Utils.SceneSwitcher;
import org.netflix.Utils.Session;
import org.netflix.Utils.TransferData;

import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.ResourceBundle;

public class VideoPlayerController implements Initializable {

    // ── Standard controls ─────────────────────────────────────────────────────
    @FXML private Label       timeLabel;
    @FXML private MediaView   mediaView;
    @FXML private Button      playBtn;
    @FXML private Button      fullscreenBtn;
    @FXML private Slider      timeSlider;
    @FXML private Slider      volumeSlider;
    @FXML private Label       videoTitle;
    @FXML private Label       episodeSubtitle;

    // ── Next episode overlay ───────────────────────────────────────────────────
    @FXML private Button      nextEpisodeBtn;
    @FXML private VBox        nextEpisodeOverlay;
    @FXML private Label       countdownLabel;
    @FXML private ProgressBar countdownBar;
    @FXML private Label       nextEpTitle;
    @FXML private Label       nextEpDesc;
    @FXML private ImageView   nextEpThumbnail;

    // ── State ─────────────────────────────────────────────────────────────────
    private MediaPlayer mediaPlayer;
    private org.netflix.Models.Media currentMedia;
    private Episode     currentEpisode;
    private Episode     nextEpisode;
    private Timeline    countdownTimeline;
    private boolean     isFullscreen = false;

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
                URL fallback = getClass().getResource("/org/Videos/WAR_MACHINE.mp4");
                if (fallback != null) {
                    mediaPlayer = new MediaPlayer(new Media(fallback.toExternalForm()));
                    mediaView.setMediaPlayer(mediaPlayer);
                } else return;
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

        // On ready
        mediaPlayer.setOnReady(() -> {
            timeSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
            mediaPlayer.volumeProperty().bind(volumeSlider.valueProperty().divide(100));
            mediaPlayer.play();
            playBtn.setText("⏸");
        });

        // Seek on slider drag
        timeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (timeSlider.isValueChanging()) {
                mediaPlayer.seek(Duration.seconds(newVal.doubleValue()));
            }
        });
        timeSlider.setOnMouseClicked(e ->
                mediaPlayer.seek(Duration.seconds(timeSlider.getValue())));

        // End of media → binge-watch overlay
        mediaPlayer.setOnEndOfMedia(() -> {
            saveWatchHistory(true);
            playBtn.setText("▶");
            if (nextEpisode != null) showNextEpisodeOverlay();
        });

        // Smart resume: seek to saved position
        seekToSavedPosition();
    }

    // ── Smart resume seek ────────────────────────────────────────────────────
    private void seekToSavedPosition() {
        User user = Session.getUser();
        if (user == null || currentEpisode == null) return;

        List<WatchHistory> history = new WatchHistoryDAO().findByUser(user.getId());
        for (WatchHistory wh : history) {
            if (wh.getEpisodeId() != null
                    && wh.getEpisodeId() == currentEpisode.getId()
                    && wh.getCompleted() == 0
                    && wh.getStoppedAtTime() > 5) {
                double resumeAt = wh.getStoppedAtTime();
                mediaPlayer.setOnReady(() -> {
                    timeSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
                    mediaPlayer.volumeProperty().bind(volumeSlider.valueProperty().divide(100));
                    mediaPlayer.seek(Duration.seconds(resumeAt));
                    mediaPlayer.play();
                    playBtn.setText("⏸");
                });
                return;
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  NEXT EPISODE RESOLUTION
    // ══════════════════════════════════════════════════════════════════════════

    private void resolveNextEpisode() {
        nextEpisode = null;
        if (currentEpisode == null || currentMedia == null) {
            updateNextBtn();
            return;
        }

        int seasonId = currentEpisode.getSeasonId();
        List<Episode> episodes = EpisodeDAO.getEpisodesBySeason(seasonId);

        boolean foundCurrent = false;
        for (Episode ep : episodes) {
            if (foundCurrent) { nextEpisode = ep; break; }
            if (ep.getId() == currentEpisode.getId()) foundCurrent = true;
        }


        if (nextEpisode == null) {
            List<Season> seasons = SeasonDAO.getSeasonsBySerie(currentMedia.getIdMedia());
            boolean foundSeason = false;
            for (Season season : seasons) {
                if (foundSeason) {
                    List<Episode> nextEps = EpisodeDAO.getEpisodesBySeason(season.getIdSeason());
                    if (!nextEps.isEmpty()) nextEpisode = nextEps.get(0);
                    break;
                }
                if (season.getIdSeason() == seasonId) foundSeason = true;
            }
        }
        updateNextBtn();
    }

    private void updateNextBtn() {
        if (nextEpisode != null) {
            nextEpisodeBtn.setVisible(true);
            nextEpisodeBtn.setManaged(true);
        }
    }



    private void setupTitle() {
        if (currentMedia != null) videoTitle.setText(currentMedia.getTitle());

        if (currentEpisode != null) {
            int seasonNum = resolveSeasonNumber(currentEpisode.getSeasonId());
            episodeSubtitle.setText("S" + seasonNum +
                    " · E" + currentEpisode.getEpisodeNumber() +
                    " — " + currentEpisode.getTitle());
        }
    }

    private int resolveSeasonNumber(int seasonId) {
        if (currentMedia == null) return 1;
        for (Season s : SeasonDAO.getSeasonsBySerie(currentMedia.getIdMedia()))
            if (s.getIdSeason() == seasonId) return s.getSeasonNumber();
        return 1;
    }


    private void showNextEpisodeOverlay() {
        if (nextEpisode == null) return;

        nextEpTitle.setText("E" + nextEpisode.getEpisodeNumber() + " — " + nextEpisode.getTitle());
        nextEpDesc.setText(nextEpisode.getDescription() != null ? nextEpisode.getDescription() : "");

        if (nextEpisode.getThumbnailPath() != null && !nextEpisode.getThumbnailPath().isBlank()) {
            try { nextEpThumbnail.setImage(new Image(nextEpisode.getThumbnailPath(), true)); }
            catch (Exception ignored) {}
        }

        nextEpisodeOverlay.setVisible(true);
        nextEpisodeOverlay.setManaged(true);

        countdownLabel.setText(String.valueOf(COUNTDOWN_DURATION));
        countdownBar.setProgress(1.0);

        countdownTimeline = new Timeline();
        for (int i = 0; i <= COUNTDOWN_DURATION; i++) {
            final int remaining = COUNTDOWN_DURATION - i;
            final double progress = remaining / (double) COUNTDOWN_DURATION;
            countdownTimeline.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(i), ev -> {
                        countdownLabel.setText(String.valueOf(remaining));
                        countdownBar.setProgress(progress);
                        if (remaining == 0) playNextEpisode();
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

    @FXML public void handlePlayNext(ActionEvent event) {
        hideNextEpisodeOverlay();
        playNextEpisode();
    }

    @FXML public void handleCancelNext(ActionEvent event) {
        hideNextEpisodeOverlay();
    }

    @FXML public void handleNextEpisode(ActionEvent event) {
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
        TransferData.setEpisode(nextEpisode);
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/org/Views/VideoPlayer.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) mediaView.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  VIDEO SOURCE RESOLUTION
    // ══════════════════════════════════════════════════════════════════════════

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
        return res != null ? res.toExternalForm() : "";
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  WATCH HISTORY
    // ══════════════════════════════════════════════════════════════════════════

    private void saveWatchHistory(boolean completed) {
        User user = Session.getUser();
        if (user == null || currentMedia == null) return;

        double stoppedAt = mediaPlayer != null
                ? mediaPlayer.getCurrentTime().toSeconds() : 0.0;
        Integer episodeId = (currentEpisode != null) ? currentEpisode.getId() : null;

        WatchHistory wh = new WatchHistory(
                user.getId(), currentMedia.getIdMedia(), episodeId,
                stoppedAt, Timestamp.from(Instant.now()), completed ? 1 : 0);
        WatchHistoryDAO.addToHistory(wh);
    }



    @FXML
    public void handleBack(ActionEvent event) {
        if (mediaPlayer != null) { saveWatchHistory(false); mediaPlayer.stop(); }
        if (countdownTimeline != null) countdownTimeline.stop();
        SceneSwitcher.goTo(event, "/org/Views/MediaDetails.fxml");
    }

    @FXML
    public void handleRewind(ActionEvent event) {
        if (mediaPlayer != null)
            mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(10)));
    }

    @FXML
    public void handleForward(ActionEvent event) {
        if (mediaPlayer != null)
            mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(10)));
    }

    @FXML
    public void togglePlay(ActionEvent event) {
        if (mediaPlayer == null) return;
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playBtn.setText("▶");
        } else {
            mediaPlayer.play();
            playBtn.setText("⏸");
        }
    }



    @FXML
    public void handleFullscreen(ActionEvent event) {
        Stage stage = (Stage) mediaView.getScene().getWindow();
        isFullscreen = !isFullscreen;
        stage.setFullScreen(isFullscreen);
        fullscreenBtn.setText(isFullscreen ? "⊠" : "⛶");
    }

    // ── Time formatter ────────────────────────────────────────────────────────
    private String formatTime(Duration elapsed, Duration total) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int intTotal   = (int) Math.floor(total.toSeconds());

        int eH = intElapsed / 3600, eM = (intElapsed % 3600) / 60, eS = intElapsed % 60;
        int tH = intTotal   / 3600, tM = (intTotal   % 3600) / 60, tS = intTotal   % 60;

        if (tH > 0)
            return String.format("%d:%02d:%02d / %d:%02d:%02d", eH, eM, eS, tH, tM, tS);
        else
            return String.format("%02d:%02d / %02d:%02d", eM, eS, tM, tS);
    }
}