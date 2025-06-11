package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import static ucr.proyectoalgoritmos.Controller.HelloController.loadView;

public class PassengersController {
    @javafx.fxml.FXML
    private Button btUpdate;
    @javafx.fxml.FXML
    private Button btCreate;
    @javafx.fxml.FXML
    private TableColumn colNationality;
    @javafx.fxml.FXML
    private Button btDelete;
    @javafx.fxml.FXML
    private TableColumn colID;
    @javafx.fxml.FXML
    private TableColumn colFlightHistory;
    @javafx.fxml.FXML
    private Button btViewAll;
    @javafx.fxml.FXML
    private TableView tblPassengers;
    @javafx.fxml.FXML
    private TableColumn colFullName;

    @javafx.fxml.FXML
    public void createPassengerOnAction(ActionEvent actionEvent) {
        //loadView("/ucr/proyectoalgoritmos/createPassenger.fxml", null);
    }

    @javafx.fxml.FXML
    public void updatePassengerOnAction(ActionEvent actionEvent) {
    }

    @javafx.fxml.FXML
    public void viewAllPassengersOnAction(ActionEvent actionEvent) {
    }

    @javafx.fxml.FXML
    public void deletePassengerOnAction(ActionEvent actionEvent) {
    }
}
