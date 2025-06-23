package ucr.proyectoalgoritmos.Domain.Archivos;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException; // Importar para manejar errores de formato JSON
import ucr.proyectoalgoritmos.Domain.TreeException;
import ucr.proyectoalgoritmos.Domain.list.ListException; // Aunque no se usa directamente aquí, es buena práctica tenerla si la clase PassengerManager la usa
import ucr.proyectoalgoritmos.Domain.passenger.PassengerManager;
import ucr.proyectoalgoritmos.util.Utility;

import java.io.IOException;
import java.nio.charset.StandardCharsets; // Importar para especificar la codificación al leer el archivo
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects; // Para Objects.requireNonNull

/**
 * Clase responsable de cargar datos de pasajeros desde un archivo JSON
 * y asegurar que el sistema de gestión de pasajeros cumpla con un número mínimo.
 */
public class PassengerData {

    private PassengerManager passengerManager;
    private int minTotalPassengers;

    public PassengerData(PassengerManager passengerManager, int minTotalPassengers) {

        this.passengerManager = Objects.requireNonNull(passengerManager, "PassengerManager no puede ser nulo.");
        if (minTotalPassengers < 0) {
            throw new IllegalArgumentException("minTotalPassengers no puede ser negativo.");
        }
        this.minTotalPassengers = minTotalPassengers;
    }


    public void loadPassengersFromJson(String filePath) throws IOException, TreeException, JsonSyntaxException, IllegalArgumentException {

        try {

            String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);

            JsonElement parsedElement = JsonParser.parseString(content);
            JsonObject jsonObject = parsedElement.getAsJsonObject();
            JsonArray passengersArray = jsonObject.getAsJsonArray("passengers");

            if (passengersArray != null) {
                for (int i = 0; i < passengersArray.size(); i++) {
                    JsonObject passengerJson = passengersArray.get(i).getAsJsonObject();

                    String id = passengerJson.has("id") && !passengerJson.get("id").isJsonNull()
                            ? passengerJson.get("id").getAsString()
                            : Utility.RandomId();

                    if (!passengerJson.has("name") || passengerJson.get("name").isJsonNull()) {
                        throw new IllegalArgumentException("Pasajero en JSON sin propiedad 'name' o es nula.");
                    }
                    String name = passengerJson.get("name").getAsString();

                    if (!passengerJson.has("nationality") || passengerJson.get("nationality").isJsonNull()) {
                        throw new IllegalArgumentException("Pasajero en JSON sin propiedad 'nationality' o es nula.");
                    }
                    String nationality = passengerJson.get("nationality").getAsString();

                    try {
                        passengerManager.registerPassenger(id, name, nationality);
                    } catch (IllegalArgumentException e) {
                        System.err.println("ADVERTENCIA: No se pudo registrar pasajero con ID " + id + " desde JSON porque ya existe: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {

            throw e;
        } catch (JsonSyntaxException e) {

            throw new JsonSyntaxException("Error en el formato del archivo JSON de pasajeros: " + e.getMessage(), e);
        } catch (IllegalStateException | ClassCastException e) {

            throw new IllegalArgumentException("Estructura JSON inesperada o datos faltantes: " + e.getMessage(), e);
        }

        int currentPassengerCount = passengerManager.getPassengerCount();
        int passengersToGenerate = minTotalPassengers - currentPassengerCount;

        if (passengersToGenerate > 0) {
            System.out.println("Generando " + passengersToGenerate + " pasajeros adicionales para alcanzar el mínimo...");
            for (int i = 0; i < passengersToGenerate; i++) {
                String id = Utility.RandomId();
                String name = Utility.RandomNames();
                String nationality = Utility.RandomNationalities();

                try {

                    passengerManager.registerPassenger(id, name, nationality);
                } catch (IllegalArgumentException e) {

                    System.err.println("ADVERTENCIA: ID generado aleatoriamente duplicado: " + id + ". Intentando generar otro...");
                    i--;
                } catch (TreeException e) {

                    throw e;
                }
            }
        }
    }
}