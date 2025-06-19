package ucr.proyectoalgoritmos.Domain.Archivos;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.route.RouteManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * La clase `DataLoader` es responsable de cargar datos de aeropuertos y rutas
 * desde archivos JSON. Actúa como un puente entre los archivos de configuración
 * y los gestores de lógica de negocio ({@link AirportManager} y {@link RouteManager}).
 */
public class DataLoader {

    private AirportManager airportManager;
    private RouteManager routeManager;

    /**
     * Constructor para inicializar el `DataLoader` con instancias de los gestores.
     * @param airportManager La instancia de {@link AirportManager} para añadir aeropuertos.
     * @param routeManager La instancia de {@link RouteManager} para añadir rutas y sincronizar el grafo.
     */
    public DataLoader(AirportManager airportManager, RouteManager routeManager){
        this.airportManager = airportManager;
        this.routeManager = routeManager;
    }

    /**
     * Carga datos de aeropuertos desde un archivo JSON.
     *
     * <p>Se espera que el archivo JSON de aeropuertos tenga el siguiente formato en la raíz:</p>
     * <pre>
     * [
     * {"code": "SJO", "name": "Juan Santamaría...", "country": "Costa Rica", "status": "ACTIVE"},
     * {"code": "MIA", "name": "Miami International...", "country": "USA", "status": "ACTIVE"},
     * // ... más objetos de aeropuerto
     * ]
     * </pre>
     *
     * @param filePath La ruta completa al archivo JSON de aeropuertos.
     * @throws IOException Si ocurre un error de lectura o acceso al archivo.
     * @throws ListException Si ocurre un error al añadir los aeropuertos al AirportManager
     * o al grafo de rutas.
     */
    public void loadAirportFromJson(String filePath) throws IOException , ListException{
        System.out.println("Loading airports from: " + filePath);
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JsonElement parsedElement =  JsonParser.parseString(content);

        // La raíz del JSON de aeropuertos es un JsonArray directamente.
        JsonArray airportsArray = parsedElement.getAsJsonArray();

        for (int i = 0 ; i < airportsArray.size(); i++){
            JsonObject airportJson = airportsArray.get(i).getAsJsonObject();
            String code = airportJson.get("code").getAsString();
            String name = airportJson.get("name").getAsString();
            String country = airportJson.get("country").getAsString();
            // String status = airportJson.get("status").getAsString(); // Descomentar si tu createAirport acepta el 'status'

            // Crea el aeropuerto en el AirportManager
            airportManager.createAirport(code , name , country); // Pasa 'status' aquí si lo descomentas arriba

            // Añade el aeropuerto como vértice al grafo de rutas.
            // Esto asegura que todos los aeropuertos existan como vértices antes de añadir cualquier ruta.
            routeManager.addAirportToGraph(code);
        }
        System.out.println("Airports loaded and added to AirportManager and Route Graph.");
    }

    /**
     * Carga datos de rutas desde un archivo JSON.
     *
     * <p>Se espera que el archivo JSON de rutas tenga el siguiente formato en la raíz:</p>
     * <pre>
     * {
     * "routes": [
     * {"origin_airport_code": "SJO", "destination_airport_code": "MIA", "distance": 1300},
     * {"origin_airport_code": "MIA", "destination_airport_code": "JFK", "distance": 1090},
     * // ... más objetos de ruta
     * ]
     * }
     * </pre>
     *
     * @param filePath La ruta completa al archivo JSON de rutas.
     * @throws IOException Si ocurre un error de lectura o acceso al archivo.
     * @throws ListException Si ocurre un error al añadir las rutas al RouteManager
     * o al grafo de rutas.
     */
    public void loadRoutesFromJson (String filePath) throws IOException , ListException{
        System.out.println("Loading routes from: " + filePath);
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JsonElement parsedElement = JsonParser.parseString(content);

        // La raíz del JSON de rutas es un JsonObject que contiene un JsonArray bajo la clave "routes".
        JsonObject jsonObject = parsedElement.getAsJsonObject();
        JsonArray routesArray = jsonObject.getAsJsonArray("routes");

        for (int i = 0; i < routesArray.size(); i++) {
            JsonObject routeJson = routesArray.get(i).getAsJsonObject();
            String origin = routeJson.get("origin_airport_code").getAsString();
            String destination = routeJson.get("destination_airport_code").getAsString();
            int distance = routeJson.get("distance").getAsInt();

            // Asegura que los aeropuertos de origen y destino existan como vértices en el grafo.
            // Esto actúa como una capa de seguridad en caso de que un aeropuerto de una ruta no estuviera
            // en el archivo de aeropuertos o no se cargó previamente.
            routeManager.addAirportToGraph(origin);
            routeManager.addAirportToGraph(destination);

            // Añade la arista al grafo del RouteManager.
            // La 'distance' del JSON se usa como el peso (weight) para la arista.
            routeManager.getGraph().addEdge(
                    routeManager.getGraph().getIndexForAirportCode(origin),
                    routeManager.getGraph().getIndexForAirportCode(destination),
                    distance
            );
            System.out.println("Added route: " + origin + " to " + destination + " (Weight: " + distance + ")");
        }
        System.out.println("Routes loaded and added to the RouteManager's graph.");
    }
}