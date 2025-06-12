package ucr.proyectoalgoritmos.Domain.Archivos;

import org.junit.jupiter.api.Test;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passenger.PassengerManager;
import ucr.proyectoalgoritmos.util.Utility;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class PassengerDataTest {

    private static final int MIN_TOTAL_PASSENGERS = 25;

    @Test
    void test() throws ListException {

        PassengerManager passengerManager = new PassengerManager();
        PassengerData passengerDataLoader = new PassengerData(passengerManager, MIN_TOTAL_PASSENGERS);

        String passengersFilePath = null;
        try {
            // Obtén la ruta del archivo JSON desde los recursos
            URL passengersUrl = PassengerDataTest.class.getClassLoader().getResource("passenger.json");

            if (passengersUrl == null) {
                System.err.println("Error: No se encontró el archivo JSON de pasajeros.");
                System.err.println("Asegúrate de que 'passengers.json' esté en 'src/main/resources' o similar.");
                return;
            }

            passengersFilePath = passengersUrl.toURI().getPath();

            // Corrección para rutas en Windows cuando se obtienen de getClass().getClassLoader().getResource()
            if (passengersFilePath.startsWith("/") && passengersFilePath.contains(":")) {
                passengersFilePath = passengersFilePath.substring(1);
            }

        } catch (URISyntaxException e) {
            System.err.println("Error al convertir URL a URI para passengers.json: " + e.getMessage());
            e.printStackTrace();
            return;
        } catch (Exception e) {
            System.err.println("Error al obtener la ruta del recurso passengers.json: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        System.out.println("--- GESTIÓN DE PASAJEROS ---");

        try {
            System.out.println("Cargando pasajeros desde: " + passengersFilePath);
            // Llama al método loadPassengersFromJson con la ruta obtenida
            passengerDataLoader.loadPassengersFromJson(passengersFilePath, MIN_TOTAL_PASSENGERS); // Pasa minTotalPassengers
            System.out.println("Carga de pasajeros completada. Total: " + passengerManager.getPassengerCount() + " pasajeros.");

            System.out.println("\nListado de pasajeros (orden inOrder):");
            System.out.println(passengerManager.passengers.inOrder()); // Asegúrate de que 'passengers' es accesible y 'inOrder()' existe

            // Opcional: Prueba de registro manual de algunos pasajeros (antes o después de la carga del JSON)
            // Ya no es necesario el bucle for inicial si loadPassengersFromJson maneja la generación mínima
            // for (int i = 0; i < 5; i++) {
            //     passengerManager.registerPassenger(Utility.RandomId(), Utility.RandomNames(), Utility.RandomNationalities());
            // }


        } catch (Exception e) {
            System.err.println("Ocurrió un error inesperado durante la gestión de pasajeros: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("--- FIN GESTIÓN DE PASAJEROS ---");
    }


    }

