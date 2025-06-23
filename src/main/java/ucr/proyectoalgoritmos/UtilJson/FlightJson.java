package ucr.proyectoalgoritmos.UtilJson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.route.RouteManager;
import ucr.proyectoalgoritmos.Serializer.CircularDoublyLinkedListDeserializer;
import ucr.proyectoalgoritmos.Serializer.CircularDoublyLinkedListSerializer;
import ucr.proyectoalgoritmos.Serializer.DoublyLinkedListDeserializer;
import ucr.proyectoalgoritmos.Serializer.DoublyLinkedListSerializer;
import ucr.proyectoalgoritmos.util.FXUtility;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;

public class FlightJson {

    private static final String FILE_PATH = "src/main/resources/flights.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        SimpleModule customListModule = new SimpleModule();

        // **ESTA LÍNEA ES LA CLAVE PARA ELIMINAR EL "Unrecognized Type: [null]"**
        customListModule.addDeserializer(CircularDoublyLinkedList.class, new CircularDoublyLinkedListDeserializer(Flight.class)); // <-- PASAR Flight.class AQUÍ
        customListModule.addSerializer(CircularDoublyLinkedList.class, new CircularDoublyLinkedListSerializer());

        // Si usas DoublyLinkedList para otros tipos de datos, mantén estas líneas,
        // pero asegúrate de que DoublyLinkedListDeserializer también reciba su elementType (ej. Airport.class)
        // customListModule.addSerializer(DoublyLinkedList.class, new DoublyLinkedListSerializer());
        // customListModule.addDeserializer(DoublyLinkedList.class, new DoublyLinkedListDeserializer(Airport.class)); // Ejemplo, si DoublyLinkedList guarda Airports

        objectMapper.registerModule(customListModule);
    }

    // Cambiar el tipo de parámetro a CircularDoublyLinkedList
    public static void saveFlightsToJson(CircularDoublyLinkedList flights) { // <--- CAMBIO AQUÍ
        try {
            File file = new File(FILE_PATH);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            objectMapper.writeValue(file, flights); // Esto usará CircularDoublyLinkedListSerializer
            System.out.println("Vuelos guardados en " + FILE_PATH);
        } catch (IOException e) {
            System.err.println("Error al guardar vuelos en JSON: " + e.getMessage());
            e.printStackTrace();
            FXUtility.alert("Error de Guardado", "No se pudo guardar la lista de vuelos.");
        } catch (Exception e) {
            System.err.println("Error inesperado durante el guardado de JSON de vuelos: " + e.getMessage());
            e.printStackTrace();
            FXUtility.alert("Error Inesperado", "Ocurrió un error inesperado al guardar los vuelos.");
        }
    }

    // Cambiar el tipo de retorno a CircularDoublyLinkedList
    public static CircularDoublyLinkedList loadFlightsFromJson(AirportManager airportManager, RouteManager routeManager) { // <--- CAMBIO AQUÍ
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            System.out.println("El archivo JSON de vuelos no existe o está vacío. Retornando una nueva CircularDoublyLinkedList.");
            return new CircularDoublyLinkedList(); // <--- CAMBIO AQUÍ
        }

        try {
            CircularDoublyLinkedList flights = objectMapper.readValue(file, CircularDoublyLinkedList.class); // <--- CAMBIO AQUÍ
            System.out.println("Vuelos cargados desde " + FILE_PATH);
            return flights;
        } catch (IOException e) {
            System.err.println("Error al cargar vuelos desde JSON: " + e.getMessage());
            e.printStackTrace();
            FXUtility.alert("Error de Carga", "No se pudo cargar la lista de vuelos.");
            return new CircularDoublyLinkedList(); // <--- CAMBIO AQUÍ
        } catch (Exception e) {
            System.err.println("Error inesperado durante la carga de JSON de vuelos: " + e.getMessage());
            e.printStackTrace();
            FXUtility.alert("Error Inesperado", "Ocurrió un error inesperado al cargar los vuelos.");
            return new CircularDoublyLinkedList(); // <--- CAMBIO AQUÍ
        }
    }


}