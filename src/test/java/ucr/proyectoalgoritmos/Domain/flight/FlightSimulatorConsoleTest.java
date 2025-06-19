package ucr.proyectoalgoritmos.Domain.flight; // Adjust package as per your project structure

import ucr.proyectoalgoritmos.Domain.flight.FlightSimulator; // Import the FlightSimulator class
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.queue.QueueException;
import ucr.proyectoalgoritmos.Domain.TreeException;

import java.io.IOException;

/**
 * Clase de prueba para ejecutar la simulación de vuelos desde la consola.
 */
public class FlightSimulatorConsoleTest {

    public static void main(String[] args) {
        try {
            FlightSimulator simulator = new FlightSimulator();

            // Parámetros de la simulación:
            // 1. flightGenerationIntervalSeconds: Cada cuántos segundos se intenta generar un nuevo vuelo.
            // 2. simulationDurationSeconds: Duración total de la simulación en segundos.
            long flightGenerationInterval = 5; // Generar un vuelo cada 5 segundos
            long simulationDuration = 300;     // Duración total de la simulación: 5 minutos

            System.out.println("Configurando simulación:");
            System.out.println("  - Intervalo de generación de vuelos: " + flightGenerationInterval + " segundos");
            System.out.println("  - Duración total de la simulación: " + simulationDuration + " segundos (" + (simulationDuration / 60) + " minutos)");
            System.out.println("  - La simulación finalizará automáticamente después de " + simulationDuration + " segundos, o cuando se alcance el límite de vuelos generados.");

            simulator.startSimulation(flightGenerationInterval, simulationDuration);

            // Puedes añadir una forma de detener la simulación manualmente si es necesario,
            // por ejemplo, usando un Scanner para esperar una entrada del usuario,
            // pero el `scheduler.schedule` ya se encarga de detenerla automáticamente.

        } catch (ListException | IOException | TreeException e) {
            System.err.println("Ocurrió un error crítico durante la inicialización o ejecución del simulador:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Ocurrió una excepción inesperada en la prueba principal:");
            e.printStackTrace();
        }
    }
}