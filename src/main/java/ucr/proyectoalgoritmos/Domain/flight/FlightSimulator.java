package ucr.proyectoalgoritmos.Domain.flight; // Adjust package as needed

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.airplane.Airplane;
import ucr.proyectoalgoritmos.Domain.airplane.Airplane.AirplaneStatus; // Import AirplaneStatus enum
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList; // Ensure this is imported for type consistency
import ucr.proyectoalgoritmos.Domain.passanger.Passenger;
import ucr.proyectoalgoritmos.Domain.passanger.PassengerManager;
import ucr.proyectoalgoritmos.Domain.stack.StackException;
import ucr.proyectoalgoritmos.route.RouteManager; // Import RouteManager
import ucr.proyectoalgoritmos.route.Route; // Import your Route class

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
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

public class FlightSimulator {
    private AirportManager airportManager;
    private PassengerManager passengerManager;
    private FlightScheduleManager flightScheduleManager;
    private RouteManager routeManager; // Make sure this is initialized correctly

    private Map<String, Airplane> airplanes; // Stores all airplanes
    private ScheduledExecutorService scheduler;
    private Random random;

    public FlightSimulator() throws ListException, IOException {
        this.airportManager = new AirportManager();
        // Pass AirportManager to RouteManager so it can resolve airport codes to indices
        this.routeManager = new RouteManager(airportManager); // <--- IMPORTANT: Pass AirportManager
        this.passengerManager = new PassengerManager();
        // Pass the already initialized AirportManager and RouteManager
        this.flightScheduleManager = new FlightScheduleManager(this.airportManager, this.routeManager);

        this.airplanes = new HashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2); // Increased thread pool size for more concurrency
        this.random = new Random();

