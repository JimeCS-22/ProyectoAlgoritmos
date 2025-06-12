package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class RouteController {

    @javafx.fxml.FXML
    private Button btCheckRoute;
    @javafx.fxml.FXML
    private ImageView imgPlaneOrigin;
    @javafx.fxml.FXML
    private TextField txtDestination;
    @javafx.fxml.FXML
    private Line routeLine;
    @javafx.fxml.FXML
    private Label lblOriginPoint;
    @javafx.fxml.FXML
    private Label lblDistance;
    @javafx.fxml.FXML
    private Circle circleOrigin;
    @javafx.fxml.FXML
    private Circle circleDestination;
    @javafx.fxml.FXML
    private Label lblDestinationPoint;
    @javafx.fxml.FXML
    private ImageView imgArrivalDest;
    @javafx.fxml.FXML
    private Label lblEstimatedDuration;
    @javafx.fxml.FXML
    private TextField txtOrigin;
    @javafx.fxml.FXML
    private Label lblRouteStatus;


    @javafx.fxml.FXML
    public void checkFlightOnAction(ActionEvent actionEvent) {
    }
}
