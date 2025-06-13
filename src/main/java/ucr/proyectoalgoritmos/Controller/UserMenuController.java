package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

import static ucr.proyectoalgoritmos.Controller.HelloController.loadViewInNewStage;

public class UserMenuController implements Initializable {

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
    @FXML private void loadFlightsPane(ActionEvent event) { loadViewInNewStage("/ucr/proyectoalgoritmos/userFlight.fxml", "Registro de Vuelo"); }
    @FXML private void loadTripsPane(ActionEvent event) { loadViewInNewStage("/ucr/proyectoalgoritmos/userTrip.fxml", "Mis Vuelos"); }
    @FXML private void loadFlightsStatusPane(ActionEvent event) { loadViewInNewStage("/ucr/proyectoalgoritmos/userFlightStatus.fxml", "Estado de Vuelo"); }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("UserMenuController inicializado correctamente.");
    }
}