package ucr.proyectoalgoritmos.Serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.Circular.ListException;
import ucr.proyectoalgoritmos.Domain.flight.Flight;

import java.io.IOException;

public class CircularDoublyLinkedListDeserializer extends StdDeserializer<CircularDoublyLinkedList> {

    private final Class<?> elementType;

    public CircularDoublyLinkedListDeserializer() {
        this(Object.class);

    }

    public CircularDoublyLinkedListDeserializer(Class<?> elementType) {
        super(CircularDoublyLinkedList.class);
        this.elementType = elementType;
    }

    @Override
    public CircularDoublyLinkedList deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        CircularDoublyLinkedList list = new CircularDoublyLinkedList();
        System.out.println("DEBUG DESERIALIZER: Iniciando deserialización de CircularDoublyLinkedList...");

        if (p.getCurrentToken() != JsonToken.START_ARRAY) {
            try {
                throw (Throwable) ctxt.reportInputMismatch(CircularDoublyLinkedList.class, "Expected START_ARRAY token for CircularDoublyLinkedList, but got %s", p.getCurrentToken());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("DEBUG DESERIALIZER: JSON Node es un array. Iterando elementos...");
        int elementCount = 0;

        while (p.nextToken() != JsonToken.END_ARRAY) {
            elementCount++;

            JsonNode elementNode = p.readValueAsTree();
            System.out.println("DEBUG DESERIALIZER: Procesando elemento #" + elementCount + ": " + elementNode.toPrettyString());

            try {

                Object obj = ctxt.readTreeAsValue(elementNode, elementType);
                list.add(obj);
                System.out.println("DEBUG DESERIALIZER: Elemento #" + elementCount + " deserializado y añadido.");

            } catch (Exception e) {
                System.err.println("ERROR DESERIALIZER: Falló al deserializar el elemento #" + elementCount + " del array. JSON del elemento: " + elementNode.toPrettyString());
                e.printStackTrace();
                throw new IOException("Error al deserializar elemento de CircularDoublyLinkedList: " + e.getMessage(), e);
            }
        }
        System.out.println("DEBUG DESERIALIZER: Deserialización de array completada. Total elementos: " + elementCount);

        System.out.println("DEBUG DESERIALIZER: Deserialización de CircularDoublyLinkedList finalizada. Tamaño final: " + list.size());
        return list;
    }
}
