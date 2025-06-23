package ucr.proyectoalgoritmos.Serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;

import java.io.IOException;

public class DoublyLinkedListSerializer extends StdSerializer<DoublyLinkedList> {

    public DoublyLinkedListSerializer() {
        this(null);
    }

    public DoublyLinkedListSerializer(Class<DoublyLinkedList> t) {
        super(t);
    }

    @Override
    public void serialize(DoublyLinkedList value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null || value.isEmpty()) {
            gen.writeStartArray();
            gen.writeEndArray();
            return;
        }

        gen.writeStartArray();
        try {
            for (int i = 0; i < value.size(); i++) {
                Object item = value.get(i);
                if (item != null) {
                    gen.writeObject(item);
                }
            }
        } catch (Exception e) {

            throw new IOException("Error serializando DoublyLinkedList: " + e.getMessage(), e);
        }
        gen.writeEndArray();
    }
}
