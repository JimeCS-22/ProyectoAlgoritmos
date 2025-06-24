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
    }

    private void initializeManagers() {
        try {
            airportManager = AirportManager.getInstance();
            routeManager = RouteManager.getInstance(airportManager);
            flightScheduleManager = FlightScheduleManager.getInstance(airportManager, routeManager);
            passengerManager = PassengerManager.getInstance();
        } catch (Exception e) {
            FXUtility.alert("Error Inicial", "No se pudieron inicializar los managers: " + e.getMessage());
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
        chkRoundTrip.selectedProperty().addListener((obs, oldVal, newVal) -> {
            dpReturn.setDisable(!newVal);
            if (!newVal) {
                dpReturn.setValue(null);
            }
        });
    }

    private void loadAirportsIntoComboBoxes() {
        try {
            ObservableList<String> airportOptions = FXCollections.observableArrayList();
            for (int i = 0; i < airportManager.getAllAirports().size(); i++) {
                Airport airport = (Airport) airportManager.getAllAirports().get(i);
                airportOptions.add(formatAirportOption(airport));
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
            // Obtener lista de todos los pasajeros desde PassengerManager
            DoublyLinkedList allPassengers = passengerManager.getAllPassengers();
            ObservableList<String> passengersOptions = FXCollections.observableArrayList();

            // Agregar opción por defecto
            passengersOptions.add("Nuevo pasajero");

            // Agregar pasajeros almacenados
            for (int i = 0; i < allPassengers.size(); i++) {
                Passenger p = (Passenger) allPassengers.get(i);
                passengersOptions.add(p.getId() + " - " + p.getName());
            }

            cbPassengers.setItems(passengersOptions);
            cbPassengers.getSelectionModel().selectFirst();
        } catch (ListException | TreeException e) {
            FXUtility.alert("Error", "No se pudieron cargar los pasajeros: " + e.getMessage());
            // Opción por defecto si hay error
            ObservableList<String> defaultOptions = FXCollections.observableArrayList("Nuevo pasajero");
            cbPassengers.setItems(defaultOptions);
            cbPassengers.getSelectionModel().selectFirst();
        }
    }

    private void loadSeatTypeComboBox() {
        ObservableList<String> seatTypes = FXCollections.observableArrayList("Economy", "Premium Economy", "Business", "First Class");
        cbSeatType.setItems(seatTypes);
        cbSeatType.getSelectionModel().selectFirst();
    }

    private void loadBaggageComboBox() {
        ObservableList<String> baggageOptions = FXCollections.observableArrayList(
                "Solo equipaje de mano", "1 Maleta documentada",
                "2 Maletas documentadas", "3+ Maletas documentadas");
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
                setDisable(empty ||
                        date.isBefore(LocalDate.now()) ||
                        (departureDate != null && date.isBefore(departureDate)));
            }
        });

        dpDeparture.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dpReturn.getValue() != null &&
                    dpReturn.getValue().isBefore(newVal)) {
                dpReturn.setValue(null);
            }
        });
    }

    @FXML
    private void handleCompleteReservation(ActionEvent event) {
        try {
            // 1. Validar datos del formulario
            if (!validateReservation()) return;

            // 2. Obtener vuelo seleccionado
            Flight selectedFlight = getSelectedFlight();
            if (selectedFlight == null) {
                FXUtility.alert("Error", "No se ha seleccionado ningún vuelo");
                return;
            }

            // 3. Obtener o crear pasajero
            Passenger passenger = getOrCreatePassenger();
            if (passenger == null) {
                FXUtility.alert("Error", "No se pudo obtener o crear el pasajero");
                return;
            }

            // 4. Procesar la reservación
            passengerManager.processTicketPurchase(passenger, selectedFlight);

            // 5. Actualizar historial del pasajero
            passengerManager.addFlightToPassengerHistory(passenger.getId(), selectedFlight);

            // 6. Limpiar formulario
            resetForm();

            FXUtility.alert("Éxito", "Reserva completada exitosamente");

        } catch (Exception e) {
            FXUtility.alert("Error", "Error al completar reserva: " + e.getMessage());
        }
    }

    private Passenger getOrCreatePassenger() throws TreeException {
        String selectedPassenger = cbPassengers.getValue();
        if (selectedPassenger == null || selectedPassenger.equals("Nuevo pasajero")) {
            // Lógica para crear nuevo pasajero
            // Aquí deberías abrir un diálogo o ventana para registrar los datos del nuevo pasajero
            FXUtility.alert("Información", "Por favor implementar lógica para crear nuevo pasajero");
            return null;
        } else {
            // Extraer ID del pasajero seleccionado
            String passengerId = selectedPassenger.split(" - ")[0];
            return passengerManager.searchPassenger(passengerId);
        }
    }

    private boolean validateReservation() {
        if (cbPassengers.getValue() == null) {
            FXUtility.alert("Error", "Debe seleccionar un pasajero");
            return false;
        }
        if (!validateSearchForm()) {
            return false;
        }
        // Agregar otras validaciones necesarias
        return true;
    }

    private boolean validateSearchForm() {
        if (cbOrigin.getValue() == null || cbDestination.getValue() == null) {
            FXUtility.alert("Error", "Debe seleccionar aeropuerto de origen y destino");
            return false;
        }

        if (extractAirportCode(cbOrigin.getValue()).equals(extractAirportCode(cbDestination.getValue()))) {
            FXUtility.alert("Error", "El aeropuerto de origen y destino no pueden ser iguales");
            return false;
        }

        if (dpDeparture.getValue() == null) {
            FXUtility.alert("Error", "Debe seleccionar una fecha de salida");
            return false;
        }

        if (chkRoundTrip.isSelected() && dpReturn.getValue() == null) {
            FXUtility.alert("Error", "Para viaje redondo debe seleccionar fecha de retorno");
            return false;
        }

        return true;
    }

    private CircularDoublyLinkedList findAvailableFlights(
            String originCode,
            String destinationCode,
            LocalDate date
    ) throws ListException {
        CircularDoublyLinkedList result = new CircularDoublyLinkedList();
        CircularDoublyLinkedList allFlights = flightScheduleManager.getScheduledFlights();

        for (int i = 0; i < allFlights.size(); i++) {
            Flight flight = (Flight) allFlights.get(i);
            if (flight.getOriginAirportCode().equals(originCode) &&
                    flight.getDestinationAirportCode().equals(destinationCode) &&
                    flight.getDepartureTime().toLocalDate().equals(date) &&
                    flight.getStatus() == Flight.FlightStatus.SCHEDULED) {
                result.add(flight);
            }
        }

        return result;
    }

    private String getAvailableDestinations(String originCode) throws ListException {
        StringBuilder destinations = new StringBuilder();
        CircularDoublyLinkedList allFlights = flightScheduleManager.getScheduledFlights();

        for (int i = 0; i < allFlights.size(); i++) {
            Flight flight = (Flight) allFlights.get(i);
            if (flight.getOriginAirportCode().equals(originCode) &&
                    flight.getStatus() == Flight.FlightStatus.SCHEDULED) {
                String destInfo = String.format("%s - %s\n",
                        flight.getDestinationAirportCode(),
                        airportManager.getAirportName(flight.getDestinationAirportCode()));

                if (!destinations.toString().contains(destInfo)) {
                    destinations.append(destInfo);
                }
            }
        }

        return destinations.toString();
    }

    private void showFlightOptions(CircularDoublyLinkedList flights) throws ListException {
        StringBuilder flightsInfo = new StringBuilder("Vuelos disponibles:\n\n");

        for (int i = 0; i < flights.size(); i++) {
            Flight flight = (Flight) flights.get(i);
            flightsInfo.append(String.format(
                    "✈️ Vuelo %s - %s a %s\n" +
                            "🕒 Salida: %s\n" +
                            "💺 Asientos disponibles: %d/%d\n" +
                            "🚪 Puerta: %s\n\n",
                    flight.getFlightNumber(),
                    flight.getOriginAirportCode(),
                    flight.getDestinationAirportCode(),
                    flight.getDepartureTime().toString(),
                    flight.getCapacity() - flight.getOccupancy(),
                    flight.getCapacity(),
                    flight.getGate()
            ));
        }

        FXUtility.alert("Vuelos Disponibles", flightsInfo.toString());
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
        cbPassengers.getSelectionModel().selectFirst();
        cbSeatType.getSelectionModel().selectFirst();
        cbBaggage.getSelectionModel().selectFirst();
        dpDeparture.setValue(null);
        dpReturn.setValue(null);
        chkRoundTrip.setSelected(false);
    }

    // Método auxiliar para obtener el vuelo seleccionado (simulado)
    private Flight getSelectedFlight() {
        // En una implementación real, esto obtendría el vuelo seleccionado de una tabla o lista
        // Aquí simulamos un vuelo de ejemplo, con el número de vuelo quemado y una capacidad quemada
        try {
            return new Flight(
                    "FL123",
                    extractAirportCode(cbOrigin.getValue()),
                    extractAirportCode(cbDestination.getValue()),
                    dpDeparture.getValue().atStartOfDay(),
                    150
            );
        } catch (Exception e) {
            return null;
        }
    }
}