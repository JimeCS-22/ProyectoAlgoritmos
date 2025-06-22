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
        // Asegúrate de que si la lista es nula o vacía, se serialice como un array vacío []
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
                    gen.writeObject(item); // Jackson se encargará de serializar 'item'
                }
            }
        } catch (Exception e) {
            // Manejo de errores
            throw new IOException("Error serializando DoublyLinkedList: " + e.getMessage(), e);
        }
        gen.writeEndArray(); // Cierra el array JSON
    }
}
