package ucr.proyectoalgoritmos.route;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.route.RouteManager;
import ucr.proyectoalgoritmos.graph.DirectedSinglyLinkedListGraph;

import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects; // Para Objects.requireNonNull

import static org.junit.jupiter.api.Assertions.*;

class RouteManagerTest {

    private RouteManager routeManager;
    private AirportManager airportManager; // Real AirportManager instance
    private String routesJsonFilePath;

    @BeforeEach
    void setUp() throws URISyntaxException, IOException, ListException {
        // 1. Instantiate a REAL AirportManager
        airportManager = new AirportManager();

        // 2. Populate the real AirportManager with the airports that are expected
        //    for the routes to be loaded successfully.
        airportManager.createAirport("SJO", "Juan Santamaria", "Costa Rica");
        airportManager.createAirport("MIA", "Miami International", "USA");
        airportManager.createAirport("JFK", "John F. Kennedy", "USA");
        airportManager.createAirport("LAX", "Los Angeles International", "USA");
        airportManager.createAirport("CDG", "Charles de Gaulle", "France");
        airportManager.createAirport("SYD", "Sydney Kingsford Smith", "Australia");
        airportManager.createAirport("LIM", "Jorge Chavez International", "Peru");
        airportManager.createAirport("FRA", "Frankfurt Airport", "Germany");
        airportManager.createAirport("DXB", "Dubai International", "UAE");
        airportManager.createAirport("NRT", "Narita International", "Japan");
        airportManager.createAirport("ORD", "O'Hare International", "USA");
        airportManager.createAirport("PEK", "Beijing Capital", "China");
        airportManager.createAirport("IST", "Istanbul Airport", "Turkey");
        airportManager.createAirport("MEX", "Mexico City International", "Mexico");
        airportManager.createAirport("LIR", "Daniel Oduber Quirós", "Costa Rica");

        // 3. Initialize RouteManager with the real AirportManager
        // RouteManager ahora usará internamente RouteGraphService
        routeManager = new RouteManager(airportManager);

        // 4. Get the path to the test routes.json file from resources
        // Confirmado que está directamente en src/main/resources
        URL routesUrl = Objects.requireNonNull(RouteManagerTest.class.getClassLoader().getResource("routes.json"),
                "El archivo 'routes.json' no se encontró en los recursos. Asegúrate de que esté directamente en 'src/main/resources/routes.json'.");
        routesJsonFilePath = Paths.get(routesUrl.toURI()).toString();
        assertTrue(new File(routesJsonFilePath).exists(), "El archivo 'routes.json' no existe en la ruta esperada: " + routesJsonFilePath);
    }

    @Test
    @DisplayName("Debe inicializar el grafo correctamente y retornarlo")
    void testGetGraph() {
        // getGraph() en RouteManager debería delegar a RouteGraphService y devolver su grafo
        assertNotNull(routeManager.getGraph(), "El grafo no debería ser nulo después de la inicialización.");
        assertTrue(routeManager.getGraph() instanceof DirectedSinglyLinkedListGraph, "El objeto retornado debería ser una instancia de DirectedSinglyLinkedListGraph.");
    }

    @Test
    @DisplayName("Debe añadir un aeropuerto al grafo si no existe")
    void testAddAirportToGraph() throws ListException {
        // RouteManager.addAirportToGraph ahora debe delegar a RouteGraphService.addVertex
        assertEquals(0, routeManager.getGraph().getNumVertices(), "El grafo debería tener 0 vértices inicialmente.");

        routeManager.addAirportToGraph("SJO");
        assertEquals(1, routeManager.getGraph().getNumVertices(), "El grafo debería tener 1 vértice después de añadir SJO.");
        assertTrue(routeManager.getGraph().containsVertex("SJO"), "El grafo debería contener SJO.");

        routeManager.addAirportToGraph("SJO"); // Añadir el mismo aeropuerto de nuevo
        assertEquals(1, routeManager.getGraph().getNumVertices(), "Añadir SJO de nuevo no debería cambiar el conteo de vértices.");

        routeManager.addAirportToGraph("MIA");
        assertEquals(2, routeManager.getGraph().getNumVertices(), "El grafo debería tener 2 vértices después de añadir MIA.");
    }

    @Test
    @DisplayName("Debe cargar rutas desde JSON y añadirlas al grafo")
    void testLoadRoutesFromJson_Successful() throws IOException, ListException {
        assertEquals(0, routeManager.getGraph().getNumVertices(), "El grafo debería tener 0 vértices antes de cargar.");

        // Esta llamada activa la lectura del JSON y la adición de vértices/aristas al grafo interno.
        routeManager.loadRoutesFromJson(routesJsonFilePath);

        DirectedSinglyLinkedListGraph graph = routeManager.getGraph();
        // Basado en el JSON de ejemplo, hay 15 aeropuertos únicos (vértices)
        // y 16 rutas (aristas).
        assertTrue(graph.getNumVertices() >= 15, "El grafo debería tener al menos 15 vértices después de cargar.");

        // Verificar que algunos vértices clave existan
        assertTrue(graph.containsVertex("SJO"), "El grafo debería contener SJO.");
        assertTrue(graph.containsVertex("MIA"), "El grafo debería contener MIA.");
        assertTrue(graph.containsVertex("CDG"), "El grafo debería contener CDG.");
        assertTrue(graph.containsVertex("SYD"), "El grafo debería contener SYD.");
        assertTrue(graph.containsVertex("LIR"), "El grafo debería contener LIR.");

        // Verificar que aristas específicas existan (usando las nuevas claves y distancias)
        // Estas llamadas ahora se resuelven a través de RouteManager.checkRouteExists o similar
        // que a su vez delega a RouteGraphService.
        assertTrue(graph.hasEdge("SJO", "MIA"), "Debería tener arista SJO->MIA.");
        assertTrue(graph.hasEdge("MIA", "JFK"), "Debería tener arista MIA->JFK.");
        assertTrue(graph.hasEdge("JFK", "CDG"), "Debería tener arista JFK->CDG.");
        assertTrue(graph.hasEdge("LAX", "SYD"), "Debería tener arista LAX->SYD.");
        assertTrue(graph.hasEdge("SJO", "LAX"), "Debería tener arista SJO->LAX.");
        assertTrue(graph.hasEdge("MEX", "SJO"), "Debería tener arista MEX->SJO.");
    }

