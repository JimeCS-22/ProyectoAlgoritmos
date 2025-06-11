package ucr.proyectoalgoritmos.Domain.flight;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.airplane.Airplane;
import ucr.proyectoalgoritmos.Domain.airplane.Airplane.AirplaneStatus;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger; // Corregido: passenger a passanger
import ucr.proyectoalgoritmos.Domain.passenger.PassengerManager; // Corregido: passenger a passanger
import ucr.proyectoalgoritmos.Domain.stack.StackException;
import ucr.proyectoalgoritmos.route.RouteManager;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FlightSimulator {
    private AirportManager airportManager;
    private PassengerManager passengerManager;
    private FlightScheduleManager flightScheduleManager;
    private RouteManager routeManager;

    private Map<String, Airplane> airplanes;
    private ScheduledExecutorService scheduler;
    private Random random;
    private AtomicInteger flightCounter;

    private volatile boolean flightLimitMessagePrinted = false;
    private final int MAX_FLIGHTS_TO_GENERATE = 15; // Define el límite como una constante

    public FlightSimulator() throws ListException, IOException {
        this.airportManager = new AirportManager();
        this.routeManager = new RouteManager(airportManager);
        this.passengerManager = new PassengerManager();
        this.flightScheduleManager = new FlightScheduleManager(this.airportManager, this.routeManager);

        this.airplanes = new HashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        this.random = new Random();
        this.flightCounter = new AtomicInteger(0);

        initializeSystem();
    }

    private void initializeSystem() throws ListException, IOException {
        System.out.println("SIMULADOR: Inicializando sistema...");
        loadAirportsFromFile("airports.json");

        DoublyLinkedList allLoadedAirports = airportManager.getAllAirports();
        if (allLoadedAirports != null && !allLoadedAirports.isEmpty()) {
            for (int i = 0; i < allLoadedAirports.size(); i++) {
                try {
                    Airport airport = (Airport) allLoadedAirports.get(i);
                    routeManager.getGraph().addVertex(airport.getCode());
                } catch (ListException e) {
                    System.err.println("ERROR: No se pudo obtener aeropuerto del índice " + i + " al añadir al grafo: " + e.getMessage());
                }
            }
        } else {
            System.err.println("ADVERTENCIA: No hay aeropuertos cargados o la lista está vacía. El grafo no tendrá vértices.");
        }


        if (routeManager.getGraph().getNumVertices() < 2) {
            System.err.println("ERROR: No hay suficientes aeropuertos (vértices) en el grafo para cargar o generar rutas. Asegúrese de que se carguen al menos 2 aeropuertos.");
        } else {
            try {
                routeManager.loadRoutesFromJson("routes.json");
                System.out.println("SIMULADOR: Rutas cargadas desde 'routes.json'.");
            } catch (IOException e) {
                System.err.println("ADVERTENCIA: No se pudieron cargar las rutas desde 'routes.json': " + e.getMessage());
                System.out.println("SIMULADOR: Generando rutas aleatorias como alternativa.");
                int numVertices = routeManager.getGraph().getNumVertices();
                int maxRoutes = numVertices * (numVertices - 1) / 2;
                routeManager.getGraph().generateRandomRoutes(Math.min(maxRoutes, 20), 7, 30, 600);
                System.out.println("SIMULADOR: Rutas aleatorias generadas.");
            }
        }

        // Añadir aviones predefinidos
        addAirplane("AIR001", 180, "SJO");
        addAirplane("AIR002", 220, "MIA");
        addAirplane("AIR003", 160, "LIR");
        addAirplane("AIR004", 200, "LAX");
        addAirplane("AIR005", 250, "JFK");
        addAirplane("AIR006", 190, "CDG");
        addAirplane("AIR007", 210, "FRA");
        addAirplane("AIR008", 170, "DXB");
        addAirplane("AIR009", 230, "NRT");
        addAirplane("AIR010", 150, "SYD");
        addAirplane("AIR011", 205, "ORD");
        addAirplane("AIR012", 240, "PEK");
        addAirplane("AIR013", 195, "IST");
        addAirplane("AIR014", 175, "MEX");
        addAirplane("AIR015", 225, "LIM");
        addAirplane("AIR016", 185, "SJO");
        addAirplane("AIR017", 215, "MIA");
        addAirplane("AIR018", 170, "LIR");
        addAirplane("AIR019", 200, "LAX");
        addAirplane("AIR020", 245, "JFK");
        addAirplane("AIR021", 192, "CDG");
        addAirplane("AIR022", 218, "FRA");
        addAirplane("AIR023", 178, "DXB");
        addAirplane("AIR024", 235, "NRT");
        addAirplane("AIR025", 155, "SYD");
        System.out.println("SIMULADOR: Aviones predefinidos añadidos.");

        // Registrar pasajeros predefinidos
        passengerManager.registerPassenger("1001", "Alice Smith", "USA");
        passengerManager.registerPassenger("1002", "Bob Johnson", "Canada");
        passengerManager.registerPassenger("1003", "Carlos Garcia", "Mexico");
        passengerManager.registerPassenger("1004", "Diana Miller", "UK");
        passengerManager.registerPassenger("1005", "Eve Brown", "Germany");
        System.out.println("SIMULADOR: Pasajeros predefinidos registrados.");
        System.out.println("SIMULADOR: Sistema inicializado con éxito.");
    }

    private void loadAirportsFromFile(String filename) throws ListException {
        int loadedCount = 0;
        try (Reader reader = Files.newBufferedReader(Paths.get(filename))) {
            Gson gson = new Gson();
            List<Airport> airportListFromFile = gson.fromJson(reader, new TypeToken<List<Airport>>() {}.getType());

            if (airportListFromFile != null && !airportListFromFile.isEmpty()) {
                for (Airport airport : airportListFromFile) {
                    if (loadedCount >= 20) {
                        System.out.println("ADVERTENCIA: Se ha alcanzado el límite de 20 aeropuertos del archivo. Saltando el resto.");
                        break;
                    }
                    airportManager.createAirport(airport.getCode(), airport.getName(), airport.getCountry());
                    loadedCount++;
                }
                System.out.println("SIMULADOR: " + loadedCount + " aeropuertos cargados desde '" + filename + "'.");
            } else {
                System.out.println("ADVERTENCIA: El archivo '" + filename + "' está vacío o no contiene datos válidos. No se cargaron aeropuertos del archivo.");
            }

            if (loadedCount < 15) {
                System.out.println("ADVERTENCIA: Solo se cargaron " + loadedCount + " aeropuertos del archivo. Se sugiere un mínimo de 15 para una simulación robusta.");
            }

        } catch (IOException e) {
            System.err.println("ERROR: No se pudo leer el archivo de aeropuertos '" + filename + "': " + e.getMessage());
            System.out.println("SIMULADOR: Generando algunos aeropuertos predeterminados en su lugar.");
            addDefaultAirports();
        } finally {
            if (airportManager.getAllAirports().isEmpty()) {
                System.out.println("SIMULADOR: No se cargaron aeropuertos por ningún método; añadiendo aeropuertos predeterminados de emergencia.");
                addDefaultAirports();
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
        System.out.println("SIMULADOR: Aeropuertos predeterminados de emergencia añadidos.");
    }

    public void addAirplane(String id, int capacity, String initialLocationAirportCode) throws ListException {
        if (airportManager.findAirport(initialLocationAirportCode) != null) {
            if (!airplanes.containsKey(id)) {
                airplanes.put(id, new Airplane(id, capacity, initialLocationAirportCode));
            } else {
                System.out.println("SIMULADOR: El avión " + id + " ya existe. Se omite la adición.");
            }
        } else {
            System.err.println("ERROR: No se puede añadir el avión " + id + ". La ubicación inicial " + initialLocationAirportCode + " no es un aeropuerto válido.");
        }
    }

    private void generateRandomFlightBasedOnRules() throws ListException, StackException {
        if (flightCounter.get() >= MAX_FLIGHTS_TO_GENERATE) {
            if (!flightLimitMessagePrinted) {
                System.out.println("ADVERTENCIA: [GENERACIÓN] Límite de " + MAX_FLIGHTS_TO_GENERATE + " vuelos alcanzado. No se generarán más vuelos.");
                flightLimitMessagePrinted = true;
            }
            return;
        }
        System.out.println("\nSIMULADOR: [GENERACIÓN] Intentando generar nuevo vuelo. Vuelos generados hasta ahora: " + flightCounter.get() + " (Límite: " + MAX_FLIGHTS_TO_GENERATE + ")");


        DoublyLinkedList allAirportsList = airportManager.getAllAirports();
        if (allAirportsList.isEmpty()) {
            System.out.println("ADVERTENCIA: [GENERACIÓN] No hay aeropuertos cargados para generar vuelos. Retornando.");
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
                System.err.println("ERROR: [GENERACIÓN] No se pudo obtener aeropuerto del índice " + i + " al filtrar por activos: " + e.getMessage());
            }
        }

        if (activeAirports.size() < 2) {
            System.out.println("ADVERTENCIA: [GENERACIÓN] Se requieren al menos 2 aeropuertos activos para generar vuelos. Actualmente hay " + activeAirports.size() + ". Retornando.");
            return;
        }

        Collections.sort(activeAirports, new Comparator<Airport>() {
            @Override
            public int compare(Airport airport1, Airport airport2) {
                int count1 = routeManager.getGraph().getOutgoingRouteCount(airport1.getCode());
                int count2 = routeManager.getGraph().getOutgoingRouteCount(airport2.getCode());
                return Integer.compare(count2, count1);
            }
        });

        List<Airport> selectedOrigins = new ArrayList<>();
        for (int i = 0; i < Math.min(5, activeAirports.size()); i++) {
            selectedOrigins.add(activeAirports.get(i));
        }

        if (selectedOrigins.isEmpty()) {
            System.out.println("ADVERTENCIA: [GENERACIÓN] No se pudieron seleccionar aeropuertos de origen. Asegúrate de tener suficientes aeropuertos activos y conectados. Retornando.");
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

            SinglyLinkedList allAirportCodesFromGraph;
            try {
                allAirportCodesFromGraph = routeManager.getGraph().getAllAirportCodes();
            } catch (ListException e) {
                System.err.println("ERROR: [GENERACIÓN] No se pudieron obtener los códigos de aeropuerto del grafo: " + e.getMessage());
                return;
            }

            if (allAirportCodesFromGraph.isEmpty() || allAirportCodesFromGraph.size() < 2) {
                System.out.println("ADVERTENCIA: [GENERACIÓN] No hay códigos de aeropuerto disponibles o suficientes en el grafo para seleccionar un destino. Retornando.");
                return;
            }

            String potentialDestinationCode = null;
            try {
                potentialDestinationCode = (String) allAirportCodesFromGraph.get(random.nextInt(allAirportCodesFromGraph.size()));
            } catch (ListException e) {
                System.err.println("ERROR: [GENERACIÓN] No se pudo obtener código de aeropuerto para destino aleatorio: " + e.getMessage());
                routeFindingAttempts++;
                continue;
            }

            if (!potentialDestinationCode.equals(originCode) &&
                    routeManager.calculateShortestRoute(originCode, potentialDestinationCode) != Integer.MAX_VALUE) {
                destinationCode = potentialDestinationCode;
                break;
            }
            routeFindingAttempts++;
        }

        if (destinationCode == null) {
            System.out.println("ADVERTENCIA: [GENERACIÓN] No se pudo encontrar una ruta válida entre aeropuertos después de " + MAX_ROUTE_FINDING_ATTEMPTS + " intentos. No se puede generar el vuelo. Retornando.");
            return;
        }

        Airplane selectedAirplane = null;
        List<Airplane> idleAirplanes = new ArrayList<>();
        for (Airplane airplane : airplanes.values()) {
            if (airplane.getStatus() == AirplaneStatus.IDLE && airplane.getCurrentLocationAirportCode().equals(originCode)) {
                idleAirplanes.add(airplane);
            }
        }

        if (idleAirplanes.isEmpty()) {
            System.out.println("ADVERTENCIA: [GENERACIÓN] No hay aviones IDLE disponibles en el aeropuerto de origen (" + originCode + ") para el vuelo. Retornando y esperando que se liberen en el siguiente ciclo.");
            return;
        }

        selectedAirplane = idleAirplanes.get(random.nextInt(idleAirplanes.size()));

        String flightNumber;
        Flight newFlight = null;
        int flightNumberAttempts = 0;
        final int MAX_FLIGHT_NUMBER_ATTEMPTS = 50;

        do {
            flightNumber = "FL" + (random.nextInt(900) + 100);
            try {
                newFlight = flightScheduleManager.createFlight(flightNumber, originCode, destinationCode,
                        LocalDateTime.now().plusSeconds(random.nextInt(3) + 1), 0, selectedAirplane.getCapacity());
                break;
            } catch (ListException e) {
                flightNumberAttempts++;
                if (flightNumberAttempts >= MAX_FLIGHT_NUMBER_ATTEMPTS) {
                    System.err.println("ERROR: [GENERACIÓN] No se pudo generar un número de vuelo único después de " + MAX_FLIGHT_NUMBER_ATTEMPTS + " intentos. No se puede programar el vuelo. Retornando.");
                    return;
                }
            }
        } while (true);

        if (newFlight == null) {
            System.err.println("ERROR: [GENERACIÓN] Fallo inesperado al crear el objeto Flight. Retornando.");
            return;
        }

        newFlight.setAirplane(selectedAirplane);
        System.out.println("DEBUG: [GENERACIÓN] Avión " + selectedAirplane.getId() + " asignado al vuelo " + flightNumber + ".");

        int estimatedDurationRealistic = routeManager.calculateShortestRoute(originCode, destinationCode);
        if (estimatedDurationRealistic == Integer.MAX_VALUE || estimatedDurationRealistic == 0) {
            estimatedDurationRealistic = 120 + random.nextInt(180);
            System.out.println("DEBUG: [GENERACIÓN] Duración de ruta no válida o cero, usando duración estimada aleatoria: " + estimatedDurationRealistic + " minutos.");
        }
        newFlight.setEstimatedDurationMinutes(estimatedDurationRealistic);

        System.out.println("\n--- Programando Vuelo " + flightNumber + " ---");
        System.out.println("De: " + airportManager.getAirportName(originCode) + " (" + originCode + ")");
        System.out.println("A: " + airportManager.getAirportName(destinationCode) + " (" + destinationCode + ")");
        System.out.println("Avión asignado: " + selectedAirplane.getId() + " (Capacidad: " + selectedAirplane.getCapacity() + ")");
        System.out.println("Salida programada: " + newFlight.getDepartureTime());
        System.out.println("Duración estimada (simulada): " + String.format("%.2f", (double)newFlight.getEstimatedDurationMinutes() / 60.0) + " horas.");

        // --- Simulación de compra de billetes y embarque de pasajeros ---
        int passengersToBoard = random.nextInt(selectedAirplane.getCapacity() / 2) + 1;
        passengersToBoard = Math.min(passengersToBoard, selectedAirplane.getCapacity() - newFlight.getOccupancy());
        passengersToBoard = Math.min(passengersToBoard, passengerManager.getPassengerCount());

        System.out.println("SIMULADOR: Simulando compra de " + passengersToBoard + " billetes para el vuelo " + flightNumber + ".");

        SinglyLinkedList allAvailablePassengerIds = passengerManager.getAllPassengerIds();
        if (allAvailablePassengerIds.isEmpty()) {
            System.out.println("ADVERTENCIA: No hay pasajeros registrados en el sistema para simular la compra de billetes en el vuelo " + flightNumber + ". El vuelo podría salir vacío.");
        } else {
            List<String> shuffledPassengerIds = new ArrayList<>();
            for (int i = 0; i < allAvailablePassengerIds.size(); i++) {
                try {
                    shuffledPassengerIds.add((String) allAvailablePassengerIds.get(i));
                } catch (ListException e) {
                    System.err.println("ERROR: No se pudo obtener ID de pasajero de la lista al preparar para shuffling: " + e.getMessage());
                }
            }
            Collections.shuffle(shuffledPassengerIds);

            int actualPassengersToAssign = Math.min(passengersToBoard, shuffledPassengerIds.size());
            System.out.println("DEBUG: Se intentará asignar " + actualPassengersToAssign + " pasajeros únicos para el vuelo " + flightNumber + ".");

            for (int i = 0; i < actualPassengersToAssign; i++) {
                String passengerId = shuffledPassengerIds.get(i);
                Passenger p = passengerManager.searchPassenger(passengerId);

                if (p != null) {
                    try {
                        // Llama a FlightScheduleManager.processTicketPurchase para añadir el pasajero al vuelo
                        flightScheduleManager.processTicketPurchase(p, newFlight);
                        // Llama a PassengerManager.processTicketPurchase para actualizar el historial del pasajero
                        passengerManager.processTicketPurchase(p, newFlight);

                    } catch (ListException | StackException e) {
                        System.err.println("ERROR: Fallo al procesar billete para pasajero " + passengerId + " en el vuelo " + flightNumber + ": " + e.getMessage());
                    }
                } else {
                    System.err.println("ADVERTENCIA: Pasajero con ID " + passengerId + " no encontrado en PassengerManager. No se pudo asignar al vuelo " + flightNumber + ".");
                }
            }
        }
        System.out.println("SIMULADOR: " + newFlight.getOccupancy() + " pasajeros asignados al vuelo " + flightNumber + " de " + newFlight.getCapacity() + " asientos.");
        // --- Fin de la simulación de compra de billetes ---


        // Programa la parte lógica del vuelo (cambios de estado, actualizaciones de avión/pasajeros)
        // y también activa la animación visual.
        long delaySecondsToTakeoff = Duration.between(LocalDateTime.now(), newFlight.getDepartureTime()).getSeconds();
        if (delaySecondsToTakeoff < 0) delaySecondsToTakeoff = 0;

        final String fNum = flightNumber;
        final Airplane plane = selectedAirplane;
        final String finalOriginCode = originCode;
        final String finalDestinationCode = destinationCode;
        // Usa la duración estimada real para el procesamiento lógico, convertida a segundos (escalada)
        final long simulatedDurationForLogicSeconds = (long) (newFlight.getEstimatedDurationMinutes() * 60 / 100);


        // Tarea 1: Despegue del Vuelo (cambio de estado, actualización del estado del avión)
        scheduler.schedule(() -> {
            try {
                Flight currentFlight = flightScheduleManager.getFlight(fNum);
                if (currentFlight != null) {
                    currentFlight.setStatus(Flight.FlightStatus.IN_PROGRESS);
                    plane.setStatus(AirplaneStatus.IN_FLIGHT); // Establece el estado del avión a EN_VUELO al despegar
                    plane.setCurrentLocationAirportCode(null); // El avión está en el aire
                    System.out.println("SIMULADOR: [LÓGICA] Vuelo " + fNum + " ha despegado de " + airportManager.getAirportName(finalOriginCode) + ". Estado: IN_PROGRESS. Avión " + plane.getId() + " ahora en IN_FLIGHT.");
                } else {
                    System.err.println("ERROR: [LÓGICA] Vuelo " + fNum + " no encontrado al intentar iniciar su simulación lógica.");
                    return;
                }
            } catch (ListException e) {
                System.err.println("ERROR: [LÓGICA] Durante la simulación de inicio de vuelo " + fNum + ": " + e.getMessage());
            } catch (Exception e) {
                System.err.println("ERROR: [LÓGICA] Excepción inesperada durante la simulación de inicio del vuelo " + fNum + ": " + e.getMessage());
            }
        }, delaySecondsToTakeoff, TimeUnit.SECONDS);


        // Tarea 2: Animación Visual del Vuelo
        scheduler.schedule(() -> {
            try {
                System.out.println("\n-----------------------------------------------------");
                System.out.println("SIMULADOR: VUELO EN CURSO (Animación): " + fNum + " (" + plane.getId() + ")");
                System.out.print(airportManager.getAirportName(finalOriginCode) + " >>>>>>>>>>>>>>>>✈️");
                System.out.flush();

                long animationSteps = 30;
                long actualVisualAnimationDurationMillis = 1500;
                long delayPerStep = actualVisualAnimationDurationMillis / animationSteps;

                for (int k = 0; k < animationSteps; k++) {
                    System.out.print(">");
                    System.out.flush();
                    Thread.sleep(delayPerStep);
                }
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + airportManager.getAirportName(finalDestinationCode));
                System.out.println("-----------------------------------------------------");
                System.out.println("SIMULADOR: ¡El vuelo " + fNum + " ha aterrizado (Animación) en " + airportManager.getAirportName(finalDestinationCode) + "!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("ERROR: Animación de vuelo " + fNum + " interrumpida: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("ERROR: Excepción inesperada durante la animación del vuelo " + fNum + ": " + e.getMessage());
            }
        }, delaySecondsToTakeoff, TimeUnit.SECONDS);


        // Tarea 3: Finalización del Vuelo (cambio de estado, actualización del estado del avión, desembarque de pasajeros)
        scheduler.schedule(() -> {
            try {
                Flight currentFlight = flightScheduleManager.getFlight(fNum);
                if (currentFlight != null) {
                    currentFlight.setStatus(Flight.FlightStatus.COMPLETED);
                    System.out.println("SIMULADOR: [LÓGICA] Vuelo " + fNum + " en estado: COMPLETED.");

                    plane.setStatus(AirplaneStatus.IDLE); // El avión vuelve a IDLE
                    plane.setCurrentLocationAirportCode(finalDestinationCode); // Actualiza la ubicación del avión
                    plane.addFlightToHistory(currentFlight); // Añade el vuelo al historial del avión
                    System.out.println("SIMULADOR: --> Vuelo " + fNum + " (Lógico) ha completado su viaje a " + airportManager.getAirportName(finalDestinationCode) + ". Avión " + plane.getId() + " ahora en IDLE y ubicado en " + airportManager.getAirportName(finalDestinationCode) + ".");

                    // Los pasajeros desembarcan (se borran del vuelo)
                    System.out.println("DEBUG: [LÓGICA] Pasajeros desembarcando del vuelo " + fNum + ". Total: " + currentFlight.getOccupancy() + ".");
                    currentFlight.clearPassengers();

                    // Remueve el vuelo completado de la lista de vuelos programados en FSM
                    try {
                        flightScheduleManager.getScheduledFlights().remove(currentFlight);
                        System.out.println("DEBUG: [LÓGICA] Vuelo " + fNum + " removido de la lista de vuelos programados.");
                    } catch (ListException e) {
                        System.err.println("ERROR: [LÓGICA] No se pudo remover el vuelo completado " + fNum + " de la lista de vuelos programados: " + e.getMessage());
                    }


                } else {
                    System.err.println("ERROR: [LÓGICA] Vuelo " + fNum + " no encontrado al intentar finalizar su simulación lógica.");
                }

            } catch (Exception e) {
                System.err.println("ERROR: [LÓGICA] Excepción inesperada durante la finalización lógica del vuelo " + fNum + ": " + e.getMessage());
            }
        }, delaySecondsToTakeoff + simulatedDurationForLogicSeconds, TimeUnit.SECONDS);

        flightCounter.incrementAndGet();
        System.out.println("SIMULADOR: Vuelo " + flightNumber + " programado exitosamente. Total vuelos generados: " + flightCounter.get());
    }

    public void startSimulation(long flightGenerationIntervalSeconds, long simulationDurationSeconds) {
        System.out.println("--- INICIANDO SIMULACIÓN DE VUELOS ---");
        System.out.println("SIMULADOR: Intervalo de generación de vuelos: " + flightGenerationIntervalSeconds + " segundos.");
        System.out.println("SIMULADOR: Duración total de la simulación: " + simulationDurationSeconds + " segundos.");
        System.out.println("SIMULADOR: Límite de vuelos a generar: " + MAX_FLIGHTS_TO_GENERATE);


        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (flightCounter.get() < MAX_FLIGHTS_TO_GENERATE) {
                    generateRandomFlightBasedOnRules();
                } else {
                    if (!flightLimitMessagePrinted) {
                        System.out.println("\n[SIMULADOR] Límite de " + MAX_FLIGHTS_TO_GENERATE + " vuelos alcanzado. No se generarán más vuelos.");
                        flightLimitMessagePrinted = true;
                    }
                }
            } catch (ListException | StackException e) {
                System.err.println("ERROR: [SCHEDULER] Error al intentar generar un vuelo aleatorio: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("ERROR: [SCHEDULER] Un error inesperado ocurrió durante la generación de vuelos: " + e.getMessage());
            }
        }, 0, flightGenerationIntervalSeconds, TimeUnit.SECONDS);

        scheduler.schedule(() -> {
            try {
                System.out.println("\n[SIMULADOR] Tiempo de simulación (" + simulationDurationSeconds + "s) finalizado. Deteniendo la simulación...");
                stopSimulation();
            } catch (ListException e) {
                System.err.println("ERROR: [SHUTDOWN] Error durante el apagado de la simulación: " + e.getMessage());
                e.printStackTrace();
            }
        }, simulationDurationSeconds, TimeUnit.SECONDS);
    }

    public void stopSimulation() throws ListException {
        System.out.println("\n--- DETENIENDO SIMULACIÓN DE VUELOS ---");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                System.err.println("ADVERTENCIA: Algunas tareas no terminaron en el tiempo especificado (" + 60 + " segundos). Forzando apagado inmediato.");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            System.err.println("ERROR: La simulación fue interrumpida durante el apagado.");
            throw new RuntimeException("Simulación interrumpida durante el apagado", e);
        }
        System.out.println("SIMULADOR: Simulador apagado.");

        System.out.println("\n--- Historial de Vuelos de Aviones ---");
        if (airplanes.isEmpty()) {
            System.out.println("No hay aviones en el sistema para mostrar historial.");
        } else {
            airplanes.values().forEach(Airplane::printFlightHistory);
        }

        System.out.println("\n--- Historial de Vuelos de Pasajeros ---");
        if (passengerManager.getPassengerCount() == 0) {
            System.out.println("No hay pasajeros registrados o con historial de vuelos para mostrar.");
        } else {
            SinglyLinkedList allPassengerIds = passengerManager.getAllPassengerIds();
            if (allPassengerIds != null && !allPassengerIds.isEmpty()) {
                for (int i = 0; i < allPassengerIds.size(); i++) {
                    try {
                        passengerManager.showFlightHistory((String) allPassengerIds.get(i));
                    } catch (ListException e) {
                        System.err.println("ERROR: No se pudo obtener ID de pasajero del índice " + i + " para mostrar historial: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("No hay IDs de pasajeros en la lista para mostrar el historial.");
            }
        }
    }
    public static void main(String[] args) {
        try {
            // Create an instance of FlightSimulator
            FlightSimulator simulator = new FlightSimulator();

            // Define simulation parameters
            long flightGenerationIntervalSeconds = 5; // Generate a new flight every 5 seconds
            long simulationDurationSeconds = 60;    // Run the simulation for 60 seconds (1 minute)

            System.out.println("\n--- Ejecutando Simulación de Vuelos ---");
            simulator.startSimulation(flightGenerationIntervalSeconds, simulationDurationSeconds);

        } catch (ListException | IOException e) {
            System.err.println("ERROR FATAL: No se pudo iniciar el simulador debido a un error de inicialización: " + e.getMessage());
            e.printStackTrace();
        }
    }

}