package ucr.proyectoalgoritmos.Serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;

import java.io.IOException;

public class DoublyLinkedListDeserializer extends StdDeserializer<DoublyLinkedList> {

    private final Class<?> elementType;

    public DoublyLinkedListDeserializer() {
        this(Object.class); // Por defecto si no se especifica, deserializa como Object
    }

    public DoublyLinkedListDeserializer(Class<?> elementType) {
        super(DoublyLinkedList.class);
        this.elementType = elementType;
    }

    @Override
    public DoublyLinkedList deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        DoublyLinkedList list = new DoublyLinkedList(); // Siempre inicializa la lista
        JsonNode node = p.getCodec().readTree(p);

        // Si el nodo JSON es null, devolvemos la lista vacía.
        if (node.isNull()) {
            return list;
        }

        if (node.isArray()) {
            for (JsonNode elementNode : node) {
                try {
                    Object element = p.getCodec().treeToValue(elementNode, elementType);
                    list.add(element);
                } catch (Exception e) {
                    throw new IOException("Error al deserializar elemento de DoublyLinkedList a tipo " + elementType.getName() + ": " + e.getMessage(), e);
                }
            }
        }
        return list;
    }
}
