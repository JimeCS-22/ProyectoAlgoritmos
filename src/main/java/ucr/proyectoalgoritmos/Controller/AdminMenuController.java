package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

import static ucr.proyectoalgoritmos.Controller.HelloController.loadViewInNewStage;

public class AdminMenuController implements Initializable {

    private HelloController helloController;

    public void setHelloController(HelloController helloController) {
        this.helloController = helloController;
    }

    @FXML
    private void logout(ActionEvent event) {
        if (helloController != null) {
            helloController.logout();
        }
    }

    @FXML
    private void exit(ActionEvent event) {
        System.exit(0);
    }

    // Métodos para cargar pantallas a través de HelloController
    @FXML private void loadAirportPane(ActionEvent event) { loadViewInNewStage("/ucr/proyectoalgoritmos/airports.fxml", "Gestor de Aeropuertos"); }
    @FXML private void loadPassengersPane(ActionEvent event) { loadViewInNewStage("/ucr/proyectoalgoritmos/passengers.fxml", "Gestor de Pasajeros"); }
    @FXML private void loadFlightsPane(ActionEvent event) { loadViewInNewStage("/ucr/proyectoalgoritmos/flights.fxml", "Gestor de Vuelos"); }
    @FXML private void loadRoutesPane(ActionEvent event) { loadViewInNewStage("/ucr/proyectoalgoritmos/routes.fxml", "Gestor de Rutas"); }
    @FXML private void loadSimulationPane(ActionEvent event) { loadViewInNewStage("/ucr/proyectoalgoritmos/simulation.fxml", "Simulación de Vuelo"); }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("AdminMenuController inicializado correctamente.");
    }
}