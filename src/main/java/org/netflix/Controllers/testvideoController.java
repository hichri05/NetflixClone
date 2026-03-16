package org.netflix.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class testvideoController implements Initializable {
    @FXML private MediaView mediaView;
    @FXML private Button playB;
    private Media media;
    private MediaPlayer mediaPlayer;
    private File file;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        URL videoUrl = getClass().getResource("/org/Images/test.mp4");
        String vd = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
        media = new Media(vd);
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
    }
    public void playMedia(){
        mediaPlayer.play();
    }
}
