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

    private BorderPane rootLayout;

    public void setRootLayout(BorderPane rootLayout) {
        this.rootLayout = rootLayout;
    }

    @FXML
    private void loadLoginPane() {
        loadView("/ucr/proyectoalgoritmos/login.fxml");
    }

    @FXML
    private void exit() {
        System.exit(0);
    }

    @FXML
    private void loadAirportPane() {
        loadView("/ucr/proyectoalgoritmos/airports.fxml");
    }

    @FXML
    private void loadPassengersPane() {
        loadView("/ucr/proyectoalgoritmos/passengers.fxml");
    }

    @FXML
    private void loadFlightsPane() {
        loadView("/ucr/proyectoalgoritmos/flight.fxml");
    }

    @FXML
    private void loadRoutesPane() {
        loadView("/ucr/proyectoalgoritmos/route.fxml");
    }

    @FXML
    private void loadSimulationPane() {
        loadView("/ucr/proyectoalgoritmos/simulation.fxml");
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
}
