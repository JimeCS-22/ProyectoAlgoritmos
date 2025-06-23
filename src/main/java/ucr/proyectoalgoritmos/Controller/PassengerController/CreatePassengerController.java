package ucr.proyectoalgoritmos.Controller.PassengerController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;
import ucr.proyectoalgoritmos.Domain.passenger.PassengerManager;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.TreeException;
import ucr.proyectoalgoritmos.UtilJson.PassengerJson;
import ucr.proyectoalgoritmos.util.FXUtility;
import ucr.proyectoalgoritmos.util.Utility;
import ucr.proyectoalgoritmos.util.ValidationUtility;

public class CreatePassengerController {

    private PassengerManager passengerManager;
    private TableView<Passenger> passengersTable; // Referencia a la tabla para actualizar

    @FXML
    private ComboBox<String> cbNationality;

    @FXML
    private Button btnEnter;

    @FXML
    private TextField tfFullName;

    @FXML
    private TextField tfId;

    // Método para inyectar la tabla desde el controlador principal
    public void setPassengersTable(TableView<Passenger> passengersTable) {
        this.passengersTable = passengersTable;
    }

    @FXML
    public void initialize() {
        try {
            passengerManager = PassengerManager.getInstance();
            setupNationalitiesComboBox();
            generateNextId();
        } catch (Exception e) {
            FXUtility.alert("Error de Inicialización",
                    "No se pudo preparar el sistema: " + e.getMessage());
        }
    }

    private void setupNationalitiesComboBox() {
        String[] nationalities = {
                "Korean", "Costarricense", "Argentina", "Española", "Colombiana", "Chilena",
                "Peruana", "Brasileña", "Estadounidense", "Mexicana", "Canadiense", "Italiana",
                "Francesa", "Alemana", "Japonesa", "China", "India"
        };

        ObservableList<String> nationalityList = FXCollections.observableArrayList(nationalities);
        cbNationality.setItems(nationalityList);
        cbNationality.getSelectionModel().selectFirst(); // Selecciona la primera por defecto
    }

    private void generateNextId() {
        try {
            int maxId = 0;
            DoublyLinkedList passengers = passengerManager.getAllPassengers();

            for (int i = 0; i < passengers.size(); i++) {
                Passenger p = (Passenger) passengers.get(i);
                try {
                    int currentId = Integer.parseInt(p.getId().replace("P", ""));
                    if (currentId > maxId) maxId = currentId;
                } catch (NumberFormatException e) {
                    System.err.println("ID no numérico: " + p.getId());
                }
            }

            String newId = "P" + String.format("%03d", maxId + 1);
            tfId.setText(newId);
            tfId.setDisable(true);

        } catch (ListException | TreeException e) {
            FXUtility.alert("Error", "No se pudo generar ID automático");
            tfId.setText("P001");
        }
    }

    @FXML
    public void enterOnAction(ActionEvent event) {
        try {
            String id = tfId.getText().trim();
            String fullName = tfFullName.getText().trim();
            String nationality = cbNationality.getValue();

            // Validación básica
            if (fullName.isEmpty()) {
                FXUtility.alert("Error", "El nombre completo es obligatorio");
                return;
            }

            if (!ValidationUtility.isValidName(fullName)) {
                FXUtility.alert("Error de Validación", "El nombre contiene caracteres inválidos.");
                return;
            }

            // Verificar si el pasajero ya existe
            if (passengerManager.searchPassenger(id) != null) {
                FXUtility.alert("Error", "ID ya en uso. Regenerando...");
                generateNextId();
                return;
            }

            // Registrar pasajero
            passengerManager.registerPassenger(id, fullName, nationality);

            // Guardar cambios
            PassengerJson.savePassengersToJson(passengerManager.getPassengersAVL());

            // Actualizar la tabla de pasajeros
            if (passengersTable != null) {
                refreshPassengersTable();
            }

            FXUtility.alert("Éxito", "Pasajero creado:\nID: " + id +
                    "\nNombre: " + fullName +
                    "\nNacionalidad: " + nationality);

            clearFields();
            generateNextId();

        } catch (TreeException e) {
            FXUtility.alert("Error", "Error en árbol: " + e.getMessage());
        } catch (Exception e) {
            FXUtility.alert("Error", "Error inesperado: " + e.getMessage());
        }
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

    private void clearFields() {
        tfFullName.clear();
        cbNationality.getSelectionModel().selectFirst(); // Resetear al primer valor
    }
}