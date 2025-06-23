package ucr.proyectoalgoritmos.Controller.UserController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.flight.FlightScheduleManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.route.RouteManager;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class UserTripController implements Initializable {

    @javafx.fxml.FXML
    private TableView<Flight> flightHistoryTable;
    @javafx.fxml.FXML
    private TableColumn<Flight, String> flightNumberColumn;
    @javafx.fxml.FXML
    private TableColumn<Flight, String> originColumn;
    @javafx.fxml.FXML
    private TableColumn<Flight, String> destinationColumn;
    @javafx.fxml.FXML
    private TableColumn<Flight, LocalDateTime> departureTimeColumn;
    @javafx.fxml.FXML
    private TableColumn<Flight, Flight.FlightStatus> statusColumn;

    private FlightScheduleManager flightScheduleManager;
    private ObservableList<Flight> historyFlightsObservableList;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        AirportManager airportManager = AirportManager.getInstance();
        RouteManager routeManager = RouteManager.getInstance(airportManager);
        flightScheduleManager = FlightScheduleManager.getInstance(airportManager, routeManager);

        flightNumberColumn.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        originColumn.setCellValueFactory(new PropertyValueFactory<>("originAirportCode"));
        destinationColumn.setCellValueFactory(new PropertyValueFactory<>("destinationAirportCode"));

        departureTimeColumn.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        departureTimeColumn.setCellFactory(column -> new TableCell<Flight, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DATE_TIME_FORMATTER));
                }
            }
        });

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        loadAndFilterHistoricalFlights();

    }

    private void loadAndFilterHistoricalFlights() {
        flightScheduleManager.reloadFlightsFromJson();
        CircularDoublyLinkedList allFlights = flightScheduleManager.getScheduledFlights();
        historyFlightsObservableList = FXCollections.observableArrayList();

        if (allFlights != null) {
            try {
                for (int i = 0; i < allFlights.size(); i++) {
                    Object obj = allFlights.get(i);
                    if (obj instanceof Flight) {
                        Flight flight = (Flight) obj;

                        if (flight.getStatus() == Flight.FlightStatus.COMPLETED ||
                                flight.getStatus() == Flight.FlightStatus.CANCELLED) {
                            historyFlightsObservableList.add(flight);
                        }
                    }
                }
            } catch (ListException e) {
                System.err.println("Error al acceder a la lista de vuelos: " + e.getMessage());
                e.printStackTrace();
            } catch (ClassCastException e) {
                System.err.println("Error de casting: Se esperaba un objeto Flight en la lista. " + e.getMessage());
                e.printStackTrace();
            }
        }
        flightHistoryTable.setItems(historyFlightsObservableList);
        flightHistoryTable.refresh();
    }
}

