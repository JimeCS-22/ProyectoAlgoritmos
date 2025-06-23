package ucr.proyectoalgoritmos.Controller.UserController;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;

import static ucr.proyectoalgoritmos.Controller.HelloController.loadViewInNewStage;

public class UserFlightController {

    @javafx.fxml.FXML
    private Button btReservation;
    @javafx.fxml.FXML
    private DatePicker dpDeparture;
    @javafx.fxml.FXML
    private Button btAddPet;
    @javafx.fxml.FXML
    private ComboBox cbDestination;
    @javafx.fxml.FXML
    private ComboBox cbPassengers;
    @javafx.fxml.FXML
    private ComboBox cbBaggage;
    @javafx.fxml.FXML
    private ComboBox cbOrigin;
    @javafx.fxml.FXML
    private ComboBox cbSeatType;
    @javafx.fxml.FXML
    private DatePicker dpReturn;


    @javafx.fxml.FXML
    public void reservationOnAction(ActionEvent actionEvent) {
        loadViewInNewStage("/ucr/proyectoalgoritmos/ticket.fxml", "Confirmación de Vuelo");
    }

    @javafx.fxml.FXML
    public void addPetOnAction(ActionEvent actionEvent) {
    }
}
