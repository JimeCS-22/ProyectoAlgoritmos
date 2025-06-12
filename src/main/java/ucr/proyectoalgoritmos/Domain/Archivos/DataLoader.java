package ucr.proyectoalgoritmos.Domain.Archivos;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.route.RouteManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DataLoader {

    private AirportManager airportManager;
    private RouteManager routeManager;

    public DataLoader (AirportManager airportManager , RouteManager routeManager){

        this.airportManager = airportManager;
        this.routeManager = routeManager;

    }

    //Cargar los aeropuertos desde un archivo JSON
    public void loadAirportFromJson(String filePath) throws IOException , ListException{

        System.out.println("Loading airports from: " + filePath);
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JsonElement parsedElement =  JsonParser.parseString(content);
        JsonObject jsonObject = parsedElement.getAsJsonObject();
        JsonArray airportsArray = jsonObject.getAsJsonArray("airports");

        for (int i = 0 ; i < airportsArray.size(); i++){

            JsonObject airportJson = airportsArray.get(i).getAsJsonObject();
            String code = airportJson.get("code").getAsString();
            String name = airportJson.get("name").getAsString();
            String country = airportJson.get("country").getAsString();

            airportManager.createAirport(code , name , country);
        }

        System.out.println("Airports loaded and added to AirportManager.");
    }

    //Cargar rutas desde un archivo JSON
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

            //El de abajo lo comenté mientras tanto, hay que des-comentarlo
           // routeManager.addRoute(origin , destination , distance);

        }

        System.out.println("Routes loaded and added to the RouteManager.");

    }

}