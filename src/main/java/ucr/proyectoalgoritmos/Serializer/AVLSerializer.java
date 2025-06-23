package ucr.proyectoalgoritmos.Serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ucr.proyectoalgoritmos.Domain.AVL;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;

import java.io.IOException;

public class AVLSerializer extends JsonSerializer<AVL> {
    @Override
    public void serialize(AVL avl, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            // Serializamos el AVL como una lista en orden
            DoublyLinkedList list = avl.inOrderList();
            gen.writeObject(list);
        } catch (ListException e) {
            throw new IOException("Error al serializar el AVL", e);
        }
    }
}