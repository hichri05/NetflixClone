package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.netflix.Models.Media;
import org.netflix.Utils.SceneSwitcher;
import org.netflix.Utils.TransferData;

public class MediaDetailsController {

    @FXML public StackPane detailHero;
    @FXML public ImageView backgroundImage;
    @FXML public Label titleLabel, descriptionLabel;
    @FXML public ChoiceBox seasonSelector;
    @FXML public VBox episodeListContainer;
    @FXML public FlowPane relatedGrid;
    @FXML public ScrollPane mainScroll;

    @FXML
    public void initialize() {
        Media media = TransferData.getMedia();
        titleLabel.setText(media.getTitle());
        descriptionLabel.setText(media.getDescription());
        String imgUrl = media.getBackdropImageUrl();
        Image img = new Image(imgUrl, true);
        backgroundImage.setImage(img);
        backgroundImage.fitWidthProperty().bind(mainScroll.widthProperty().multiply(0.996));
        StackPane.setAlignment(backgroundImage, Pos.CENTER);
        mainScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }
    @FXML
    public void handlePlay(ActionEvent actionEvent) {
        SceneSwitcher.goTo(actionEvent, "/org/Views/VideoPlayer.fxml");
    }

    public void handleAddToMyList(ActionEvent actionEvent) {

    }
}
