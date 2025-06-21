package ucr.proyectoalgoritmos.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import static ucr.proyectoalgoritmos.Controller.HelloController.loadViewInNewStage;

import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.graph.AdjacencyMatrixGraph;
import ucr.proyectoalgoritmos.graph.GraphException;
import ucr.proyectoalgoritmos.util.FXUtility;

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
        colDepartureBoard.setCellValueFactory(new PropertyValueFactory<>("departuresBoard"));
        colPassengerQueue.setCellValueFactory(new PropertyValueFactory<>("passengerQueue"));

        // Asigna la ObservableList a la TableView
        tblAirports.setItems(airportData);

        // Carga los aeropuertos existentes en la tabla al iniciar el controlador
        loadAirportsIntoTable();

        // Inicializa el grafo si es necesario (ejemplo)
        // this.graph = new AdjacencyMatrixGraph(20); /

    }

    public void refreshAirportTable() {
        loadAirportsIntoTable();
    }


    @FXML
    public void createAirportOnAction(ActionEvent actionEvent) {
        loadViewInNewStage("/ucr/proyectoalgoritmos/createAirport.fxml", "Create New Airport");
    }

    @FXML
    public void searchAirportOnAction(ActionEvent actionEvent) {

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Verificar Aeropuerto");
            dialog.setHeaderText("Verificar si un aeropuerto existe en el grafo");
            dialog.setContentText("Ingrese el código del aeropuerto:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().isEmpty()) {
                try {
                    Object vertexToFind = result.get();

                    boolean contains = graph.containsVertex(vertexToFind);
                    if (contains) {
                        FXUtility.alert("Información" , "El aeropuerto '" + vertexToFind + "' EXISTE en el grafo.");
                    } else {
                       FXUtility.alert("Información" , "El aeropuerto '" + vertexToFind + "' NO EXISTE en el grafo.");
                    }
                } catch (GraphException e) {
                    showAlert("Error del Grafo", e.getMessage());
                } catch (Exception e) {
                    showAlert("Error", "Ocurrió un error inesperado: " + e.getMessage());
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
    }

    @FXML
    public void updateAirportOnAction(ActionEvent actionEvent) {
    }

    @FXML
    public void deleteAirportOnAction(ActionEvent actionEvent) {
    }

    private void loadAirportsIntoTable() {
        airportData.clear(); // Limpia los datos existentes en la tabla

        try {
            DoublyLinkedList airports = AirportManager.getInstance().getAirportList();

            for (int i = 0; i < airports.size(); i++) {
                airportData.add((Airport) airports.get(i));
            }
        } catch (ListException e) {
            showAlert("Error al cargar aeropuertos", "No se pudieron cargar los aeropuertos: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
