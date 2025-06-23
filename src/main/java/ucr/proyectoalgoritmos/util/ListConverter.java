package ucr.proyectoalgoritmos.util; // Consider placing it in a 'util' or 'converter' package

import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;

/**
 * Clase utilitaria para convertir entre diferentes implementaciones de listas
 * como DoublyLinkedList y CircularDoublyLinkedList.
 * Permite transformar una lista de un tipo a otro, creando una nueva instancia
 * del tipo de lista deseado y transfiriendo sus elementos.
 */
public class ListConverter {

    /**
     * Convierte una CircularDoublyLinkedList a una DoublyLinkedList.
     * Itera sobre los elementos de la lista circular y los añade a una nueva
     * instancia de DoublyLinkedList.
     *
     * @param sourceList La CircularDoublyLinkedList a convertir.
     * @return Una nueva instancia de DoublyLinkedList que contiene los mismos elementos.
     * @throws ListException Si ocurre un error al acceder o añadir elementos a las listas.
     * @throws IllegalArgumentException Si la lista de origen es nula.
     */
    public static DoublyLinkedList convertToDoublyLinkedList(CircularDoublyLinkedList sourceList) throws ListException {
        if (sourceList == null) {
            throw new IllegalArgumentException("La lista de origen no puede ser nula para la conversión.");
        }

        DoublyLinkedList targetList = new DoublyLinkedList();
        if (sourceList.isEmpty()) {
            return targetList; // Retorna una lista doblemente enlazada vacía si el origen está vacío
        }

        // Iterar sobre la CircularDoublyLinkedList y añadir los elementos a la DoublyLinkedList
        // Asumiendo que CircularDoublyLinkedList tiene un método 'size()' y 'get(index)'
        for (int i = 0; i < sourceList.size(); i++) {
            targetList.add(sourceList.get(i));
        }
        return targetList;
    }

    /**
     * Convierte una DoublyLinkedList a una CircularDoublyLinkedList.
     * Itera sobre los elementos de la lista doblemente enlazada y los añade a una nueva
     * instancia de CircularDoublyLinkedList.
     *
     * @param sourceList La DoublyLinkedList a convertir.
     * @return Una nueva instancia de CircularDoublyLinkedList que contiene los mismos elementos.
     * @throws ListException Si ocurre un error al acceder o añadir elementos a las listas.
     * @throws IllegalArgumentException Si la lista de origen es nula.
     */
    public static CircularDoublyLinkedList convertToCircularDoublyLinkedList(DoublyLinkedList sourceList) throws ListException {
        if (sourceList == null) {
            throw new IllegalArgumentException("La lista de origen no puede ser nula para la conversión.");
        }

        CircularDoublyLinkedList targetList = new CircularDoublyLinkedList();
        if (sourceList.isEmpty()) {
            return targetList; // Retorna una lista circular doblemente enlazada vacía si el origen está vacío
        }

        // Iterar sobre la DoublyLinkedList y añadir los elementos a la CircularDoublyLinkedList
        // Asumiendo que DoublyLinkedList tiene un método 'size()' y 'get(index)'
        for (int i = 0; i < sourceList.size(); i++) {
            targetList.add(sourceList.get(i));
        }
        return targetList;
    }

    // Puedes añadir más métodos de conversión si tienes otras implementaciones de lista,
    // por ejemplo, convertir a una LinkedList simple, etc.
}