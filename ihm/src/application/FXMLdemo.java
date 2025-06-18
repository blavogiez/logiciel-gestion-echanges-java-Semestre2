package application;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class FXMLdemo extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        URL fxmlFileUrl = getClass().getResource("interface.fxml");
        if (fxmlFileUrl == null) {
            System.out.println("Impossible de charger le fichier fxml");
            System.exit(-1);
        }
        loader.setLocation(fxmlFileUrl);
        Parent root = loader.load();

        // Load the CSS file
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        // Load the icon image
        Image icon = new Image(getClass().getResourceAsStream("icon.png"));
        // Set the icon for the stage
        stage.getIcons().add(icon);

        stage.setScene(scene);
        stage.setTitle("SchoolBuilder");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
