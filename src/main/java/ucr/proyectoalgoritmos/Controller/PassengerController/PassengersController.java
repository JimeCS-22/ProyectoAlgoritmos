package ucr.proyectoalgoritmos.Controller.PassengerController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import ucr.proyectoalgoritmos.Domain.TreeException;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;
import ucr.proyectoalgoritmos.Domain.passenger.PassengerManager;
import ucr.proyectoalgoritmos.util.FXUtility;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import static ucr.proyectoalgoritmos.Controller.HelloController.loadViewInNewStage;

public class PassengersController {
    @FXML
    private Button btUpdate;
    @FXML
    private BorderPane rootLayout;
    @FXML
    private Button btCreate;
    @FXML
    private TableColumn<Passenger, String> colNationality;
    @FXML
    private Button btDelete;
    @FXML
    private TableColumn<Passenger, String> colID;
    @FXML
    private TableColumn<Passenger, String> colFlightHistory;
    @FXML
    private Button btViewAll;
    @FXML
    private TableView<Passenger> tblPassengers;
    @FXML
    private TableColumn<Passenger, String> colFullName;
    @FXML
    private Button btSearch;

    private ObservableList<Passenger> passengerData;
    private final PassengerManager passengerManager = PassengerManager.getInstance();

    public void setRootLayout(BorderPane rootLayout) {
        this.rootLayout = rootLayout;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        loadPassengersIntoTable();
    }

    private void setupTableColumns() {
        passengerData = FXCollections.observableArrayList();

        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colNationality.setCellValueFactory(new PropertyValueFactory<>("nationality"));
        colFlightHistory.setCellValueFactory(new PropertyValueFactory<>("flightHistory"));

        tblPassengers.setItems(passengerData);
    }

