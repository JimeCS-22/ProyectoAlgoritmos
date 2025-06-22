package ucr.proyectoalgoritmos.Domain.route; // <--- This package was previously 'ucr.proyectoalgoritmos.Domain.route'. Make sure it's consistent.

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.route.RouteGraphService; // Import RouteGraphService
import ucr.proyectoalgoritmos.graph.DirectedSinglyLinkedListGraph;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * Gestiona la lógica de rutas y la interacción con el grafo subyacente.
 * Actúa como una fachada, delegando operaciones complejas del grafo a RouteGraphService.
 */
public class RouteManager {
    // Instancia de RouteGraphService para manejar las operaciones del grafo.
    private RouteGraphService routeService;
    // Administrador de aeropuertos, si RouteManager necesita interactuar con él directamente.
    private AirportManager airportManager;
    // Instancia de GSON para la serialización/deserialización de JSON.
    private Gson gson;
    private static RouteManager instance;

    /**
     * Constructor para RouteManager.
     * @param airportManager La instancia de AirportManager para gestionar los aeropuertos.
     */
    public RouteManager(AirportManager airportManager) {
        this.airportManager = airportManager;
        // Inicializa RouteGraphService. El '0' aquí es un placeholder para maxVertices
        // si RouteGraphService permite un constructor con tamaño inicial o se expande dinámicamente.
        // Si tu grafo necesita un tamaño fijo, asegúrate de pasárselo aquí.
        this.routeService = new RouteGraphService(0);
        // Inicializa Gson con formato bonito para depuración.
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }



    /**
     * Carga las rutas desde un archivo JSON y las añade al grafo.
     * @param filePath La ruta del archivo JSON que contiene las rutas.
     * @throws IOException Si ocurre un error al leer el archivo.
     */
    public void loadRoutesFromJson(String filePath) throws IOException {
        try (Reader reader = new FileReader(filePath)) {
            // Usa el Wrapper para deserializar el objeto JSON completo
            RouteListWrapper wrapper = gson.fromJson(reader, RouteListWrapper.class);

            // Verifica si el wrapper o la lista de rutas son nulos
            if (wrapper == null || wrapper.getRoutes() == null) {
                System.err.println("Advertencia: No se encontraron rutas en el archivo JSON o el formato es incorrecto.");
                return;
            }

            // Obtiene la lista real de objetos Route (POJOs)
            List<Route> loadedRoutes = wrapper.getRoutes();

            for (Route route : loadedRoutes) {
                try {
                    // Añade los aeropuertos de origen y destino al grafo a través de RouteGraphService
                    routeService.addVertex(route.getOrigin_airport_code());
                    routeService.addVertex(route.getDestination_airport_code());
                    // Añade la arista (ruta) al grafo con su distancia
                    routeService.addEdge(route.getOrigin_airport_code(), route.getDestination_airport_code(), route.getDistance());
                } catch (ListException | IllegalArgumentException e) {
                    System.err.println("Error al procesar ruta: " + route.getOrigin_airport_code() + "->" + route.getDestination_airport_code() + ": " + e.getMessage());
                    // Puedes decidir si quieres relanzar la excepción o simplemente logearla.
                }
            }
        }
    }

    /**
     * Añade un aeropuerto al grafo interno si no existe.
     * Delega la operación a RouteGraphService.
     * @param airportCode El código del aeropuerto a añadir.
     * @throws ListException Si ocurre un error interno en la lista.
     */
    public void addAirportToGraph(String airportCode) throws ListException {
        routeService.addVertex(airportCode);
    }

    /**
     * Obtiene el grafo dirigido interno que representa las rutas.
     * @return La instancia de DirectedSinglyLinkedListGraph.
     */
    public DirectedSinglyLinkedListGraph getGraph() {
        // Accede al grafo a través del getter de RouteGraphService
        return routeService.getInternalGraph();
    }

    /**
     * Calcula la ruta más corta (distancia mínima) entre dos aeropuertos.
     * Delega la operación a RouteGraphService.
     * @param startCode El código del aeropuerto de origen.
     * @param endCode El código del aeropuerto de destino.
     * @return La distancia de la ruta más corta, o Integer.MAX_VALUE si no hay ruta.
     * @throws ListException Si ocurre un error interno durante el cálculo.
     */
    public int calculateShortestRoute(String startCode, String endCode) throws ListException {
        return routeService.shortestPath(startCode, endCode);
    }

    /**
     * Verifica si existe una ARISTA DIRECTA (ruta de un solo segmento) entre dos aeropuertos.
     * Esto difiere de 'calculateShortestRoute' que busca cualquier camino.
     * @param originCode El código del aeropuerto de origen.
     * @param destinationCode El código del aeropuerto de destino.
     * @return true si existe una arista directa, false en caso contrario.
     */
    public boolean checkRouteExists(String originCode, String destinationCode) {
        // AHORA DELEGA LA VERIFICACIÓN DE RUTA DIRECTA A RouteGraphService.hasDirectRoute()
        return routeService.hasDirectRoute(originCode, destinationCode);
    }

    // Puedes añadir más métodos aquí en RouteManager si los necesitas,
    // delegando a routeService o a airportManager según corresponda.
    // Ejemplo:
    /*
    public int getOutgoingRouteCount(String airportCode) {
        return routeService.getOutgoingRouteCount(airportCode);
    }
    */

    public RouteManager() {
    }

    public static synchronized RouteManager getInstance() {
        if (instance == null) {
            instance = new RouteManager();
        }
        return instance;
    }
}