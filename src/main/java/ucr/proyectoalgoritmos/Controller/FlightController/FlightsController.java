package ucr.proyectoalgoritmos.Controller.FlightController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.Circular.ListException;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.flight.FlightScheduleManager;
import ucr.proyectoalgoritmos.Domain.route.RouteManager;
import ucr.proyectoalgoritmos.util.FXUtility;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.ResourceBundle;

import static ucr.proyectoalgoritmos.Controller.HelloController.loadViewInNewStage;

public class FlightsController implements Initializable {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    @javafx.fxml.FXML
    private TableView<Flight> tableView;
    @javafx.fxml.FXML
    private Button btCreate;
    @javafx.fxml.FXML
    private Button btSearch;
    @javafx.fxml.FXML
    private Button btUpdate;
    @javafx.fxml.FXML
    private Button btDelete;

    @FXML
    private TableColumn<Flight, String> flightNumberColumn;
    @FXML
    private TableColumn<Flight, String> originCodeColumn;
    @FXML
    private TableColumn<Flight, String> destinationCodeColumn;
    @FXML
    private TableColumn<Flight, LocalDateTime> departureTimeColumn;
    @FXML
    private TableColumn<Flight, Integer> capacityColumn;
    @FXML
    private TableColumn<Flight, Integer> occupancyColumn;
    @FXML
    private TableColumn<Flight, Flight.FlightStatus> statusColumn; // Enum directo
    @FXML
    private TableColumn<Flight, String> passengersColumn; // Para mostrar un resumen (ej. "3/10") o la lista

    private FlightScheduleManager flightScheduleManager;
    private ObservableList<Flight> flightObservableList;
    private AirportManager airportManager;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        AirportManager airportManager = AirportManager.getInstance();
        RouteManager routeManager = RouteManager.getInstance();

        flightScheduleManager = FlightScheduleManager.getInstance(airportManager, routeManager);

