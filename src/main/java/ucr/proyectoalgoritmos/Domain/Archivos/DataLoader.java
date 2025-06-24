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


    public DataLoader(AirportManager airportManager, RouteManager routeManager){
        this.airportManager = airportManager;
        this.routeManager = routeManager;
    }


    public void loadAirportFromJson(String filePath) throws IOException , ListException{
        System.out.println("Loading airports from: " + filePath);
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JsonElement parsedElement =  JsonParser.parseString(content);

        JsonArray airportsArray = parsedElement.getAsJsonArray();

        for (int i = 0 ; i < airportsArray.size(); i++){
            JsonObject airportJson = airportsArray.get(i).getAsJsonObject();
            String code = airportJson.get("code").getAsString();
            String name = airportJson.get("name").getAsString();
            String country = airportJson.get("country").getAsString();

            airportManager.createAirport(code , name , country);

            routeManager.addAirportToGraph(code);
        }
        System.out.println("Airports loaded and added to AirportManager and Route Graph.");
    }

    /**
     * Carga datos de rutas desde un archivo JSON.
     */
    public void loadRoutesFromJson (String filePath) throws IOException , ListException{
        System.out.println("Loading routes from: " + filePath);
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JsonElement parsedElement = JsonParser.parseString(content);

        JsonObject jsonObject = parsedElement.getAsJsonObject();
        JsonArray routesArray = jsonObject.getAsJsonArray("routes");

        for (int i = 0; i < routesArray.size(); i++) {
            JsonObject routeJson = routesArray.get(i).getAsJsonObject();
            String origin = routeJson.get("origin_airport_code").getAsString();
            String destination = routeJson.get("destination_airport_code").getAsString();
            int distance = routeJson.get("distance").getAsInt();

            routeManager.addAirportToGraph(origin);
            routeManager.addAirportToGraph(destination);

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