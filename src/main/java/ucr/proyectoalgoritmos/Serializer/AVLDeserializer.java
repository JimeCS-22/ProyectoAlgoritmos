package ucr.proyectoalgoritmos.Serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import ucr.proyectoalgoritmos.Domain.AVL;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.TreeException;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;

import java.io.IOException;

public class AVLDeserializer extends JsonDeserializer<AVL> {
    private final Class<?> elementClass;

    public AVLDeserializer(Class<?> elementClass) {
        this.elementClass = elementClass;
    }

    @Override
    public AVL deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        System.out.println("Iniciando deserialización..."); // Debug
        AVL avl = new AVL();
        JsonNode node = p.getCodec().readTree(p);

        if (node.isArray()) {
            System.out.println("Nodos a deserializar: " + node.size()); // Debug
            for (JsonNode elementNode : node) {
                try {
                    Object element = p.getCodec().treeToValue(elementNode, elementClass);
                    avl.insert((Passenger) element);
                } catch (TreeException e) {
                    throw new IOException("Error al insertar elemento en el AVL durante la deserialización", e);
                }
            }
        }
        return avl;
    }
}