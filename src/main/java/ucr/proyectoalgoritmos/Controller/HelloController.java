package ucr.proyectoalgoritmos.Controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {

    public static void loadView(String fxmlPath, BorderPane rootLayout) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloController.class.getResource(fxmlPath));
            Parent view = loader.load();

            if (rootLayout != null) {
                rootLayout.setCenter(view);
            } else {
                Stage stage = new Stage();
                stage.setScene(new Scene(view));
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadViewInNewStage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloController.class.getResource(fxmlPath));
            Parent view = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(view));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}