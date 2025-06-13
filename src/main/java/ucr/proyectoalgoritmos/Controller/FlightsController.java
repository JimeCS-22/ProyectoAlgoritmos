package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

import static ucr.proyectoalgoritmos.Controller.HelloController.loadViewInNewStage;

public class FlightsController {
    @javafx.fxml.FXML
    private TableView tableView;
    @javafx.fxml.FXML
    private Button btCreate;
    @javafx.fxml.FXML
    private Button btSearch;
    @javafx.fxml.FXML
    private Button btUpdate;
    @javafx.fxml.FXML
    private Button btDelete;

    @javafx.fxml.FXML
    public void searchFlightOnAction(ActionEvent actionEvent) {
    }

    @javafx.fxml.FXML
    public void createFlightOnAction(ActionEvent actionEvent) {
        loadViewInNewStage("/ucr/proyectoalgoritmos/createFlight.fxml", "Create New Flight");
    }

    @javafx.fxml.FXML
    public void updateFlightOnAction(ActionEvent actionEvent) {
    }

    @javafx.fxml.FXML
    public void deleteFlightOnAction(ActionEvent actionEvent) {
    }
}
