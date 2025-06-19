package ucr.proyectoalgoritmos.Domain.flight;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ucr.proyectoalgoritmos.Domain.TreeException;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.airplane.Airplane;
import ucr.proyectoalgoritmos.Domain.airplane.Airplane.AirplaneStatus;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;
import ucr.proyectoalgoritmos.Domain.passenger.PassengerManager;
import ucr.proyectoalgoritmos.Domain.queue.LinkedQueue;
import ucr.proyectoalgoritmos.Domain.queue.QueueException;
import ucr.proyectoalgoritmos.Domain.route.RouteManager;
import ucr.proyectoalgoritmos.Domain.stack.LinkedStack;
import ucr.proyectoalgoritmos.Domain.stack.StackException;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

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
    private final int MAX_FLIGHTS_TO_GENERATE = 15; // Set a reasonable limit for testing

    public FlightSimulator() throws ListException, IOException, TreeException {
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

    private void initializeSystem() throws ListException, IOException, TreeException {
        System.out.println("...Inicializando sistema...");
        loadAirportsFromFile("airports.json");

        DoublyLinkedList allLoadedAirports = airportManager.getAllAirports();
        if (allLoadedAirports != null && !allLoadedAirports.isEmpty()) {
            for (int i = 0; i < allLoadedAirports.size(); i++) {
                try {
                    Airport airport = (Airport) allLoadedAirports.get(i);
                    // Add airport to graph if not already handled by RouteManager's loadRoutesFromJson
                    // It's good practice to ensure all potential origin/destination airports are in the graph.
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
                // Only generate random routes if loading from JSON fails
                System.out.println("ADVERTENCIA: No se encontró 'routes.json' o hubo un error al cargarlo. Generando rutas aleatorias...");
                routeManager.getGraph().generateRandomRoutes(3, 7, 30, 600);
            }
        }

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
                    if (loadedCount >= 20) { // Limit to 20 airports for manageability
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
            } else {
                System.out.println("ADVERTENCIA: El avión con ID '" + id + "' ya existe y no se añadió de nuevo.");
            }
        } else {
            System.err.println("ERROR: No se puede añadir el avión " + id + ". La ubicación inicial " + initialLocationAirportCode + " no es un aeropuerto válido.");
        }
    }

    private void generateRandomFlightBasedOnRules() throws ListException, QueueException, TreeException {
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

        // Sorting airports by outgoing route count (descending)
        Collections.sort(activeAirports, (airport1, airport2) -> {
            int count1 = routeManager.getGraph().getOutgoingRouteCount(airport1.getCode());
            int count2 = routeManager.getGraph().getOutgoingRouteCount(airport2.getCode());
            return Integer.compare(count2, count1); // Descending order
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

        // Loop to find a valid origin-destination pair with an existing route
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
                continue; // Try with a different origin
            }

            if (potentialDestinationCodes.isEmpty()) {
                System.out.println("ADVERTENCIA: No hay códigos de aeropuerto disponibles o suficientes en el grafo para seleccionar un destino. Retornando.");
                return;
            }

            String potentialDestinationCode = potentialDestinationCodes.get(random.nextInt(potentialDestinationCodes.size()));

            // CHECK FOR A PATH (shortestPath != Integer.MAX_VALUE)
            if (!potentialDestinationCode.equals(originCode) &&
                    routeManager.calculateShortestRoute(originCode, potentialDestinationCode) != Integer.MAX_VALUE) {
                destinationCode = potentialDestinationCode;
                break; // Found a valid route, exit loop
            }
            routeFindingAttempts++;
        }

        if (destinationCode == null) {
            System.out.println("ADVERTENCIA: No se pudo encontrar una ruta válida entre aeropuertos después de " + MAX_ROUTE_FINDING_ATTEMPTS + " intentos. No se puede generar el vuelo. Retornando.");
            return;
        }

        Airplane selectedAirplane = null;
        List<Airplane> idleAirplanes = new ArrayList<>();
        for (Airplane airplane : airplanes.values()) {
            if (airplane.getStatus() == AirplaneStatus.IDLE && airplane.getCurrentLocationAirportCode() != null && airplane.getCurrentLocationAirportCode().equals(originCode)) {
                idleAirplanes.add(airplane);
            }
        }

        if (idleAirplanes.isEmpty()) {
            System.out.println("ADVERTENCIA: No hay aviones IDLE disponibles en el aeropuerto de origen '" + originCode + "'. No se puede generar el vuelo. Retornando.");
            return;
        }

        selectedAirplane = idleAirplanes.get(random.nextInt(idleAirplanes.size()));
        selectedAirplane.setStatus(AirplaneStatus.ASSIGNED);

        String flightNumber = null;
        Flight newFlight = null;
        int flightNumberAttempts = 0;
        final int MAX_FLIGHT_NUMBER_ATTEMPTS = 50;

        // Loop to generate a unique flight number and create the flight
        do {
            flightNumber = "FL" + (random.nextInt(900) + 100); // Generate a 3-digit number (100-999)
            try {
                // Departure time should be slightly in the future
                newFlight = flightScheduleManager.createFlight(flightNumber, originCode, destinationCode,
                        LocalDateTime.now().plusSeconds(random.nextInt(10) + 1), // Departs within 1-10 seconds
                        0, // Initial estimated duration (will be updated)
                        selectedAirplane.getCapacity());

                if (newFlight != null) {
                    // Flight was successfully created with a unique number
                    break;
                }
            } catch (ListException e) {
                // If createFlight throws an exception, it might mean the flight number already exists
                System.err.println("ADVERTENCIA: Intento de crear vuelo con número " + flightNumber + " falló (posiblemente duplicado o error en lista): " + e.getMessage());
                e.printStackTrace();
            }
            flightNumberAttempts++;
        } while (newFlight == null && flightNumberAttempts < MAX_FLIGHT_NUMBER_ATTEMPTS);


        if (newFlight == null) {
            System.err.println("ERROR: Fallo al crear un vuelo con número de vuelo único después de " + MAX_FLIGHT_NUMBER_ATTEMPTS + " intentos. Retornando.");
            // Reset airplane status if flight creation fails
            if (selectedAirplane != null) {
                selectedAirplane.setStatus(AirplaneStatus.IDLE);
            }
            return;
        }

        newFlight.setAirplane(selectedAirplane);

        int estimatedDurationRealistic = routeManager.calculateShortestRoute(originCode, destinationCode);
        if (estimatedDurationRealistic == Integer.MAX_VALUE || estimatedDurationRealistic == 0) {
            // Fallback if shortest path is not found or is 0 (which shouldn't happen for valid routes)
            estimatedDurationRealistic = 120 + random.nextInt(180); // Random 2 to 5 hours (120 to 300 minutes)
        }
        newFlight.setEstimatedDurationMinutes(estimatedDurationRealistic);

        System.out.println("\n--- Programando Vuelo " + flightNumber + " ---");
        System.out.println("De: " + (airportManager.findAirport(originCode) != null ? airportManager.findAirport(originCode).getName() : originCode) + " (" + originCode + ")");
        System.out.println("A: " + (airportManager.findAirport(destinationCode) != null ? airportManager.findAirport(destinationCode).getName() : destinationCode) + " (" + destinationCode + ")");
        System.out.println("Avión asignado: " + selectedAirplane.getId() + " (Capacidad: " + selectedAirplane.getCapacity() + ")");
        System.out.println("Salida programada: " + newFlight.getDepartureTime().toLocalDate() + " " + newFlight.getDepartureTime().toLocalTime().withNano(0)); // Show full departure time
        System.out.println("Duración estimada: " + (int) Math.round((double) newFlight.getEstimatedDurationMinutes() / 60.0) + " h. (" + newFlight.getEstimatedDurationMinutes() + " min)");

        // Passengers assignment logic
        int passengersToBoard = random.nextInt(selectedAirplane.getCapacity() / 2) + 1; // At least 1, up to half capacity
        passengersToBoard = Math.min(passengersToBoard, selectedAirplane.getCapacity() - newFlight.getOccupancy()); // Don't exceed plane capacity
        passengersToBoard = Math.min(passengersToBoard, passengerManager.getPassengerCount()); // Don't exceed total available passengers

        System.out.println("Intentando asignar " + passengersToBoard + " pasajeros al vuelo " + flightNumber + ".");

        List<Passenger> allAvailablePassengersFromAVL = null;
        try {
            // ADJUSTED LINE: Using the getter method for proper encapsulation
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
            Collections.shuffle(shuffledPassengers); // Randomize passenger selection

            int actualPassengersAssigned = 0; // Track how many passengers were actually assigned

            for (int i = 0; i < passengersToBoard && i < shuffledPassengers.size(); i++) {
                Passenger p = shuffledPassengers.get(i);
                if (p != null) {
                    try {
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
        }
        System.out.println(newFlight.getOccupancy() + " pasajeros finales a bordo del vuelo " + flightNumber + ".");


        long delaySecondsToTakeoff = Duration.between(LocalDateTime.now(), newFlight.getDepartureTime()).getSeconds();
        if (delaySecondsToTakeoff < 0) delaySecondsToTakeoff = 0; // Ensure delay is not negative if departure time is in the past

        final String fNum = flightNumber;
        final Airplane plane = selectedAirplane;
        final String finalOriginCode = originCode;
        final String finalDestinationCode = destinationCode;
        // Adjusted simulated duration for logic. If shortestPath is in minutes, convert to seconds.
        final long simulatedDurationForLogicSeconds = newFlight.getEstimatedDurationMinutes() * 60 / 100; // Dividing by 100 to make it faster for simulation. Can adjust.

        // Schedule flight takeoff
        scheduler.schedule(() -> {
            try {
                Flight currentFlight = flightScheduleManager.findFlight(fNum);
                if (currentFlight != null) {
                    currentFlight.setStatus(Flight.FlightStatus.IN_PROGRESS);
                    plane.setStatus(AirplaneStatus.IN_FLIGHT);

                    // --- THE FIX IS HERE ---
                    // Before setting to null, set it to the origin airport code.
                    // The plane is AT the origin airport before takeoff.
                    // You set it to null *after* it's in the air, but the error happens
                    // when it's *about to take off* and its location is still the origin.
                    // Then, when it lands, you set it to the destination.
                    // So, if an exception happens *before* it lands, and you previously set it to null,
                    // the next flight trying to pick it up might find its location as null.

                    // Debugging: What's the plane's location right before takeoff?
                    System.out.println("DEBUG (FlightSimulator): Antes del despegue - Avión " + plane.getId() + " ubicación: " + plane.getCurrentLocationAirportCode() + ". Intentando despegar de: " + finalOriginCode);

                    // The logic should be:
                    // 1. Plane is at origin (should be already set during airplane creation or previous landing)
                    // 2. Flight starts, plane status changes to IN_FLIGHT.
                    // 3. ONLY after the plane has "left" the airport (conceptually),
                    //    then you can set its location to null (or some "in air" status).
                    // The error is happening because `plane.setCurrentLocationAirportCode(null)`
                    // is called *immediately* after `plane.setStatus(AirplaneStatus.IN_FLIGHT);`,
                    // but the problem occurs if the previous flight that *landed* this plane failed to
                    // set its `currentLocationAirportCode` to the destination.

                    // Let's assume the previous flight *should* have left the plane at its destination.
                    // So, at this point (takeoff), its location *should* be `finalOriginCode`.
                    // The problem arises because if the previous flight ended with an error,
                    // or if the plane was never properly placed at an airport *initially*,
                    // then `currentLocationAirportCode` could be null here.

                    // The error `El código del aeropuerto actual no puede ser nulo o vacío.` is from `Airplane.java:108`
                    // which is YOUR setter: `public void setCurrentLocationAirportCode(String currentLocationAirportCode)`
                    // This means the `null` is coming from *some other part of your code* that calls this setter.

                    // Looking at your `remove` method in `AVL.java`, it might have decremented size incorrectly.
                    // No, the error is definitely in `FlightSimulator`.

                    // The problem is THIS line:
                    // plane.setCurrentLocationAirportCode(null);
                    // This line makes the airplane's location NULL.
                    // Then, *later*, when a *new* flight attempts to find an idle airplane at a specific origin:
                    // `if (airplane.getStatus() == AirplaneStatus.IDLE && airplane.getCurrentLocationAirportCode() != null && airplane.getCurrentLocationAirportCode().equals(originCode))`
                    // This logic will work fine.

                    // The actual error: `ERROR: Excepción inesperada durante la simulación de inicio del vuelo FL509: El código del aeropuerto actual no puede ser nulo o vacío.`
                    // This means `plane.setCurrentLocationAirportCode(null)` IS THE PROBLEM.
                    // You should NOT set the airplane's location to null when it's taking off.
                    // An airplane that's IN_FLIGHT doesn't have a specific *airport* location.
                    // Its location is "in the air, en route from X to Y".
                    // The `currentLocationAirportCode` should *only* represent an airport.

                    // FIX: Remove or comment out the problematic line.
                    // An airplane that is IN_FLIGHT doesn't have a `currentLocationAirportCode`.
                    // You should only set this *after* it lands.

                    // --- ORIGINAL PROBLEM LINE ---
                    // plane.setCurrentLocationAirportCode(null); // <--- THIS LINE IS THE PROBLEM
                    // --- END ORIGINAL PROBLEM LINE ---

                    // The `Airplane` constructor sets `currentLocationAirportCode`.
                    // `addAirplane` uses it.
                    // When a flight is selected, `selectedAirplane.getCurrentLocationAirportCode().equals(originCode)` is checked.
                    // This means the plane *must* have a location at the origin airport before takeoff.

                    // The `IllegalArgumentException` is thrown because `setCurrentLocationAirportCode` is called with `null`.
                    // You are calling it with `null` here, which is what's causing the error.
                    // An airplane is "in the air" during a flight, so its `currentLocationAirportCode` should effectively be "not at an airport".
                    // However, your `setCurrentLocationAirportCode` method *validates* against null.
                    //
                    // Option 1 (Recommended): Remove the problematic line. An in-flight plane simply doesn't have a `currentLocationAirportCode`.
                    // When it lands, you set it to the destination.
                    // This means the `getCurrentLocationAirportCode()` for an `IN_FLIGHT` plane would return its *last known airport*.
                    // This is acceptable if your system uses this field only for "at an airport" state.

                    // Option 2: Introduce a special constant for "in-flight".
                    // For example, `plane.setCurrentLocationAirportCode(Airplane.IN_FLIGHT_CODE);`
                    // But then your AirportManager would need to handle this special code.
                    // For now, let's go with Option 1.

                    // The code causing the error:
                    // plane.setCurrentLocationAirportCode(null); // This is what triggers the IllegalArgumentException

                    // Removed the problematic line.
                    // The airplane's currentLocationAirportCode should *not* be set to null here.
                    // It represents the airport where it *last was* or *will be*.
                    // When it's IN_FLIGHT, this field should ideally reflect its departure airport until it lands.
                    // Or, if it truly means "current physical airport location", then it should be null,
                    // but you need to adjust `setCurrentLocationAirportCode` to allow null for "in-flight" state,
                    // or use a flag. The simplest fix given your validation is to remove the `null` setting.

                    // Debugging: Verify the plane's state just before the exception.
                    System.out.println("DEBUG (FlightSimulator): Avión " + plane.getId() + " status ANTES de cambiar a IN_FLIGHT: " + plane.getStatus() + ", Location: " + plane.getCurrentLocationAirportCode());

                    // The fix is to NOT set the airport code to null upon takeoff.
                    // It should implicitly remain at the origin until it lands, or you introduce a more complex "in-air" state.
                    // Given your current setup, it's best to leave it at the origin and update only upon landing.
                    // The `currentLocationAirportCode` should only represent an actual airport.
                    // If it's in flight, it's not at an airport.

                    // Re-evaluated: The `IllegalArgumentException` is thrown BY `setCurrentLocationAirportCode`.
                    // The line `plane.setCurrentLocationAirportCode(null);` is indeed the *direct cause*.
                    // You added that line. Remove it. An airplane's `currentLocationAirportCode` should only be updated
                    // when it's *at* an airport (initial location, or landed at destination).
                    // When it's in flight, its `currentLocationAirportCode` logically doesn't apply to a *current airport*.
                    // You could leave it as the origin airport, or set it to a special "in-flight" string if you want to.
                    // But setting it to `null` is what triggers *your own validation*.

                } else {
                    System.err.println("Vuelo " + fNum + " no encontrado al intentar iniciar su simulación.");
                }
            } catch (ListException e) {
                System.err.println("ERROR: Durante la simulación de inicio de vuelo " + fNum + ": " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("ERROR: Excepción inesperada durante la simulación de inicio del vuelo " + fNum + ": " + e.getMessage());
                e.printStackTrace();
            }
        }, delaySecondsToTakeoff, TimeUnit.SECONDS);

        // Schedule flight animation (can be at the same time as takeoff or slightly before/after)
        scheduler.schedule(() -> {
            try {
                System.out.println("\n-----------------------------------------------------");
                System.out.println("VUELO EN CURSO ✈️: " + fNum + " (" + plane.getId() + ")");
                System.out.print((airportManager.findAirport(finalOriginCode) != null ? airportManager.findAirport(finalOriginCode).getName() : finalOriginCode) + " >>>>>>>>>>>>>>>>✈️");
                System.out.flush();

                long animationSteps = 30;
                long actualVisualAnimationDurationMillis = 1500; // Total animation time
                long delayPerStep = actualVisualAnimationDurationMillis / animationSteps;

                for (int k = 0; k < animationSteps; k++) {
                    System.out.print(">");
                    System.out.flush();
                    Thread.sleep(delayPerStep);
                }
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + (airportManager.findAirport(finalDestinationCode) != null ? airportManager.findAirport(finalDestinationCode).getName() : finalDestinationCode));
                System.out.println("-----------------------------------------------------");
                System.out.println("¡El vuelo " + fNum + " ha aterrizado en " + (airportManager.findAirport(finalDestinationCode) != null ? airportManager.findAirport(finalDestinationCode).getName() : finalDestinationCode) + "!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupted status
                System.err.println("ERROR: Animación de vuelo " + fNum + " interrumpida: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("ERROR: Excepción inesperada durante la animación del vuelo " + fNum + ": " + e.getMessage());
                e.printStackTrace();
            }
        }, delaySecondsToTakeoff, TimeUnit.SECONDS); // Start animation at takeoff time

        // Schedule flight completion (after takeoff + simulated duration)
        scheduler.schedule(() -> {
            try {
                Flight currentFlight = flightScheduleManager.findFlight(fNum);
                if (currentFlight != null) {
                    currentFlight.setStatus(Flight.FlightStatus.COMPLETED);
                    System.out.println("Vuelo " + fNum + " en estado: COMPLETED.");

                    plane.setStatus(AirplaneStatus.IDLE);
                    // --- DEBUG START ---
                    System.out.println("DEBUG (FlightSimulator): Después de aterrizar - Avión " + plane.getId() + " cambiando ubicación a: '" + finalDestinationCode + "'");
                    // --- DEBUG END ---
                    plane.setCurrentLocationAirportCode(finalDestinationCode); // Plane is now at destination
                    plane.addFlightToHistory(currentFlight); // Add flight to airplane's history
                    System.out.println("Vuelo " + fNum + " ha completado su viaje a " + (airportManager.findAirport(finalDestinationCode) != null ? airportManager.findAirport(finalDestinationCode).getName() : finalDestinationCode) + ". Avión " + plane.getId() + " ubicado en " + (airportManager.findAirport(finalDestinationCode) != null ? airportManager.findAirport(finalDestinationCode).getName() : finalDestinationCode) + ".");

                    // Process passenger arrival
                    LinkedQueue passengersOnFlight = currentFlight.getPassengers();
                    if (passengersOnFlight != null && !passengersOnFlight.isEmpty()) {
                        int initialPassengerCount = passengersOnFlight.size();
                        for (int i = 0; i < initialPassengerCount; i++) {
                            try {
                                Passenger p = (Passenger) passengersOnFlight.deQueue();
                                passengerManager.processFlightCompletion(p, currentFlight);
                            } catch (QueueException e) {
                                System.err.println("ERROR: Fallo al procesar pasajero de vuelo completado " + fNum + ": " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }

                    // Remove flight from scheduled flights (assuming flightScheduleManager holds current/scheduled flights)
                    try {
                        flightScheduleManager.getScheduledFlights().remove(currentFlight);
                    } catch (ListException e) {
                        System.err.println("ERROR: No se pudo remover el vuelo completado " + fNum + " de la lista de vuelos programados: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("ERROR: Vuelo " + fNum + " no encontrado al intentar finalizar su simulación lógica.");
                }

            } catch (Exception e) {
                System.err.println("ERROR: Excepción inesperada durante la finalización lógica del vuelo " + fNum + ": " + e.getMessage());
                e.printStackTrace();
            }
        }, delaySecondsToTakeoff + simulatedDurationForLogicSeconds, TimeUnit.SECONDS);

        flightCounter.incrementAndGet();
        System.out.println("Vuelo " + flightNumber + " programado exitosamente. Vuelos generados hasta ahora: " + flightCounter.get());
    }

    private String formatFlightDetailsForHistory(Flight flight) {
        String formattedDate = flight.getDepartureTime().toLocalDate().toString();
        String formattedTime = flight.getDepartureTime().toLocalTime().withNano(0).toString();

        return String.format("    - Vuelo: %s | De: %s | A: %s | Salida: %s %s | Duración: %d min | Pasajeros: %d/%d | Estado: %s",
                flight.getFlightNumber(),
                flight.getOriginAirportCode(),
                flight.getDestinationAirportCode(),
                formattedDate,
                formattedTime,
                flight.getEstimatedDurationMinutes(),
                flight.getOccupancy(), // Added occupancy for clarity
                flight.getCapacity(),   // Added capacity for clarity
                flight.getStatus()
        );
    }

    public void printAirplaneFlightHistory() {
        System.out.println("\n--- Historial de Vuelos de Aviones ---");
        if (airplanes.isEmpty()) {
            System.out.println("No hay aviones registrados en el sistema.");
            return;
        }
        for (Airplane airplane : airplanes.values()) {
            System.out.printf("Historial del %s (Ubicación actual: %s, Estado: %s):\n",
                    airplane.toString(), airplane.getCurrentLocationAirportCode(), airplane.getStatus());

            LinkedStack history = airplane.getFlightHistory();
            if (history == null || history.isEmpty()) {
                System.out.println("    - No hay vuelos registrados en el historial.");
            } else {
                // Peek at all elements without removing them
                Object[] flightsArray = history.toArray();
                // Iterate from the bottom of the stack (first flight) to the top (most recent)
                for (int i = 0; i < flightsArray.length; i++) {
                    Flight flightInHistory = (Flight) flightsArray[i];
                    System.out.println(formatFlightDetailsForHistory(flightInHistory));
                }
            }
        }
    }


    public void startSimulation(long flightGenerationIntervalSeconds, long totalSimulationDurationSeconds) {
        System.out.println("Configurando simulación:\n" +
                "  - Intervalo de generación de vuelos: " + flightGenerationIntervalSeconds + " segundos\n" +
                "  - Duración total de la simulación: " + totalSimulationDurationSeconds + " segundos (" + (totalSimulationDurationSeconds / 60) + " minutos)\n" +
                "  - La simulación finalizará automáticamente después de " + totalSimulationDurationSeconds + " segundos, o cuando se alcance el límite de vuelos generados.");

        System.out.println("--- INICIANDO SIMULACIÓN DE VUELOS ---");

        // Schedule repeated flight generation
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Check if the limit has been reached, but allow existing scheduled tasks to complete
                if (flightCounter.get() < MAX_FLIGHTS_TO_GENERATE) {
                    generateRandomFlightBasedOnRules();
                } else {
                    // Stop generating new flights once the limit is reached
                    if (!flightLimitMessagePrinted) { // Print warning only once
                        System.out.println("\nADVERTENCIA: Límite de vuelos generados (" + MAX_FLIGHTS_TO_GENERATE + ") alcanzado. Deteniendo la generación de nuevos vuelos.");
                        flightLimitMessagePrinted = true;
                    }
                }
            } catch (ListException | QueueException | TreeException e) {
                System.err.println("ERROR FATAL: Error al generar vuelo aleatorio: " + e.getMessage());
                e.printStackTrace();
                shutdownSimulation(); // Consider shutting down on fatal errors
            } catch (Exception e) {
                System.err.println("ERROR FATAL: Excepción inesperada durante la generación de vuelos: " + e.getMessage());
                e.printStackTrace();
                shutdownSimulation(); // Consider shutting down on fatal errors
            }
        }, 0, flightGenerationIntervalSeconds, TimeUnit.SECONDS);

        // Schedule simulation shutdown after totalSimulationDurationSeconds
        scheduler.schedule(this::shutdownSimulation, totalSimulationDurationSeconds, TimeUnit.SECONDS);
    }

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
                Thread.currentThread().interrupt(); // Restore the interrupted status
                scheduler.shutdownNow();
            }
        }
        System.out.println("Simulación finalizada.");
        printSimulationSummary(); // Print summary after shutdown
    }

    public void printSimulationSummary() {
        System.out.println("\n--- Resumen de la Simulación ---");
        System.out.println("Vuelos generados: " + flightCounter.get());
        try {
            System.out.println("Vuelos programados actualmente: " + flightScheduleManager.getScheduledFlights().size());
        } catch (ListException e) {
            System.err.println("Error al obtener el tamaño de vuelos programados: " + e.getMessage());
        }

        // You can add more summary statistics here if needed
        // e.g., printAirplaneFlightHistory(), passenger counts, etc.
        printAirplaneFlightHistory();
        System.out.println("\nTotal de pasajeros registrados en el sistema: " + passengerManager.getPassengerCount());
    }
}