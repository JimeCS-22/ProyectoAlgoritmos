package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;

import static ucr.proyectoalgoritmos.Controller.HelloController.loadView;

public class UserFlightController {

    @javafx.fxml.FXML
    private Button btCheckIn;
    @javafx.fxml.FXML
    private Button btReservation;

    @javafx.fxml.FXML
    public void checkInOnAction(ActionEvent actionEvent) {
        loadView("/ucr/proyectoalgoritmos/createPassenger.fxml", null);
    }

    @javafx.fxml.FXML
    public void reservationOnAction(ActionEvent actionEvent) {
        loadView("/ucr/proyectoalgoritmos/ticket.fxml", null);
    }
}
