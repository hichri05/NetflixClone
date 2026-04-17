package org.netflix.Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.netflix.DAO.MovieDAO;
import org.netflix.Models.Movie;

import java.io.IOException;
import java.net.URL;


public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("signIn"), 640, 480);
        String cssPath = getClass().getResource("/org/Styles/style.css").toExternalForm();
        scene.getStylesheets().add(cssPath);

        stage.setTitle("Netflix - Sign In");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/org/Images/icon.png")));
        stage.setFullScreen(true);
        stage.setScene(scene);
        stage.show();

    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/org/Views/"+fxml + ".fxml"));
        return fxmlLoader.load();
    }
//
    public static void main(String[] args) {
        for (Movie m: MovieDAO.getAllMovies()) {
            System.out.println(m);
        }
        launch();
    }

}