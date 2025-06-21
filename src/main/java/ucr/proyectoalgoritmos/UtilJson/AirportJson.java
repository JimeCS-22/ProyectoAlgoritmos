package ucr.proyectoalgoritmos.UtilJson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.Serializer.DoublyLinkedListDeserializer;
import ucr.proyectoalgoritmos.Serializer.DoublyLinkedListSerializer;
import ucr.proyectoalgoritmos.Serializer.SinglyLinkedListDeserializer;
import ucr.proyectoalgoritmos.Serializer.SinglyLinkedListSerializer;
import ucr.proyectoalgoritmos.util.FXUtility;

import java.io.File;
import java.io.IOException;

public class AirportJson {

    private static final String FILE_PATH = "src/main/resources/airports.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // **REGISTRAR TUS SERIALIZADORES/DESERIALIZADORES PERSONALIZADOS PARA DoublyLinkedList**
        SimpleModule module = new SimpleModule();
        module.addSerializer(DoublyLinkedList.class, new DoublyLinkedListSerializer());
        module.addDeserializer(DoublyLinkedList.class, new DoublyLinkedListDeserializer());
        objectMapper.registerModule(module);
    }

    /**
     * Guarda tu DoublyLinkedList personalizada de aeropuertos a un archivo JSON.
     * Jackson usará el DoublyLinkedListSerializer registrado.
     * @param airports La DoublyLinkedList de objetos Airport a guardar.
     */
    public static void saveAirportsToJson(DoublyLinkedList airports) {
        try {
            objectMapper.writeValue(new File(FILE_PATH), airports);
            System.out.println("Aeropuertos guardados en " + FILE_PATH);
        } catch (IOException e) {
            System.err.println("Error al guardar aeropuertos en JSON: " + e.getMessage());
            FXUtility.alert("Error de Guardado", "No se pudo guardar la lista de aeropuertos.");
        } catch (Exception e) {
            System.err.println("Error inesperado durante el guardado de JSON: " + e.getMessage());
            FXUtility.alert("Error Inesperado", "Ocurrió un error inesperado al guardar los aeropuertos.");
        }
    }

    /**
     * Carga los aeropuertos desde un archivo JSON y los coloca en tu DoublyLinkedList personalizada.
     * Jackson usará el DoublyLinkedListDeserializer registrado.
     * @return Una DoublyLinkedList de objetos Airport cargados desde el archivo JSON.
     */
    public static DoublyLinkedList loadAirportsFromJson() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            System.out.println("El archivo JSON no existe o está vacío. Retornando una nueva DoublyLinkedList.");
            return new DoublyLinkedList(); // Retorna tu propia lista doble vacía
        }

        try {
            // Jackson ahora sabe cómo leer directamente en tu DoublyLinkedList
            DoublyLinkedList airports = objectMapper.readValue(file, DoublyLinkedList.class);
            System.out.println("Aeropuertos cargados desde " + FILE_PATH);
            return airports;
        } catch (IOException e) {
            System.err.println("Error al cargar aeropuertos desde JSON: " + e.getMessage());
            FXUtility.alert("Error de Carga", "No se pudo cargar la lista de aeropuertos.");
            return new DoublyLinkedList();
        } catch (Exception e) {
            System.err.println("Error inesperado durante la carga de JSON: " + e.getMessage());
            FXUtility.alert("Error Inesperado", "Ocurrió un error inesperado al cargar los aeropuertos.");
            e.printStackTrace();
            return new DoublyLinkedList();
        }
    }
}
