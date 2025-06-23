package ucr.proyectoalgoritmos.Controller.FlightController;

import javafx.event.ActionEvent;

import java.net.URL;
import java.time.LocalTime;
import java.time.LocalDate;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.flight.FlightScheduleManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.route.RouteManager;
import ucr.proyectoalgoritmos.UtilJson.FlightJson;
import ucr.proyectoalgoritmos.util.FXUtility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.ResourceBundle;

public class CreateFlightController implements Initializable {
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
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");


    @FXML
    public void enterOnAction(ActionEvent event) {
        // 1. Obtener y validar entradas de usuario
        String flightNum = flightNumber.getText().trim();
        String origin = originCode.getText().trim();
        String destination = destinationCode.getText().trim();
        String depTimeStr = departureTime.getText().trim();
        String occupancyStr = occupancy.getText().trim();
        String capacityStr = capacity.getText().trim();
        Flight.FlightStatus selectedStatus = status.getValue();

        // Validaciones básicas de campos no vacíos
        if (flightNum.isEmpty() || origin.isEmpty() || destination.isEmpty() ||
                depTimeStr.isEmpty() || occupancyStr.isEmpty() || capacityStr.isEmpty() ||
                selectedStatus == null) {
            FXUtility.alert("Validación de Entrada", "Todos los campos son obligatorios. Por favor, rellene todos los campos.");
            return;
        }

        // ******** INICIO DE CORRECCIÓN ********

        LocalTime parsedDepartureTime; // Declara la variable aquí
        try {
            parsedDepartureTime = LocalTime.parse(depTimeStr, TIME_FORMATTER); // Asigna el valor parseado
        } catch (DateTimeParseException e) {
            FXUtility.alert("Error de Formato", "El formato de la hora de salida debe ser HH:mm (ej. 14:30).");
            return; // Si falla, sal del método
        }

        // Ahora que sabemos que parsedDepartureTime tiene un valor válido, podemos usarlo
        LocalDateTime flightDepartureDateTime = LocalDateTime.of(LocalDate.now(), parsedDepartureTime);

        // ******** FIN DE CORRECCIÓN ********


        // Validar números
        int flightOccupancy;
        int flightCapacity;
        try {
            flightOccupancy = Integer.parseInt(occupancyStr);
            flightCapacity = Integer.parseInt(capacityStr);
            if (flightOccupancy < 0 || flightCapacity <= 0) {
                FXUtility.alert("Validación de Entrada", "Ocupación y capacidad deben ser números positivos.");
                return;
            }
            if (flightOccupancy > flightCapacity) {
                FXUtility.alert("Validación de Entrada", "La ocupación no puede ser mayor que la capacidad.");
                return;
            }
        } catch (NumberFormatException e) {
            FXUtility.alert("Error de Formato", "Capacidad y ocupación deben ser números válidos.");
            return;
        }

        try {
            // 2. Verificar existencia de aeropuertos de origen y destino
            Airport originAirport = airportManager.findAirport(origin);
            Airport destinationAirport = airportManager.findAirport(destination);

            if (originAirport == null) {
                FXUtility.alert("Aeropuerto Inválido", "El código de aeropuerto de origen no existe: " + origin);
                return;
            }
            if (destinationAirport == null) {
                FXUtility.alert("Aeropuerto Inválido", "El código de aeropuerto de destino no existe: " + destination);
                return;
            }

            // Opcional: Verificar si los aeropuertos están activos (si tu lógica lo requiere)
            if (originAirport.getStatus() != Airport.AirportStatus.ACTIVE) {
                FXUtility.alert("Aeropuerto No Operativo", "El aeropuerto de origen ('" + origin + "') no está activo.");
                return;
            }
            if (destinationAirport.getStatus() != Airport.AirportStatus.ACTIVE) {
                FXUtility.alert("Aeropuerto No Operativo", "El aeropuerto de destino ('" + destination + "') no está activo.");
                return;
            }

            // 3. Verificar si existe una ruta entre los aeropuertos (asumiendo que RouteManager gestiona rutas)
            // Esto depende de cómo tengas implementado tu RouteManager y si las rutas son bidireccionales o no.
            // Ejemplo (adaptar según tu RouteManager):
            // if (!routeManager.routeExists(origin, destination)) {
            //    FXUtility.alert("Ruta Inválida", "No existe una ruta directa definida entre " + origin + " y " + destination + ".");
            //    return;
            // }

            // 4. Crear el objeto Flight
            Flight newFlight = new Flight(
                    flightNum,
                    origin,
                    destination,
                    flightDepartureDateTime,  // Ahora es un LocalDateTime válido
                    flightCapacity,
                    flightOccupancy,
                    selectedStatus
            );

            // 5. Añadir el vuelo al FlightScheduleManager
            flightScheduleManager.addFlight(newFlight);
            System.out.println("[INFO] Vuelo creado y añadido: " + newFlight);

            // 6. Guardar la lista actualizada de vuelos en JSON
            FlightJson.saveFlightsToJson(flightScheduleManager.getAllFlights());

            FXUtility.alert("Éxito", "Vuelo '" + flightNum + "' creado y guardado exitosamente.");

            // Opcional: Limpiar los campos después de una creación exitosa
            clearFormFields();

        } catch (ListException e) {
            FXUtility.alert("Error de Operación", "Error al procesar la lista de vuelos/aeropuertos: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            FXUtility.alert("Error Inesperado", "Ocurrió un error inesperado al crear el vuelo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para limpiar los campos del formulario
    private void clearFormFields() {
        flightNumber.clear();
        originCode.clear();
        destinationCode.clear();
        departureTime.clear();
        occupancy.clear();
        capacity.clear();
        status.setValue(Flight.FlightStatus.SCHEDULED); // Reiniciar al valor por defecto
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Obtener las instancias de los managers primero
        airportManager = AirportManager.getInstance(); // Asumiendo que AirportManager también es Singleton
        routeManager = RouteManager.getInstance();     // Asumiendo que RouteManager también es Singleton

        // Ahora, pasar estas instancias a FlightScheduleManager.getInstance()
        flightScheduleManager = FlightScheduleManager.getInstance(airportManager, routeManager);

        // Poblar el ChoiceBox
        status.getItems().addAll(Arrays.asList(Flight.FlightStatus.values()));
        status.setValue(Flight.FlightStatus.SCHEDULED); // Establecer un valor por defecto
    }
}
