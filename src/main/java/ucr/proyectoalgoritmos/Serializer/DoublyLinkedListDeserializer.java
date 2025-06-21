package ucr.proyectoalgoritmos.Serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;

import java.io.IOException;

public class DoublyLinkedListDeserializer extends StdDeserializer<DoublyLinkedList> {

    public DoublyLinkedListDeserializer() {
        this(null);
    }

    public DoublyLinkedListDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public DoublyLinkedList deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        DoublyLinkedList list = new DoublyLinkedList(); // Crea una nueva instancia de tu lista doble
        JsonNode node = p.getCodec().readTree(p); // Lee todo el array JSON como un nodo

        if (node.isArray()) {
            for (JsonNode elementNode : node) {
                // Deserializa cada elemento del array JSON a un objeto Airport
                // y añádelo a tu DoublyLinkedList.
                Airport airport = ctxt.readValue(elementNode.traverse(p.getCodec()), Airport.class);
                list.add(airport); // Usa tu método add() de DoublyLinkedList
            }
        }
        return list;
    }

}
