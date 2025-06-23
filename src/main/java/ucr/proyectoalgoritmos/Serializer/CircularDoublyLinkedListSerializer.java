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
        gen.writeStartArray(); // Abre el array JSON
        try {
            if (value == null || value.isEmpty()) { // Manejar listas nulas o vacías explícitamente
                // No hace falta hacer nada, el array ya se cerrará abajo.
            } else {
                // Iterar correctamente sobre la lista circular para evitar bucles infinitos
                // Asegúrate que tu CircularDoublyLinkedList tiene un método get(int index) o un iterador.
                for (int i = 0; i < value.size(); i++) {
                    Object item = value.get(i); // Asumo que get(i) es seguro y funciona en CircularDoublyLinkedList
                    if (item != null) {
                        gen.writeObject(item); // Deja que Jackson serialice el objeto (Flight en este caso)
                    } else {
                        gen.writeNull(); // Escribe 'null' si el elemento es nulo
                    }
                }
            }
        } catch (ucr.proyectoalgoritmos.Domain.list.ListException e) { // <-- ¡Cuidado con el paquete aquí!
            // Es mejor lanzar IOException directamente o wrappearla.
            throw new IOException("Error al serializar CircularDoublyLinkedList: " + e.getMessage(), e);
        } finally {
            gen.writeEndArray(); // Siempre cierra el array, incluso si hay un error en el bucle
        }
    }











}
