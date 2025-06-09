package ucr.proyectoalgoritmos.Domain.flight; // Or a more general 'Domain.history' package

import ucr.proyectoalgoritmos.Domain.flight.FlightHistory;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList; // Or CircularDoublyLinkedList
import ucr.proyectoalgoritmos.Domain.list.ListException;

// This class wraps a list of FlightHistory objects for a passenger
public class FlightHistoryList {
    private SinglyLinkedList histories; // Use your SinglyLinkedList

    public FlightHistoryList() {
        this.histories = new SinglyLinkedList();
    }

    public void add(FlightHistory history) throws ListException {
        if (history != null) {
            histories.add(history);
        } else {
            throw new ListException("Cannot add null flight history.");
        }
    }

    public boolean isEmpty() {
        return histories.isEmpty();
    }

    public int size() throws ListException {
        return histories.size();
    }

    // You can add more methods here like get(index), clear(), etc.
    // For printing, we'll iterate through it.
    public SinglyLinkedList getHistories() { // Provide access to the underlying list for iteration
        return histories;
    }

    @Override
    public String toString() {
        if (histories.isEmpty()) {
            return "No flight history recorded.";
        }
        StringBuilder sb = new StringBuilder();
        try {
            for (int i = 0; i < histories.size(); i++) {
                sb.append("  ").append(histories.get(i).toString()).append("\n");
            }
        } catch (ListException e) {
            sb.append("Error retrieving history: ").append(e.getMessage());
        }
        return sb.toString().trim();
    }
}