package ucr.proyectoalgoritmos.Controller;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView; // Aunque no se usa, lo mantengo si estaba en tu código
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.route.RouteManager;
import ucr.proyectoalgoritmos.Domain.route.RouteManager.ShortestPathResult;

import java.io.IOException;

public class RouteController {

    @FXML
    private Button btCheckRoute;
    @FXML
    private ImageView imgPlaneOrigin;
    @FXML
    private TextField txtDestination;
    @FXML
    private Line routeLine;
    @FXML
    private Label lblOriginPoint;
    @FXML
    private Label lblDistance;
    @FXML
    private Circle circleOrigin;
    @FXML
    private Circle circleDestination;
    @FXML
    private Label lblDestinationPoint;
    @FXML
    private ImageView imgArrivalDest;
    @FXML
    private Label lblEstimatedDuration;
    @FXML
    private TextField txtOrigin;
    @FXML
    private Label lblRouteStatus;

    private static final String AIRPORTS_JSON_PATH = "src/main/resources/airports.json";
    private static final String ROUTES_JSON_PATH = "src/main/resources/routes.json";


    private RouteManager routeManager;
    private AirportManager airportManager;

    @FXML
    public void initialize() {
        airportManager = new AirportManager();
        try {
            airportManager.loadAirportsFromJson(AIRPORTS_JSON_PATH);
            System.out.println("Aeropuertos cargados exitosamente en RouteController.");
        } catch (IOException | ListException e) {
            System.err.println("Error al cargar aeropuertos en RouteController: " + e.getMessage());
            lblRouteStatus.setText("Error al cargar aeropuertos.");
            lblRouteStatus.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }

        routeManager = RouteManager.getInstance(airportManager);
        try {
            routeManager.loadRoutesFromJson(ROUTES_JSON_PATH);
            if (routeManager.getGraph() != null) {
                // AQUÍ ES DONDE ESTABA EL ERROR: Cambiado de .size() a .getNumVertices()
                System.out.println("Número de vértices en el grafo: " + routeManager.getGraph().getNumVertices());
                System.out.println("¿Grafo contiene 'MEX'? " + routeManager.getGraph().containsVertex("MEX"));
                System.out.println("¿Grafo contiene 'SJO'? " + routeManager.getGraph().containsVertex("SJO"));
                System.out.println("¿Existe ruta directa de 'MEX' a 'SJO'? " + routeManager.checkRouteExists("MEX", "SJO"));
                System.out.println("----------------------------------------------\n");
            }


        } catch (IOException e) {

            lblRouteStatus.setText("Error al cargar rutas.");
            lblRouteStatus.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }

        routeLine.setVisible(false);
        lblEstimatedDuration.setText("N/A");
        lblDistance.setText("N/A");
        lblRouteStatus.setText("Esperando entrada...");
    }


    @FXML
    public void checkFlightOnAction(ActionEvent actionEvent) {
        String originCode = txtOrigin.getText().trim().toUpperCase();
        String destinationCode = txtDestination.getText().trim().toUpperCase();

        lblRouteStatus.setText("Verificando...");
        lblRouteStatus.setStyle("-fx-text-fill: gray;");
        lblEstimatedDuration.setText("N/A");
        lblDistance.setText("N/A");
        routeLine.setVisible(false);
        lblOriginPoint.setText(originCode.isEmpty() ? "Origen" : originCode);
        lblDestinationPoint.setText(destinationCode.isEmpty() ? "Destino" : destinationCode);


        if (originCode.isEmpty() || destinationCode.isEmpty()) {
            lblRouteStatus.setText("Por favor, ingrese códigos de origen y destino.");
            lblRouteStatus.setStyle("-fx-text-fill: red;");
            return;
        }

        try {

            if (routeManager.getGraph() == null) {
                lblRouteStatus.setText("Error interno: Grafo no inicializado.");
                lblRouteStatus.setStyle("-fx-text-fill: red;");
                return;
            }

            if (!routeManager.getGraph().containsVertex(originCode)) {
                lblRouteStatus.setText("El aeropuerto de origen '" + originCode + "' no existe en el grafo.");
                lblRouteStatus.setStyle("-fx-text-fill: red;");
                return;
            }
            if (!routeManager.getGraph().containsVertex(destinationCode)) {
                lblRouteStatus.setText("El aeropuerto de destino '" + destinationCode + "' no existe en el grafo.");
                lblRouteStatus.setStyle("-fx-text-fill: red;");
                return;
            }

            ShortestPathResult result = routeManager.findShortestRouteDetails(originCode, destinationCode, "distance");

            if (result != null) {
                lblDistance.setText(String.format("%.2f km", result.totalDistance));
                lblEstimatedDuration.setText(String.format("%.0f min", result.totalDuration));
                lblRouteStatus.setText("Ruta encontrada.");
                lblRouteStatus.setStyle("-fx-text-fill: green;");
                routeLine.setVisible(true);

                System.out.println("Ruta encontrada: " + result.toString());
            } else {
                lblRouteStatus.setText("No se encontró ruta entre " + originCode + " y " + destinationCode + ".");
                lblRouteStatus.setStyle("-fx-text-fill: orange;");
            }


        } catch (ListException | IllegalArgumentException e) {
            lblRouteStatus.setText("Error al buscar ruta: " + e.getMessage());
            lblRouteStatus.setStyle("-fx-text-fill: red;");
            System.err.println("Error en checkFlightOnAction: " + e.getMessage());
            e.printStackTrace();
        }
    }
}