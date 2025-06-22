package ucr.proyectoalgoritmos.Serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.Circular.ListException;

import java.io.IOException;

public class CircularDoublyLinkedListDeserializer extends StdDeserializer<CircularDoublyLinkedList> {

    public CircularDoublyLinkedListDeserializer() {
        this(null);
    }

    public CircularDoublyLinkedListDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public CircularDoublyLinkedList deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        CircularDoublyLinkedList list = new CircularDoublyLinkedList();
        JsonNode node = p.getCodec().readTree(p); // Lee el JSON como un árbol de nodos

        if (node.isArray()) { // Verifica si el nodo es un array (que es lo que esperamos)
            for (JsonNode elementNode : node) {
                try {

                    Object obj = ctxt.readValue(elementNode.traverse(p.getCodec()), Object.class); // Deserializa a Object o el tipo específico
                    list.add(obj); // Añade el objeto a tu lista circular

                } catch (Exception e) {
                    // Captura otras excepciones durante la deserialización de un elemento individual.
                    throw new IOException("Error al deserializar elemento de CircularDoublyLinkedList: " + e.getMessage(), e);
                }
            }
        }
        return list;
    }
}
