package ucr.proyectoalgoritmos.Controller;

import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class CreateFlightController {
    @javafx.fxml.FXML
    private TextField departureTime;
    @javafx.fxml.FXML
    private TextField destinationCode;
    @javafx.fxml.FXML
    private TextField occupancy;
    @javafx.fxml.FXML
    private Button Enter;
    @javafx.fxml.FXML
    private TextField originCode;
    @javafx.fxml.FXML
    private TextField flightNumber;
    @javafx.fxml.FXML
    private TextField capacity;
    @javafx.fxml.FXML
    private ChoiceBox status;

    @javafx.fxml.FXML
    public void enterOnAction(Event event) {
    }
}
