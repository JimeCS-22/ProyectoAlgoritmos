package ucr.proyectoalgoritmos.Domain.flight;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ucr.proyectoalgoritmos.Domain.TreeException;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.airplane.Airplane;
import ucr.proyectoalgoritmos.Domain.airplane.Airplane.AirplaneStatus;
import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;
import ucr.proyectoalgoritmos.Domain.passenger.PassengerManager;
import ucr.proyectoalgoritmos.Domain.queue.QueueException;
import ucr.proyectoalgoritmos.Domain.route.RouteManager;
import ucr.proyectoalgoritmos.Domain.stack.LinkedStack;
import ucr.proyectoalgoritmos.Domain.stack.StackException;
import ucr.proyectoalgoritmos.util.ListConverter;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit; // Asegúrate de que esto esté importado
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ScheduledFuture;

public class FlightSimulator {
    // --- Atributos de Managers ---
    private AirportManager airportManager;
    private PassengerManager passengerManager;
    private FlightScheduleManager flightScheduleManager;
    private RouteManager routeManager;

    // --- Atributos de Simulación y Control ---
    private Map<String, Airplane> airplanes;
    private ScheduledExecutorService scheduler;
    private Random random;
    private AtomicInteger flightCounter;

    private volatile boolean flightLimitMessagePrinted = false;
    private final int MAX_FLIGHTS_TO_GENERATE = 15;

    private volatile boolean paused = false;

    // Mapa para almacenar datos en tiempo real de vuelos activos
    private Map<String, FlightData> inProgressFlightsData;
    // Mapa para almacenar los ScheduledFuture de las tareas de simulación de datos de cada vuelo
    private final Map<String, ScheduledFuture<?>> flightDataTasks;

    // --- Clase Interna: FlightData (para datos en tiempo real de la UI) ---
    public static class FlightData {
        public String flightNumber;
        public int currentAltitude;  // Metros
        public int currentSpeed;     // Km/h
        public int currentHeading;   // Grados (0-359)
        public long elapsedTimeSeconds; // Segundos transcurridos desde el despegue
        public LocalDateTime departureTime; // Hora real de despegue
        public long estimatedFlightDurationMinutes; // Duración estimada del vuelo en minutos

        public FlightData() {
            this.currentAltitude = 0;
            this.currentSpeed = 0;
            this.currentHeading = 0;
            this.elapsedTimeSeconds = 0;
            this.estimatedFlightDurationMinutes = 0;
        }
        public FlightData(String flightNumber, LocalDateTime departureTime) {
            this.flightNumber = flightNumber;
            this.currentAltitude = 0;
            this.currentSpeed = 0;
            this.currentHeading = 0;
            this.elapsedTimeSeconds = 0;
            this.departureTime = departureTime;
            this.estimatedFlightDurationMinutes = 0; // Se debe establecer externamente con un setter después.
            // O se podría pasar como parámetro aquí si siempre se sabe al construir.
        }

        // Getters para los datos
        public int getCurrentAltitude() { return currentAltitude; }
        public int getCurrentSpeed() { return currentSpeed; }
        public int getCurrentHeading() { return currentHeading; }
        public long getElapsedTimeSeconds() { return elapsedTimeSeconds; }
        public long getEstimatedFlightDurationMinutes() { return estimatedFlightDurationMinutes; }


        // Setters
        public void setCurrentAltitude(int currentAltitude) { this.currentAltitude = currentAltitude; }
        public void setCurrentSpeed(int currentSpeed) { this.currentSpeed = currentSpeed; }
        public void setCurrentHeading(int currentHeading) { this.currentHeading = currentHeading; }
        public void setElapsedTimeSeconds(long elapsedTimeSeconds) { this.elapsedTimeSeconds = elapsedTimeSeconds; }
        public void setEstimatedFlightDurationMinutes(long estimatedFlightDurationMinutes) { this.estimatedFlightDurationMinutes = estimatedFlightDurationMinutes; }
    }

    // --- Constructor de FlightSimulator ---
    public FlightSimulator() throws ListException, IOException, TreeException {
        // Inicialización de managers
        this.airportManager = new AirportManager();
        this.routeManager = new RouteManager(airportManager);
        this.passengerManager = new PassengerManager();
        this.flightScheduleManager = new FlightScheduleManager(this.airportManager, this.routeManager);

        this.airplanes = new HashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        this.random = new Random();
        this.flightCounter = new AtomicInteger(0);

        // Inicializar los mapas
        this.inProgressFlightsData = new ConcurrentHashMap<>();
        this.flightDataTasks = new ConcurrentHashMap<>();

        initializeSystem();
    }

