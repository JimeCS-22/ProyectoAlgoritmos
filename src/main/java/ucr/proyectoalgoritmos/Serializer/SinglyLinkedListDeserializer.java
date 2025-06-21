package ucr.proyectoalgoritmos.Serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;

import java.io.IOException;

public class SinglyLinkedListDeserializer extends StdDeserializer<SinglyLinkedList> {

    public SinglyLinkedListDeserializer() {
        this(null);
    }

    public SinglyLinkedListDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public SinglyLinkedList deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        SinglyLinkedList list = new SinglyLinkedList(); // Crea una nueva instancia de tu lista
        JsonNode node = p.getCodec().readTree(p); // Lee todo el array JSON como un nodo

        if (node.isArray()) {
            for (JsonNode elementNode : node) {
                Airport airport = ctxt.readValue(elementNode.traverse(p.getCodec()), Airport.class);
                list.add(airport); // Usa tu método add()
            }
        }
        return list;
    }

}
