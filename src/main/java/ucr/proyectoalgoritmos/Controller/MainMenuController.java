package ucr.proyectoalgoritmos.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;

public class MainMenuController {

    @FXML
    private MenuItem loginSettings;

    @FXML
    private MenuItem menuExit;

    @FXML
    private MenuItem AirportsSettings;

    @FXML
    private MenuItem Passengers;

    @FXML
    private MenuItem FlightsSettings;

    @FXML
    private MenuItem RoutesSettings;

    @FXML
    private MenuItem Simulation;

    @FXML
    private void loadLoginPane() {
        HelloController.loadView("/ucr/proyectoalgoritmos/login.fxml", null);
    }

    @FXML
    private void exit() {
        System.exit(0);
    }

}
