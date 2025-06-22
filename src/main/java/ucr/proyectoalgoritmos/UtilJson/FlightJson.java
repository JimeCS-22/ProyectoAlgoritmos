package ucr.proyectoalgoritmos.UtilJson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Serializer.CircularDoublyLinkedListDeserializer;
import ucr.proyectoalgoritmos.Serializer.CircularDoublyLinkedListSerializer;
import ucr.proyectoalgoritmos.Serializer.DoublyLinkedListDeserializer;
import ucr.proyectoalgoritmos.Serializer.DoublyLinkedListSerializer;
import ucr.proyectoalgoritmos.util.FXUtility;

import java.io.File;
import java.io.IOException;

public class FlightJson {


    private static final String FILE_PATH = "src/main/resources/flights.json"; // Ruta específica para vuelos
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        SimpleModule module = new SimpleModule();

        // REGISTRAR SERIALIZADORES/DESERIALIZADORES PARA DoublyLinkedList
        // Esto es necesario porque tu lista principal de vuelos (scheduledFlights) es una DoublyLinkedList.
        module.addSerializer(DoublyLinkedList.class, new DoublyLinkedListSerializer());
        module.addDeserializer(DoublyLinkedList.class, new DoublyLinkedListDeserializer());

        // REGISTRAR SERIALIZADORES/DESERIALIZADORES PARA CircularDoublyLinkedList
        // Esto es crucial para las listas de pasajeros dentro de Flight y las listas de espera.
        module.addSerializer(CircularDoublyLinkedList.class, new CircularDoublyLinkedListSerializer());
        module.addDeserializer(CircularDoublyLinkedList.class, new CircularDoublyLinkedListDeserializer());

        objectMapper.registerModule(module);
    }

    /**
     * Guarda tu DoublyLinkedList personalizada de vuelos a un archivo JSON.
     * Jackson usará los serializadores registrados para DoublyLinkedList y CircularDoublyLinkedList.
     * @param flights La DoublyLinkedList de objetos Flight a guardar.
     */
    public static void saveFlightsToJson(DoublyLinkedList flights) {
        try {
            objectMapper.writeValue(new File(FILE_PATH), flights);
            System.out.println("Vuelos guardados en " + FILE_PATH);
        } catch (IOException e) {
            System.err.println("Error al guardar vuelos en JSON: " + e.getMessage());
            FXUtility.alert("Error de Guardado", "No se pudo guardar la lista de vuelos.");
        } catch (Exception e) {
            System.err.println("Error inesperado durante el guardado de JSON de vuelos: " + e.getMessage());
            FXUtility.alert("Error Inesperado", "Ocurrió un error inesperado al guardar los vuelos.");
        }
    }

    /**
     * Carga los vuelos desde un archivo JSON y los coloca en tu DoublyLinkedList personalizada.
     * Jackson usará los deserializadores registrados para DoublyLinkedList y CircularDoublyLinkedList.
     * @return Una DoublyLinkedList de objetos Flight cargados desde el archivo JSON.
     */
    public static DoublyLinkedList loadFlightsFromJson() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            System.out.println("El archivo JSON de vuelos no existe o está vacío. Retornando una nueva DoublyLinkedList.");
            return new DoublyLinkedList(); // Retorna tu propia lista doble vacía
        }

        try {
            // Jackson ahora sabe cómo leer directamente en tu DoublyLinkedList que contiene Flights
            DoublyLinkedList flights = objectMapper.readValue(file, DoublyLinkedList.class);
            System.out.println("Vuelos cargados desde " + FILE_PATH);
            return flights;
        } catch (IOException e) {
            System.err.println("Error al cargar vuelos desde JSON: " + e.getMessage());
            FXUtility.alert("Error de Carga", "No se pudo cargar la lista de vuelos.");
            return new DoublyLinkedList();
        } catch (Exception e) {
            System.err.println("Error inesperado durante la carga de JSON de vuelos: " + e.getMessage());
            FXUtility.alert("Error Inesperado", "Ocurrió un error inesperado al cargar los vuelos.");
            e.printStackTrace();
            return new DoublyLinkedList();
        }
    }

}