        // 1. System Initialization
        initializeSystem();
    }

    private void initializeSystem() throws ListException, IOException {
        System.out.println("--- System Initialization ---");

        // 1.1 Load Airports
        loadAirportsFromFile("airports.json");

        // Add all loaded airports as vertices to the graph (no try-catch here, ListException propagates)
        System.out.println("[INIT] Adding airports as graph vertices...");
        DoublyLinkedList allLoadedAirports = airportManager.getAllAirports();
        if (allLoadedAirports != null && !allLoadedAirports.isEmpty()) {
            for (int i = 0; i < allLoadedAirports.size(); i++) {
                Airport airport = (Airport) allLoadedAirports.get(i); // Explicit cast
                routeManager.getGraph().addVertex(airport.getCode()); // Add each airport's code as a vertex
            }
            System.out.println("[INIT] All " + routeManager.getGraph().getNumVertices() + " airports added as graph vertices.");
        } else {
            System.out.println("[WARN] No airports found in AirportManager to add as graph vertices.");
        }

        // --- START NEW/MODIFIED CODE FOR ROUTE LOADING ---
        // 1.2 Load Routes from routes.json (instead of generating random ones)
        // Ensure that airports (vertices) are added to the graph BEFORE loading routes (edges).
        if (routeManager.getGraph().getNumVertices() < 2) {
            System.err.println("[ERROR] Not enough airports (vertices) in the graph to load routes. Please ensure at least 2 airports are loaded.");
        } else {
            System.out.println("[INIT] Loading routes from 'routes.json'...");
            try {
                // Call the new method in RouteManager to load routes
                routeManager.loadRoutesFromJson("routes.json");
                System.out.println("[INIT] Routes loaded from 'routes.json'. Total routes: " + routeManager.getGraph().getNumEdges());
            } catch (IOException e) {
                System.err.println("[ERROR] Failed to load routes from 'routes.json': " + e.getMessage());
                // Fallback: If routes.json fails, generate random routes as a last resort
                System.out.println("[INFO] Generating random routes as a fallback due to file loading error.");
                routeManager.getGraph().generateRandomRoutes(3, 7, 100, 3000);
            }
        }
        // --- END NEW/MODIFIED CODE FOR ROUTE LOADING ---


        // Add some initial airplanes
        addAirplane("AIR001", 200, "SJO");
        addAirplane("AIR002", 150, "MIA");
        addAirplane("AIR003", 250, "LIR");
        addAirplane("AIR004", 100, "LAX");
        addAirplane("AIR005", 300, "JFK");
        System.out.println("[INIT] Initial airplanes added.");

        // Add some initial passengers
        passengerManager.registerPassenger("1001", "Alice Smith", "USA");
        passengerManager.registerPassenger("1002", "Bob Johnson", "Canada");
        passengerManager.registerPassenger("1003", "Carlos Garcia", "Mexico");
        passengerManager.registerPassenger("1004", "Diana Miller", "UK");
        passengerManager.registerPassenger("1005", "Eve Brown", "Germany");
        System.out.println("[INIT] Initial passengers registered.");

        System.out.println("--- System Initialization Complete ---");
    }

    private void loadAirportsFromFile(String filename) throws ListException {
        System.out.println("[INIT] Attempting to load airports from " + filename + "...");
        int loadedCount = 0;
        try (Reader reader = Files.newBufferedReader(Paths.get(filename))) {
            Gson gson = new Gson();
            List<Airport> airportListFromFile = gson.fromJson(reader, new TypeToken<List<Airport>>() {
            }.getType());

            if (airportListFromFile != null) {
                for (Airport airport : airportListFromFile) {
                    if (loadedCount >= 20) { // Limit to 20 airports from file
                        System.out.println("[INIT] Reached max 20 airports from file. Stopping.");
                        break;
                    }
                    airportManager.createAirport(airport.getCode(), airport.getName(), airport.getCountry());
                    loadedCount++;
                }
            }

            if (loadedCount < 15) {
                System.out.println("[WARN] Only " + loadedCount + " airports loaded from file. Minimum 15 suggested for robust simulation.");
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Could not read airports file '" + filename + "': " + e.getMessage());
            System.out.println("[INFO] Generating some default airports instead.");
            addDefaultAirports(); // Fallback to default airports if file loading fails
        } finally {
            if (airportManager.getAllAirports().isEmpty()) { // Final check if no airports exist
                System.out.println("[INFO] No airports loaded/created, adding emergency default airports.");
                addDefaultAirports();
            }
            System.out.println("[INIT] Total airports loaded/created: " + airportManager.getAllAirports().size() + ".");
        }
    }

    private void addDefaultAirports() throws ListException {
        // Only add if not already present, to avoid duplicates if called multiple times
        if (airportManager.findAirport("SJO") == null)
            airportManager.createAirport("SJO", "Juan Santamaría", "Costa Rica");
        if (airportManager.findAirport("LIR") == null)
            airportManager.createAirport("LIR", "Daniel Oduber Quirós", "Costa Rica");
        if (airportManager.findAirport("MIA") == null)
            airportManager.createAirport("MIA", "Miami International", "USA");
        if (airportManager.findAirport("JFK") == null)
            airportManager.createAirport("JFK", "John F. Kennedy", "USA");
        if (airportManager.findAirport("LAX") == null)
            airportManager.createAirport("LAX", "Los Angeles International", "USA");
        if (airportManager.findAirport("CDG") == null)
            airportManager.createAirport("CDG", "Charles de Gaulle", "France");
        if (airportManager.findAirport("FRA") == null)
            airportManager.createAirport("FRA", "Frankfurt Airport", "Germany");
        if (airportManager.findAirport("DXB") == null)
            airportManager.createAirport("DXB", "Dubai International", "UAE");
        if (airportManager.findAirport("NRT") == null)
            airportManager.createAirport("NRT", "Narita International", "Japan");
        if (airportManager.findAirport("SYD") == null)
            airportManager.createAirport("SYD", "Sydney Airport", "Australia");
        if (airportManager.findAirport("ORD") == null)
            airportManager.createAirport("ORD", "O'Hare International", "USA");
        if (airportManager.findAirport("PEK") == null)
            airportManager.createAirport("PEK", "Beijing Capital", "China");
        if (airportManager.findAirport("IST") == null)
            airportManager.createAirport("IST", "Istanbul Airport", "Turkey");
        if (airportManager.findAirport("MEX") == null)
            airportManager.createAirport("MEX", "Mexico City Int'l", "Mexico");
        if (airportManager.findAirport("LIM") == null)
            airportManager.createAirport("LIM", "Jorge Chávez Int'l", "Peru");
    }

    public void addAirplane(String id, int capacity, String initialLocationAirportCode) throws ListException {
        if (airportManager.findAirport(initialLocationAirportCode) != null) {
            if (!airplanes.containsKey(id)) {
                airplanes.put(id, new Airplane(id, capacity, initialLocationAirportCode));
                System.out.println("[INFO] Airplane '" + id + "' (Capacity: " + capacity + ") created at " + initialLocationAirportCode);
            } else {
                System.out.println("[WARN] Airplane " + id + " already exists. Skipping.");
            }
        } else {
            System.err.println("[ERROR] Cannot add airplane " + id + ". Initial location " + initialLocationAirportCode + " is not a valid airport.");
        }
    }

    private void generateRandomFlightBasedOnRules() throws ListException, StackException {
        System.out.println("\n--- Generating new flight request ---");

        // Find the 5 airports with the most routes
        DoublyLinkedList allAirportsList = airportManager.getAllAirports();
        if (allAirportsList.isEmpty()) {
            System.out.println("[WARN] No airports available to generate flights. Cannot generate flight.");
            return;
        }

        List<Airport> activeAirports = new ArrayList<>();
        for (int i = 0; i < allAirportsList.size(); i++) {
            Airport airport = (Airport) allAirportsList.get(i);
            if (airport.getStatus() == Airport.AirportStatus.ACTIVE) {
                activeAirports.add(airport);
            }
        }

        if (activeAirports.isEmpty()) {
            System.out.println("[WARN] No active airports to generate flights. Cannot generate flight.");
            return;
        }

        // Sort by outgoing route count (descending)
        Collections.sort(activeAirports, new Comparator<Airport>() {
            @Override
            public int compare(Airport airport1, Airport airport2) {
                int count1 = routeManager.getGraph().getOutgoingRouteCount(airport1.getCode());
                int count2 = routeManager.getGraph().getOutgoingRouteCount(airport2.getCode());
                return Integer.compare(count2, count1); // Descending order
            }
        });

        List<Airport> selectedOrigins = new ArrayList<>();
        for (int i = 0; i < Math.min(5, activeAirports.size()); i++) {
            selectedOrigins.add(activeAirports.get(i));
        }

        if (selectedOrigins.isEmpty()) {
            System.out.println("[WARN] Could not select suitable origin airports. Cannot generate flight.");
            return;
        }

        // Pick a random origin from the selected top airports
        Airport originAirport = selectedOrigins.get(random.nextInt(selectedOrigins.size()));
        String originCode = originAirport.getCode();

        // Find a random destination that is not the origin and has a route
        SinglyLinkedList allAirportCodesFromGraph;
        try {
            allAirportCodesFromGraph = routeManager.getGraph().getAllAirportCodes();
        } catch (ListException e) {
            System.err.println("[ERROR] Failed to get airport codes from graph: " + e.getMessage());
            return; // Cannot proceed without airport codes
        }


        String destinationCode = null;
        int attempts = 0;
        final int MAX_ATTEMPTS = 50;

        // Loop to find a valid destination with a route
        while (destinationCode == null || destinationCode.equals(originCode) ||
                routeManager.calculateShortestRoute(originCode, destinationCode) == Integer.MAX_VALUE) {

            // Check if there are any airport codes to select from
            if (allAirportCodesFromGraph.isEmpty()) {
                System.out.println("[WARN] No destination airports available in graph to select from. Cannot generate flight.");
                return;
            }

            // Prevent infinite loop if no valid destination can be found
            if (attempts >= MAX_ATTEMPTS) {
                System.out.println("[WARN] Failed to find a valid destination with a route from " + originCode + " after " + MAX_ATTEMPTS + " attempts. Cannot generate flight.");
                return;
            }

            // Get a random airport code from the SinglyLinkedList and cast it to String
            destinationCode = (String) allAirportCodesFromGraph.get(random.nextInt(allAirportCodesFromGraph.size()));
            attempts++;
        }

        // --- MODIFIED LOGIC FOR AIRPLANE SELECTION AND FLIGHT SIMULATION ---
        // Find an IDLE airplane at the origin airport
        Airplane selectedAirplane = null;
        for (Airplane airplane : airplanes.values()) {
            if (airplane.getCurrentLocationAirportCode() != null && // Check if location is not null
                    airplane.getCurrentLocationAirportCode().equals(originCode) &&
                    airplane.getStatus() == AirplaneStatus.IDLE) { // Ensure it's IDLE and at the origin
                selectedAirplane = airplane;
                break; // Found one, use it
            }
        }

        String flightNumber = "FL" + (random.nextInt(900) + 100);
        LocalDateTime departureTime = LocalDateTime.now().plusHours(random.nextInt(12) + 1);
        int capacity;

        if (selectedAirplane == null) {
            System.out.println("[WARN] No IDLE airplane available at " + originCode + " for flight to " + destinationCode + ". Flight will be created but not simulated immediately.");
            // Create the flight schedule anyway, so it exists and passengers can be assigned
            capacity = 150 + random.nextInt(150); // Default capacity if no plane selected yet
            flightScheduleManager.createFlight(flightNumber, originCode, destinationCode, departureTime, capacity);

        } else {
            // If an airplane is found and is IDLE, proceed with flight creation
            capacity = selectedAirplane.getCapacity(); // Use selected airplane's capacity
            capacity = random.nextInt(capacity / 2) + capacity / 2; // Adjust capacity relative to selected plane for varied occupancy

            // Create the flight schedule
            flightScheduleManager.createFlight(flightNumber, originCode, destinationCode, departureTime, capacity);

            // Now, trigger the flight simulation with the selected airplane
            System.out.println("[SIM] Initiating simulation for flight " + flightNumber + " with airplane " + selectedAirplane.getId() + " from " + originCode + " to " + destinationCode);
            flightScheduleManager.simulateFlight(flightNumber, airportManager, passengerManager, selectedAirplane);
        }

        // Simulate some passengers buying tickets for the *created* flight (whether simulated or not)
        // This part needs to happen *after* flight creation.
        int passengersBuyingTickets = random.nextInt(capacity / 2) + 1; // Base on the *final* flight capacity
        System.out.println("[INFO] Attempting to process tickets for " + passengersBuyingTickets + " passengers for flight " + flightNumber);
        for (int i = 0; i < passengersBuyingTickets; i++) {
            String passengerId = "100" + (random.nextInt(5) + 1); // Assuming passenger IDs are 1001-1005
            Passenger p = passengerManager.searchPassenger(passengerId);
            if (p != null) {
                flightScheduleManager.processTicketPurchase(p, originCode, destinationCode);
            } else {
                System.err.println("[ERROR] Passenger " + passengerId + " not found for ticket purchase simulation.");
            }
        }
    }

    public void startSimulation(long flightGenerationIntervalSeconds, long simulationDurationSeconds) {
        System.out.println("\n--- Starting Flight Simulation ---");
        System.out.println("Flights will be generated every " + flightGenerationIntervalSeconds + " seconds.");
        System.out.println("Simulation will run for " + simulationDurationSeconds + " seconds.");

        scheduler.scheduleAtFixedRate(() -> {
            try {
                generateRandomFlightBasedOnRules();
            } catch (ListException | StackException e) {
                System.err.println("[RUNTIME ERROR] Error generating random flight: " + e.getMessage());
                // For a scheduled task, you might want more sophisticated error handling
                // For now, it will just log and continue trying on the next interval
            }
        }, 1, flightGenerationIntervalSeconds, TimeUnit.SECONDS);

        scheduler.schedule(() -> {
            try {
                stopSimulation();
            } catch (ListException e) {
                System.err.println("[RUNTIME ERROR] Error during simulation shutdown: " + e.getMessage());
            }
        }, simulationDurationSeconds, TimeUnit.SECONDS);
    }

    public void stopSimulation() throws ListException {
        System.out.println("\n--- Stopping Flight Simulation ---");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) { // Give tasks 10 seconds to finish
                scheduler.shutdownNow();
                System.out.println("[WARN] Forced shutdown of scheduler. Some tasks might not have completed.");
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            System.err.println("[ERROR] Simulation shutdown was interrupted.");
            throw new RuntimeException("Simulation shutdown interrupted", e);
        }
        System.out.println("\n--- Flight Simulation Stopped ---");

        System.out.println("\n--- Airplane Flight Histories ---");
        if (airplanes.isEmpty()) {
            System.out.println("No airplanes in the system.");
        } else {
            airplanes.values().forEach(Airplane::printFlightHistory);
        }
        System.out.println("---------------------------------");

        System.out.println("\n--- Passenger Flight Histories ---");
        if (passengerManager.getPassengerCount() == 0) {
            System.out.println("No passengers registered or with flight history.");
        } else {
            // Print histories for the initial passengers
            passengerManager.showFlightHistory("1001");
            passengerManager.showFlightHistory("1002");
            passengerManager.showFlightHistory("1003");
            passengerManager.showFlightHistory("1004");
            passengerManager.showFlightHistory("1005");
        }
        System.out.println("----------------------------------");

        System.out.println("\n--- Final Airport Status ---");
        airportManager.listAirports(true, true); // Assuming listAirports shows active airports and queue sizes
        System.out.println("\n--- Final Flight Schedule ---");
        flightScheduleManager.listAllFlights();
        System.out.println("-----------------------------");
    }

    public static void main(String[] args) {
        try {
            FlightSimulator simulator = new FlightSimulator();
            simulator.startSimulation(10, 60); // Generate every 10s, run for 60s

        } catch (ListException e) {
            System.err.println("[FATAL ERROR] Failed to initialize Flight Simulator due to ListException: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("[FATAL ERROR] Failed to initialize Flight Simulator due to IOException (e.g., airports file issue): " + e.getMessage());
            e.printStackTrace();
        } catch (RuntimeException e) {
            System.err.println("[FATAL ERROR] An unexpected runtime error occurred during simulation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}