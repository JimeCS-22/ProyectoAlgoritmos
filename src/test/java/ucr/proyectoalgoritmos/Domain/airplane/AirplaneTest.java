package ucr.proyectoalgoritmos.Domain.airplane; // Asegúrate de que este paquete sea correcto

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ucr.proyectoalgoritmos.Domain.flight.Flight; // Importa tu clase Flight
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.queue.QueueException;
import ucr.proyectoalgoritmos.Domain.stack.StackException; // Importa tu StackException

import java.time.LocalDateTime; // *** NUEVA IMPORTACIÓN ***
import static org.junit.jupiter.api.Assertions.*; // Importa todas las aserciones de JUnit

class AirplaneTest {

    private Airplane airplane;

    // Este método se ejecuta antes de cada test. Es útil para inicializar objetos comunes.
    @BeforeEach
    void setUp() {
        // Inicializamos un avión estándar para la mayoría de los tests
        airplane = new Airplane("AV-TEST-001", 150, "SJO");
    }


    @Test
    @DisplayName("El constructor debe inicializar correctamente los atributos")
    void testConstructorAndGetters() {
        assertNotNull(airplane, "El objeto Airplane no debería ser nulo.");
        assertEquals("AV-TEST-001", airplane.getId(), "El ID del avión no coincide.");
        assertEquals(150, airplane.getCapacity(), "La capacidad del avión no coincide.");
        assertEquals("SJO", airplane.getCurrentLocationAirportCode(), "La ubicación inicial no coincide.");
        assertEquals(Airplane.AirplaneStatus.IDLE, airplane.getStatus(), "El estado inicial debería ser IDLE.");
        assertNotNull(airplane.getFlightHistory(), "El historial de vuelos no debería ser nulo.");
        assertTrue(airplane.getFlightHistory().isEmpty(), "El historial de vuelos debería estar vacío al inicio.");
    }