    @FXML
    public void createPassengerOnAction(ActionEvent actionEvent) {
        try {
            // Cargar la vista de creación
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ucr/proyectoalgoritmos/createPassenger.fxml"));
            Parent root = loader.load();

            // Obtener el controlador y pasarle la referencia de la tabla
            CreatePassengerController createController = loader.getController();
            createController.setPassengersTable(tblPassengers); // tblPassengers es tu TableView

            // Mostrar la ventana
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Crear Nuevo Pasajero");
            stage.show();

        } catch (IOException e) {
            FXUtility.alert("Error", "No se pudo cargar la ventana de creación: " + e.getMessage());
        }
    }
    @FXML
    public void searchPassengerOnAction(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Buscar Pasajero");
        dialog.setHeaderText("Buscar pasajero por ID");
        dialog.setContentText("Ingrese el ID del pasajero:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(id -> {
            try {
                Passenger foundPassenger = passengerManager.searchPassenger(id.trim());

                if (foundPassenger != null) {
                    showPassengerDetails(foundPassenger);
                } else {
                    FXUtility.alert("Información", "El pasajero con ID '" + id + "' no existe.");
                }
            } catch (Exception e) {
                showErrorAlert("Error al buscar pasajero", e);
            }
        });
    }

    private void showPassengerDetails(Passenger passenger) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles del Pasajero");
        alert.setHeaderText("Información del pasajero");

        String content = String.format(
                "ID: %s\nNombre: %s\nNacionalidad: %s\nVuelos realizados: %d",
                passenger.getId(),
                passenger.getName(),
                passenger.getNationality(),
                passenger.getFlightHistory().size()
        );

        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void viewAllPassengersOnAction(ActionEvent actionEvent) {
        try {
            DoublyLinkedList allPassengers = passengerManager.getAllPassengers();

            if (allPassengers.isEmpty()) {
                FXUtility.alert("Información", "No hay pasajeros registrados.");
                return;
            }

            showAllPassengersInDialog(allPassengers);
        } catch (ListException | TreeException e) {
            showErrorAlert("Error al obtener pasajeros", e);
        }
    }

    private void showAllPassengersInDialog(DoublyLinkedList allPassengers) throws ListException {
        StringBuilder passengerListText = new StringBuilder("Listado de pasajeros:\n\n");

        for (int i = 0; i < allPassengers.size(); i++) {
            Passenger passenger = (Passenger) allPassengers.get(i);
            passengerListText.append(String.format(
                    "ID: %s\nNombre: %s\nNacionalidad: %s\n\n",
                    passenger.getId(),
                    passenger.getName(),
                    passenger.getNationality()
            ));
        }

        createExpandableAlert("Listado Completo", "Pasajeros registrados", passengerListText.toString()).showAndWait();
    }

    @FXML
    public void updatePassengerOnAction(ActionEvent actionEvent) {
        // Paso 1: Solicitar ID del pasajero a actualizar
        TextInputDialog idInputDialog = FXUtility.dialog("Actualizar Pasajero", "Ingrese el ID del pasajero a actualizar:");
        idInputDialog.setContentText("ID del Pasajero:");

        Optional<String> idResult = idInputDialog.showAndWait();
        if (!idResult.isPresent() || idResult.get().trim().isEmpty()) {
            FXUtility.alert("Actualización Cancelada", "La operación fue cancelada o el ID estaba vacío.");
            return;
        }

        String passengerIdToUpdate = idResult.get().trim();
        Passenger passengerToUpdate = null;

        try {
            passengerToUpdate = passengerManager.searchPassenger(passengerIdToUpdate);
        } catch (Exception e) {
            FXUtility.alert("Error de Búsqueda", "Ocurrió un error al buscar el pasajero: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (passengerToUpdate == null) {
            FXUtility.alert("Pasajero No Encontrado", "No se encontró ningún pasajero con el ID: '" + passengerIdToUpdate + "'.");
            return;
        }

        // Paso 2: Solicitar nuevo nombre
        TextInputDialog nameDialog = FXUtility.dialog("Actualizar Pasajero", "Ingrese el NUEVO NOMBRE (actual: " + passengerToUpdate.getName() + "):");
        nameDialog.setContentText("Nombre Completo:");
        nameDialog.getEditor().setText(passengerToUpdate.getName());
        Optional<String> nameResult = nameDialog.showAndWait();
        if (!nameResult.isPresent() || nameResult.get().trim().isEmpty()) {
            FXUtility.alert("Actualización Cancelada", "El nombre no puede estar vacío.");
            return;
        }
        String newName = nameResult.get().trim();

        // Paso 3: Solicitar nueva nacionalidad
        TextInputDialog nationalityDialog = FXUtility.dialog("Actualizar Pasajero", "Ingrese la NUEVA NACIONALIDAD (actual: " + passengerToUpdate.getNationality() + "):");
        nationalityDialog.setContentText("Nacionalidad:");
        nationalityDialog.getEditor().setText(passengerToUpdate.getNationality());
        Optional<String> nationalityResult = nationalityDialog.showAndWait();
        if (!nationalityResult.isPresent() || nationalityResult.get().trim().isEmpty()) {
            FXUtility.alert("Actualización Cancelada", "La nacionalidad no puede estar vacía.");
            return;
        }
        String newNationality = nationalityResult.get().trim();

        // Paso 4: Confirmar cambios
        String confirmationMessage = String.format(
                "¿Está seguro de que desea actualizar el pasajero %s con los siguientes datos?\n\n" +
                        "Nombre: %s (antes: %s)\n" +
                        "Nacionalidad: %s (antes: %s)",
                passengerIdToUpdate,
                newName, passengerToUpdate.getName(),
                newNationality, passengerToUpdate.getNationality()
        );

        String confirmationResponse = FXUtility.alertYesNo(
                "Confirmar Actualización de Pasajero",
                "Confirme los cambios",
                confirmationMessage
        );

        if ("YES".equals(confirmationResponse)) {
            // Actualizar el objeto pasajero
            passengerToUpdate.setName(newName);
            passengerToUpdate.setNationality(newNationality);

            try {
                boolean updated = passengerManager.updatePassenger(passengerToUpdate);

                if (updated) {
                    FXUtility.alert("Éxito", "Pasajero '" + passengerIdToUpdate + "' actualizado correctamente.");
                    loadPassengersIntoTable(); // Actualizar la tabla
                } else {
                    FXUtility.alert("Error de Actualización", "No se pudo actualizar el pasajero '" + passengerIdToUpdate + "'.");
                }
            } catch (Exception e) {
                FXUtility.alert("Error Inesperado", "Ocurrió un error al intentar actualizar el pasajero: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            FXUtility.alert("Actualización Cancelada", "La actualización del pasajero ha sido cancelada.");
        }
    }

    @FXML
    public void deletePassengerOnAction(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ucr/proyectoalgoritmos/deletePassenger.fxml"));
            Parent root = loader.load();

            DeletePassengerController deleteController = loader.getController();
            deleteController.setPassengersTable(tblPassengers);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Eliminar Pasajero");
            stage.show();

        } catch (IOException e) {
            FXUtility.alert("Error", "No se pudo cargar la ventana de eliminación: " + e.getMessage());
        }
    }

    private void loadPassengersIntoTable() {
        passengerData.clear();

        try {
            DoublyLinkedList passengers = passengerManager.getAllPassengers();

            for (int i = 0; i < passengers.size(); i++) {
                passengerData.add((Passenger) passengers.get(i));
            }
        } catch (ListException | TreeException e) {
            showErrorAlert("Error al cargar pasajeros", e);
        }
    }

    private Alert createExpandableAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText("Detalles:");

        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setExpanded(true);

        return alert;
    }

    private void showErrorAlert(String title, Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Ocurrió un error");
        alert.setContentText(e.getMessage());

        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }
}