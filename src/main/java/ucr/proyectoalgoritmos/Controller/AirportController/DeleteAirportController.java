package ucr.proyectoalgoritmos.Controller.AirportController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.util.FXUtility;

import java.util.Optional;

public class DeleteAirportController {

    @FXML
    private TextField country;
    @FXML
    private TextField name;
    @FXML
    private Button Enter; // Este será el botón de eliminar
    @FXML
    private TextField nameToSearch;
    @FXML
    private Button SearchButton;
    @FXML
    private TextField airportCode;
    @FXML
    private TextField departuresBoard; // Solo para mostrar, no para editar/eliminar contenido
    @FXML
    private TextField passengerQueue;  // Solo para mostrar, no para editar/eliminar contenido
    @FXML
    private ComboBox<Airport.AirportStatus> status; // Especifica el tipo para el ComboBox

    private AirportManager airportManager;
    private Airport selectedAirport; // Para almacenar el aeropuerto encontrado

    @FXML
    public void initialize() {
        airportManager = AirportManager.getInstance(); // Obtener la instancia singleton
        disableFields(true); // Deshabilitar todos los campos al inicio
        Enter.setDisable(true); // Deshabilitar el botón de eliminar al inicio
        // Configurar los items del ComboBox si no lo haces en el FXML
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
            selectedAirport = airportManager.getAirportByName(searchName); // Buscar por nombre
            if (selectedAirport != null) {
                // Aeropuerto encontrado, cargar datos en los campos y habilitar el botón de eliminar
                airportCode.setText(selectedAirport.getCode());
                name.setText(selectedAirport.getName());
                country.setText(selectedAirport.getCountry());
                status.setValue(selectedAirport.getStatus());
                // Mostrar tamaño de las listas, no el contenido directo
                departuresBoard.setText("Vuelos: " + selectedAirport.getDeparturesBoard().size());
                passengerQueue.setText("Pasajeros: " + selectedAirport.getPassengerQueue().size());

                disableFields(false); // Habilitar campos para mostrar los datos
                Enter.setDisable(false); // Habilitar el botón de eliminar
                nameToSearch.setDisable(true); // Deshabilitar campo de búsqueda
                SearchButton.setDisable(true); // Deshabilitar botón de búsqueda
                FXUtility.alert("Aeropuerto Encontrado", "Se encontró el aeropuerto: " + selectedAirport.getName());
            } else {
                FXUtility.alert("No Encontrado", "No se encontró ningún aeropuerto con el nombre: " + searchName);
                clearFields(); // Limpiar campos si no se encuentra
                disableFields(true); // Asegurarse de que los campos estén deshabilitados
                Enter.setDisable(true); // Asegurarse de que el botón de eliminar esté deshabilitado
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

        // Usar FXUtility.alertYesNo para la confirmación
        String confirmationResult = FXUtility.alertYesNo(
                "Confirmar Eliminación",
                "¿Está seguro de que desea eliminar el aeropuerto?",
                "Aeropuerto a eliminar: " + selectedAirport.getName() + " (" + selectedAirport.getCode() + ")"
        );

        if (confirmationResult.equals("YES")) { // Si el usuario hizo clic en "Yes"
            try {
                // Eliminar el aeropuerto utilizando su código (es el identificador único)
                boolean deleted = airportManager.deleteAirport(selectedAirport.getCode());
                if (deleted) {
                    FXUtility.alert("Éxito", "Aeropuerto eliminado exitosamente.");
                    // Resetear la interfaz después de la eliminación
                    clearFields();
                    disableFields(true);
                    Enter.setDisable(true);
                    nameToSearch.setDisable(false); // Habilitar búsqueda para nueva operación
                    SearchButton.setDisable(false); // Habilitar botón de búsqueda
                    nameToSearch.clear(); // Limpiar campo de búsqueda
                    selectedAirport = null; // Limpiar referencia al aeropuerto seleccionado
                } else {
                    FXUtility.alert("Error", "No se pudo eliminar el aeropuerto. Es posible que ya no exista.");
                }
            } catch (ListException e) {
                FXUtility.alert("Error de Eliminación", "Ocurrió un error al eliminar el aeropuerto: " + e.getMessage());
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
        // nameToSearch.clear(); // Se limpia después de eliminar en enterOnAction
    }

    private void disableFields(boolean disable) {
        // Deshabilitar/habilitar todos los campos de detalle del aeropuerto
        airportCode.setDisable(disable);
        name.setDisable(disable);
        country.setDisable(disable);
        status.setDisable(disable);
        departuresBoard.setDisable(disable);
        passengerQueue.setDisable(disable);
    }
}
