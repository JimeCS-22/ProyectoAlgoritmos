package ucr.proyectoalgoritmos.Domain.Archivos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ucr.proyectoalgoritmos.Domain.TreeException;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passenger.PassengerManager;
// No se necesita importar Utility aquí a menos que se use directamente en el test de forma extensa
// import ucr.proyectoalgoritmos.util.Utility;

import java.io.File;
import java.io.FileNotFoundException; // Importar específicamente si se espera
import java.io.IOException;
import java.nio.file.NoSuchFileException; // Importar si se espera directamente de Files.readAllBytes
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class PassengerDataTest {

    private PassengerManager passengerManager;
    private PassengerData passengerDataLoader;
    private static final int MIN_TOTAL_PASSENGERS = 25; // Define el mínimo requerido
    private String passengersFilePath; // Ruta al archivo JSON de pasajeros

    @BeforeEach
    void setUp() throws URISyntaxException {
        // Inicializa un nuevo PassengerManager y PassengerData antes de cada test.
        passengerManager = new PassengerManager();
        passengerDataLoader = new PassengerData(passengerManager, MIN_TOTAL_PASSENGERS);

        // Obtiene la URL del archivo JSON desde los recursos del proyecto.
        URL passengersUrl = PassengerDataTest.class.getClassLoader().getResource("passenger.json");

        // Asegura que el archivo 'passenger.json' exista en los recursos.
        assertNotNull(passengersUrl, "El archivo 'passenger.json' no se encontró en los recursos. Asegúrate de que esté en src/main/resources.");

        // Convierte la URL a una ruta de sistema de archivos compatible con el SO.
        passengersFilePath = Paths.get(passengersUrl.toURI()).toString();

        // Asegura que el archivo realmente exista en la ruta obtenida.
        assertTrue(new File(passengersFilePath).exists(), "El archivo 'passenger.json' no existe en la ruta esperada: " + passengersFilePath);
    }

    @Test
    @DisplayName("Debe cargar pasajeros desde el archivo JSON y alcanzar el mínimo requerido")
    void testLoadPassengersFromJson_SuccessfulLoading() throws IOException, TreeException, ListException {
        // Ejecuta el método a probar.
        // passengerDataLoader.loadPassengersFromJson ahora solo toma la ruta del archivo.
        passengerDataLoader.loadPassengersFromJson(passengersFilePath);

        // Asegura que se hayan cargado pasajeros.
        assertNotNull(passengerManager.getAllPassengers(), "La lista de pasajeros no debe ser nula después de la carga.");

        // Asegura que el número de pasajeros sea al menos el mínimo requerido.
        assertTrue(passengerManager.getPassengerCount() >= MIN_TOTAL_PASSENGERS,
                "El número total de pasajeros cargados (" + passengerManager.getPassengerCount() +
                        ") debe ser al menos " + MIN_TOTAL_PASSENGERS);

        // Opcional: Podrías añadir aserciones para verificar la existencia de pasajeros específicos
        // si conoces los IDs y nombres esperados del archivo JSON de prueba.
        // Por ejemplo:
        // Passenger p = passengerManager.searchPassenger("algunIDDelJSON");
        // assertNotNull(p);
        // assertEquals("AlgunNombreDelJSON", p.getName());
    }

    @Test
    @DisplayName("Debe lanzar IOException (o subclase) si el archivo JSON no existe")
    void testLoadPassengersFromJson_FileNotFound() {
        // Define una ruta que no existe para simular el escenario de archivo no encontrado.
        String nonExistentPath = "/path/to/definitely/non_existent_passenger.json";

        // Asegura que se lanza una IOException (que incluye FileNotFoundException y NoSuchFileException).
        assertThrows(IOException.class, () -> {
            passengerDataLoader.loadPassengersFromJson(nonExistentPath);
        }, "Debe lanzar IOException si el archivo no existe.");

        // Asegura que no se hayan cargado pasajeros si la carga falla.
        assertEquals(0, passengerManager.getPassengerCount(), "No se deben cargar pasajeros si el archivo no se encuentra.");
    }

}