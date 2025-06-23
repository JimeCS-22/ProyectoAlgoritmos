package ucr.proyectoalgoritmos.Controller.AirportController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.util.FXUtility;

public class UpdateAirportController {
    @FXML
    private TextField nameToSearch;
    @FXML
    private TextField airportCode;
    @FXML
    private TextField country;
    @FXML
    private TextField name;
    @FXML
    private ComboBox<Airport.AirportStatus> status;
    @FXML
    private Button Enter;
    @FXML
    private Button SearchButton;

    private AirportManager airportManager = AirportManager.getInstance();
    private Airport selectedAirport;
    @FXML
    private TextField departuresBoard;
    @FXML
    private TextField passengerQueue;

    @FXML
    public void initialize() {

        ObservableList<Airport.AirportStatus> statusOptions = FXCollections.observableArrayList(Airport.AirportStatus.values());
        status.setItems(statusOptions);
        status.getSelectionModel().selectFirst();


        disableEditFields();
        Enter.setDisable(true);
    }

    @FXML
    public void searchOnAction(ActionEvent actionEvent) {
        String searchName = nameToSearch.getText();
        if (searchName.isEmpty()) {
            FXUtility.alert("Error de Búsqueda", "Por favor, ingrese el nombre del aeropuerto a buscar.");
            return;
        }

        try {
            selectedAirport = airportManager.getAirportByName(searchName);

            if (selectedAirport != null) {

                airportCode.setText(selectedAirport.getCode());
                country.setText(selectedAirport.getCountry());
                name.setText(selectedAirport.getName()); // El nombre actual
                status.getSelectionModel().select(selectedAirport.getStatus());


                nameToSearch.setDisable(true);
                SearchButton.setDisable(true);
                enableEditFields();
                Enter.setDisable(false);

                FXUtility.alert("Aeropuerto Encontrado", "Se encontró el aeropuerto: " + selectedAirport.getName());
            } else {
                FXUtility.alert("Aeropuerto No Encontrado", "No se encontró ningún aeropuerto con el nombre: " + searchName);
                clearFields();
                disableEditFields();
                nameToSearch.setDisable(false);
                SearchButton.setDisable(false);
            }
        } catch (ListException e) {
            FXUtility.alert("Error de Búsqueda", "Ocurrió un error al buscar el aeropuerto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void enterOnAction(ActionEvent actionEvent) {
        if (selectedAirport == null) {
            FXUtility.alert("Error de Actualización", "Primero debe buscar y seleccionar un aeropuerto.");
            return;
        }

        if (airportCode.getText().isEmpty() || country.getText().isEmpty() || name.getText().isEmpty() || status.getValue() == null) {
            FXUtility.alert("Error de Validación", "Todos los campos obligatorios (código, país, nombre, estado) deben ser llenados.");
            return;
        }

        try {

            Airport updatedAirport = new Airport(
                    selectedAirport.getCode(),
                    name.getText(),
                    country.getText()
            );
            updatedAirport.setStatus(status.getValue());


            updatedAirport.setDeparturesBoard(selectedAirport.getDeparturesBoard());
            updatedAirport.setPassengerQueue(selectedAirport.getPassengerQueue());


            airportManager.updateAirport(updatedAirport);
            FXUtility.alert("Actualización Exitosa", "El aeropuerto " + updatedAirport.getName() + " ha sido actualizado.");

            clearFields();
            disableEditFields();
            nameToSearch.setDisable(false);
            SearchButton.setDisable(false);
            nameToSearch.clear();
            selectedAirport = null;

        } catch (ListException e) {
            FXUtility.alert("Error de Actualización", "Ocurrió un error al actualizar el aeropuerto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields() {
        airportCode.clear();
        country.clear();
        name.clear();
        status.getSelectionModel().clearSelection();

    }

    private void enableEditFields() {
        airportCode.setDisable(false);
        country.setDisable(false);
        name.setDisable(false);
        status.setDisable(false);

    }

    private void disableEditFields() {
        airportCode.setDisable(true);
        country.setDisable(true);
        name.setDisable(true);
        status.setDisable(true);
        Enter.setDisable(true);

    }
}
