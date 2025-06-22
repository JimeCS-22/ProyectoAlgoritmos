package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import java.time.LocalTime;
import java.time.LocalDate;
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
import ucr.proyectoalgoritmos.Domain.route.RouteManager;
import ucr.proyectoalgoritmos.UtilJson.FlightJson;
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

    private FlightScheduleManager flightScheduleManager;
    private AirportManager airportManager;
    private RouteManager routeManager;

    @FXML
    public void initialize() {
        status.getItems().setAll(Flight.FlightStatus.values());
        status.getSelectionModel().select(Flight.FlightStatus.SCHEDULED); // Estado por defecto

        try {
            this.airportManager = AirportManager.getInstance();
            this.routeManager = RouteManager.getInstance();
            this.flightScheduleManager = FlightScheduleManager.getInstance(airportManager, routeManager);
        } catch (Exception e) {
            FXUtility.alert("Error de Inicialización", "No se pudo preparar el sistema para crear vuelos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void enterOnAction(ActionEvent event) {
        // Declara departureLDT y departureLocalTime fuera del bloque try-catch
        // para que sean accesibles en todo el método.
        LocalDateTime departureLDT = null; // Inicializa a null para evitar "might not have been initialized"
        LocalTime departureLocalTime = null; // Inicializa a null

        try {
            String fNumber = flightNumber.getText().trim();
            String oCode = originCode.getText().trim();
            String dCode = destinationCode.getText().trim();
            String depTimeStr = departureTime.getText().trim();
            String capStr = capacity.getText().trim();

            if (fNumber.isEmpty() || oCode.isEmpty() || dCode.isEmpty() || depTimeStr.isEmpty() || capStr.isEmpty()) {
                FXUtility.alert("Error de Validación", "Por favor, complete todos los campos obligatorios.");
                return;
            }

            try {
                // Formateador para solo la hora (HH:mm)
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                // Parsea solo la parte de la hora y asigna a la variable declarada arriba
                departureLocalTime = LocalTime.parse(depTimeStr, timeFormatter);
                // Combina la hora parseada con la fecha actual del sistema
                departureLDT = LocalDateTime.of(LocalDate.now(), departureLocalTime);
            } catch (DateTimeParseException e) {
                FXUtility.alert("Formato de Hora Inválido", "Use el formato HH:MM (ej. 14:30) para la hora de salida.");
                return;
            }

            int flightCapacity;
            try {
                flightCapacity = Integer.parseInt(capStr);
                if (flightCapacity <= 0) {
                    FXUtility.alert("Capacidad Inválida", "La capacidad debe ser un número entero positivo.");
                    return;
                }
            } catch (NumberFormatException e) {
                FXUtility.alert("Capacidad Inválida", "La capacidad debe ser un número entero válido.");
                return;
            }

            // Duración estimada (puedes añadir un campo en la UI si es variable)
            int estimatedDuration = 120; // Ejemplo: 120 minutos

            // Crea el vuelo a través del manager
            Flight newFlight = flightScheduleManager.createFlight(
                    fNumber, oCode, dCode, departureLDT, estimatedDuration, flightCapacity
            );

            // Guarda la lista actualizada de vuelos usando FlightJson
            FlightJson.saveFlightsToJson(flightScheduleManager.getScheduledFlights());

            // Aquí departureLocalTime ya es accesible
            FXUtility.alert("Éxito", "Vuelo " + newFlight.getFlightNumber() + " creado y guardado exitosamente para hoy a las " + departureLocalTime + ".");
            clearFields();

        } catch (ListException e) {
            FXUtility.alert("Error al Crear Vuelo", "Error de lógica: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            FXUtility.alert("Error de Entrada", "Datos de entrada inválidos: " + e.getMessage());
        } catch (Exception e) {
            FXUtility.alert("Error Inesperado", "Ocurrió un error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields() {
        flightNumber.clear();
        originCode.clear();
        destinationCode.clear();
        departureTime.clear();
        capacity.clear();
        occupancy.clear(); // Limpia también por si acaso
        status.getSelectionModel().select(Flight.FlightStatus.SCHEDULED);
    }
}
