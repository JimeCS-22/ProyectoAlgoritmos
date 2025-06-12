package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
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

    @Deprecated
    public void exit(ActionEvent actionEvent) {
        System.exit(0);
    }

    @FXML
    private void loadAirportPane() {
        loadView("/ucr/proyectoalgoritmos/airports.fxml", null);
    }

    @FXML
    private void loadPassengersPane() {
        loadView("/ucr/proyectoalgoritmos/passengers.fxml", null);
    }

    @FXML
    private void loadFlightsPane() {
        loadView("/ucr/proyectoalgoritmos/flights.fxml", null);
    }

    @FXML
    private void loadRoutesPane() {
        loadView("/ucr/proyectoalgoritmos/routes.fxml", null);
    }

    @FXML
    private void loadSimulationPane() {
        loadView("/ucr/proyectoalgoritmos/simulation.fxml", null);
    }

}
