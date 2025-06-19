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

    /**
     * Constructor para PassengerData.
     * @param passengerManager El gestor de pasajeros al que se añadirán los pasajeros.
     * @param minTotalPassengers El número mínimo de pasajeros que el sistema debe tener.
     * @throws IllegalArgumentException Si passengerManager es nulo o minTotalPassengers es negativo.
     */
    public PassengerData(PassengerManager passengerManager, int minTotalPassengers) {
        // Validaciones en el constructor para asegurar que los objetos esenciales no son nulos.
        this.passengerManager = Objects.requireNonNull(passengerManager, "PassengerManager no puede ser nulo.");
        if (minTotalPassengers < 0) {
            throw new IllegalArgumentException("minTotalPassengers no puede ser negativo.");
        }
        this.minTotalPassengers = minTotalPassengers;
    }

    /**
     * Carga pasajeros desde un archivo JSON y genera pasajeros adicionales
     * si el conteo total no alcanza el mínimo especificado.
     *
     * @param filePath La ruta al archivo JSON de pasajeros.
     * @throws IOException Si ocurre un error al leer el archivo (por ejemplo, el archivo no existe o hay problemas de permisos).
     * @throws TreeException Si ocurre un error al registrar pasajeros en el AVL del PassengerManager.
     * @throws JsonSyntaxException Si el archivo JSON tiene un formato inválido.
     * @throws IllegalArgumentException Si hay datos faltantes o inválidos en el JSON o al generar pasajeros.
     */
    public void loadPassengersFromJson(String filePath) throws IOException, TreeException, JsonSyntaxException, IllegalArgumentException {
        // Aquí no necesitamos pasar 'Id' (se asume que era un remanente del test, ahora es minTotalPassengers)
        // La impresión en consola del "Loading passengers" se deja para una capa superior (ej. main)

        try {
            // Leer el contenido del archivo con codificación UTF-8 para evitar problemas de caracteres
            String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);

            // Parsear el contenido JSON
            JsonElement parsedElement = JsonParser.parseString(content);
            JsonObject jsonObject = parsedElement.getAsJsonObject();
            JsonArray passengersArray = jsonObject.getAsJsonArray("passengers");

            // Si el array de pasajeros existe, procesar cada uno
            if (passengersArray != null) {
                for (int i = 0; i < passengersArray.size(); i++) {
                    JsonObject passengerJson = passengersArray.get(i).getAsJsonObject();

                    // Uso de has() para verificar la existencia de la propiedad antes de obtenerla
                    // y manejo de posibles nulos o tipos incorrectos con excepciones claras.
                    String id = passengerJson.has("id") && !passengerJson.get("id").isJsonNull()
                            ? passengerJson.get("id").getAsString()
                            : Utility.RandomId(); // Genera ID si no existe o es nulo en el JSON

                    // Asegurarse de que 'name' y 'nationality' existan y no sean nulos
                    if (!passengerJson.has("name") || passengerJson.get("name").isJsonNull()) {
                        throw new IllegalArgumentException("Pasajero en JSON sin propiedad 'name' o es nula.");
                    }
                    String name = passengerJson.get("name").getAsString();

                    if (!passengerJson.has("nationality") || passengerJson.get("nationality").isJsonNull()) {
                        throw new IllegalArgumentException("Pasajero en JSON sin propiedad 'nationality' o es nula.");
                    }
                    String nationality = passengerJson.get("nationality").getAsString();

                    // Intentar registrar el pasajero. PassengerManager.registerPassenger ya maneja duplicados.
                    try {
                        passengerManager.registerPassenger(id, name, nationality);
                    } catch (IllegalArgumentException e) {
                        // Si el pasajero ya existe (ID duplicado), lo logueamos pero no detenemos la carga
                        // Considera usar un logger real aquí en vez de System.err.
                        System.err.println("ADVERTENCIA: No se pudo registrar pasajero con ID " + id + " desde JSON porque ya existe: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            // Propagar IOException directamente, ya que es un problema de lectura de archivo.
            throw e;
        } catch (JsonSyntaxException e) {
            // Propagar JsonSyntaxException si el JSON está mal formado.
            throw new JsonSyntaxException("Error en el formato del archivo JSON de pasajeros: " + e.getMessage(), e);
        } catch (IllegalStateException | ClassCastException e) {
            // Capturar errores si el JSON no tiene el formato esperado (ej. "passengers" no es un array)
            throw new IllegalArgumentException("Estructura JSON inesperada o datos faltantes: " + e.getMessage(), e);
        }

        // Generar pasajeros adicionales si no se alcanza el mínimo
        int currentPassengerCount = passengerManager.getPassengerCount();
        int passengersToGenerate = minTotalPassengers - currentPassengerCount;

        if (passengersToGenerate > 0) {
            System.out.println("Generando " + passengersToGenerate + " pasajeros adicionales para alcanzar el mínimo...");
            for (int i = 0; i < passengersToGenerate; i++) {
                String id = Utility.RandomId();
                String name = Utility.RandomNames();
                String nationality = Utility.RandomNationalities();

                try {
                    // Volver a intentar el registro en caso de colisión de ID generado aleatoriamente
                    // Esto puede ocurrir pero es poco probable con IDs aleatorios si el rango es grande.
                    passengerManager.registerPassenger(id, name, nationality);
                } catch (IllegalArgumentException e) {
                    // ID generado aleatoriamente ya existe, intentar generar otro en la siguiente iteración
                    System.err.println("ADVERTENCIA: ID generado aleatoriamente duplicado: " + id + ". Intentando generar otro...");
                    i--; // Decrementar 'i' para intentar generar otro pasajero en esta posición
                } catch (TreeException e) {
                    // Propagar TreeException si hay un problema fundamental con la estructura del árbol
                    throw e;
                }
            }
        }
    }
}