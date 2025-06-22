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
        gen.writeStartArray(); // Inicia el array JSON
        try {
            for (int i = 0; i < value.size(); i++) {
                // Escribe cada elemento de la lista.
                // Jackson intentará serializar el objeto contenido (ej. Passenger).
                // Si Passenger (o el tipo que guardes) no es directamente serializable,
                // necesitarás un serializador para Passenger también.
                gen.writeObject(value.get(i));
            }
        } catch (ucr.proyectoalgoritmos.Domain.list.ListException e) {
            // Manejar la excepción si ocurre un error al acceder a los elementos de la lista.
            // En un caso real, podrías querer registrar esto o lanzar una excepción más específica.
            throw new IOException("Error al serializar CircularDoublyLinkedList: " + e.getMessage(), e);
        }
        gen.writeEndArray(); // Cierra el array JSON
    }












}
