package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;

import javax.swing.*;

import static ucr.proyectoalgoritmos.Controller.HelloController.loadView;

public class AdminMenuController {
    @javafx.fxml.FXML
    private MenuItem AirportsSettings;
    @javafx.fxml.FXML
    private MenuItem Passengers;
    @javafx.fxml.FXML
    private MenuItem FlightsSettings;
    @javafx.fxml.FXML
    private MenuItem Simulation;
    @javafx.fxml.FXML
    private MenuItem RoutesSettings;
    private static BorderPane root = new BorderPane();

    @Deprecated
    public void exit(ActionEvent actionEvent) {
        System.exit(0);
    }

    @FXML
    private void loadAirportPane() {
        loadView("/ucr/proyectoalgoritmos/airports.fxml", root);
    }

    @FXML
    private void loadPassengersPane() {
        loadView("/ucr/proyectoalgoritmos/passengers.fxml", root);
    }

    @FXML
    private void loadFlightsPane() {
        loadView("/ucr/proyectoalgoritmos/flights.fxml", root);
    }

    @FXML
    private void loadRoutesPane() {
        loadView("/ucr/proyectoalgoritmos/route.fxml", root);
    }

    @FXML
    private void loadSimulationPane() {
        loadView("/ucr/proyectoalgoritmos/simulation.fxml", root);
    }

}
