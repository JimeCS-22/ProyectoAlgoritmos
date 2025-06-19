package ucr.proyectoalgoritmos.Domain.Archivos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions; // Importar Assertions para usar sus métodos estáticos

import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.route.RouteManager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

class DataLoaderTest {

    private AirportManager airportManager;
    private RouteManager routeManager;
    private DataLoader dataLoader;
    private String airportsFilePath;
    private String routesFilePath;

    /**
     * Configuración inicial para cada prueba.
     * Este método se ejecuta automáticamente antes de cada método de prueba (@Test)
     * para asegurar que cada test comienza con un estado limpio y predecible.
     *
     * @throws URISyntaxException Si hay un problema al convertir la URL del recurso a URI.
     */
    @BeforeEach
    void setUp() throws URISyntaxException {
        // Inicializa nuevas instancias de los gestores para cada test
        airportManager = new AirportManager();
        // El constructor de RouteManager asume que recibe un AirportManager
        routeManager = new RouteManager(airportManager);
        dataLoader = new DataLoader(airportManager, routeManager);

        // Intenta obtener las URLs de los archivos JSON desde el classpath (normalmente src/main/resources)
        URL airportsUrl = DataLoaderTest.class.getClassLoader().getResource("airports.json");
        URL routesUrl = DataLoaderTest.class.getClassLoader().getResource("routes.json");

        // Afirma que los archivos JSON existen. Si no, la prueba fallará aquí.
        Assertions.assertNotNull(airportsUrl, "Error: El archivo 'airports.json' no se encontró en el classpath. Asegúrate de que esté en src/main/resources.");
        Assertions.assertNotNull(routesUrl, "Error: El archivo 'routes.json' no se encontró en el classpath. Asegúrate de que esté en src/main/resources.");

        // Convierte las URLs a rutas de archivo del sistema, usando Paths.get para mayor robustez
        airportsFilePath = Paths.get(airportsUrl.toURI()).toString();
        routesFilePath = Paths.get(routesUrl.toURI()).toString();

        System.out.println("Ruta de aeropuertos: " + airportsFilePath);
        System.out.println("Ruta de rutas: " + routesFilePath);
    }

