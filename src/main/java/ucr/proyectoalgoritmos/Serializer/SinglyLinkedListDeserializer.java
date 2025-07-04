package ucr.proyectoalgoritmos.Serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.flight.Flight;
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
        SinglyLinkedList list = new SinglyLinkedList();
        JsonNode node = p.getCodec().readTree(p);


        if (node.isNull()) {
            return list;
        }

        if (node.isArray()) {
            for (JsonNode elementNode : node) {

                Flight flight = ctxt.readValue(elementNode.traverse(p.getCodec()), Flight.class);
                list.add(flight);
            }
        }
        return list;
    }
}
