package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import ucr.proyectoalgoritmos.Controller.MainMenuController;

import java.io.IOException;

public class AirportController {
    @javafx.fxml.FXML
    private Button btCreate;
    private BorderPane rootLayout;
    @javafx.fxml.FXML
    private Button btModify;

    public void setRootLayout(BorderPane rootLayout) {
        this.rootLayout = rootLayout;
    }

    @javafx.fxml.FXML
    public void createAirportOnAction(ActionEvent actionEvent) {
        loadView("/ucr/proyectoalgoritmos/createAirport.fxml");
    }

    @Deprecated
    public void searchAirportOnAction(ActionEvent actionEvent) {
    }

    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
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

    @javafx.fxml.FXML
    public void modifyAirportOnAction(ActionEvent actionEvent) {
    }
}
