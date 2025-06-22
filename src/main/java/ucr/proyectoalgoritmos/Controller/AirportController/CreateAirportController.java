package ucr.proyectoalgoritmos.Controller.AirportController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.UtilJson.AirportJson;
import ucr.proyectoalgoritmos.util.FXUtility;

public class CreateAirportController {
    @javafx.fxml.FXML
    private TextField country;
    @javafx.fxml.FXML
    private TextField name;
    @javafx.fxml.FXML
    private Button Enter;
    @javafx.fxml.FXML
    private TextField airportCode;
    @javafx.fxml.FXML
    private TextField departuresBoard;
    @javafx.fxml.FXML
    private TextField passengerQueue;
    @javafx.fxml.FXML
    private ComboBox<Airport.AirportStatus> status;
    private AirportManager airportManager; // Instancia del AirportManager Singleton

    @FXML
    public void initialize() {
        status.getItems().setAll(Airport.AirportStatus.values());
        status.getSelectionModel().select(Airport.AirportStatus.ACTIVE); // Estado por defecto

        try {
            // Obtén la instancia del AirportManager Singleton
            this.airportManager = AirportManager.getInstance();
            // Esto asegura que la lista de aeropuertos se cargue al inicio
            // y que airportManager.getAirports() tenga datos.
        } catch (Exception e) {
            FXUtility.alert("Error de Inicialización", "No se pudo preparar el sistema para crear aeropuertos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void enterOnAction(ActionEvent event) {
        try {
            String code = airportCode.getText().trim().toUpperCase(); // ¡IMPORTANTE! Códigos IATA en mayúsculas
            String airportName = name.getText().trim();
            String airportCountry = country.getText().trim();
            Airport.AirportStatus selectedStatus = status.getSelectionModel().getSelectedItem();

            if (code.isEmpty() || airportName.isEmpty() || airportCountry.isEmpty() || selectedStatus == null){
                FXUtility.alert("Error de Validación", "Por favor, complete todos los campos.");
                return;
            }

            // Crea el nuevo objeto Airport
            Airport newAirport = new Airport(code , airportName , airportCountry);
            newAirport.setStatus(selectedStatus);

            // Delega la lógica de añadir y validar duplicidad al AirportManager
            // El AirportManager lanzará una ListException si el código ya existe.
            this.airportManager.addAirport(newAirport);

            // *** IMPORTANTE: Guarda la lista actualizada en JSON ***
            // Usa el método getAirports() del manager para obtener la lista actualizada.
            AirportJson.saveAirportsToJson(this.airportManager.getAllAirports());

            FXUtility.alert("Éxito", "Aeropuerto " + newAirport.getName() + " ha sido creado exitosamente.");
            clearFields();

        } catch (ListException e) {
            // Captura las excepciones de lógica lanzadas por AirportManager.addAirport()
            FXUtility.alert("Error de Lógica", e.getMessage());
        } catch (IllegalArgumentException e) {
            // Si addAirport lanza IllegalArgumentException (ej. aeropuerto nulo)
            FXUtility.alert("Error de Entrada", e.getMessage());
        } catch (Exception e) {
            // Captura cualquier otro error inesperado
            FXUtility.alert("Error del Sistema", "Ocurrió un error inesperado: " + e.getMessage());
            e.printStackTrace(); // Imprime la traza completa para depuración
        }
    }

    private void clearFields() {
        airportCode.clear();
        name.clear();
        country.clear();
        status.getSelectionModel().select(Airport.AirportStatus.ACTIVE);
    }
}
