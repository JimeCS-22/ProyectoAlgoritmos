package ucr.proyectoalgoritmos.Controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.Circular.CircularLinkedList;
import ucr.proyectoalgoritmos.Domain.FlightManager;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.flight.FlightScheduleManager;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.util.FXUtility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CreateFlightController {
    @FXML
    private TextField departureTime;
    @FXML
    private TextField destinationCode;
    @FXML
    private TextField occupancy;
    @FXML
    private Button Enter;
    @FXML
    private TextField originCode;
    @FXML
    private TextField flightNumber;
    @FXML
    private TextField capacity;
    @FXML
    private ChoiceBox<Flight.FlightStatus> status;


    @FXML
    public void enterOnAction(Event event) {


    }




}
