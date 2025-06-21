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
        gen.writeStartArray();
        try {
            for (int i = 0; i < value.size(); i++) {
                // Asumimos que tu get(i) devuelve el Airport.
                // Jackson se encargará de serializar el objeto Airport por nosotros.
                gen.writeObject(value.get(i));
            }
        } catch (ListException e) {
            // Manejo de errores: si tu get() lanza una excepción, regístrala o re-lánzala como IOException
            System.err.println("Error al serializar SinglyLinkedList: " + e.getMessage());
            throw new IOException("Error al acceder a elementos de la lista durante la serialización", e);
        }
        gen.writeEndArray();
    }

}
