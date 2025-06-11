package ucr.proyectoalgoritmos.Domain.Archivos;

import org.junit.jupiter.api.Test;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.route.RouteManager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

class DataLoaderTest {

    @Test
    void test() {

        // 1. Inicialización de Gestores
        AirportManager airportManager = new AirportManager();
        RouteManager routeManager = new RouteManager(airportManager);
        DataLoader dataLoader = new DataLoader(airportManager, routeManager);

        // Rutas a los archivos JSON. Se buscan en el classpath (usualmente src/main/resources)
        String airportsFilePath = null;
        String routesFilePath = null;
        try {
            URL airportsUrl = DataLoaderTest.class.getClassLoader().getResource("airports.json");
            URL routesUrl = DataLoaderTest.class.getClassLoader().getResource("routes.json");

            if (airportsUrl == null) {
                System.err.println("Error: No se encontró el archivo JSON de aeropuertos.");
                System.err.println("Asegúrate de que 'airports.json' esté en 'src/main/resources' o similar.");
                return;
            }
            if (routesUrl == null) {
                System.err.println("Error: No se encontró el archivo JSON de rutas.");
                System.err.println("Asegúrate de que 'routes.json' esté en 'src/main/resources' o similar.");
                return;
            }

            airportsFilePath = airportsUrl.toURI().getPath();
            routesFilePath = routesUrl.toURI().getPath();

            if (airportsFilePath.startsWith("/") && airportsFilePath.contains(":")) {
                airportsFilePath = airportsFilePath.substring(1);
            }
            if (routesFilePath.startsWith("/") && routesFilePath.contains(":")) {
                routesFilePath = routesFilePath.substring(1);
            }
            // ************************************************

        } catch (URISyntaxException e) { // Añadir este catch para URISyntaxException
            System.err.println("Error al convertir URL a URI: " + e.getMessage());
            e.printStackTrace();
            return;
        } catch (Exception e) {
            System.err.println("Error al obtener la ruta de los recursos: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        try {
            // 2. Carga de Aeropuertos desde archivo
            System.out.println("--- INICIANDO CARGA DE DATOS ---");
            System.out.println("Cargando aeropuertos desde: " + airportsFilePath);
            dataLoader.loadAirportFromJson(airportsFilePath);
            System.out.println("Carga de aeropuertos completada. Total: " + airportManager.getAirportCount() + " aeropuertos.");
            System.out.println("--------------------------------\n");

            // 3. Sincronización de Aeropuertos con el Grafo de Rutas
            System.out.println("--- SINCRONIZANDO AEROPUERTOS CON EL GRAFO ---");
            if (airportManager.getAirportCount() > 0) {
                for (int i = 0; i < airportManager.getAirportCount(); i++) {
                    Airport airport = (Airport) airportManager.getAllAirports().get(i);
                    routeManager.getGraph().addVertex(airport.getCode());
                }
                System.out.println("Aeropuertos añadidos como vértices al grafo de rutas.");
            } else {
                System.out.println("No se cargaron aeropuertos. No se pueden añadir vértices al grafo.");
            }
            System.out.println("--------------------------------------------\n");

            // 4. Carga de Rutas desde archivo
            System.out.println("--- CARGANDO RUTAS ---");
            System.out.println("Cargando rutas desde: " + routesFilePath);
            dataLoader.loadRoutesFromJson(routesFilePath);
            System.out.println("Carga de rutas completada.");
            System.out.println("----------------------\n");


            // 5. Listado y Gestión de Aeropuertos
            System.out.println("--- LISTADO DE AEROPUERTOS ---");
            System.out.println("Listado inicial de aeropuertos (activos e inactivos):");
            airportManager.listAirports(true, true);

            System.out.println("\nCambiando estado de 'MAD' a INACTIVO...");
            airportManager.setAirportStatus("MAD", Airport.AirportStatus.INACTIVE);
            System.out.println("Listado de aeropuertos después del cambio (solo activos):");
            airportManager.listAirports(false, true);
            System.out.println("------------------------------\n");

            // 6. Cálculo de Ruta Más Corta
            System.out.println("--- CÁLCULO DE RUTAS ---");
            String originCode1 = "SJO";
            String destinationCode1 = "MIA";
            System.out.println("Calculando ruta más corta de " + originCode1 + " a " + destinationCode1 + "...");
            int shortestDistance1 = routeManager.calculateShortestRoute(originCode1, destinationCode1);

            if (shortestDistance1 != Integer.MAX_VALUE) {
                System.out.println("Distancia encontrada: " + shortestDistance1 + " km");
            } else {
                System.out.println("No se encontró ruta de " + originCode1 + " a " + destinationCode1);
            }

            System.out.println("\nCalculando ruta más corta de SJO a CDG (vía MIA, JFK):");
            int shortestDistanceSJO_CDG = routeManager.calculateShortestRoute("SJO", "CDG");
            if (shortestDistanceSJO_CDG != Integer.MAX_VALUE) {
                System.out.println("Distancia encontrada: " + shortestDistanceSJO_CDG + " km");
            } else {
                System.out.println("No se encontró ruta de SJO a CDG");
            }

            System.out.println("\nIntentando ruta no existente o con aeropuertos no cargados (SJO a ZZZ):");
            int nonExistentRoute = routeManager.calculateShortestRoute("SJO", "ZZZ");
            if (nonExistentRoute != Integer.MAX_VALUE) {
                System.out.println("Distancia encontrada: " + nonExistentRoute + " km");
            } else {
                System.out.println("No se encontró ruta de SJO a ZZZ");
            }
            System.out.println("------------------------\n");

        } catch (ListException e) {
            System.err.println("Error de lista: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error de entrada/salida al leer el archivo. Asegúrate de que los JSON estén en 'src/main/resources' y sean accesibles: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Ocurrió un error inesperado: " + e.getMessage());
            e.printStackTrace();
        }



    }
}

