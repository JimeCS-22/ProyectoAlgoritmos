package ucr.proyectoalgoritmos.Domain.aeropuetos; // Un paquete común para tus pruebas

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*; // Importa los métodos de aserción estáticos

import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;

/**
 * Clase de testeo exhaustiva para {@link AirportManager} utilizando JUnit 5.
 * Cada método anotado con `@Test` representa una prueba individual de una funcionalidad específica.
 * Se utiliza `@BeforeEach` para asegurar un estado inicial limpio antes de cada test.
 */
public class AirportManagerTest {

    private AirportManager manager;

    /**
     * Este método se ejecuta **antes de cada método de prueba**.
     * Su función es inicializar una nueva instancia de {@link AirportManager},
     * asegurando que cada test comience con un estado limpio y predecible.
     */
    @BeforeEach
    void setup() {
        manager = new AirportManager();
        // Opcional: imprimir un mensaje para saber que se reinició el manager.
        // System.out.println(">>> Configuración: AirportManager reinicializado para el test.");
    }

    /**
     * Prueba la creación de aeropuertos, incluyendo la prevención de duplicados.
     */
    @Test
    void testCreateAirport() throws ListException {
        System.out.println("\n--- Ejecutando testCreateAirport ---");

        // Caso 1: Crear un aeropuerto nuevo exitosamente.
        manager.createAirport("SJO", "Juan Santamaria", "Costa Rica");
        assertNotNull(manager.findAirport("SJO"), "FALLO: SJO no debería ser nulo después de la creación.");
        assertEquals("Juan Santamaria", manager.findAirport("SJO").getName(), "FALLO: Nombre de SJO incorrecto.");
        assertEquals(1, manager.getAirportCount(), "FALLO: El conteo de aeropuertos debería ser 1.");

        // Caso 2: Crear otro aeropuerto.
        manager.createAirport("LAX", "Los Angeles International", "USA");
        assertEquals(2, manager.getAirportCount(), "FALLO: El conteo de aeropuertos debería ser 2.");

        // Caso 3: Intentar crear un aeropuerto duplicado.
        // Esperamos que no lance una excepción, pero el manager debe manejarlo internamente.
        // Aquí no usamos assertThrows porque tu método imprime y retorna, no lanza para duplicados.
        manager.createAirport("SJO", "Juan Santamaria Duplicado", "Costa Rica");
        assertEquals(2, manager.getAirportCount(), "FALLO: El conteo de aeropuertos no debería cambiar al intentar crear un duplicado.");
        assertEquals("Juan Santamaria", manager.findAirport("SJO").getName(), "FALLO: El nombre de SJO no debería haber cambiado por el duplicado.");

        System.out.println("testCreateAirport: PASSED");
    }

    /**
     * Prueba la funcionalidad de buscar un aeropuerto por su código.
     */
    @Test
    void testFindAirport() throws ListException {
        System.out.println("\n--- Ejecutando testFindAirport ---");

        // Preparar datos de prueba
        manager.createAirport("SJO", "Juan Santamaria", "Costa Rica");
        manager.createAirport("LAX", "Los Angeles International", "USA");

        // Caso 1: Buscar un aeropuerto existente.
        Airport foundAirport = manager.findAirport("SJO");
        assertNotNull(foundAirport, "FALLO: SJO debería haber sido encontrado.");
        assertEquals("Juan Santamaria", foundAirport.getName(), "FALLO: El nombre del aeropuerto encontrado es incorrecto.");

        // Caso 2: Buscar un aeropuerto inexistente.
        Airport notFoundAirport = manager.findAirport("XYZ");
        assertNull(notFoundAirport, "FALLO: XYZ no debería haber sido encontrado.");

        System.out.println("testFindAirport: PASSED");
    }

    /**
     * Prueba la funcionalidad de obtener solo el nombre de un aeropuerto.
     */
    @Test
    void testGetAirportName() throws ListException {
        System.out.println("\n--- Ejecutando testGetAirportName ---");

        // Preparar datos de prueba
        manager.createAirport("SJO", "Juan Santamaria", "Costa Rica");

        // Caso 1: Obtener nombre de aeropuerto existente.
        String name = manager.getAirportName("SJO");
        assertEquals("Juan Santamaria", name, "FALLO: Nombre de SJO incorrecto.");

        // Caso 2: Obtener nombre de aeropuerto inexistente.
        String unknownName = manager.getAirportName("NON");
        assertEquals("Desconocido (NON)", unknownName, "FALLO: Mensaje de nombre desconocido incorrecto.");

        System.out.println("testGetAirportName: PASSED");
    }

    /**
     * Prueba la funcionalidad de eliminar un aeropuerto.
     */
    @Test
    void testDeleteAirport() throws ListException {
        System.out.println("\n--- Ejecutando testDeleteAirport ---");

        // Preparar datos de prueba
        manager.createAirport("SJO", "Juan Santamaria", "Costa Rica");
        manager.createAirport("LAX", "Los Angeles International", "USA");
        manager.createAirport("JFK", "John F. Kennedy", "USA");
        assertEquals(3, manager.getAirportCount(), "FALLO: Se esperaban 3 aeropuertos antes de eliminar.");

        // Caso 1: Eliminar un aeropuerto existente.
        boolean deleted = manager.deleteAirport("JFK");
        assertTrue(deleted, "FALLO: JFK debería haber sido eliminado.");
        assertNull(manager.findAirport("JFK"), "FALLO: JFK no debería existir después de ser eliminado.");
        assertEquals(2, manager.getAirportCount(), "FALLO: Se esperaban 2 aeropuertos después de eliminar JFK.");

        // Caso 2: Intentar eliminar un aeropuerto inexistente.
        boolean notDeleted = manager.deleteAirport("XYZ");
        assertFalse(notDeleted, "FALLO: Se eliminó un aeropuerto inexistente (XYZ).");
        assertEquals(2, manager.getAirportCount(), "FALLO: El conteo no debería cambiar al intentar eliminar inexistente.");

        System.out.println("testDeleteAirport: PASSED");
    }

