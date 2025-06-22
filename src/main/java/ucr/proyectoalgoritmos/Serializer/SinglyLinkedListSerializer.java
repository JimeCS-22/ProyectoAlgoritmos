package ucr.proyectoalgoritmos.Serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;

import java.io.IOException;

public class SinglyLinkedListSerializer extends StdSerializer<SinglyLinkedList> {

    public SinglyLinkedListSerializer() {
        this(null);
    }

    public SinglyLinkedListSerializer(Class<SinglyLinkedList> t) {
        super(t);
    }

    @Override
    public void serialize(SinglyLinkedList value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // MUY IMPORTANTE: Si la lista es nula o vacía, escribe un array JSON vacío.
        if (value == null || value.isEmpty()) {
            gen.writeStartArray();
            gen.writeEndArray();
            return;
        }

        gen.writeStartArray(); // Inicia el array JSON
        try {
            for (int i = 0; i < value.size(); i++) {
                Object item = value.get(i);
                if (item != null) {
                    gen.writeObject(item); // Jackson se encargará de serializar 'item' (ej. Flight)
                }
            }
        } catch (Exception e) {
            // Considera un manejo más específico de ListException si tu get(i) la lanza
            throw new IOException("Error serializando SinglyLinkedList: " + e.getMessage(), e);
        }
        gen.writeEndArray(); // Cierra el array JSON
    }
}
