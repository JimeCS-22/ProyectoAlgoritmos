package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;

import static ucr.proyectoalgoritmos.Controller.HelloController.loadView;

public class UserMenuController {
    @javafx.fxml.FXML
    private MenuItem menuExit;
    @javafx.fxml.FXML
    private MenuItem FlightsSettings;
    @javafx.fxml.FXML
    private MenuItem Trips;
    @javafx.fxml.FXML
    private MenuItem FlightStatus;

    @javafx.fxml.FXML
    public void exit(ActionEvent actionEvent) {
    }

    @javafx.fxml.FXML
    public void loadFlightsStatusPane(ActionEvent actionEvent) {
        loadView("/ucr/proyectoalgoritmos/userFlightStatus.fxml", null);
    }

    @javafx.fxml.FXML
    public void loadTripsPane(ActionEvent actionEvent) {
        loadView("/ucr/proyectoalgoritmos/userTrip.fxml", null);
    }

    @javafx.fxml.FXML
    public void loadFlightsPane(ActionEvent actionEvent) {
        loadView("/ucr/proyectoalgoritmos/userFlight.fxml", null);
    }
}
