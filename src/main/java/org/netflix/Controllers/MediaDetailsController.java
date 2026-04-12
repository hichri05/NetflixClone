package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.netflix.DAO.MediaDAO;
import org.netflix.DAO.MovieDAO;
import org.netflix.DAO.UserDAO;
import org.netflix.Models.Media;
import org.netflix.Models.User;
import org.netflix.Utils.SceneSwitcher;
import org.netflix.Utils.Session;
import org.netflix.Utils.TransferData;

import java.io.IOException;
import java.util.List;

public class MediaDetailsController {

    @FXML public StackPane detailHero;
    @FXML public ImageView backgroundImage;
    @FXML public Label titleLabel, descriptionLabel;
    @FXML public ChoiceBox seasonSelector;
    @FXML public VBox episodeListContainer;
    @FXML public FlowPane relatedGrid;
    @FXML public ScrollPane mainScroll;
    @FXML public Button mylistbtn;
    Media media;

    @FXML
    public void initialize() {
        media = TransferData.getMedia();
        User user = Session.getUser();

        titleLabel.setText(media.getTitle());
        descriptionLabel.setText(media.getDescription());
        String imgUrl = media.getBackdropImageUrl();
        Image img = new Image(imgUrl, true);
        backgroundImage.setImage(img);
        backgroundImage.fitWidthProperty().bind(mainScroll.widthProperty().multiply(0.996));
        StackPane.setAlignment(backgroundImage, Pos.CENTER);
        mainScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        updateButtonUI(user, media);
    }
    @FXML
    public void handlePlay(ActionEvent actionEvent) {
        SceneSwitcher.goTo(actionEvent, "/org/Views/VideoPlayer.fxml");
    }

    public void handleAddToMyList(ActionEvent actionEvent) {
        User user = Session.getUser();
        if (user == null) {
            System.out.println("No user logged in!");
            return;
        }


        if (UserDAO.isFavorite(user.getId(), media.getIdMedia())) {
            RemoveFromList(media);
        }else{
            boolean success = MediaDAO.addToFavorites(user.getId(), media.getIdMedia());
            if (success) {
                System.out.println("Successfully added to favorites");
            } else {
                System.err.println("Failed to add to list.");
            }
        }
        updateButtonUI(user, media);
    }
    @FXML
    private void RemoveFromList(Media media) {
        if (media == null) return;
        System.out.println("Removing: " + media.getTitle());

        User user = Session.getUser();
        MediaDAO.removeFromFavorites(user.getId(), media.getIdMedia());
        List<Media> favorites = UserDAO.getUserFavorites(user.getId());
        //MainController.displayMyList(favorites);
    }
    private void updateButtonUI(User u,  Media m) {
        if (UserDAO.isFavorite(u.getId(), m.getIdMedia())) {
            mylistbtn.setText("✓ In My List");
        } else {
            mylistbtn.setText("+ My List");
        }
    }

    public void handleBack(ActionEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/main.fxml");
    }
}
