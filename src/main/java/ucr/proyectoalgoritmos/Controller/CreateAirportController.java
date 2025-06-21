package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
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
    private DoublyLinkedList airportList;

    @javafx.fxml.FXML
    public void initialize() {
        status.getItems().setAll(Airport.AirportStatus.values());
        status.getSelectionModel().select(Airport.AirportStatus.ACTIVE);
    }

    @javafx.fxml.FXML
    public void enterOnAction(ActionEvent event) {
        try {
            String code = airportCode.getText().trim();
            String airportName = name.getText().trim();
            String airportCountry = country.getText().trim();
            Airport.AirportStatus selectedStatus = status.getSelectionModel().getSelectedItem();

            if (code.isEmpty() || airportName.isEmpty() || airportCountry.isEmpty() || selectedStatus == null){
                FXUtility.alert("Error de Validación", "Por favor, complete todos los campos.");
                return;
            }

            DoublyLinkedList currentAirports = AirportManager.getInstance().getAirportList();
            boolean codeExists = false;
            for (int i = 0; i < currentAirports.size(); i++) {
                Airport existingAirport = (Airport) currentAirports.get(i);
                if (existingAirport.getCode().equalsIgnoreCase(code)) {
                    codeExists = true;
                    break;
                }
            }

            if (codeExists){
                FXUtility.alert("Error de Duplicación", "Ya existe un aeropuerto con el código: " + code);
                return;
            }

            Airport newAirport = new Airport(code , airportName , airportCountry);
            newAirport.setStatus(selectedStatus);

            // Agrega el nuevo aeropuerto a la lista del manejador
            AirportManager.getInstance().addAirport(newAirport);

            // *** IMPORTANTE: Guarda la lista actualizada en JSON ***
            AirportJson.saveAirportsToJson(AirportManager.getInstance().getAirportList());

            FXUtility.alert("Éxito", "Aeropuerto " + newAirport.getName() + " ha sido creado exitosamente.");
            clearFields();

        } catch (ListException e) {
            FXUtility.alert("Error de Lógica", e.getMessage());
        } catch (Exception e) {
            FXUtility.alert("Error del Sistema", "Ocurrió un error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields() {
        airportCode.clear();
        name.clear();
        country.clear();
        status.getSelectionModel().select(Airport.AirportStatus.ACTIVE);
    }
}
