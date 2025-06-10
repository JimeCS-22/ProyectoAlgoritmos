package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;

import static ucr.proyectoalgoritmos.Controller.HelloController.loadView;

public class PassengersController {
    @javafx.fxml.FXML
    private Button btUpdate;
    @javafx.fxml.FXML
    private Button btCreate;

    @javafx.fxml.FXML
    public void createPassengerOnAction(ActionEvent actionEvent) {
        loadView("/ucr/proyectoalgoritmos/createPassenger.fxml", null);
    }

    @javafx.fxml.FXML
    public void updatePassengerOnAction(ActionEvent actionEvent) {
    }
}
