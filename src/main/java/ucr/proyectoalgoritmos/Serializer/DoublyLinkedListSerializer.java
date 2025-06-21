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
        gen.writeStartArray(); // La lista doble se representará como un array JSON
        try {
            for (int i = 0; i < value.size(); i++) {
                // Asumimos que tu get(i) devuelve el Airport.
                // Jackson se encargará de serializar el objeto Airport por nosotros.
                gen.writeObject(value.get(i));
            }
        } catch (ListException e) {
            System.err.println("Error al serializar DoublyLinkedList: " + e.getMessage());
            throw new IOException("Error al acceder a elementos de la lista durante la serialización", e);
        }
        gen.writeEndArray();
    }

}
