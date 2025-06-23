package ucr.proyectoalgoritmos.Controller.AirportController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.UtilJson.AirportJson;
import ucr.proyectoalgoritmos.util.FXUtility;

import java.util.Optional;

public class DeleteAirportController {

    @FXML
    private TextField country;
    @FXML
    private TextField name;
    @FXML
    private Button Enter;
    @FXML
    private TextField nameToSearch;
    @FXML
    private Button SearchButton;
    @FXML
    private TextField airportCode;
    @FXML
    private TextField departuresBoard;
    @FXML
    private TextField passengerQueue;
    @FXML
    private ComboBox<Airport.AirportStatus> status;

    private AirportManager airportManager;
    private Airport selectedAirport;

    private AirportController airportController;

    public void setAirportController(AirportController airportController) {
        this.airportController = airportController;
    }

    @FXML
    public void initialize() {
        airportManager = AirportManager.getInstance();
        disableFields(true);
        Enter.setDisable(true);

        ObservableList<Airport.AirportStatus> statusOptions = FXCollections.observableArrayList(Airport.AirportStatus.values());
        status.setItems(statusOptions);
    }

    @FXML
    public void searchOnAction(ActionEvent actionEvent) {
        String searchName = nameToSearch.getText().trim();
        if (searchName.isEmpty()) {
            FXUtility.alert("Campo Requerido", "Por favor, ingrese el nombre del aeropuerto a buscar.");
            return;
        }

        try {
            selectedAirport = airportManager.getAirportByName(searchName);
            if (selectedAirport != null) {
                airportCode.setText(selectedAirport.getCode());
                name.setText(selectedAirport.getName());
                country.setText(selectedAirport.getCountry());
                status.setValue(selectedAirport.getStatus());
                departuresBoard.setText("Vuelos: " + selectedAirport.getDeparturesBoard().size());
                passengerQueue.setText("Pasajeros: " + selectedAirport.getPassengerQueue().size());

                disableFields(false);
                Enter.setDisable(false);
                nameToSearch.setDisable(true);
                SearchButton.setDisable(true);
                FXUtility.alert("Aeropuerto Encontrado", "Se encontró el aeropuerto: " + selectedAirport.getName());
            } else {
                FXUtility.alert("No Encontrado", "No se encontró ningún aeropuerto con el nombre: " + searchName);
                clearFields();
                disableFields(true);
                Enter.setDisable(true);
            }
        } catch (ListException e) {
            FXUtility.alert("Error de Búsqueda", "Ocurrió un error al buscar el aeropuerto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void enterOnAction(ActionEvent actionEvent) {
        if (selectedAirport == null) {
            FXUtility.alert("Error", "Primero debe buscar y seleccionar un aeropuerto para eliminar.");
            return;
        }

        String confirmationResult = FXUtility.alertYesNo(
                "Confirmar Eliminación",
                "¿Está seguro de que desea eliminar el aeropuerto?",
                "Aeropuerto a eliminar: " + selectedAirport.getName() + " (" + selectedAirport.getCode() + ")"
        );

        if (confirmationResult.equals("YES")) {
            try {
                // *** CAMBIO CLAVE: Llama a deleteAirport con el código del aeropuerto ***
                boolean deleted = airportManager.deleteAirport(selectedAirport.getCode());
                if (deleted) {
                    FXUtility.alert("Éxito", "Aeropuerto eliminado exitosamente.");

                    // Notificar al controlador principal que refresque la tabla
                    if (airportController != null) {
                        airportController.refreshAirportTable();
                    }

                    // Cerrar la ventana actual después de una eliminación exitosa
                    Stage stage = (Stage) Enter.getScene().getWindow();
                    stage.close();

                } else {
                    FXUtility.alert("Error", "No se pudo eliminar el aeropuerto. Es posible que ya no exista.");
                }
            } catch (ListException e) {
                FXUtility.alert("Error de Eliminación", "Ocurrió un error al eliminar el aeropuerto: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) { // Capturar cualquier otra excepción del guardado (si deleteAirport la propaga)
                FXUtility.alert("Error de Persistencia", "Ocurrió un error al guardar los cambios en el archivo: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            FXUtility.alert("Cancelado", "Operación de eliminación cancelada.");
        }
    }

    private void clearFields() {
        airportCode.clear();
        name.clear();
        country.clear();
        status.setValue(null);
        departuresBoard.clear();
        passengerQueue.clear();
    }

    private void disableFields(boolean disable) {
        airportCode.setDisable(disable);
        name.setDisable(disable);
        country.setDisable(disable);
        status.setDisable(disable);
        departuresBoard.setDisable(disable);
        passengerQueue.setDisable(disable);
    }
}
