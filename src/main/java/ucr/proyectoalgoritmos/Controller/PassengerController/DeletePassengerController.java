package ucr.proyectoalgoritmos.Controller.PassengerController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;
import ucr.proyectoalgoritmos.Domain.passenger.PassengerManager;
import ucr.proyectoalgoritmos.Domain.TreeException;
import ucr.proyectoalgoritmos.UtilJson.PassengerJson;
import ucr.proyectoalgoritmos.util.FXUtility;

public class DeletePassengerController {

    @FXML private TextField tfId;
    @FXML private TextField tfName;
    @FXML private Button btnDelete;
    @FXML private TextField tfSearchId;
    @FXML private Button btnSearch;
    @FXML private TextField tfNationality;
    private TableView<Passenger> passengersTable; // Referencia a la tabla para actualizar

    private PassengerManager passengerManager;
    private Passenger selectedPassenger;

    // Método para inyectar la tabla desde el controlador principal
    public void setPassengersTable(TableView<Passenger> passengersTable) {
        this.passengersTable = passengersTable;
    }

    @FXML
    public void initialize() {
        passengerManager = PassengerManager.getInstance();
        disableFields(true);
        btnDelete.setDisable(true);

    }

    @FXML
    public void searchOnAction(ActionEvent actionEvent) {
        String searchId = tfSearchId.getText().trim();
        if (searchId.isEmpty()) {
            FXUtility.alert("Campo Requerido", "Por favor, ingrese el ID del pasajero a buscar.");
            return;
        }

        try {
            selectedPassenger = (Passenger) passengerManager.searchPassenger(searchId);
            if (selectedPassenger != null) {
                // Mostrar datos del pasajero encontrado
                tfId.setText(selectedPassenger.getId());
                tfName.setText(selectedPassenger.getName());
                tfNationality.setText(selectedPassenger.getNationality());

                disableFields(false);
                btnDelete.setDisable(false);
                tfSearchId.setDisable(true);
                btnSearch.setDisable(true);
                tfId.setDisable(false);
                tfName.setDisable(false);
                tfNationality.setDisable(false);

            } else {
                FXUtility.alert("No Encontrado", "No se encontró ningún pasajero con ID: " + searchId);
                clearFields();
                disableFields(true);
                btnDelete.setDisable(true);
                tfSearchId.setDisable(true);
                tfId.setDisable(false);
                tfName.setDisable(false);
                tfNationality.setDisable(false);

            }
        } catch (TreeException e) {
            FXUtility.alert("Error de Búsqueda", "Error al buscar pasajero: " + e.getMessage());
        }
    }

    @FXML
    public void deleteOnAction(ActionEvent actionEvent) {
        if (selectedPassenger == null) {
            FXUtility.alert("Error", "Primero debe buscar un pasajero para eliminar.");
            return;
        }

        String confirmation = FXUtility.alertYesNo(
                "Confirmar Eliminación",
                "¿Está seguro de eliminar este pasajero?",
                "ID: " + selectedPassenger.getId() + "\n" +
                        "Nombre: " + selectedPassenger.getName()
        );

        if ("YES".equals(confirmation)) {
            try {
                passengerManager.removeAndRenumberPassengers(selectedPassenger.getId());
                PassengerJson.savePassengersToJson(passengerManager.getPassengersAVL());

                // Actualizar la tabla de pasajeros directamente
                if (passengersTable != null) {
                    refreshPassengersTable();
                }

                FXUtility.alert("Éxito", "Pasajero eliminado correctamente.");
                resetForm();

            } catch (Exception e) {
                FXUtility.alert("Error", "No se pudo eliminar el pasajero: " + e.getMessage());
            }
        }
    }

    private void showPassengerDetails() {
        tfId.setText(selectedPassenger.getId());
        tfName.setText(selectedPassenger.getName());
        tfNationality.setText(selectedPassenger.getNationality());
        disableFields(false);
        btnDelete.setDisable(false);
        tfSearchId.setDisable(true);
        btnSearch.setDisable(true);
    }

    private void handlePassengerNotFound(String searchId) {
        FXUtility.alert("No Encontrado", "No existe pasajero con ID: " + searchId);
        clearFields();
        disableFields(true);
        btnDelete.setDisable(true);
    }

    private void clearFields() {
        tfId.clear();
        tfName.clear();
        tfNationality.clear();
    }

    private void disableFields(boolean disable) {
        tfId.setDisable(disable);
        tfName.setDisable(disable);
        tfNationality.setDisable(disable);
    }

    private void resetForm() {
        clearFields();
        disableFields(true);
        btnDelete.setDisable(true);
        tfSearchId.setDisable(false);
        btnSearch.setDisable(false);
        tfSearchId.clear();
        selectedPassenger = null;
    }

    private void refreshPassengersTable() {
        try {
            DoublyLinkedList passengers = passengerManager.getAllPassengers();
            ObservableList<Passenger> passengerData = FXCollections.observableArrayList();

            for (int i = 0; i < passengers.size(); i++) {
                passengerData.add((Passenger) passengers.get(i));
            }

            passengersTable.setItems(passengerData);
            passengersTable.refresh();

        } catch (ListException | TreeException e) {
            FXUtility.alert("Error", "No se pudo actualizar la tabla: " + e.getMessage());
        }
    }
}