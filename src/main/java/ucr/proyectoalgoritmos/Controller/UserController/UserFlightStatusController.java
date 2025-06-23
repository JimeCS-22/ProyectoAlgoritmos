package ucr.proyectoalgoritmos.Controller.UserController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ucr.proyectoalgoritmos.Controller.HelloController;
import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.flight.Flight.FlightStatus;
import ucr.proyectoalgoritmos.Domain.flight.FlightScheduleManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.route.RouteManager;
import ucr.proyectoalgoritmos.util.FXUtility;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserFlightStatusController implements Initializable {

    // Componentes FXML
    @FXML private Label lblNumeroVuelo;
    @FXML private Label lblEstadoVuelo;
    @FXML private Label lblSalidaProgramada;
    @FXML private Label lblOrigen;
    @FXML private Label lblDestino;
    @FXML private Button btnBuscarVuelo;
    @FXML private TableView<Flight> flightStatusTable;
    @FXML private TableColumn<Flight, String> colNumeroVuelo;
    @FXML private TableColumn<Flight, String> colOrigen;
    @FXML private TableColumn<Flight, String> colDestino;
    @FXML private TableColumn<Flight, LocalDateTime> colSalidaProgramada;
    @FXML private TableColumn<Flight, FlightStatus> colEstado;
    @FXML private TableColumn<Flight, String> colPuerta;

    // Managers y datos
    private FlightScheduleManager flightScheduleManager;
    private AirportManager airportManager;
    private RouteManager routeManager;
    private ObservableList<Flight> otherFlightsObservableList;
    private Flight currentFlight;
    private HelloController helloController;
    private String currentFlightNumber;

    public void setHelloController(HelloController helloController) {
        this.helloController = helloController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupManagers();
        configureTableColumns();
        initializeDataStructures();
        loadFlightData();
        flightStatusTable.refresh();
//        promptForFlightNumber();
    }

    private void setupManagers() {
        airportManager = AirportManager.getInstance();
        routeManager = RouteManager.getInstance(airportManager);
        flightScheduleManager = FlightScheduleManager.getInstance(airportManager, routeManager);
    }

    private void configureTableColumns() {
        colNumeroVuelo.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        colOrigen.setCellValueFactory(new PropertyValueFactory<>("originAirportCode"));
        colDestino.setCellValueFactory(new PropertyValueFactory<>("destinationAirportCode"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPuerta.setCellValueFactory(new PropertyValueFactory<>("gate"));

        // Formateador para la columna de fecha y hora
        colSalidaProgramada.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        colSalidaProgramada.setCellFactory(column -> new TableCell<Flight, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd/MMM/yyyy - HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatter.format(item));
            }
        });
    }

    private void initializeDataStructures() {
        otherFlightsObservableList = FXCollections.observableArrayList();
        flightStatusTable.setItems(otherFlightsObservableList);
    }

    private void promptForFlightNumber() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Buscar Vuelo");
        dialog.setHeaderText("Ingrese el número de vuelo");
        dialog.setContentText("Número de vuelo:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::setFlightNumber);
    }

    public void setFlightNumber(String flightNumber) {
        this.currentFlightNumber = flightNumber;
        loadFlightData();
    }

    private void loadFlightData() {
        loadCurrentFlight();
        loadOtherFlights();
    }

    private void loadCurrentFlight() {
        try {
            if (currentFlightNumber == null || currentFlightNumber.trim().isEmpty()) {
                showNoFlightMessage();
                return;
            }

            currentFlight = flightScheduleManager.findFlight(currentFlightNumber);

            if (currentFlight != null) {
                // Forzar asignación de puerta
                String gate = currentFlight.getGate();
                displayFlightInfo(currentFlight);
            } else {
                showFlightNotFoundMessage();
                showAvailableFlights();
            }
        } catch (ListException e) {
            FXUtility.alert("Error de Datos", "Error al buscar el vuelo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayFlightInfo(Flight flight) throws ListException {
        lblNumeroVuelo.setText(flight.getFlightNumber());

        String originName = airportManager.getAirportName(flight.getOriginAirportCode());
        String destinationName = airportManager.getAirportName(flight.getDestinationAirportCode());

        lblOrigen.setText(String.format("%s (%s)", originName, flight.getOriginAirportCode()));
        lblDestino.setText(String.format("%s (%s)", destinationName, flight.getDestinationAirportCode()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd/MMM/yyyy - HH:mm");
        lblSalidaProgramada.setText(flight.getDepartureTime().format(formatter));

        // Mostrar la puerta de abordaje
        lblEstadoVuelo.setText(flight.getStatus().toString() + " - Puerta: " + flight.getGate());
        setFlightStatusLabel(flight.getStatus());
    }

    private void setFlightStatusLabel(FlightStatus status) {
        lblEstadoVuelo.setText(status.toString());

        switch (status) {
            case SCHEDULED:
            case ASSIGNED:
                lblEstadoVuelo.setTextFill(javafx.scene.paint.Color.web("#28a745")); // Verde
                break;
            case IN_PROGRESS:
                lblEstadoVuelo.setTextFill(javafx.scene.paint.Color.web("#007bff")); // Azul
                break;
            case COMPLETED:
                lblEstadoVuelo.setTextFill(javafx.scene.paint.Color.web("#6c757d")); // Gris
                break;
            case CANCELLED:
                lblEstadoVuelo.setTextFill(javafx.scene.paint.Color.web("#dc3545")); // Rojo
                break;
        }
    }

    private void showNoFlightMessage() {
        lblNumeroVuelo.setText("N/A");
        lblOrigen.setText("N/A");
        lblDestino.setText("N/A");
        lblSalidaProgramada.setText("N/A");
        lblEstadoVuelo.setText("N/A");
    }

    private void showFlightNotFoundMessage() {
        FXUtility.alert("Vuelo No Encontrado",
                "No se encontró el vuelo con número: " + currentFlightNumber);
    }

    private void showAvailableFlights() {
        try {
            CircularDoublyLinkedList flights = flightScheduleManager.getScheduledFlights();
            StringBuilder availableFlights = new StringBuilder();

            for (int i = 0; i < flights.size(); i++) {
                Flight flight = (Flight) flights.get(i);
                availableFlights.append(flight.getFlightNumber());
                if (i < flights.size() - 1) {
                    availableFlights.append(", ");
                }
            }

            FXUtility.alert("Vuelos Disponibles",
                    "Vuelos registrados en el sistema:\n" + availableFlights.toString());
        } catch (ListException e) {
            FXUtility.alert("Error", "No se pudieron obtener los vuelos disponibles");
        }
    }

    private void loadOtherFlights() {
        try {
            ObservableList<Flight> allFlights = FXCollections.observableArrayList();
            CircularDoublyLinkedList flights = flightScheduleManager.getScheduledFlights();

            for (int i = 0; i < flights.size(); i++) {
                Flight flight = (Flight) flights.get(i);
                if (currentFlight == null || !flight.getFlightNumber().equals(currentFlight.getFlightNumber())) {
                    // Forzar la asignación de puerta llamando a getGate()
                    String gate = flight.getGate(); // Esto asignará una puerta si no tiene
                    allFlights.add(flight);
                }
            }

            otherFlightsObservableList.setAll(allFlights);
        } catch (ListException e) {
            FXUtility.alert("Error de Carga", "No se pudieron cargar otros vuelos: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearchFlight() {
        promptForFlightNumber();
    }

    @FXML
    private void handleLogout() {
        if (helloController != null) {
            helloController.logout();
        }
    }
}