    /**
     * Prueba integral el flujo de trabajo de carga de datos, sincronización con el grafo
     * y operaciones de gestión y cálculo de rutas.
     *
     * @throws ListException Si ocurre un error relacionado con las operaciones de lista.
     * @throws IOException Si hay un problema de entrada/salida al leer los archivos.
     * @throws Exception Para cualquier otro error inesperado.
     */
    @Test
    void testDataLoaderWorkflow() throws ListException, IOException, Exception { // Propaga las excepciones para que JUnit las maneje
        System.out.println("\n--- INICIANDO TEST COMPLETO DE CARGA DE DATOS Y OPERACIONES ---");

        // 1. Carga de Aeropuertos desde archivo
        System.out.println("Cargando aeropuertos desde: " + airportsFilePath);
        dataLoader.loadAirportFromJson(airportsFilePath);
        System.out.println("Carga de aeropuertos completada. Total: " + airportManager.getAirportCount() + " aeropuertos.");
        // Verifica que se cargaron 9 aeropuertos, según el JSON de ejemplo
        Assertions.assertEquals(9, airportManager.getAirportCount(), "FALLO: El número de aeropuertos cargados no es el esperado (debería ser 5).");
        // Verifica que un aeropuerto específico (SJO) fue cargado correctamente
        Assertions.assertNotNull(airportManager.findAirport("SJO"), "FALLO: El aeropuerto SJO no se cargó correctamente.");
        System.out.println("--------------------------------\n");

        // 2. Sincronización de Aeropuertos con el Grafo de Rutas
        System.out.println("--- SINCRONIZANDO AEROPUERTOS CON EL GRAFO ---");
        if (airportManager.getAirportCount() > 0) {
            for (int i = 0; i < airportManager.getAirportCount(); i++) {
                Airport airport = (Airport) airportManager.getAllAirports().get(i);
                // Asume que RouteManager tiene un método `addAirportToGraph` para añadir vértices al grafo interno
                routeManager.addAirportToGraph(airport.getCode());
            }
            System.out.println("Aeropuertos añadidos como vértices al grafo de rutas.");
            // Si tu RouteManager tiene un método para obtener el número de vértices, puedes añadir una aserción aquí:
            // Assertions.assertEquals(airportManager.getAirportCount(), routeManager.getGraph().getVertexCount(), "FALLO: El número de vértices en el grafo no coincide con los aeropuertos cargados.");
        } else {
            // Si no se cargaron aeropuertos, la prueba debe fallar aquí
            Assertions.fail("No se cargaron aeropuertos. No se pueden añadir vértices al grafo ni continuar con la prueba.");
        }
        System.out.println("--------------------------------------------\n");

        // 3. Carga de Rutas desde archivo
        System.out.println("--- CARGANDO RUTAS ---");
        System.out.println("Cargando rutas desde: " + routesFilePath);
        dataLoader.loadRoutesFromJson(routesFilePath);
        System.out.println("Carga de rutas completada.");
        // Verifica que una ruta específica (SJO-MIA) existe después de la carga
        Assertions.assertTrue(routeManager.checkRouteExists("SJO", "MIA"), "FALLO: La ruta SJO-MIA no se cargó correctamente.");
        // Si tu RouteManager tiene un método para obtener el número total de rutas, puedes añadir una aserción:
        // Assertions.assertEquals(5, routeManager.getRouteCount(), "FALLO: El número de rutas cargadas no es el esperado (debería ser 5).");
        System.out.println("----------------------\n");

        // 4. Listado y Gestión de Aeropuertos
        System.out.println("--- LISTADO Y CAMBIO DE ESTADO DE AEROPUERTOS ---");
        System.out.println("Listado inicial de todos los aeropuertos cargados:");
        // Muestra todos los aeropuertos (activos, cerrados, en mantenimiento, inactivos)
        airportManager.listAirports(true, true, true);

        System.out.println("\nCambiando estado de 'MAD' a INACTIVE...");
        // Usa el método `activateOrDeactivateAirport` y el estado `INACTIVE`
        airportManager.activateOrDeactivateAirport("MAD", Airport.AirportStatus.INACTIVE);

        Airport madAirport = airportManager.findAirport("MAD");
        // Verifica que el aeropuerto MAD existe y que su estado fue actualizado a INACTIVE
        Assertions.assertNotNull(madAirport, "FALLO: Aeropuerto MAD no encontrado para cambiar estado.");
        Assertions.assertEquals(Airport.AirportStatus.INACTIVE, madAirport.getStatus(), "FALLO: El estado de MAD no cambió a INACTIVE.");

        System.out.println("Listado de aeropuertos después del cambio (solo activos):");
        // Muestra solo aeropuertos activos para verificar que MAD ya no aparece
        airportManager.listAirports(true, false, false);
        System.out.println("------------------------------\n");

        // 5. Cálculo de Ruta Más Corta
        System.out.println("--- CÁLCULO DE RUTAS ---");
        String originCode1 = "SJO";
        String destinationCode1 = "MIA";
        System.out.println("Calculando ruta más corta de " + originCode1 + " a " + destinationCode1 + "...");
        int shortestDistance1 = routeManager.calculateShortestRoute(originCode1, destinationCode1);

        // Verifica la distancia de SJO a MIA (2000 km según el JSON de ejemplo)
        Assertions.assertEquals(1300, shortestDistance1, "FALLO: La distancia de SJO-MIA es incorrecta.");
        System.out.println("Distancia encontrada (SJO-MIA): " + shortestDistance1 + " km");

        System.out.println("\nCalculando ruta más corta de SJO a CDG (SJO -> MIA -> JFK -> CDG):");
        // Verifica la distancia de SJO a CDG (1300 + 1500 + 6000 = 9500 km)
        int shortestDistanceSJO_CDG = routeManager.calculateShortestRoute("SJO", "CDG");
        Assertions.assertEquals(6010, shortestDistanceSJO_CDG, "FALLO: La distancia de SJO-CDG es incorrecta.");
        System.out.println("Distancia encontrada (SJO-CDG): " + shortestDistanceSJO_CDG + " km");

        System.out.println("\nIntentando calcular ruta a un aeropuerto no existente o no cargado (SJO a ZZZ):");
        int nonExistentRoute = routeManager.calculateShortestRoute("SJO", "ZZZ");
        // Se espera Integer.MAX_VALUE si no se encuentra una ruta (convención común)
        Assertions.assertEquals(Integer.MAX_VALUE, nonExistentRoute, "FALLO: Se encontró una ruta a un aeropuerto inexistente (ZZZ).");
        System.out.println("No se encontró ruta de SJO a ZZZ (Correcto)");
        System.out.println("------------------------\n");

        System.out.println("testDataLoaderWorkflow: PASSED exitosamente.");
    }
}