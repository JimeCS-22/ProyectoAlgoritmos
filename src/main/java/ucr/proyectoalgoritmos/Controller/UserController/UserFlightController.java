package ucr.proyectoalgoritmos.Controller.UserController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import ucr.proyectoalgoritmos.Controller.HelloController;
import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.TreeException;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.flight.FlightScheduleManager;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;
import ucr.proyectoalgoritmos.Domain.passenger.PassengerManager;
import ucr.proyectoalgoritmos.Domain.route.RouteManager;
import ucr.proyectoalgoritmos.util.FXUtility;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class UserFlightController implements Initializable {

    @FXML private Button btReservation;
    @FXML private DatePicker dpDeparture;
    @FXML private Button btAddPet;
    @FXML private ComboBox<String> cbDestination;
    @FXML private ComboBox<String> cbPassengers;
    @FXML private ComboBox<String> cbBaggage;
    @FXML private ComboBox<String> cbOrigin;
    @FXML private ComboBox<String> cbSeatType;
    @FXML private DatePicker dpReturn;
    @FXML private CheckBox chkRoundTrip;

    private AirportManager airportManager;
    private FlightScheduleManager flightScheduleManager;
    private RouteManager routeManager;
    private PassengerManager passengerManager;
    private HelloController helloController;

    public void setHelloController(HelloController helloController) {
        this.helloController = helloController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeManagers();
        setupUIComponents();

        // Configuración inicial del dpReturn
        dpReturn.setDisable(true);  // Deshabilitado por defecto
        dpReturn.setValue(null);    // Sin valor seleccionado
    }

    private void initializeManagers() {
        try {
            airportManager = AirportManager.getInstance();
            routeManager = RouteManager.getInstance(airportManager);
            flightScheduleManager = FlightScheduleManager.getInstance(airportManager, routeManager);
            passengerManager = PassengerManager.getInstance();
        } catch (Exception e) {
            FXUtility.alert("Error Inicial", "No se pudieron inicializar los managers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupUIComponents() {
        loadAirportsIntoComboBoxes();
        loadPassengersComboBox();
        loadSeatTypeComboBox();
        loadBaggageComboBox();
        setupDatePickers();
        setupRoundTripToggle();
    }

    private void setupRoundTripToggle() {
        // Deshabilitar dpReturn inicialmente
        dpReturn.setDisable(true);

        // Listener para habilitar/deshabilitar según el checkbox
        chkRoundTrip.selectedProperty().addListener((obs, oldVal, newVal) -> {
            dpReturn.setDisable(!newVal);
            if (!newVal) {
                dpReturn.setValue(null); // Limpiar la fecha si se deselecciona
            }
        });
    }

    private void loadAirportsIntoComboBoxes() {
        try {
            ObservableList<String> airportOptions = FXCollections.observableArrayList();
            DoublyLinkedList allAirports = airportManager.getAllAirports();

            if (allAirports != null && !allAirports.isEmpty()) {
                for (int i = 0; i < allAirports.size(); i++) {
                    Airport airport = (Airport) allAirports.get(i);
                    airportOptions.add(formatAirportOption(airport));
                }
            } else {
                FXUtility.alert("Advertencia", "No se pudieron cargar los aeropuertos. La lista está vacía.");
            }

            cbOrigin.setItems(airportOptions);
            cbDestination.setItems(FXCollections.observableArrayList(airportOptions));

            if (!airportOptions.isEmpty()) {
                cbOrigin.getSelectionModel().selectFirst();
                if (airportOptions.size() > 1) {
                    cbDestination.getSelectionModel().select(1);
                }
            }
        } catch (ListException e) {
            FXUtility.alert("Error de Carga", "Error al cargar aeropuertos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String formatAirportOption(Airport airport) {
        return String.format("%s - %s (%s)",
                airport.getCode(),
                airport.getName(),
                airport.getCountry());
    }

    private String extractAirportCode(String selectedOption) {
        if (selectedOption == null || selectedOption.isEmpty()) {
            throw new IllegalArgumentException("La selección de aeropuerto no puede estar vacía");
        }
        return selectedOption.split(" - ")[0].trim();
    }

    private void loadPassengersComboBox() {
        try {
            DoublyLinkedList allPassengers = passengerManager.getAllPassengers();
            ObservableList<String> passengersOptions = FXCollections.observableArrayList();

            passengersOptions.add("NUEVO PASAJERO...");

            if (allPassengers != null && !allPassengers.isEmpty()) {
                for (int i = 0; i < allPassengers.size(); i++) {
                    Passenger p = (Passenger) allPassengers.get(i);
                    passengersOptions.add(p.getId() + " - " + p.getName());
                }
            }

            cbPassengers.setItems(passengersOptions);
            cbPassengers.getSelectionModel().selectFirst();
        } catch (ListException | TreeException e) {
            FXUtility.alert("Error de Carga", "No se pudieron cargar los pasajeros existentes: " + e.getMessage());
            e.printStackTrace();
            ObservableList<String> defaultOptions = FXCollections.observableArrayList("NUEVO PASAJERO...");
            cbPassengers.setItems(defaultOptions);
            cbPassengers.getSelectionModel().selectFirst();
        }
    }

    private void loadSeatTypeComboBox() {
        ObservableList<String> seatTypes = FXCollections.observableArrayList(
                "Economy", "Premium Economy", "Business", "First Class");
        cbSeatType.setItems(seatTypes);
        cbSeatType.getSelectionModel().selectFirst();
    }

    private void loadBaggageComboBox() {
        ObservableList<String> baggageOptions = FXCollections.observableArrayList(
                "Solo equipaje de mano",
                "1 Maleta documentada",
                "2 Maletas documentadas",
                "3+ Maletas documentadas");
        cbBaggage.setItems(baggageOptions);
        cbBaggage.getSelectionModel().selectFirst();
    }

    private void setupDatePickers() {
        dpDeparture.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        dpReturn.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate departureDate = dpDeparture.getValue();
                setDisable(empty || date.isBefore(LocalDate.now()) ||
                        (departureDate != null && date.isBefore(departureDate)));
            }
        });

        dpDeparture.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dpReturn.getValue() != null && dpReturn.getValue().isBefore(newVal)) {
                dpReturn.setValue(null);
            }
        });
    }

    @FXML
    private void handleCompleteReservation(ActionEvent event) {
        try {
            if (!validateReservation()) return;

            Passenger passenger = getOrCreatePassenger();
            if (passenger == null) return;

            String originCode = extractAirportCode(cbOrigin.getValue());
            String destinationCode = extractAirportCode(cbDestination.getValue());
            LocalDate departureDate = dpDeparture.getValue();

            CircularDoublyLinkedList availableFlights = findAvailableFlights(
                    originCode, destinationCode, departureDate);

            if (availableFlights.isEmpty()) {
                showAlternativeFlights(originCode);
                return;
            }

            Flight selectedFlight = (Flight) availableFlights.get(0);
            flightScheduleManager.processTicketPurchase(passenger, selectedFlight);
            passengerManager.addFlightToPassengerHistory(passenger.getId(), selectedFlight);

            showReservationSummary(selectedFlight, passenger);
            resetForm();

        } catch (Exception e) {
            FXUtility.alert("Error", "Error al completar reserva: " + e.getMessage());
        }
    }

    private Passenger getOrCreatePassenger() throws TreeException {
        String selectedPassenger = cbPassengers.getValue();

        if (selectedPassenger == null || selectedPassenger.equals("NUEVO PASAJERO...")) {
            // Crear nuevo pasajero
            String newPassengerId = FXUtility.prompt("Nuevo Pasajero", "Ingrese el ID del pasajero:");
            if (newPassengerId == null || newPassengerId.trim().isEmpty()) {
                FXUtility.alert("Error", "El ID del pasajero no puede estar vacío");
                return null;
            }

            String newPassengerName = FXUtility.prompt("Nuevo Pasajero", "Ingrese el nombre completo:");
            if (newPassengerName == null || newPassengerName.trim().isEmpty()) {
                FXUtility.alert("Error", "El nombre del pasajero no puede estar vacío");
                return null;
            }

            String nationality = FXUtility.prompt("Nuevo Pasajero", "Ingrese la nacionalidad:");
            if (nationality == null || nationality.trim().isEmpty()) {
                FXUtility.alert("Error", "La nacionalidad no puede estar vacía");
                return null;
            }

            try {
                passengerManager.registerPassenger(newPassengerId, newPassengerName, nationality);
                loadPassengersComboBox(); // Actualizar la lista de pasajeros
                return passengerManager.searchPassenger(newPassengerId);
            } catch (Exception e) {
                FXUtility.alert("Error", "No se pudo registrar el pasajero: " + e.getMessage());
                return null;
            }
        } else {
            // Pasajero existente
            String passengerId = selectedPassenger.split(" - ")[0].trim();
            return passengerManager.searchPassenger(passengerId);
        }
    }

    private boolean validateReservation() {
        // Validar campos obligatorios
        if (cbOrigin.getValue() == null || cbDestination.getValue() == null ||
                dpDeparture.getValue() == null || cbPassengers.getValue() == null) {
            FXUtility.alert("Error", "Debe completar todos los campos obligatorios");
            return false;
        }

        // Validar origen y destino diferentes
        String originCode = extractAirportCode(cbOrigin.getValue());
        String destinationCode = extractAirportCode(cbDestination.getValue());

        if (originCode.equals(destinationCode)) {
            FXUtility.alert("Error", "El aeropuerto de origen y destino no pueden ser iguales");
            return false;
        }

        // Validar fecha de retorno si es viaje redondo
        if (chkRoundTrip.isSelected() && dpReturn.getValue() == null) {
            FXUtility.alert("Error", "Debe seleccionar una fecha de retorno para viaje redondo");
            return false;
        }

        return true;
    }

    private void showAlternativeFlights(String originCode) throws ListException {
        StringBuilder message = new StringBuilder();
        message.append("No hay vuelos disponibles para la ruta seleccionada.\n\n");
        message.append("Vuelos disponibles desde ").append(originCode).append(":\n");

        CircularDoublyLinkedList allFlights = flightScheduleManager.getScheduledFlights();
        boolean foundFlights = false;

        for (int i = 0; i < allFlights.size(); i++) {
            Flight flight = (Flight) allFlights.get(i);
            if (flight.getOriginAirportCode().equals(originCode) &&
                    flight.getStatus() == Flight.FlightStatus.SCHEDULED) {

                message.append("- ").append(flight.getDestinationAirportCode())
                        .append(" (").append(airportManager.getAirportName(flight.getDestinationAirportCode()))
                        .append(") - ").append(flight.getDepartureTime().toLocalDate())
                        .append("\n");
                foundFlights = true;
            }
        }

        if (!foundFlights) {
            message.append("No hay vuelos disponibles desde este aeropuerto");
        }

        FXUtility.alert("Vuelos Disponibles", message.toString());
    }

    private void showReservationSummary(Flight flight, Passenger passenger) {
        StringBuilder summary = new StringBuilder();
        summary.append("¡Reserva completada con éxito!\n\n");
        summary.append("Detalles del vuelo:\n");
        summary.append("- Número: ").append(flight.getFlightNumber()).append("\n");
        summary.append("- Origen: ").append(airportManager.getAirportName(flight.getOriginAirportCode())).append("\n");
        summary.append("- Destino: ").append(airportManager.getAirportName(flight.getDestinationAirportCode())).append("\n");
        summary.append("- Fecha: ").append(flight.getDepartureTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        summary.append("- Puerta: ").append(flight.getGate()).append("\n\n");
        summary.append("Detalles del pasajero:\n");
        summary.append("- Nombre: ").append(passenger.getName()).append("\n");
        summary.append("- ID: ").append(passenger.getId()).append("\n");
        summary.append("- Asiento: ").append(cbSeatType.getValue()).append("\n");
        summary.append("- Equipaje: ").append(cbBaggage.getValue()).append("\n");

        FXUtility.alert("Reserva Exitosa", summary.toString());
    }


    private CircularDoublyLinkedList findAvailableFlights(String originCode, String destinationCode, LocalDate date)
            throws ListException {

        CircularDoublyLinkedList result = new CircularDoublyLinkedList();
        CircularDoublyLinkedList allFlights = flightScheduleManager.getScheduledFlights();

        for (int i = 0; i < allFlights.size(); i++) {
            Flight flight = (Flight) allFlights.get(i);

            // Filtramos por:
            // 1. Aeropuertos de origen/destino
            // 2. Fecha de salida
            // 3. Estado SCHEDULED
            // 4. Que no esté lleno
            if (flight.getOriginAirportCode().equals(originCode) &&
                    flight.getDestinationAirportCode().equals(destinationCode) &&
                    flight.getDepartureTime().toLocalDate().equals(date) &&
                    flight.getStatus() == Flight.FlightStatus.SCHEDULED &&
                    !flight.isFull()) {

                result.add(flight);
            }
        }
        return result;
    }

    @FXML
    private void handleAddPet(ActionEvent event) {
        FXUtility.alert("Mascotas",
                "Actualmente no permitimos mascotas en cabina. Consulte nuestras políticas para transporte de mascotas.");
    }

    @FXML
    private void handleBackToMenu(ActionEvent event) {
        if (helloController != null) {
            helloController.loadContentIntoHost("/ucr/proyectoalgoritmos/user-menubar.fxml");
        }
    }

    private void resetForm() {
        cbOrigin.getSelectionModel().selectFirst();
        if (cbDestination.getItems().size() > 1) {
            cbDestination.getSelectionModel().select(1);
        }
        dpDeparture.setValue(null);
        dpReturn.setDisable(true);
        dpReturn.setValue(null);
        chkRoundTrip.setSelected(false);
        cbPassengers.getSelectionModel().selectFirst();
        cbSeatType.getSelectionModel().selectFirst();
        cbBaggage.getSelectionModel().selectFirst();
    }
}