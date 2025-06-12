package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class UserFlightStatusController {

    @javafx.fxml.FXML
    private Label lblNumeroVuelo;
    @javafx.fxml.FXML
    private Label lblEstadoVuelo;
    @javafx.fxml.FXML
    private Label lblSalidaProgramada;
    @javafx.fxml.FXML
    private Label lblOrigen;
    @javafx.fxml.FXML
    private Label lblDestino;
    @javafx.fxml.FXML
    private Button btnActualizarEstado;
    @javafx.fxml.FXML
    private TableView flightStatusTable;


    @javafx.fxml.FXML
    public void actualizarEstadoOnAction(ActionEvent actionEvent) {
    }
}
