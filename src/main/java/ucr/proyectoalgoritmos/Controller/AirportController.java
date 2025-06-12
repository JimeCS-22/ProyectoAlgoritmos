package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import static ucr.proyectoalgoritmos.Controller.HelloController.loadView;

import ucr.proyectoalgoritmos.Controller.MainMenuController;
import ucr.proyectoalgoritmos.Domain.Circular.ListException;
import ucr.proyectoalgoritmos.graph.AdjacencyMatrixGraph;
import ucr.proyectoalgoritmos.graph.GraphException;

import java.io.IOException;
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

    public void setRootLayout(BorderPane rootLayout) {
        this.rootLayout = rootLayout;
    }

    @FXML
    public void createAirportOnAction(ActionEvent actionEvent) {
        loadView("/ucr/proyectoalgoritmos/createAirport.fxml", null);
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
                        //TextResult.setText("El aeropuerto '" + vertexToFind + "' EXISTE en el grafo.");
                    } else {
                       // TextResult.setText("El aeropuerto '" + vertexToFind + "' NO EXISTE en el grafo.");
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
}