        flightNumberColumn.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        originCodeColumn.setCellValueFactory(new PropertyValueFactory<>("originAirportCode"));
        destinationCodeColumn.setCellValueFactory(new PropertyValueFactory<>("destinationAirportCode"));
        departureTimeColumn.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        occupancyColumn.setCellValueFactory(new PropertyValueFactory<>("occupancy"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        passengersColumn.setCellValueFactory(new PropertyValueFactory<>("passengersDisplay"));

        updateTableView();
    }

    private void updateTableView() {
        CircularDoublyLinkedList currentFlights = flightScheduleManager.getScheduledFlights();
        if (currentFlights != null) {
            flightObservableList = FXCollections.observableArrayList();

            try {
                for (int i = 0; i < currentFlights.size(); i++) {
                    flightObservableList.add((Flight) currentFlights.get(i));
                }
            } catch (ucr.proyectoalgoritmos.Domain.list.ListException e) {
                System.err.println("Error al iterar sobre CircularDoublyLinkedList: " + e.getMessage());
                e.printStackTrace();
            } catch (ClassCastException e) {
                System.err.println("Error de casting al convertir a Flight. Asegúrese de que la lista contenga solo objetos Flight. " + e.getMessage());
                e.printStackTrace();
            }

            tableView.setItems(flightObservableList);
            tableView.refresh();
        }
    }


    @javafx.fxml.FXML
    public void searchFlightOnAction(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Buscar Vuelo");
        dialog.setHeaderText("Ingrese el número de vuelo a buscar:");
        dialog.setContentText("Número de Vuelo:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().isEmpty()) {
            String flightNumberToSearch = result.get().trim();

            try {
                Flight foundFlight = flightScheduleManager.findFlight(flightNumberToSearch);

                if (foundFlight != null) {

                    String originAirportName = "N/A";
                    String destinationAirportName = "N/A";

                    if (airportManager != null) {
                        Airport origin = airportManager.findAirport(foundFlight.getOriginAirportCode());
                        if (origin != null) {
                            originAirportName = origin.getName();
                        }
                        Airport destination = airportManager.findAirport(foundFlight.getDestinationAirportCode());
                        if (destination != null) {
                            destinationAirportName = destination.getName();
                        }
                    }

                    String flightInfo = String.format(
                            "Número de Vuelo: %s\n" +
                                    "Origen: %s (%s)\n" +
                                    "Destino: %s (%s)\n" +
                                    "Hora de Salida: %s\n" +
                                    "Capacidad: %d\n" +
                                    "Ocupación: %d\n" +
                                    "Estado: %s\n" +
                                    "Pasajeros: %s",
                            foundFlight.getFlightNumber(),
                            foundFlight.getOriginAirportCode(),
                            originAirportName,
                            foundFlight.getDestinationAirportCode(),
                            destinationAirportName,
                            foundFlight.getDepartureTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                            foundFlight.getCapacity(),
                            foundFlight.getOccupancy(),
                            foundFlight.getStatus(),
                            foundFlight.getPassengersDisplay()
                    );
                    FXUtility.alert("Vuelo Encontrado", flightInfo);
                } else {
                    FXUtility.alert("Vuelo No Encontrado", "No se encontró ningún vuelo con el número: '" + flightNumberToSearch + "'.");
                }
            } catch (Exception e) {
                showAlert("Error Inesperado", "Ocurrió un error inesperado al buscar el vuelo: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            FXUtility.alert("Búsqueda Cancelada", "La búsqueda de vuelo fue cancelada o el número de vuelo estaba vacío.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @javafx.fxml.FXML
    public void createFlightOnAction(ActionEvent actionEvent) {
        loadViewInNewStage("/ucr/proyectoalgoritmos/createFlight.fxml", "Create New Flight");
        updateTableView();
    }

    @javafx.fxml.FXML
    public void updateFlightOnAction(ActionEvent actionEvent) {

        TextInputDialog idInputDialog = FXUtility.dialog("Actualizar Vuelo", "Ingrese el NÚMERO DE VUELO del vuelo a actualizar:");
        idInputDialog.setContentText("Número de Vuelo:");

        Optional<String> idResult = idInputDialog.showAndWait();
        if (!idResult.isPresent() || idResult.get().trim().isEmpty()) {
            FXUtility.alert("Actualización Cancelada", "La operación de actualización de vuelo fue cancelada o el número de vuelo estaba vacío.");
            return;
        }

        String flightNumberToUpdate = idResult.get().trim();
        Flight flightToUpdate = null;

        try {
            flightToUpdate = flightScheduleManager.findFlight(flightNumberToUpdate);
        } catch (ucr.proyectoalgoritmos.Domain.list.ListException e) { // Usar ListException de tu dominio
            FXUtility.alert("Error de Búsqueda", "Ocurrió un error al buscar el vuelo: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (flightToUpdate == null) {
            FXUtility.alert("Vuelo No Encontrado", "No se encontró ningún vuelo con el número: '" + flightNumberToUpdate + "'.");
            return;
        }

        TextInputDialog originDialog = FXUtility.dialog("Actualizar Vuelo", "Ingrese el NUEVO CÓDIGO DE ORIGEN (actual: " + flightToUpdate.getOriginAirportCode() + "):");
        originDialog.setContentText("Código Origen:");
        originDialog.getEditor().setText(flightToUpdate.getOriginAirportCode()); // Precargar valor actual
        Optional<String> originResult = originDialog.showAndWait();
        if (!originResult.isPresent() || originResult.get().trim().isEmpty()) {
            FXUtility.alert("Actualización Cancelada", "El código de origen no puede estar vacío. Actualización cancelada.");
            return;
        }
        String newOriginCode = originResult.get().trim();

        TextInputDialog destinationDialog = FXUtility.dialog("Actualizar Vuelo", "Ingrese el NUEVO CÓDIGO DE DESTINO (actual: " + flightToUpdate.getDestinationAirportCode() + "):");
        destinationDialog.setContentText("Código Destino:");
        destinationDialog.getEditor().setText(flightToUpdate.getDestinationAirportCode());
        Optional<String> destinationResult = destinationDialog.showAndWait();
        if (!destinationResult.isPresent() || destinationResult.get().trim().isEmpty()) {
            FXUtility.alert("Actualización Cancelada", "El código de destino no puede estar vacío. Actualización cancelada.");
            return;
        }
        String newDestinationCode = destinationResult.get().trim();

        LocalDateTime currentDepartureDateTime = flightToUpdate.getDepartureTime();

        DateTimeFormatter timeOnlyFormatter = DateTimeFormatter.ofPattern("HH:mm");

        DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        TextInputDialog timeDialog = FXUtility.dialog(
                "Actualizar Vuelo",
                "Ingrese la NUEVA HORA (HH:MM) para la fecha " + currentDepartureDateTime.format(dateOnlyFormatter) +
                        "\n(Hora actual: " + currentDepartureDateTime.format(timeOnlyFormatter) + "):"
        );
        timeDialog.setContentText("Nueva Hora (HH:MM):");
        timeDialog.getEditor().setText(currentDepartureDateTime.format(timeOnlyFormatter)); // Precargar solo la hora actual
        Optional<String> timeResult = timeDialog.showAndWait();

        if (!timeResult.isPresent() || timeResult.get().trim().isEmpty()) {
            FXUtility.alert("Actualización Cancelada", "La hora de salida no puede estar vacía. Actualización cancelada.");
            return;
        }
        String newTimeString = timeResult.get().trim();

        LocalDateTime newDepartureTime;
        try {

            LocalTime newLocalTime = LocalTime.parse(newTimeString, timeOnlyFormatter);
            newDepartureTime = LocalDateTime.of(currentDepartureDateTime.toLocalDate(), newLocalTime);

        } catch (DateTimeParseException e) {
            FXUtility.alert("Formato de Hora Inválido", "Por favor, ingrese la hora en formato HH:MM (ej. 08:30).");
            return;
        }

        TextInputDialog capacityDialog = FXUtility.dialog("Actualizar Vuelo", "Ingrese la NUEVA CAPACIDAD (actual: " + flightToUpdate.getCapacity() + "):");
        capacityDialog.setContentText("Capacidad:");
        capacityDialog.getEditor().setText(String.valueOf(flightToUpdate.getCapacity()));
        Optional<String> capacityResult = capacityDialog.showAndWait();
        if (!capacityResult.isPresent() || capacityResult.get().trim().isEmpty()) {
            FXUtility.alert("Actualización Cancelada", "La capacidad no puede estar vacía. Actualización cancelada.");
            return;
        }
        int newCapacity;
        try {
            newCapacity = Integer.parseInt(capacityResult.get().trim());
            if (newCapacity <= 0) {
                FXUtility.alert("Capacidad Inválida", "La capacidad debe ser un número positivo.");
                return;
            }
        } catch (NumberFormatException e) {
            FXUtility.alert("Capacidad Inválida", "La capacidad debe ser un número entero válido.");
            return;
        }

        TextInputDialog occupancyDialog = FXUtility.dialog("Actualizar Vuelo", "Ingrese la NUEVA OCUPACIÓN (actual: " + flightToUpdate.getOccupancy() + "):");
        occupancyDialog.setContentText("Ocupación:");
        occupancyDialog.getEditor().setText(String.valueOf(flightToUpdate.getOccupancy()));
        Optional<String> occupancyResult = occupancyDialog.showAndWait();
        if (!occupancyResult.isPresent() || occupancyResult.get().trim().isEmpty()) {
            FXUtility.alert("Actualización Cancelada", "La ocupación no puede estar vacía. Actualización cancelada.");
            return;
        }
        int newOccupancy;
        try {
            newOccupancy = Integer.parseInt(occupancyResult.get().trim());
            if (newOccupancy < 0 || newOccupancy > newCapacity) {
                FXUtility.alert("Ocupación Inválida", "La ocupación debe ser un número no negativo y no puede exceder la capacidad.");
                return;
            }
        } catch (NumberFormatException e) {
            FXUtility.alert("Ocupación Inválida", "La ocupación debe ser un número entero válido.");
            return;
        }

        ChoiceDialog<Flight.FlightStatus> statusDialog = new ChoiceDialog<>(flightToUpdate.getStatus(), Flight.FlightStatus.values());
        statusDialog.setTitle("Actualizar Vuelo");
        statusDialog.setHeaderText("Seleccione el NUEVO ESTADO del vuelo (actual: " + flightToUpdate.getStatus() + "):");
        statusDialog.setContentText("Estado del Vuelo:");
        Optional<Flight.FlightStatus> statusResult = statusDialog.showAndWait();
        if (!statusResult.isPresent()) {
            FXUtility.alert("Actualización Cancelada", "El estado del vuelo no fue seleccionado. Actualización cancelada.");
            return;
        }
        Flight.FlightStatus newStatus = statusResult.get();

        String confirmationMessage = String.format(
                "¿Está seguro de que desea actualizar el vuelo %s con los siguientes datos?\n\n" +
                        "Código Origen: %s (antes: %s)\n" +
                        "Código Destino: %s (antes: %s)\n" +
                        "Hora de Salida: %s (antes: %s)\n" + // Utiliza el DATE_TIME_FORMATTER aquí
                        "Capacidad: %d (antes: %d)\n" +
                        "Ocupación: %d (antes: %d)\n" +
                        "Estado: %s (antes: %s)",
                flightNumberToUpdate,
                newOriginCode, flightToUpdate.getOriginAirportCode(),
                newDestinationCode, flightToUpdate.getDestinationAirportCode(),
                newDepartureTime.format(DATE_TIME_FORMATTER), flightToUpdate.getDepartureTime().format(DATE_TIME_FORMATTER), // Ambos usando el formateador consistente
                newCapacity, flightToUpdate.getCapacity(),
                newOccupancy, flightToUpdate.getOccupancy(),
                newStatus, flightToUpdate.getStatus()
        );

        String confirmationResponse = FXUtility.alertYesNo(
                "Confirmar Actualización de Vuelo",
                "Confirme los cambios",
                confirmationMessage
        );

        if ("YES".equals(confirmationResponse)) {

            flightToUpdate.setOriginAirportCode(newOriginCode);
            flightToUpdate.setDestinationAirportCode(newDestinationCode);
            flightToUpdate.setDepartureTime(newDepartureTime);
            flightToUpdate.setCapacity(newCapacity);
            flightToUpdate.setOccupancy(newOccupancy);
            flightToUpdate.setStatus(newStatus);

            try {

                boolean updated = flightScheduleManager.updateFlight(flightToUpdate);

                if (updated) {
                    FXUtility.alert("Éxito", "Vuelo '" + flightNumberToUpdate + "' actualizado correctamente.");
                    updateTableView();
                } else {
                    FXUtility.alert("Error de Actualización", "No se pudo actualizar el vuelo '" + flightNumberToUpdate + "'. Verifique los datos o si el vuelo aún existe.");
                }
            } catch (Exception e) {
                FXUtility.alert("Error Inesperado", "Ocurrió un error inesperado al intentar actualizar el vuelo: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            FXUtility.alert("Actualización Cancelada", "La actualización del vuelo ha sido cancelada.");
        }


    }

    @javafx.fxml.FXML
    public void deleteFlightOnAction(ActionEvent actionEvent) {

        TextInputDialog inputDialog = FXUtility.dialog("Eliminar Vuelo", "Ingrese el número de vuelo a eliminar:");

        inputDialog.setContentText("Número de Vuelo:");

        Optional<String> result = inputDialog.showAndWait();

        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String flightNumberToDelete = result.get().trim();

            Flight flightFoundForConfirmation = null;
            try {
                flightFoundForConfirmation = flightScheduleManager.findFlight(flightNumberToDelete);
            } catch (ucr.proyectoalgoritmos.Domain.list.ListException e) {

                FXUtility.alert("Error de Búsqueda", "Ocurrió un error al buscar el vuelo para confirmación: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            if (flightFoundForConfirmation == null) {

                FXUtility.alert("Vuelo No Encontrado", "No se encontró ningún vuelo con el número: '" + flightNumberToDelete + "'. No se puede eliminar.");
                return;
            }

            String confirmationMessage = String.format(
                    "¿Está seguro de que desea eliminar el siguiente vuelo?\n\n" +
                            "Número de Vuelo: %s\n" +
                            "Origen: %s\n" +
                            "Destino: %s\n" +
                            "Hora de Salida: %s\n" +
                            "Capacidad: %d\n" +
                            "Estado: %s",
                    flightFoundForConfirmation.getFlightNumber(),
                    flightFoundForConfirmation.getOriginAirportCode(),
                    flightFoundForConfirmation.getDestinationAirportCode(),
                    flightFoundForConfirmation.getDepartureTime().format(DATE_TIME_FORMATTER),
                    flightFoundForConfirmation.getCapacity(),
                    flightFoundForConfirmation.getStatus()
            );

            String confirmationResponse = FXUtility.alertYesNo(
                    "Confirmar Eliminación de Vuelo",
                    "Confirmación de Eliminación",
                    confirmationMessage
            );

            if ("YES".equals(confirmationResponse)) {
                try {
                    boolean deleted = flightScheduleManager.removeFlight(flightNumberToDelete);

                    if (deleted) {

                        FXUtility.alert("Éxito", "Vuelo '" + flightNumberToDelete + "' eliminado correctamente.");
                        updateTableView();
                    } else {

                        FXUtility.alert("Error de Eliminación", "No se pudo eliminar el vuelo '" + flightNumberToDelete + "'. Verifique que aún exista.");
                    }
                } catch (Exception e) {
                    FXUtility.alert("Error Inesperado", "Ocurrió un error inesperado al intentar eliminar el vuelo: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {

                FXUtility.alert("Eliminación Cancelada", "La eliminación del vuelo ha sido cancelada.");
            }
        } else {

            FXUtility.alert("Cancelado", "La operación de eliminación de vuelo fue cancelada o el número de vuelo estaba vacío.");
        }

    }

}
