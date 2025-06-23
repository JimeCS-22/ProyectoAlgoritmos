package ucr.proyectoalgoritmos.util;

import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;

/**
 * Utilidad para conversión entre implementaciones de listas enlazadas
 */
public final class ListConverter {

    private ListConverter() {
        throw new AssertionError("Clase utilitaria");
    }

    /**
     * Convierte CircularDoublyLinkedList a DoublyLinkedList
     */
    public static DoublyLinkedList convertToDoublyLinkedList(CircularDoublyLinkedList sourceList) throws ListException {
        if (sourceList == null) {
            throw new IllegalArgumentException("Lista fuente no puede ser nula");
        }

        DoublyLinkedList targetList = new DoublyLinkedList();
        if (sourceList.isEmpty()) {
            return targetList;
        }

        for (int i = 0; i < sourceList.size(); i++) {
            targetList.add(sourceList.get(i));
        }
        return targetList;
    }

    /**
     * Convierte DoublyLinkedList a CircularDoublyLinkedList
     */
    public static CircularDoublyLinkedList convertToCircularDoublyLinkedList(DoublyLinkedList sourceList) throws ListException {
        if (sourceList == null) {
            throw new IllegalArgumentException("Lista fuente no puede ser nula");
        }

        CircularDoublyLinkedList targetList = new CircularDoublyLinkedList();
        if (sourceList.isEmpty()) {
            return targetList;
        }

        for (int i = 0; i < sourceList.size(); i++) {
            targetList.add(sourceList.get(i));
        }
        return targetList;
    }
}