    // --- Métodos de Inicialización del Sistema ---
    private void initializeSystem() throws ListException, IOException, TreeException {
        System.out.println("...Inicializando sistema...");
        loadAirportsFromFile("airports.json");

        DoublyLinkedList allLoadedAirports = airportManager.getAllAirports();
        if (allLoadedAirports != null && !allLoadedAirports.isEmpty()) {
            for (int i = 0; i < allLoadedAirports.size(); i++) {
                try {
                    Airport airport = (Airport) allLoadedAirports.get(i);
                    routeManager.getGraph().addVertex(airport.getCode());
                } catch (ListException e) {
                    System.err.println("ERROR: No se pudo obtener aeropuerto del índice " + i + " al añadir al grafo: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            System.err.println("ADVERTENCIA: No hay aeropuertos cargados o la lista está vacía.");
        }

        if (routeManager.getGraph().getNumVertices() < 2) {
            System.err.println("ERROR: No hay suficientes aeropuertos en el grafo para cargar o generar rutas. Asegúrese de que se carguen al menos 2 aeropuertos.");
        } else {
            try {
                routeManager.loadRoutesFromJson("routes.json");
            } catch (IOException e) {
                System.out.println("ADVERTENCIA: No se encontró 'routes.json' o hubo un error al cargarlo. Generando rutas aleatorias...");
                routeManager.getGraph().generateRandomRoutes(3, 7, 30, 600);
            }
        }

        // Añadir aviones de ejemplo
        addAirplane("AIR001", 180, "SJO"); addAirplane("AIR002", 220, "MIA");
        addAirplane("AIR003", 160, "LIR"); addAirplane("AIR004", 200, "LAX");
        addAirplane("AIR005", 250, "JFK"); addAirplane("AIR006", 190, "CDG");
        addAirplane("AIR007", 210, "FRA"); addAirplane("AIR008", 170, "DXB");
        addAirplane("AIR009", 230, "NRT"); addAirplane("AIR010", 150, "SYD");
        addAirplane("AIR011", 205, "ORD"); addAirplane("AIR012", 240, "PEK");
        addAirplane("AIR013", 195, "IST"); addAirplane("AIR014", 175, "MEX");
        addAirplane("AIR015", 225, "LIM"); addAirplane("AIR016", 185, "SJO");
        addAirplane("AIR017", 215, "MIA"); addAirplane("AIR018", 170, "LIR");
        addAirplane("AIR019", 200, "LAX"); addAirplane("AIR020", 245, "JFK");
        addAirplane("AIR021", 192, "CDG"); addAirplane("AIR022", 218, "FRA");
        addAirplane("AIR023", 178, "DXB"); addAirplane("AIR024", 235, "NRT");
        addAirplane("AIR025", 155, "SYD");

        // Registrar pasajeros de ejemplo
        try {
            passengerManager.registerPassenger("1001", "Alice Smith", "USA");
            passengerManager.registerPassenger("1002", "Bob Johnson", "Canada");
            passengerManager.registerPassenger("1003", "Carlos Garcia", "Mexico");
            passengerManager.registerPassenger("1004", "Diana Miller", "UK");
            passengerManager.registerPassenger("1005", "Eve Brown", "Germany");
            passengerManager.registerPassenger("1006", "Frank White", "France");
            passengerManager.registerPassenger("1007", "Grace Hall", "Brazil");
            passengerManager.registerPassenger("1008", "Henry King", "India");
            passengerManager.registerPassenger("1009", "Ivy Lee", "South Korea");
            passengerManager.registerPassenger("1010", "Jack Green", "Australia");
        } catch (TreeException e) {
            System.err.println("ERROR: Fallo al registrar pasajeros predefinidos: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Sistema inicializado con éxito.");
    }

    private void loadAirportsFromFile(String filename) throws ListException {
        int loadedCount = 0;
        try (Reader reader = Files.newBufferedReader(Paths.get(filename))) {
            Gson gson = new Gson();
            List<Airport> airportListFromFile = gson.fromJson(reader, new TypeToken<List<Airport>>() {
            }.getType());

            if (airportListFromFile != null && !airportListFromFile.isEmpty()) {
                for (Airport airport : airportListFromFile) {
                    if (loadedCount >= 20) { // Limitar a 20 aeropuertos para manejabilidad
                        break;
                    }
                    airportManager.createAirport(airport.getCode(), airport.getName(), airport.getCountry());
                    loadedCount++;
                }
            }

        } catch (IOException e) {
            System.err.println("ERROR: No se encontró el archivo '" + filename + "'. Cargando aeropuertos por defecto.");
            addDefaultAirports();
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("ERROR: El archivo JSON de aeropuertos '" + filename + "' tiene un formato inválido: " + e.getMessage());
            addDefaultAirports();
        } finally {
            try {
                if (airportManager.getAllAirports().isEmpty()) {
                    System.out.println("ADVERTENCIA: La lista de aeropuertos está vacía después de intentar cargar. Añadiendo aeropuertos por defecto.");
                    addDefaultAirports();
                }
            } catch (ListException e) {
                System.err.println("ERROR: No se pudo verificar si la lista de aeropuertos está vacía: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void addDefaultAirports() throws ListException {
        if (airportManager.findAirport("SJO") == null) airportManager.createAirport("SJO", "Juan Santamaría", "Costa Rica");
        if (airportManager.findAirport("LIR") == null) airportManager.createAirport("LIR", "Daniel Oduber Quirós", "Costa Rica");
        if (airportManager.findAirport("MIA") == null) airportManager.createAirport("MIA", "Miami International", "USA");
        if (airportManager.findAirport("JFK") == null) airportManager.createAirport("JFK", "John F. Kennedy", "USA");
        if (airportManager.findAirport("LAX") == null) airportManager.createAirport("LAX", "Los Angeles International", "USA");
        if (airportManager.findAirport("CDG") == null) airportManager.createAirport("CDG", "Charles de Gaulle", "France");
        if (airportManager.findAirport("FRA") == null) airportManager.createAirport("FRA", "Frankfurt Airport", "Germany");
        if (airportManager.findAirport("DXB") == null) airportManager.createAirport("DXB", "Dubai International", "UAE");
        if (airportManager.findAirport("NRT") == null) airportManager.createAirport("NRT", "Narita International", "Japan");
        if (airportManager.findAirport("SYD") == null) airportManager.createAirport("SYD", "Sydney Airport", "Australia");
        if (airportManager.findAirport("ORD") == null) airportManager.createAirport("ORD", "O'Hare International", "USA");
        if (airportManager.findAirport("PEK") == null) airportManager.createAirport("PEK", "Beijing Capital", "China");
        if (airportManager.findAirport("IST") == null) airportManager.createAirport("IST", "Istanbul Airport", "Turkey");
        if (airportManager.findAirport("MEX") == null) airportManager.createAirport("MEX", "Mexico City Int'l", "Mexico");
        if (airportManager.findAirport("LIM") == null) airportManager.createAirport("LIM", "Jorge Chávez Int'l", "Peru");
    }

    public void addAirplane(String id, int capacity, String initialLocationAirportCode) throws ListException {
        if (airportManager.findAirport(initialLocationAirportCode) != null) {
            if (!airplanes.containsKey(id)) {
                airplanes.put(id, new Airplane(id, capacity, initialLocationAirportCode));
                System.out.println("Avión " + id + " añadido en " + initialLocationAirportCode + ".");
            } else {
                System.out.println("ADVERTENCIA: El avión con ID '" + id + "' ya existe y no se añadió de nuevo.");
            }
        } else {
            System.err.println("ERROR: No se puede añadir el avión " + id + ". La ubicación inicial " + initialLocationAirportCode + " no es un aeropuerto válido.");
        }
    }
    /**
     * Activa un vuelo, cambiándolo a estado IN_PROGRESS,
     * actualizando el estado del avión y configurando sus datos de simulación en tiempo real.
     * Este método es centralizado para ser usado tanto por la generación automática
     * como por el avance manual de vuelos.
     * @param flight El objeto Flight a activar.
     */
    public void activateFlight(Flight flight) {
        if (flight == null) {
            System.err.println("ERROR: No se puede activar un vuelo nulo.");
            return;
        }

        try {
            System.out.println("\nSIMULADOR: Activando vuelo " + flight.getFlightNumber() + " (DESPEGUE)...");
            flight.setStatus(Flight.FlightStatus.IN_PROGRESS);
            flight.setActualDepartureTime(LocalDateTime.now()); // Establece la hora real de despegue

            Airplane assignedPlane = flight.getAirplane();
            if (assignedPlane != null) {
                assignedPlane.setStatus(Airplane.AirplaneStatus.IN_FLIGHT);
                assignedPlane.setLocationInFlight(); // Marca el avión como "en vuelo"
                System.out.println("Avión " + assignedPlane.getId() + " ahora IN_FLIGHT.");
            } else {
                System.err.println("ADVERTENCIA: Vuelo " + flight.getFlightNumber() + " no tiene un avión asignado. No se pudo actualizar el estado del avión.");
            }

            // Inicializar y almacenar los datos del vuelo en progreso.
            // Estos datos serán actualizados periódicamente por la tarea global en `startSimulation`.
            FlightData newFlightData = new FlightData(flight.getFlightNumber(), flight.getActualDepartureTime());
            newFlightData.setEstimatedFlightDurationMinutes(flight.getEstimatedDurationMinutes()); // Esencial para la simulación de progreso
            inProgressFlightsData.put(flight.getFlightNumber(), newFlightData);

            System.out.println("\n-----------------------------------------------------");
            System.out.println("VUELO ACTIVO ✈️: " + flight.getFlightNumber() + " (" + (assignedPlane != null ? assignedPlane.getId() : "N/A") + ")");
            System.out.println("Ruta: " + flight.getOriginAirportCode() + " -> " + flight.getDestinationAirportCode());
            System.out.println("-----------------------------------------------------");

            System.out.println("SIMULADOR: Vuelo " + flight.getFlightNumber() + " activado. La simulación de sus datos en tiempo real ha comenzado.");
        } catch (Exception e) {
            System.err.println("ERROR: Excepción durante la activación del vuelo " + flight.getFlightNumber() + ": " + e.getMessage());
            e.printStackTrace();
            // Si ocurre un error, intentar revertir el estado del vuelo y el avión
            flight.setStatus(Flight.FlightStatus.SCHEDULED); // O a un estado de error
            Airplane assignedPlane = flight.getAirplane();
            if (assignedPlane != null) {
                assignedPlane.setStatus(AirplaneStatus.IDLE);
                assignedPlane.setLocationType(Airplane.AirplaneLocationType.AIRPORT);
            }
            inProgressFlightsData.remove(flight.getFlightNumber()); // Limpiar cualquier dato de progreso parcial
        }
    }

    // --- Métodos Getters ---
    public FlightScheduleManager getFlightScheduleManager() {
        return flightScheduleManager;
    }

    /**
     * Obtener los datos de un vuelo en progreso para la UI.
     * @param flightNumber Número del vuelo.
     * @return Objeto FlightData si el vuelo está en progreso, de lo contrario null.
     */
    public FlightData getFlightInProgressData(String flightNumber) {
        return inProgressFlightsData.get(flightNumber);
    }

    // --- Lógica de Simulación de Datos de Vuelo en Tiempo Real ---
    /**
     * Actualiza los datos de simulación (altitud, velocidad, rumbo, tiempo transcurrido)
     * para un vuelo dado, basándose en el tiempo real.
     * También gestiona la finalización del vuelo cuando se alcanza su duración estimada.
     * Este método es llamado periódicamente por un ScheduledExecutorService.
     */
    private void updateFlightInProgressData(String flightNumber, Flight flight) throws ListException, StackException {
        // Si el vuelo ya no está en progreso, lo removemos y cancelamos su tarea si existía
        if (flight.getStatus() != Flight.FlightStatus.IN_PROGRESS) {
            if (inProgressFlightsData.containsKey(flightNumber)) {
                inProgressFlightsData.remove(flightNumber);
                System.out.println("SIMULADOR: Datos en tiempo real para vuelo " + flightNumber + " removidos (no en progreso).");
            }
            ScheduledFuture<?> task = flightDataTasks.remove(flightNumber);
            if (task != null) {
                task.cancel(true);
            }
            return;
        }

        FlightData data = inProgressFlightsData.get(flightNumber);
        if (data == null) {
            // Esto no debería ocurrir si `startFlightDataSimulation` se llama correctamente, pero como fallback
            data = new FlightData(flightNumber, flight.getActualDepartureTime() != null ? flight.getActualDepartureTime() : LocalDateTime.now());
            // Es crucial que el `estimatedFlightDurationMinutes` se establezca aquí para que la lógica de progreso funcione
            data.setEstimatedFlightDurationMinutes(flight.getEstimatedDurationMinutes());
            inProgressFlightsData.put(flightNumber, data);
        }

        // Calcula el tiempo transcurrido desde el despegue
        Duration duration = Duration.between(data.departureTime, LocalDateTime.now());
        data.setElapsedTimeSeconds(Math.max(0, duration.getSeconds())); // ¡CORRECCIÓN APLICADA AQUÍ! Asegura que no sea negativo

        // Calcula la duración total estimada del vuelo en segundos
        // Usa el valor de FlightData, que ya debería haber sido establecido desde el Flight original
        long totalSimulatedDurationSeconds = data.getEstimatedFlightDurationMinutes() * 60;

        // Evitar división por cero y asegurar una duración mínima
        if (totalSimulatedDurationSeconds <= 0) {
            totalSimulatedDurationSeconds = 10 * 60; // Por ejemplo, un mínimo de 10 minutos (600 segundos) si no se pudo determinar
        }

        double progressRatio = (double) data.getElapsedTimeSeconds() / totalSimulatedDurationSeconds;
        progressRatio = Math.min(1.0, progressRatio); // Asegura que no exceda 1.0

        // --- Lógica para determinar si el vuelo ha finalizado ---
        if (data.getElapsedTimeSeconds() >= totalSimulatedDurationSeconds) {
            System.out.println("SIMULADOR: Vuelo " + flightNumber + " ha alcanzado su duración estimada. Marcando como COMPLETED.");
            flight.setStatus(Flight.FlightStatus.COMPLETED);
            flight.setActualArrivalTime(LocalDateTime.now()); // Establece la hora real de llegada

            // Actualizar el estado del avión
            Airplane assignedPlane = flight.getAirplane();
            if (assignedPlane != null) {
                assignedPlane.setCurrentLocationAirportCode(flight.getDestinationAirportCode());
                assignedPlane.setStatus(Airplane.AirplaneStatus.IDLE);
                assignedPlane.setLocationType(Airplane.AirplaneLocationType.AIRPORT);
                assignedPlane.addFlightToHistory(flight);
                System.out.println("Avión " + assignedPlane.getId() + " aterrizado en " + assignedPlane.getCurrentLocationAirportCode() + " y ahora IDLE.");
            }

            // Eliminar de los datos en progreso y cancelar la tarea individual
            inProgressFlightsData.remove(flightNumber);
            ScheduledFuture<?> task = flightDataTasks.remove(flightNumber);
            if (task != null) {
                task.cancel(true); // Intenta cancelar la tarea de actualización de datos de ese vuelo
            }

            // Remover el vuelo de la lista de programados (si tu gestión lo requiere)
            try {
                flightScheduleManager.removeFlight(flight.getFlightNumber());
                System.out.println("Vuelo " + flightNumber + " removido de la lista de vuelos programados después de completar.");
            } catch (Exception e) {
                System.err.println("ADVERTENCIA: No se pudo remover el vuelo " + flightNumber + " de la lista después de completar: " + e.getMessage());
            }

            return; // El vuelo ha terminado, no hay más actualizaciones
        }

        // --- Lógica de simulación de altitud, velocidad y rumbo para ir MÁS RÁPIDO ---
        // Basada en el porcentaje de progreso del vuelo

        // Definimos umbrales para las fases como porcentajes de la duración total
        double accelerationPhaseEnd = 0.10; // Primer 10% del vuelo
        double cruisePhaseEnd = 0.85;       // Hasta el 85% del vuelo (para dar más tiempo de descenso)

        // Valores máximos deseados
        int maxAltitude = 11000; // Ej: 11,000 metros (36,000 pies)
        int maxSpeed = 900;      // Ej: 900 km/h

        // Fase de ACELERACIÓN y ASCENSO
        if (progressRatio < accelerationPhaseEnd) {
            double phaseProgress = progressRatio / accelerationPhaseEnd; // Progreso dentro de esta fase (0 a 1)
            data.setCurrentAltitude((int) (maxAltitude * phaseProgress));
            data.setCurrentSpeed((int) (maxSpeed * phaseProgress));
            data.setCurrentHeading(calculateTargetHeading(flight.getOriginAirportCode(), flight.getDestinationAirportCode()));
        }
        // Fase de CRUCERO
        else if (progressRatio < cruisePhaseEnd) {
            data.setCurrentAltitude(maxAltitude + random.nextInt(200) - 100); // Pequeñas variaciones alrededor de la altitud de crucero
            data.setCurrentSpeed(maxSpeed + random.nextInt(40) - 20);       // Pequeñas variaciones alrededor de la velocidad de crucero
            data.setCurrentHeading(calculateTargetHeading(flight.getOriginAirportCode(), flight.getDestinationAirportCode()));
        }
        // Fase de DESCENSO y FRENADO
        else {
            double phaseProgress = (progressRatio - cruisePhaseEnd) / (1.0 - cruisePhaseEnd); // Progreso dentro de esta fase (0 a 1)
            data.setCurrentAltitude((int) (maxAltitude * (1.0 - phaseProgress))); // Baja desde la altitud máxima hasta 0
            data.setCurrentSpeed((int) (maxSpeed * (1.0 - phaseProgress)));     // Baja desde la velocidad máxima hasta 0
            data.setCurrentHeading(calculateTargetHeading(flight.getOriginAirportCode(), flight.getDestinationAirportCode()));
        }

        // Asegurarse de que los valores no sean negativos y tengan sentido
        data.setCurrentAltitude(Math.max(0, data.getCurrentAltitude()));
        data.setCurrentSpeed(Math.max(0, data.getCurrentSpeed()));
        data.setCurrentHeading((data.getCurrentHeading() + 360) % 360); // Asegura que esté entre 0 y 359

        // Ajustes finales para un aterrizaje suave visualmente
        // Esto ayudará a que la velocidad y altitud lleguen a cero al final.
        if (data.getElapsedTimeSeconds() >= totalSimulatedDurationSeconds - 5 && data.getElapsedTimeSeconds() < totalSimulatedDurationSeconds) {
            // Últimos 5 segundos del vuelo
            data.setCurrentAltitude(Math.max(0, data.getCurrentAltitude() - (int)(data.currentAltitude / (double)(totalSimulatedDurationSeconds - data.getElapsedTimeSeconds() + 1))));
            data.setCurrentSpeed(Math.max(0, data.getCurrentSpeed() - (int)(data.currentSpeed / (double)(totalSimulatedDurationSeconds - data.getElapsedTimeSeconds() + 1))));
        } else if (data.getElapsedTimeSeconds() >= totalSimulatedDurationSeconds) {
            data.setCurrentAltitude(0);
            data.setCurrentSpeed(0);
        }

        System.out.println(String.format("DEBUG SIM: Vuelo %s - Progreso: %.2f%%, Tiempo: %d s / %d s, Altitud: %d m, Velocidad: %d km/h, Rumbo: %d°",
                flightNumber, progressRatio * 100, data.getElapsedTimeSeconds(), totalSimulatedDurationSeconds, data.getCurrentAltitude(), data.getCurrentSpeed(), data.getCurrentHeading()));
    }

    /**
     * Calcula un rumbo objetivo simplificado entre dos códigos de aeropuerto.
     * Esta es una implementación de ejemplo y NO es geográficamente precisa;
     * es solo para fines de visualización básica.
     * @param originCode Código del aeropuerto de origen.
     * @param destinationCode Código del aeropuerto de destino.
     * @return Un rumbo en grados (0-359).
     */
    private int calculateTargetHeading(String originCode, String destinationCode) {
        // Puedes agregar más lógica aquí o buscar coordenadas reales y usar fórmulas de rumbo.
        // Ejemplos simplificados para algunas rutas comunes
        if (originCode.equals("SJO") && destinationCode.equals("LIR")) return 270; // SJO a LIR (Oeste)
        if (originCode.equals("LIR") && destinationCode.equals("SJO")) return 90; // LIR a SJO (Este)
        if (originCode.equals("SJO") && destinationCode.equals("MIA")) return 45; // SJO a Miami (Noreste)
        if (originCode.equals("MIA") && destinationCode.equals("SJO")) return 225; // Miami a SJO (Suroeste)
        if (originCode.equals("LAX") && destinationCode.equals("JFK")) return 75; // LAX a JFK (Este-Noreste)
        if (originCode.equals("JFK") && destinationCode.equals("LAX")) return 255; // JFK a LAX (Oeste-Suroeste)
        return (int) (Math.random() * 360); // Rumbo aleatorio por defecto si la ruta no está definida
    }

    // --- Métodos de Generación de Vuelos Automática ---
    /**
     * Genera un vuelo aleatorio basado en las reglas del sistema.
     * Asigna aeropuertos de origen y destino, avión, pasajeros y lo programa.
     * NO INICIA LA SIMULACIÓN DE DATOS DEL VUELO AQUÍ. Solo lo programa.
     */
    private void generateRandomFlightBasedOnRules() throws ListException, TreeException {
        if (paused) {
            return;
        }

        if (flightCounter.get() >= MAX_FLIGHTS_TO_GENERATE) {
            if (!flightLimitMessagePrinted) {
                System.out.println("ADVERTENCIA: Límite de " + MAX_FLIGHTS_TO_GENERATE + " vuelos alcanzado. No se generarán más vuelos.");
                flightLimitMessagePrinted = true;
            }
            return;
        }

        DoublyLinkedList allAirportsList = airportManager.getAllAirports();
        if (allAirportsList.isEmpty()) {
            System.out.println("ADVERTENCIA: No hay aeropuertos cargados para generar vuelos. Retornando.");
            return;
        }

        List<Airport> activeAirports = new ArrayList<>();
        for (int i = 0; i < allAirportsList.size(); i++) {
            try {
                Airport airport = (Airport) allAirportsList.get(i);
                if (airport.getStatus() == Airport.AirportStatus.ACTIVE) {
                    activeAirports.add(airport);
                }
            } catch (ListException e) {
                System.err.println("ERROR: No se pudo obtener aeropuerto del índice " + i + " al filtrar por activos: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (activeAirports.size() < 2) {
            System.out.println("ADVERTENCIA: Se requieren al menos 2 aeropuertos activos para generar vuelos. Actualmente hay " + activeAirports.size() + ". Retornando.");
            return;
        }

        Collections.sort(activeAirports, (airport1, airport2) -> {
            int count1 = routeManager.getGraph().getOutgoingRouteCount(airport1.getCode());
            int count2 = routeManager.getGraph().getOutgoingRouteCount(airport2.getCode());
            return Integer.compare(count2, count1); // Orden descendente
        });

        List<Airport> selectedOrigins = new ArrayList<>();
        for (int i = 0; i < Math.min(5, activeAirports.size()); i++) {
            selectedOrigins.add(activeAirports.get(i));
        }

        if (selectedOrigins.isEmpty()) {
            System.out.println("ADVERTENCIA: No se pudieron seleccionar aeropuertos de origen con suficientes rutas salientes. Retornando.");
            return;
        }

        Airport originAirport = null;
        String originCode = null;
        String destinationCode = null;
        int routeFindingAttempts = 0;
        final int MAX_ROUTE_FINDING_ATTEMPTS = 100;

        while (routeFindingAttempts < MAX_ROUTE_FINDING_ATTEMPTS) {
            originAirport = selectedOrigins.get(random.nextInt(selectedOrigins.size()));
            originCode = originAirport.getCode();

            List<String> potentialDestinationCodes = new ArrayList<>();
            try {
                SinglyLinkedList graphAirportCodes = routeManager.getGraph().getAllAirportCodes();
                for (int i = 0; i < graphAirportCodes.size(); i++) {
                    potentialDestinationCodes.add((String) graphAirportCodes.get(i));
                }
            } catch (ListException e) {
                System.err.println("ERROR: No se pudieron obtener los códigos de aeropuerto del grafo para destino: " + e.getMessage());
                e.printStackTrace();
                routeFindingAttempts++;
                continue;
            }

            if (potentialDestinationCodes.isEmpty()) {
                System.out.println("ADVERTENCIA: No hay códigos de aeropuerto disponibles o suficientes en el grafo para seleccionar un destino. Retornando.");
                return;
            }

            String potentialDestinationCode = potentialDestinationCodes.get(random.nextInt(potentialDestinationCodes.size()));

            if (!potentialDestinationCode.equals(originCode) &&
                    routeManager.calculateShortestRoute(originCode, potentialDestinationCode) != Integer.MAX_VALUE) {
                destinationCode = potentialDestinationCode;
                break;
            }
            routeFindingAttempts++;
        }

        if (destinationCode == null) {
            System.out.println("ADVERTENCIA: No se pudo encontrar una ruta válida entre aeropuertos después de " + MAX_ROUTE_FINDING_ATTEMPTS + " intentos. No se puede generar el vuelo. Retornando.");
            return;
        }

        Airplane selectedAirplane = null;
        List<Airplane> idleAirplanesAtOrigin = new ArrayList<>();
        for (Airplane airplane : airplanes.values()) {
            if (airplane.getStatus() == AirplaneStatus.IDLE &&
                    airplane.getLocationType() == Airplane.AirplaneLocationType.AIRPORT &&
                    airplane.getCurrentLocationAirportCode() != null &&
                    airplane.getCurrentLocationAirportCode().equals(originCode)) {
                idleAirplanesAtOrigin.add(airplane);
            }
        }

        if (idleAirplanesAtOrigin.isEmpty()) {
            System.out.println("ADVERTENCIA: No hay aviones IDLE disponibles en el aeropuerto de origen '" + originCode + "'. No se puede generar el vuelo. Retornando.");
            return;
        }

        selectedAirplane = idleAirplanesAtOrigin.get(random.nextInt(idleAirplanesAtOrigin.size()));
        selectedAirplane.setStatus(AirplaneStatus.ASSIGNED);

        String flightNumber = null;
        Flight newFlight = null;
        int flightNumberAttempts = 0;
        final int MAX_FLIGHT_NUMBER_ATTEMPTS = 50;

        do {
            flightNumber = "FL" + (random.nextInt(900) + 100);
            try {
                // Generar un vuelo con una hora de salida en el futuro cercano
                newFlight = flightScheduleManager.createFlight(flightNumber, originCode, destinationCode,
                        LocalDateTime.now().plusMinutes(random.nextInt(10) + 1), // Hora de salida en los próximos 1-10 minutos
                        0,
                        selectedAirplane.getCapacity());

                if (newFlight != null) {
                    break;
                }
            } catch (ListException e) {
                System.err.println("ADVERTENCIA: Intento de crear vuelo con número " + flightNumber + " falló (posiblemente duplicado o error en lista): " + e.getMessage());
                e.printStackTrace();
            }
            flightNumberAttempts++;
        } while (newFlight == null && flightNumberAttempts < MAX_FLIGHT_NUMBER_ATTEMPTS);


        if (newFlight == null) {
            System.err.println("ERROR: Fallo al crear un vuelo con número de vuelo único después de " + MAX_FLIGHT_NUMBER_ATTEMPTS + " intentos. Retornando.");
            if (selectedAirplane != null) {
                selectedAirplane.setStatus(AirplaneStatus.IDLE); // Liberar avión si no se puede crear el vuelo
            }
            return;
        }

        newFlight.setAirplane(selectedAirplane);

        // ¡Ajuste para duración de vuelo más corta para simulación más rápida!
        int estimatedDurationRealistic = routeManager.calculateShortestRoute(originCode, destinationCode);
        if (estimatedDurationRealistic == Integer.MAX_VALUE || estimatedDurationRealistic == 0) {
            estimatedDurationRealistic = 120 + random.nextInt(180); // Duración aleatoria si no hay ruta
        }
        // Reducimos la duración estimada para que los vuelos terminen más rápido
        newFlight.setEstimatedDurationMinutes(Math.max(10, estimatedDurationRealistic / 3)); // Mínimo 10 minutos, o un tercio de la duración real

        System.out.println("\n--- Programando Vuelo " + flightNumber + " ---");
        System.out.println("De: " + (airportManager.findAirport(originCode) != null ? airportManager.findAirport(originCode).getName() : originCode) + " (" + originCode + ")");
        System.out.println("A: " + (airportManager.findAirport(destinationCode) != null ? airportManager.findAirport(destinationCode).getName() : destinationCode) + " (" + destinationCode + ")");
        System.out.println("Avión asignado: " + selectedAirplane.getId() + " (Capacidad: " + selectedAirplane.getCapacity() + ")");
        System.out.println("Salida programada: " + newFlight.getScheduledDepartureTime().toLocalDate() + " " + newFlight.getScheduledDepartureTime().toLocalTime().withNano(0));
        System.out.println("Duración estimada: " + (int) Math.round((double) newFlight.getEstimatedDurationMinutes() / 60.0) + " h. (" + newFlight.getEstimatedDurationMinutes() + " min)");

        // Asignar pasajeros
        int passengersToBoard = random.nextInt(selectedAirplane.getCapacity() / 2) + 1; // Mínimo 1, máximo la mitad de la capacidad
        passengersToBoard = Math.min(passengersToBoard, selectedAirplane.getCapacity() - newFlight.getOccupancy()); // No exceder capacidad
        passengersToBoard = Math.min(passengersToBoard, passengerManager.getPassengerCount()); // No exceder total de pasajeros

        System.out.println("Intentando asignar " + passengersToBoard + " pasajeros al vuelo " + flightNumber + ".");

        List<Passenger> allAvailablePassengersFromAVL = null;
        try {
            DoublyLinkedList dll = passengerManager.getPassengersAVL().inOrderList();
            if (dll != null && !dll.isEmpty()) {
                allAvailablePassengersFromAVL = new ArrayList<>();
                for (int i = 0; i < dll.size(); i++) {
                    allAvailablePassengersFromAVL.add((Passenger) dll.get(i));
                }
            }
        } catch (ListException e) {
            System.err.println("ERROR: No se pudo obtener la lista de pasajeros del AVL: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (allAvailablePassengersFromAVL == null || allAvailablePassengersFromAVL.isEmpty()) {
            System.out.println("ADVERTENCIA: No hay pasajeros registrados en el sistema para simular la compra de billetes en el vuelo " + flightNumber + ". El vuelo podría salir vacío.");
        } else {
            List<Passenger> shuffledPassengers = new ArrayList<>(allAvailablePassengersFromAVL);
            Collections.shuffle(shuffledPassengers);

            int actualPassengersAssigned = 0;

            for (int i = 0; i < passengersToBoard && i < shuffledPassengers.size(); i++) {
                Passenger p = shuffledPassengers.get(i);
                if (p != null) { // Agregué esta comprobación de nulidad
                    try {
                        // Llama a los managers para procesar el billete
                        flightScheduleManager.processTicketPurchase(p, newFlight);
                        passengerManager.processTicketPurchase(p, newFlight);
                        actualPassengersAssigned++;
                    } catch (ListException | QueueException e) {
                        System.err.println("ERROR: Fallo al procesar billete para pasajero " + p.getId() + " en el vuelo " + flightNumber + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("ADVERTENCIA: Pasajero nulo en la lista mezclada. No se pudo asignar al vuelo " + flightNumber + ".");
                }
            }
            System.out.println(actualPassengersAssigned + " pasajeros asignados al vuelo " + flightNumber + " de " + newFlight.getCapacity() + " asientos.");
            System.out.println(newFlight.getOccupancy() + " pasajeros finales a bordo del vuelo " + flightNumber + ".");
        }

        flightCounter.incrementAndGet(); // Incrementa el contador de vuelos generados
        System.out.println("Total de vuelos generados hasta ahora: " + flightCounter.get());
    }


    // --- Métodos de Control de la Simulación ---

    /**
     * Inicia el ciclo de vida de la simulación.
     * Configura la generación automática de vuelos y la actualización de datos en tiempo real.
     * @param flightGenerationIntervalSeconds Intervalo en segundos para generar nuevos vuelos.
     * @param totalSimulationDurationSeconds Duración total de la simulación en segundos.
     */
    public void startSimulation(long flightGenerationIntervalSeconds, long totalSimulationDurationSeconds) {
        System.out.println("--- INICIANDO SIMULACIÓN DE VUELOS ---");
        paused = false;
        flightLimitMessagePrinted = false; // Resetear el mensaje de límite al iniciar
        flightCounter.set(0); // Resetear el contador de vuelos generados

        // Tarea 1: Generar nuevos vuelos periódicamente
        scheduler.scheduleAtFixedRate(() -> {
            if (!paused) {
                try {
                    if (flightCounter.get() < MAX_FLIGHTS_TO_GENERATE) {
                        generateRandomFlightBasedOnRules();
                    } else {
                        if (!flightLimitMessagePrinted) {
                            System.out.println("\nADVERTENCIA: Límite de vuelos generados (" + MAX_FLIGHTS_TO_GENERATE + ") alcanzado. Deteniendo la generación de nuevos vuelos.");
                            flightLimitMessagePrinted = true;
                        }
                    }
                } catch (ListException | TreeException e) {
                    System.err.println("ERROR FATAL: Error al generar vuelo aleatorio: " + e.getMessage());
                    e.printStackTrace();
                    // shutdownSimulation(); // Descomentar si un error aquí debe detener completamente la simulación
                } catch (Exception e) {
                    System.err.println("ERROR FATAL: Excepción inesperada durante la generación de vuelos: " + e.getMessage());
                    e.printStackTrace();
                    // shutdownSimulation(); // Descomentar si un error aquí debe detener completamente la simulación
                }
            }
        }, 0, flightGenerationIntervalSeconds, TimeUnit.SECONDS);

        // Tarea 2: Actualizar los datos de los vuelos en progreso cada segundo
        // Esta tarea itera sobre los vuelos en progreso y llama a updateFlightInProgressData.
        scheduler.scheduleAtFixedRate(() -> {
            if (!paused) {
                // Iterar sobre una copia de las claves para evitar ConcurrentModificationException
                // si un vuelo termina y se remueve mientras se itera.
                for (String flightNumber : new HashSet<>(inProgressFlightsData.keySet())) {
                    try {
                        Flight flight = flightScheduleManager.findFlight(flightNumber); // Asume que FlightScheduleManager tiene findFlight(String)
                        if (flight != null) { // Solo procesar si el objeto Flight existe
                            if (flight.getStatus() == Flight.FlightStatus.IN_PROGRESS) {
                                updateFlightInProgressData(flightNumber, flight);
                            } else {
                                // Si un vuelo ya no está en progreso (ej. fue completado por la lógica de duración),
                                // nos aseguramos de limpiar sus datos y su tarea.
                                inProgressFlightsData.remove(flightNumber);
                                ScheduledFuture<?> task = flightDataTasks.remove(flightNumber);
                                if (task != null) {
                                    task.cancel(true);
                                }
                                System.out.println("SIMULADOR: Datos en tiempo real para vuelo " + flightNumber + " removidos (estado cambiado/finalizado).");
                            }
                        } else {
                            // Si el objeto Flight mismo no se encuentra (ej. fue removido por FlightScheduleManager),
                            // también limpiamos sus datos.
                            inProgressFlightsData.remove(flightNumber);
                            ScheduledFuture<?> task = flightDataTasks.remove(flightNumber);
                            if (task != null) {
                                task.cancel(true);
                            }
                            System.out.println("SIMULADOR: Datos en tiempo real para vuelo " + flightNumber + " removidos (vuelo no encontrado en FlightScheduleManager).");
                        }
                    } catch (Exception e) {
                        System.err.println("ERROR: Excepción durante la actualización de datos de vuelo en progreso para " + flightNumber + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }, 0, 1, TimeUnit.SECONDS); // Actualiza cada 1 segundo

        // Tarea 3: Apagar la simulación después de una duración total
        if (totalSimulationDurationSeconds > 0) { // Solo si se especifica una duración total
            scheduler.schedule(this::shutdownSimulation, totalSimulationDurationSeconds, TimeUnit.SECONDS);
        }
    }


    /**
     * Apaga todos los hilos del simulador, limpia los datos en progreso
     * y muestra un resumen de la simulación.
     */
    public void shutdownSimulation() {
        System.out.println("\n--- FINALIZANDO SIMULACIÓN DE VUELOS ---");
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("Advertencia: Algunas tareas no terminaron en el tiempo especificado. Forzando el apagado.");
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.err.println("Error: El apagado de la simulación fue interrumpido.");
                Thread.currentThread().interrupt();
                scheduler.shutdownNow();
            }
        }

        // Limpiar todas las tareas de datos de vuelo individuales que puedan seguir activas
        for (ScheduledFuture<?> task : flightDataTasks.values()) {
            task.cancel(true);
        }
        flightDataTasks.clear();
        inProgressFlightsData.clear(); // Limpiar todos los datos en progreso

        // Resetear estados de vuelos para una nueva simulación si es necesario (opcional)
        try {
            DoublyLinkedList allFlights = ListConverter.convertToDoublyLinkedList(flightScheduleManager.getScheduledFlights());
            for (int i = 0; i < allFlights.size(); i++) {
                Flight f = (Flight) allFlights.get(i);
                // Si el vuelo está en IN_PROGRESS o COMPLETED, lo volvemos a SCHEDULED para futuras simulaciones.
                if (f.getStatus() == Flight.FlightStatus.IN_PROGRESS || f.getStatus() == Flight.FlightStatus.COMPLETED) {
                    f.setStatus(Flight.FlightStatus.SCHEDULED);
                    // También liberar el avión asociado
                    Airplane assignedPlane = f.getAirplane();
                    if (assignedPlane != null && assignedPlane.getStatus() != AirplaneStatus.IDLE) {
                        assignedPlane.setStatus(AirplaneStatus.IDLE);
                        // Asegurarse de que su ubicación sea el aeropuerto de destino si completó o el de origen si se canceló.
                        if (f.getDestinationAirportCode() != null) {
                            assignedPlane.setCurrentLocationAirportCode(f.getDestinationAirportCode());
                        } else { // Si no llegó a destino (ej. cancelado), vuelve a origen
                            assignedPlane.setCurrentLocationAirportCode(f.getOriginAirportCode());
                        }
                        assignedPlane.setLocationType(Airplane.AirplaneLocationType.AIRPORT);
                    }
                }
            }
        } catch (ListException e) {
            System.err.println("ERROR al resetear estados de vuelos: " + e.getMessage());
        }


        System.out.println("Simulación finalizada.");
        printSimulationSummary();
    }

    /**
     * Imprime un resumen de la simulación.
     */
    public void printSimulationSummary() {
        System.out.println("\n--- Resumen de la Simulación ---");
        System.out.println("Vuelos programados actualmente: " + flightScheduleManager.getScheduledFlights().size());
        System.out.println("Vuelos generados: " + flightCounter.get());
        try {
            printAirplaneFlightHistory();
        } catch (ListException e) {
            System.err.println("Error al imprimir historial de vuelos de aviones: " + e.getMessage());
        }
        System.out.println("\nTotal de pasajeros registrados en el sistema: " + passengerManager.getPassengerCount());
    }

    /**
     * Imprime el historial de vuelos de cada avión.
     */
    private void printAirplaneFlightHistory() throws ListException {
        System.out.println("\n--- Historial de Vuelos por Avión ---");
        if (airplanes.isEmpty()) {
            System.out.println("No hay aviones registrados.");
            return;
        }
        for (Airplane airplane : airplanes.values()) {
            System.out.println("\nAvión ID: " + airplane.getId() + " (Capacidad: " + airplane.getCapacity() + ", Estado: " + airplane.getStatus() + ")");
            LinkedStack history = airplane.getFlightHistory();
            if (history != null && !history.isEmpty()) {
                // Se usa un enfoque temporal de pila para obtener la lista sin modificar la pila original
                DoublyLinkedList historyList = new DoublyLinkedList();
                LinkedStack tempStack = new LinkedStack();
                try {
                    while (!history.isEmpty()) {
                        Object element = history.pop();
                        historyList.add(element);
                        tempStack.push(element);
                    }
                    while (!tempStack.isEmpty()) {
                        history.push(tempStack.pop());
                    }
                } catch (StackException e) {
                    System.err.println("Error al acceder/restaurar historial del avión " + airplane.getId() + ": " + e.getMessage());
                }

                System.out.println("  Historial de Vuelos (" + historyList.size() + " vuelos):");
                for (int i = 0; i < historyList.size(); i++) {
                    try {
                        Flight hFlight = (Flight) historyList.get(i);
                        System.out.println("    - Vuelo " + hFlight.getFlightNumber() + ": " +
                                hFlight.getOriginAirportCode() + " -> " + hFlight.getDestinationAirportCode() +
                                " (Duración: " + hFlight.getEstimatedDurationMinutes() + " min, Estado: " + hFlight.getStatus() + ")");
                    } catch (ListException e) {
                        System.err.println("Error al obtener vuelo del historial en índice " + i + ": " + e.getMessage());
                    }
                }
            } else {
                System.out.println("  No tiene historial de vuelos.");
            }
        }
    }

    // --- MÉTODOS PARA PAUSA Y AVANCE MANUAL ---

    /**
     * Pausa la generación automática de vuelos y la actualización de datos en tiempo real.
     */
    public void pauseSimulator() {
        this.paused = true;
        System.out.println("SIMULADOR: Tareas automáticas de generación/avance pausadas.");
    }

    /**
     * Reanuda la generación automática de vuelos y la actualización de datos en tiempo real.
     */
    public void resumeSimulator() {
        this.paused = false;
        System.out.println("SIMULADOR: Tareas automáticas de generación/avance reanudadas.");
    }

    /**
     * Avanza al siguiente vuelo programado, poniéndolo en estado IN_PROGRESS
     * e iniciando su simulación de datos en tiempo real.
     * Detiene cualquier vuelo que estuviera previamente en progreso para enfocarse en uno solo.
     * Diseñado para ser llamado por una acción manual (ej. botón "Siguiente Vuelo").
     *
     * @return true si un vuelo fue activado exitosamente, false si no hay vuelos pendientes.
     */
    public Flight advanceToNextScheduledFlight() throws ListException {
        // No need to explicitly "end" a flight here.
        // The UI's updateLiveFlightData will detect if a flight completed (data becomes null)
        // or if a new flight is IN_PROGRESS and switch visualization.

        // 1. Get the list of scheduled flights.
        CircularDoublyLinkedList rawScheduledFlights = flightScheduleManager.getScheduledFlights();
        DoublyLinkedList scheduledFlights = ListConverter.convertToDoublyLinkedList(rawScheduledFlights);

        if (scheduledFlights.isEmpty()) {
            System.out.println("SIMULADOR: No hay vuelos programados para avanzar manualmente.");
            return null; // Returns null as no flight was activated
        }

        // 2. Find the flight with the earliest departure time that is in SCHEDULED status.
        Flight nextFlightToProcess = null;
        // Uses LocalDateTime.MAX to ensure any flight's departure date is earlier.
        LocalDateTime earliestDeparture = LocalDateTime.MAX;

        for (int i = 0; i < scheduledFlights.size(); i++) {
            Flight currentFlight = (Flight) scheduledFlights.get(i);
            if (currentFlight.getStatus() == Flight.FlightStatus.SCHEDULED &&
                    currentFlight.getScheduledDepartureTime().isBefore(earliestDeparture)) {
                earliestDeparture = currentFlight.getScheduledDepartureTime();
                nextFlightToProcess = currentFlight;
            }
        }

        if (nextFlightToProcess == null) {
            System.out.println("SIMULADOR: No se encontraron vuelos en estado SCHEDULED para activar manualmente.");
            return null; // Returns null as no flight was activated
        }

        // 3. Mark the selected flight as IN_PROGRESS.
        nextFlightToProcess.setStatus(Flight.FlightStatus.IN_PROGRESS);
        nextFlightToProcess.setActualDepartureTime(LocalDateTime.now()); // ¡CORRECCIÓN APLICADA AQUÍ! Establece la hora real de despegue al activar manualmente

        System.out.println("SIMULADOR: Vuelo activado manualmente: " + nextFlightToProcess.getFlightNumber() +
                " de " + nextFlightToProcess.getOriginAirportCode() +
                " a " + nextFlightToProcess.getDestinationAirportCode());

        // 4. Initialize FlightData for this flight if it doesn't already exist.
        if (!inProgressFlightsData.containsKey(nextFlightToProcess.getFlightNumber())) {
            FlightData newFlightData = new FlightData(nextFlightToProcess.getFlightNumber(),
                    nextFlightToProcess.getActualDepartureTime()); // Usar la hora de despegue real
            newFlightData.setEstimatedFlightDurationMinutes(nextFlightToProcess.getEstimatedDurationMinutes()); // ¡CORRECCIÓN APLICADA AQUÍ!
            inProgressFlightsData.put(nextFlightToProcess.getFlightNumber(), newFlightData);
        }

        // 5. Return the activated flight so the UI can use it.
        return nextFlightToProcess;
    }

    /**
     * Método auxiliar para finalizar un vuelo que esté actualmente en progreso.
     * Esto es necesario cuando se avanza a un nuevo vuelo manualmente o automáticamente,
     * para "limpiar" el estado del vuelo anterior y su avión.
     * @throws ListException si hay un problema con las operaciones de lista.
     * @throws StackException si hay un problema con las operaciones de pila (historial de avión).
     */
    private void endCurrentInProgressFlight() throws ListException, StackException {
        Flight currentInProgress = null;

        // Buscar el vuelo que está actualmente en progreso
        // Iterar sobre una copia de las claves para evitar ConcurrentModificationException
        for (String flightNumber : new HashSet<>(inProgressFlightsData.keySet())) {
            try {
                Flight f = flightScheduleManager.findFlight(flightNumber);
                if (f != null && f.getStatus() == Flight.FlightStatus.IN_PROGRESS) {
                    currentInProgress = f;
                    break;
                }
            } catch (Exception e) {
                System.err.println("Error al buscar vuelo " + flightNumber + " en progreso para finalizar: " + e.getMessage());
            }
        }

        if (currentInProgress != null) {
            System.out.println("SIMULADOR: Finalizando vuelo " + currentInProgress.getFlightNumber() + " para dar paso al siguiente...");

            // Marcar el vuelo como completado
            currentInProgress.setStatus(Flight.FlightStatus.COMPLETED);
            currentInProgress.setActualArrivalTime(LocalDateTime.now());

            // Actualizar el estado del avión
            Airplane assignedPlane = currentInProgress.getAirplane();
            if (assignedPlane != null) {
                assignedPlane.setCurrentLocationAirportCode(currentInProgress.getDestinationAirportCode());
                assignedPlane.setStatus(Airplane.AirplaneStatus.IDLE);
                assignedPlane.setLocationType(Airplane.AirplaneLocationType.AIRPORT);
                assignedPlane.addFlightToHistory(currentInProgress);
                System.out.println("Avión " + assignedPlane.getId() + " asociado al vuelo " + currentInProgress.getFlightNumber() + " ahora IDLE en " + assignedPlane.getCurrentLocationAirportCode() + ".");
            }

            // Limpiar los datos de simulación en tiempo real y cancelar la tarea de actualización
            inProgressFlightsData.remove(currentInProgress.getFlightNumber());
            ScheduledFuture<?> task = flightDataTasks.remove(currentInProgress.getFlightNumber());
            if (task != null) {
                task.cancel(true);
                System.out.println("Tarea de simulación de datos para vuelo " + currentInProgress.getFlightNumber() + " cancelada.");
            }

            // Remover el vuelo de la lista de programados, ya que se ha completado
            try {
                flightScheduleManager.removeFlight(currentInProgress.getFlightNumber());
                System.out.println("Vuelo " + currentInProgress.getFlightNumber() + " removido de la lista de vuelos programados.");
            } catch (Exception e) {
                System.err.println("ADVERTENCIA: No se pudo remover el vuelo " + currentInProgress.getFlightNumber() + " de la lista: " + e.getMessage());
            }

            System.out.println("Vuelo " + currentInProgress.getFlightNumber() + " finalizado.");
        }
    }
}