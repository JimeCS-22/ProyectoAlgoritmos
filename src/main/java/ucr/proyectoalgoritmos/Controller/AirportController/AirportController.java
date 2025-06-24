package ucr.proyectoalgoritmos.Controller.AirportController;

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

import static ucr.proyectoalgoritmos.Controller.HelloController.loadViewInNewStage;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.graph.AdjacencyMatrixGraph;
import ucr.proyectoalgoritmos.graph.GraphException;
import ucr.proyectoalgoritmos.util.FXUtility;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class AirportController {
    @javafx.fxml.FXML
    private Button btCreate;
    private BorderPane rootLayout;
    private AdjacencyMatrixGraph graph;
    @FXML
    private Button btSearch;
    @FXML
    private TableColumn colDepartureBoard;
    @FXML
    private TableColumn colName;
    @FXML
    private TableColumn colPassengerQueue;
    @FXML
    private TableColumn colCountry;
    @FXML
    private TableView tblAirports;
    @FXML
    private TableColumn colCode;
    @FXML
    private Button btViewAll;
    @FXML
    private TableColumn colStatus;
    @FXML
    private Button btUpdate;
    @FXML
    private Button btDelete;
    private ObservableList<Airport> airportData;
    private AirportManager airportManager = AirportManager.getInstance();

    public void setRootLayout(BorderPane rootLayout) {
        this.rootLayout = rootLayout;
    }

    @FXML
    public void initialize() {

        airportData = FXCollections.observableArrayList();
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCountry.setCellValueFactory(new PropertyValueFactory<>("country"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDepartureBoard.setCellValueFactory(new PropertyValueFactory<>("departuresBoardSize"));
        colPassengerQueue.setCellValueFactory(new PropertyValueFactory<>("passengerQueueSize"));

        tblAirports.setItems(airportData);

        loadAirportsIntoTable();



    }

    public void refreshAirportTable() {

        loadAirportsIntoTable();
    }


    @FXML
    public void createAirportOnAction(ActionEvent actionEvent) {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ucr/proyectoalgoritmos/createAirport.fxml"));
            Parent root = fxmlLoader.load();

            CreateAirportController createAirportController = fxmlLoader.getController();

            createAirportController.setAirportController(this);

            Stage stage = new Stage();
            stage.setTitle("Create New Airport");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            refreshAirportTable();

        } catch (IOException e) {
            FXUtility.alert("Error", "No se pudo cargar la vista 'Crear Aeropuerto': " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void searchAirportOnAction(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Verificar Aeropuerto");
        dialog.setHeaderText("Verificar si un aeropuerto existe en la lista");
        dialog.setContentText("Ingrese el código del aeropuerto:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().isEmpty()) {
            String airportCode = result.get().trim();

            try {

                Airport foundAirport = airportManager.findAirport(airportCode);

                if (foundAirport != null) {
                    FXUtility.alert("Información", "El aeropuerto con código '" + airportCode + "' EXISTE en la lista.\n" +
                            "Nombre: " + foundAirport.getName() + "\n" +
                            "País: " + foundAirport.getCountry() + "\n" +
                            "Estado: " + foundAirport.getStatus());
                } else {
                    FXUtility.alert("Información", "El aeropuerto con código '" + airportCode + "' NO EXISTE en la lista.");
                }
            } catch (ListException e) {

                showAlert("Error de Lista", "Ocurrió un error al buscar en la lista: " + e.getMessage());
            } catch (Exception e) {

                showAlert("Error Inesperado", "Ocurrió un error inesperado: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void viewAllAirportsOnAction(ActionEvent actionEvent) {
        try {
            DoublyLinkedList allAirports = airportManager.getAllAirports();

            if (allAirports == null || allAirports.isEmpty()) {
                FXUtility.alert("Información", "No hay aeropuertos registrados en el sistema.");
                return;
            }

            String airportListText = "Listado de todos los aeropuertos:\n\n";

            for (int i = 0; i < allAirports.size(); i++) {
                Airport airport = (Airport) allAirports.get(i);
                airportListText += "Código: " + airport.getCode() + "\n";
                airportListText += "Nombre: " + airport.getName() + "\n";
                airportListText += "País: " + airport.getCountry() + "\n";
                airportListText += "Estado: " + airport.getStatus() + "\n";
                airportListText += "----------------------------------\n";
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Listado de Aeropuertos");
            alert.setHeaderText("Todos los aeropuertos registrados");
            alert.setContentText("A continuación, se muestra la lista completa de aeropuertos:");

            TextArea textArea = new TextArea(airportListText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            Label label = new Label("Detalles:");

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(expContent);
            alert.getDialogPane().setExpanded(true);

            alert.showAndWait();

        } catch (ListException e) {
            FXUtility.alert("Error de Lista", "Ocurrió un error al acceder a la lista de aeropuertos: " + e.getMessage());
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String exceptionText = sw.toString();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Inesperado");
            alert.setHeaderText("Ocurrió un error inesperado al mostrar los aeropuertos.");
            alert.setContentText("Inténtelo de nuevo. Si el problema persiste, contacte a soporte.");

            Label label = new Label("Detalles del error:");

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(expContent);
            alert.showAndWait();
        }
    }

    @FXML
    public void updateAirportOnAction(ActionEvent actionEvent) {

        loadViewInNewStage("/ucr/proyectoalgoritmos/updateAirport.fxml", "Update Airport");
        refreshAirportTable();

    }

    @FXML
    public void deleteAirportOnAction(ActionEvent actionEvent) {

        try{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ucr/proyectoalgoritmos/DeleteAirport.fxml"));
        Parent root = fxmlLoader.load();

        DeleteAirportController deleteAirportController = fxmlLoader.getController();
        deleteAirportController.setAirportController(this); // Pasar referencia

        Stage stage = new Stage();
        stage.setTitle("Eliminar Aeropuerto");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        refreshAirportTable();

    } catch (IOException e) {
        FXUtility.alert("Error", "No se pudo cargar la vista 'Eliminar Aeropuerto': " + e.getMessage());
        e.printStackTrace();
    }


    }

    private void loadAirportsIntoTable() {
        airportData.clear();

        try {
            DoublyLinkedList airports = AirportManager.getInstance().getAllAirports();

            for (int i = 0; i < airports.size(); i++) {
                airportData.add((Airport) airports.get(i));
            }
        } catch (ListException e) {
            showAlert("Error al cargar aeropuertos", "No se pudieron cargar los aeropuertos: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
