package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import ucr.proyectoalgoritmos.util.GifPlayer;

import java.net.URL;
import java.util.ResourceBundle;

public class SimulationController {

    @javafx.fxml.FXML
    private AnchorPane simulationPanel;
    @javafx.fxml.FXML
    private Slider speedSlider;
    @javafx.fxml.FXML
    private Line flightPathLine;
    @javafx.fxml.FXML
    private Label lblHeading;
    @javafx.fxml.FXML
    private Label lblSpeed;
    @javafx.fxml.FXML
    private Button btnPauseSimulation;
    @javafx.fxml.FXML
    private Label lblFlightInfo;
    @javafx.fxml.FXML
    private Button btnResetSimulation;
    @javafx.fxml.FXML
    private Label lblElapsedTime;
    @javafx.fxml.FXML
    private Button btnStartSimulation;
    @javafx.fxml.FXML
    private Label lblAltitude;
    @javafx.fxml.FXML
    private ImageView airplaneIcon;

//    @Deprecated
//    public void initialize(URL location, ResourceBundle resources) {
//        // Para un GIF dividido en frames (frame0.png, frame1.png, etc.)
//        GifPlayer gifPlayer = new GifPlayer("/ucr/proyectoalgoritmos/images/plane-gif.gif", 24, 40);
//        gifImageView.setImage(gifPlayer.getView().getImage());
//    }

    @Deprecated
    public void createAirportOnAction(ActionEvent actionEvent) {
    }

    @Deprecated
    public void searchAirportOnAction(ActionEvent actionEvent) {
    }

    @javafx.fxml.FXML
    public void pauseSimulation(ActionEvent actionEvent) {
    }

    @javafx.fxml.FXML
    public void resetSimulation(ActionEvent actionEvent) {
    }

    @javafx.fxml.FXML
    public void startSimulation(ActionEvent actionEvent) {
    }
}
