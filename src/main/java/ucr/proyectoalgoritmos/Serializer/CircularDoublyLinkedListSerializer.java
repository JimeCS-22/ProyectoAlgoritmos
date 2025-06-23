package ucr.proyectoalgoritmos.Serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.Circular.ListException;

import java.io.IOException;

public class CircularDoublyLinkedListSerializer extends StdSerializer<CircularDoublyLinkedList> {

    public CircularDoublyLinkedListSerializer() {
        this(null);
    }

    public CircularDoublyLinkedListSerializer(Class<CircularDoublyLinkedList> t) {
        super(t);
    }

    @Override
    public void serialize(CircularDoublyLinkedList value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartArray();
        try {
            if (value == null || value.isEmpty()) {

            } else {

                for (int i = 0; i < value.size(); i++) {
                    Object item = value.get(i);
                    if (item != null) {
                        gen.writeObject(item); // Deja que Jackson serialice el objeto (Flight en este caso)
                    } else {
                        gen.writeNull();
                    }
                }
            }
        } catch (ucr.proyectoalgoritmos.Domain.list.ListException e) {

            throw new IOException("Error al serializar CircularDoublyLinkedList: " + e.getMessage(), e);
        } finally {
            gen.writeEndArray();
        }
    }











}
