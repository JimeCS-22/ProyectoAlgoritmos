package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;

import static ucr.proyectoalgoritmos.Controller.HelloController.loadViewInNewStage;

public class UserFlightController {

    @javafx.fxml.FXML
    private Button btCheckIn;
    @javafx.fxml.FXML
    private Button btReservation;

    @javafx.fxml.FXML
    public void checkInOnAction(ActionEvent actionEvent) {
        loadViewInNewStage("/ucr/proyectoalgoritmos/createPassenger.fxml", "Check-In");
    }

    @javafx.fxml.FXML
    public void reservationOnAction(ActionEvent actionEvent) {
        loadViewInNewStage("/ucr/proyectoalgoritmos/ticket.fxml", "Confirmación de Vuelo");
    }
}