    @Test
    @DisplayName("El constructor debe lanzar IllegalArgumentException para ID nulo o vacío")
    void testConstructorInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Airplane(null, 100, "MIA");
        }, "Debería lanzar IllegalArgumentException para ID nulo.");
        assertThrows(IllegalArgumentException.class, () -> {
            new Airplane("   ", 100, "MIA");
        }, "Debería lanzar IllegalArgumentException para ID vacío.");
    }

    @Test
    @DisplayName("El constructor debe lanzar IllegalArgumentException para capacidad inválida")
    void testConstructorInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Airplane("AV-002", 0, "MIA");
        }, "Debería lanzar IllegalArgumentException para capacidad cero.");
        assertThrows(IllegalArgumentException.class, () -> {
            new Airplane("AV-003", -50, "MIA");
        }, "Debería lanzar IllegalArgumentException para capacidad negativa.");
    }

    @Test
    @DisplayName("El constructor debe lanzar IllegalArgumentException para código de aeropuerto nulo o vacío")
    void testConstructorInvalidLocation() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Airplane("AV-004", 100, null);
        }, "Debería lanzar IllegalArgumentException para ubicación nula.");
        assertThrows(IllegalArgumentException.class, () -> {
            new Airplane("AV-005", 100, "   ");
        }, "Debería lanzar IllegalArgumentException para ubicación vacía.");
    }

    @Test
    @DisplayName("Setear la ubicación actual del avión debe funcionar correctamente")
    void testSetCurrentLocationAirportCode() {
        String newLocation = "LAX";
        airplane.setCurrentLocationAirportCode(newLocation);
        assertEquals(newLocation, airplane.getCurrentLocationAirportCode(), "La ubicación del avión no se actualizó correctamente.");
    }

    @Test
    @DisplayName("Setear la ubicación actual debe lanzar IllegalArgumentException para nulo o vacío")
    void testSetCurrentLocationAirportCodeInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            airplane.setCurrentLocationAirportCode(null);
        }, "Debería lanzar IllegalArgumentException para ubicación nula.");
        assertThrows(IllegalArgumentException.class, () -> {
            airplane.setCurrentLocationAirportCode("   ");
        }, "Debería lanzar IllegalArgumentException para ubicación vacía.");
    }

    @Test
    @DisplayName("Setear el estado del avión debe funcionar correctamente")
    void testSetStatus() {
        airplane.setStatus(Airplane.AirplaneStatus.IN_FLIGHT);
        assertEquals(Airplane.AirplaneStatus.IN_FLIGHT, airplane.getStatus(), "El estado del avión no se actualizó correctamente a IN_FLIGHT.");

        airplane.setStatus(Airplane.AirplaneStatus.MAINTENANCE);
        assertEquals(Airplane.AirplaneStatus.MAINTENANCE, airplane.getStatus(), "El estado del avión no se actualizó correctamente a MAINTENANCE.");
    }

    @Test
    @DisplayName("Setear el estado del avión debe lanzar IllegalArgumentException para estado nulo")
    void testSetStatusNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            airplane.setStatus(null);
        }, "Debería lanzar IllegalArgumentException para estado nulo.");
    }

    @Test
    @DisplayName("Añadir vuelos al historial debe apilarlos correctamente")
    void testAddFlightToHistory() throws StackException, ListException, QueueException {
        // *** CAMBIOS AQUI: Usar LocalDateTime y el constructor de 5 parámetros de Flight ***
        // Para LocalDateTime.of(año, mes, día, hora, minuto)
        Flight flight1 = new Flight("FL101", "SJO", "MIA", LocalDateTime.of(2025, 7, 1, 10, 0), 150);
        Flight flight2 = new Flight("FL102", "MIA", "JFK", LocalDateTime.of(2025, 7, 2, 14, 0), 150);

        airplane.addFlightToHistory(flight1);
        assertFalse(airplane.getFlightHistory().isEmpty(), "El historial no debería estar vacío después de añadir un vuelo.");
        assertEquals(1, airplane.getFlightHistory().size(), "El tamaño del historial debería ser 1.");
        assertEquals(flight1, airplane.getFlightHistory().top(), "El último vuelo añadido no es el esperado.");

        airplane.addFlightToHistory(flight2);
        assertEquals(2, airplane.getFlightHistory().size(), "El tamaño del historial debería ser 2.");
        assertEquals(flight2, airplane.getFlightHistory().top(), "El último vuelo añadido no es el esperado (flight2).");
        // Aseguramos que flight1 sigue debajo de flight2
        airplane.getFlightHistory().pop(); // Sacamos flight2
        assertEquals(flight1, airplane.getFlightHistory().top(), "Después de sacar el último, el anterior debería ser flight1.");
    }

    @Test
    @DisplayName("Añadir vuelo nulo al historial debe lanzar IllegalArgumentException")
    void testAddFlightToHistoryNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            airplane.addFlightToHistory(null);
        }, "Debería lanzar IllegalArgumentException para vuelo nulo.");
    }


    @Test
    @DisplayName("Dos aviones con el mismo ID deben ser iguales y tener el mismo hashCode")
    void testEqualsAndHashCodeSameId() {
        Airplane sameIdAirplane = new Airplane("AV-TEST-001", 200, "LAX"); // Diferente capacidad y ubicación
        Airplane anotherSameIdAirplane = new Airplane("AV-TEST-001", 100, "CDG"); // Otra vez mismo ID

        // Test de equals
        assertTrue(airplane.equals(sameIdAirplane), "Aviones con el mismo ID deberían ser iguales.");
        assertTrue(airplane.equals(anotherSameIdAirplane), "Aviones con el mismo ID deberían ser iguales.");
        assertEquals(airplane, sameIdAirplane, "assertEquals para objetos iguales.");

        // Test de hashCode
        assertEquals(airplane.hashCode(), sameIdAirplane.hashCode(), "Aviones iguales deberían tener el mismo hashCode.");
        assertEquals(airplane.hashCode(), anotherSameIdAirplane.hashCode(), "Aviones iguales deberían tener el mismo hashCode.");
    }

    @Test
    @DisplayName("Dos aviones con IDs diferentes no deben ser iguales y tener diferente hashCode")
    void testEqualsAndHashCodeDifferentId() {
        Airplane differentIdAirplane = new Airplane("AV-DIFF-002", 150, "SJO");

        assertFalse(airplane.equals(differentIdAirplane), "Aviones con diferente ID no deberían ser iguales.");
        assertNotEquals(airplane, differentIdAirplane, "assertNotEquals para objetos diferentes.");
        assertNotEquals(airplane.hashCode(), differentIdAirplane.hashCode(), "Aviones diferentes deberían tener diferente hashCode.");
    }

    @Test
    @DisplayName("Equals debe manejar objetos nulos y de diferentes clases")
    void testEqualsEdgeCases() {
        assertFalse(airplane.equals(null), "equals() con null debe ser falso.");
        assertFalse(airplane.equals("Esto no es un avión"), "equals() con objeto de diferente clase debe ser falso.");
    }


    @Test
    @DisplayName("toString() debe retornar el formato esperado")
    void testToString() {
        String expectedToString = "Avión [ID: AV-TEST-001, Capacidad: 150, Ubicación: SJO, Estado: IDLE]";
        assertEquals(expectedToString, airplane.toString(), "El método toString() no retorna el formato esperado.");
    }
}