package ucr.proyectoalgoritmos.UtilJson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import ucr.proyectoalgoritmos.Domain.AVL;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;
import ucr.proyectoalgoritmos.Domain.TreeException;
import ucr.proyectoalgoritmos.Serializer.DoublyLinkedListDeserializer;
import ucr.proyectoalgoritmos.Serializer.DoublyLinkedListSerializer;
import ucr.proyectoalgoritmos.util.FXUtility;

import java.io.File;

public class PassengerJson {
    private static final String FILE_PATH = "src/main/resources/passenger.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // Configuración básica
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Configuración especial para Passenger
        objectMapper.addMixIn(Passenger.class, PassengerMixin.class);

        // Registro de serializadores (usando tu deserializador actual)
        SimpleModule module = new SimpleModule();
        module.addSerializer(DoublyLinkedList.class, new DoublyLinkedListSerializer());
        module.addDeserializer(DoublyLinkedList.class,
                new DoublyLinkedListDeserializer(Passenger.class));
        objectMapper.registerModule(module);
    }

    // MixIn para configurar cómo se deserializa Passenger
    private abstract static class PassengerMixin {
        @com.fasterxml.jackson.annotation.JsonCreator
        public PassengerMixin(
                @com.fasterxml.jackson.annotation.JsonProperty("id") String id,
                @com.fasterxml.jackson.annotation.JsonProperty("name") String name,
                @com.fasterxml.jackson.annotation.JsonProperty("nationality") String nationality) {
        }
    }

    public static AVL loadPassengersFromJson() {
        try {
            // Cargar la lista directamente
            DoublyLinkedList passengerList = objectMapper.readValue(
                    new File(FILE_PATH),
                    DoublyLinkedList.class
            );

            // Cargar al AVL
            AVL passengersAVL = new AVL();
            for (int i = 0; i < passengerList.size(); i++) {
                passengersAVL.insert((Passenger) passengerList.get(i));
            }

            return passengersAVL;

        } catch (Exception e) {
            System.err.println("Error al cargar pasajeros: " + e.getMessage());
            FXUtility.alert("Error", "No se pudieron cargar los pasajeros");
            return new AVL();
        }
    }

    public static void savePassengersToJson(AVL passengers) {
        try {
            objectMapper.writeValue(new File(FILE_PATH), passengers.inOrderList());
        } catch (Exception e) {
            System.err.println("Error al guardar pasajeros: " + e.getMessage());
            FXUtility.alert("Error", "No se pudieron guardar los pasajeros");
        }
    }
}