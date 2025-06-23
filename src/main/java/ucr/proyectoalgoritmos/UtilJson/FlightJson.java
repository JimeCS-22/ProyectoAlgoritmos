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
        customListModule.addDeserializer(CircularDoublyLinkedList.class, new CircularDoublyLinkedListDeserializer(Flight.class)); // <-- PASAR Flight.class AQUÍ
        customListModule.addSerializer(CircularDoublyLinkedList.class, new CircularDoublyLinkedListSerializer());

        objectMapper.registerModule(customListModule);
    }

    public static void saveFlightsToJson(CircularDoublyLinkedList flights) {
        try {
            File file = new File(FILE_PATH);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            objectMapper.writeValue(file, flights);
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

    public static CircularDoublyLinkedList loadFlightsFromJson(AirportManager airportManager, RouteManager routeManager) {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {

            return new CircularDoublyLinkedList();
        }

        try {
            CircularDoublyLinkedList flights = objectMapper.readValue(file, CircularDoublyLinkedList.class);

            return flights;
        } catch (IOException e) {
            System.err.println("Error al cargar vuelos desde JSON: " + e.getMessage());
            e.printStackTrace();
            FXUtility.alert("Error de Carga", "No se pudo cargar la lista de vuelos.");
            return new CircularDoublyLinkedList();
        } catch (Exception e) {
            System.err.println("Error inesperado durante la carga de JSON de vuelos: " + e.getMessage());
            e.printStackTrace();
            FXUtility.alert("Error Inesperado", "Ocurrió un error inesperado al cargar los vuelos.");
            return new CircularDoublyLinkedList();
        }
    }


}