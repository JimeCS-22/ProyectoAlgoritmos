package ucr.proyectoalgoritmos.Domain.flight; // Or a more general 'Domain.history' package

import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;

// This class wraps a list of FlightHistory objects for a passenger
public class FlightHistoryList {
    private SinglyLinkedList histories; // Use your SinglyLinkedList

    public FlightHistoryList() {
        this.histories = new SinglyLinkedList();
    }

    /**
     * Adds a FlightHistory object to the list.
     * @param history The FlightHistory object to add. Must not be null.
     * @throws ListException if the provided history is null or if there's an issue with the underlying list.
     */
    public void add(FlightHistory history) throws ListException {
        if (history != null) {
            this.histories.add(history);
            System.out.println("FHL DEBUG: Added history for flight " + history.getFlightNumber() + ". Total histories: " + histories.size());
        } else {
            throw new ListException("Cannot add null flight history to the list.");
        }
    }

    /**
     * Checks if the flight history list is empty.
     * @return true if the list contains no history entries, false otherwise.
     */
    public boolean isEmpty() {
        return histories.isEmpty();
    }

    /**
     * Returns the number of flight history entries in the list.
     * @return The number of entries.
     * @throws ListException if there's an issue getting the size from the underlying list.
     */
    public int size() throws ListException {
        return histories.size();
    }

    /**
     * Retrieves a FlightHistory object at the specified index.
     * @param index The index of the history entry to retrieve.
     * @return The FlightHistory object at the given index.
     * @throws ListException if the index is out of bounds or the list is empty.
     */
    public FlightHistory get(int index) throws ListException {
        return (FlightHistory) histories.get(index);
    }

    /**
     * Provides access to the underlying SinglyLinkedList of histories.
     * Use this for iterating or performing advanced list operations if necessary.
     * @return The SinglyLinkedList containing FlightHistory objects.
     */
    public SinglyLinkedList getHistories() {
        return histories;
    }

    @Override
    public String toString() {
        if (histories.isEmpty()) {
            return "No flight history recorded for this passenger.";
        }
        StringBuilder sb = new StringBuilder("Flight History:\n");
        try {
            for (int i = 0; i < histories.size(); i++) {
                sb.append("  ").append(histories.get(i).toString()).append("\n");
            }
        } catch (ListException e) {
            sb.append("  [Error retrieving flight history: ").append(e.getMessage()).append("]\n");
        }
        return sb.toString().trim(); // Use trim() to remove trailing newline if any
    }
}