    @Test
    @DisplayName("Debe manejar el archivo JSON no existente lanzando IOException")
    void testLoadRoutesFromJson_FileNotFound() {
        String nonExistentPath = "/ruta/a/archivo_no_existente_rutas.json";

        assertThrows(IOException.class, () -> {
            routeManager.loadRoutesFromJson(nonExistentPath);
        }, "Debería lanzar IOException para un archivo no existente.");

        assertEquals(0, routeManager.getGraph().getNumVertices(), "El grafo debería permanecer vacío si el archivo no se encuentra.");
    }

    @Test
    @DisplayName("Debe manejar el archivo JSON mal formado lanzando JsonSyntaxException")
    void testLoadRoutesFromJson_MalformedJson() throws IOException {
        String malformedJsonPath = "temp_malformed_routes.json";
        try (FileWriter writer = new FileWriter(malformedJsonPath)) {
            writer.write("{ \"routes\": [ { \"origin_airport_code\": \"SJO\", \"destination_airport_code\": \"MIA\", \"distance\": 1300 }, \"malformed_entry\" ]}");
        }

        assertThrows(JsonSyntaxException.class, () -> {
            routeManager.loadRoutesFromJson(malformedJsonPath);
        }, "Debería lanzar JsonSyntaxException para JSON mal formado.");

        File tempFile = new File(malformedJsonPath);
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }


    @Test
    @DisplayName("Debe retornar Integer.MAX_VALUE si la ruta más corta no se encuentra o los aeropuertos no existen")
    void testCalculateShortestRoute_NotFound() throws IOException, ListException {
        routeManager.loadRoutesFromJson(routesJsonFilePath); // Cargar algunas rutas primero

        // Aquí se prueban los métodos de RouteManager que delegan a RouteGraphService
        assertEquals(Integer.MAX_VALUE, routeManager.calculateShortestRoute("SJO", "XYZ"),
                "Debería retornar Integer.MAX_VALUE si el aeropuerto de destino no existe en el grafo.");
        assertEquals(Integer.MAX_VALUE, routeManager.calculateShortestRoute("ABC", "MIA"),
                "Debería retornar Integer.MAX_VALUE si el aeropuerto de origen no existe en el grafo.");
        assertEquals(Integer.MAX_VALUE, routeManager.calculateShortestRoute("ABC", "XYZ"),
                "Debería retornar Integer.MAX_VALUE si ambos aeropuertos no existen en el grafo.");

        assertEquals(Integer.MAX_VALUE, routeManager.calculateShortestRoute("JFK", "PEK"),
                "Debería retornar Integer.MAX_VALUE si no hay un camino entre aeropuertos existentes.");

        assertEquals(6010, routeManager.calculateShortestRoute("SJO", "CDG"),
                "La ruta más corta de SJO a CDG debería ser 6010.");

        assertEquals(11090, routeManager.calculateShortestRoute("MEX", "SYD"),
                "La ruta más corta de MEX a SYD debería ser 11090.");
    }

    @Test
    @DisplayName("Debe verificar si una ruta directa existe")
    void testCheckRouteExists() throws IOException, ListException {
        routeManager.loadRoutesFromJson(routesJsonFilePath); // Cargar algunas rutas primero

        // Estas llamadas también delegan a los métodos apropiados en RouteManager
        assertTrue(routeManager.checkRouteExists("SJO", "MIA"), "SJO->MIA debería existir.");
        assertTrue(routeManager.checkRouteExists("MIA", "JFK"), "MIA->JFK debería existir.");
        assertTrue(routeManager.checkRouteExists("JFK", "CDG"), "JFK->CDG debería existir.");
        assertTrue(routeManager.checkRouteExists("LAX", "SYD"), "LAX->SYD debería existir.");
        assertTrue(routeManager.checkRouteExists("MEX", "SJO"), "MEX->SJO debería existir.");
        assertTrue(routeManager.checkRouteExists("LIR", "MIA"), "LIR->MIA debería existir.");


        assertFalse(routeManager.checkRouteExists("MIA", "SJO"), "MIA->SJO no debería existir directamente.");
        assertFalse(routeManager.checkRouteExists("CDG", "SJO"), "CDG->SJO no debería existir directamente.");
        assertFalse(routeManager.checkRouteExists("SJO", "NONEXISTENT"), "Ruta a un aeropuerto no existente no debería existir.");
        assertFalse(routeManager.checkRouteExists("NONEXISTENT", "SJO"), "Ruta desde un aeropuerto no existente no debería existir.");
        assertFalse(routeManager.checkRouteExists("SJO", "NRT"), "SJO->NRT no debería ser una ruta directa.");
    }
}