    /**
     * Prueba la funcionalidad de cambiar el estado de un aeropuerto.
     */
    @Test
    void testActivateDeactivateAirport() throws ListException {
        System.out.println("\n--- Ejecutando testActivateDeactivateAirport ---");

        // Preparar datos de prueba
        manager.createAirport("SJO", "Juan Santamaria", "Costa Rica");
        Airport sjo = manager.findAirport("SJO");
        assertNotNull(sjo, "FALLO: SJO no debería ser nulo.");
        assertEquals(Airport.AirportStatus.ACTIVE, sjo.getStatus(), "FALLO: SJO debería estar ACTIVE inicialmente.");

        // Caso 1: Cambiar a CLOSED.
        manager.activateOrDeactivateAirport("SJO", Airport.AirportStatus.CLOSED);
        assertEquals(Airport.AirportStatus.CLOSED, sjo.getStatus(), "FALLO: SJO no cambió a CLOSED.");

        // Caso 2: Cambiar a UNDER_MAINTENANCE.
        manager.activateOrDeactivateAirport("SJO", Airport.AirportStatus.UNDER_MAINTENANCE);
        assertEquals(Airport.AirportStatus.UNDER_MAINTENANCE, sjo.getStatus(), "FALLO: SJO no cambió a UNDER_MAINTENANCE.");

        // Caso 3: Cambiar de nuevo a ACTIVE.
        manager.activateOrDeactivateAirport("SJO", Airport.AirportStatus.ACTIVE);
        assertEquals(Airport.AirportStatus.ACTIVE, sjo.getStatus(), "FALLO: SJO no cambió a ACTIVE.");

        // Caso 4: Intentar cambiar el estado de un aeropuerto inexistente.
        boolean updatedInexistent = manager.activateOrDeactivateAirport("XYZ", Airport.AirportStatus.CLOSED);
        assertFalse(updatedInexistent, "FALLO: No debería ser posible actualizar un aeropuerto inexistente.");

        System.out.println("testActivateDeactivateAirport: PASSED");
    }

    /**
     * Prueba la funcionalidad de listar aeropuertos con diferentes filtros.
     * Esta prueba requiere inspección manual de la salida por consola.
     */
    @Test
    void testListAirports() throws ListException {
        System.out.println("\n--- Ejecutando testListAirports ---");

        // Preparar datos de prueba con diferentes estados
        manager.createAirport("SJO", "Juan Santamaria", "Costa Rica"); // Activo
        manager.createAirport("LAX", "Los Angeles International", "USA"); // Activo
        manager.createAirport("JFK", "John F. Kennedy", "USA"); // Activo
        manager.activateOrDeactivateAirport("SJO", Airport.AirportStatus.CLOSED); // Ahora SJO está CLOSED
        manager.activateOrDeactivateAirport("LAX", Airport.AirportStatus.UNDER_MAINTENANCE); // Ahora LAX está UNDER_MAINTENANCE

        System.out.println("--- Listando solo aeropuertos ACTIVOS (debería mostrar solo JFK) ---");
        manager.listAirports(true, false, false);

        System.out.println("\n--- Listando solo aeropuertos CERRADOS (debería mostrar solo SJO) ---");
        manager.listAirports(false, true, false);

        System.out.println("\n--- Listando solo aeropuertos EN MANTENIMIENTO (debería mostrar solo LAX) ---");
        manager.listAirports(false, false, true);

        System.out.println("\n--- Listando aeropuertos ACTIVOS y CERRADOS (debería mostrar JFK y SJO) ---");
        manager.listAirports(true, true, false);

        System.out.println("\n--- Listando TODOS los aeropuertos (debería mostrar SJO, LAX, JFK) ---");
        manager.listAirports(true, true, true);

        System.out.println("testListAirports: PASSED (Revisar salida de consola para verificar filtros)");
        // Para esta prueba en particular, una aserción directa es difícil sin capturar System.out.
        // Se confía en la inspección visual o se podría usar una librería para capturar la salida de consola.
    }

    /**
     * Prueba que el conteo de aeropuertos sea correcto.
     */
    @Test
    void testGetAirportCount() throws ListException {
        System.out.println("\n--- Ejecutando testGetAirportCount ---");

        assertEquals(0, manager.getAirportCount(), "FALLO: El conteo inicial debería ser 0.");

        manager.createAirport("SJO", "Juan Santamaria", "Costa Rica");
        assertEquals(1, manager.getAirportCount(), "FALLO: El conteo debería ser 1 después de añadir uno.");

        manager.createAirport("LAX", "Los Angeles International", "USA");
        assertEquals(2, manager.getAirportCount(), "FALLO: El conteo debería ser 2 después de añadir otro.");

        manager.deleteAirport("SJO");
        assertEquals(1, manager.getAirportCount(), "FALLO: El conteo debería ser 1 después de eliminar uno.");

        System.out.println("testGetAirportCount: PASSED");
    }
}