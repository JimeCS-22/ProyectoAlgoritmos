package ucr.proyectoalgoritmos.Domain.flight;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.airplane.Airplane;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.passanger.Passenger;
import ucr.proyectoalgoritmos.Domain.passanger.PassengerManager;
import ucr.proyectoalgoritmos.Domain.stack.StackException;
import ucr.proyectoalgoritmos.route.RouteManager;

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
    private RouteManager routeManager;

    private Map<String, Airplane> airplanes;
    private ScheduledExecutorService scheduler;
    private Random random;

    public FlightSimulator() throws ListException {
        this.airportManager = new AirportManager();
        this.routeManager = new RouteManager();
        this.passengerManager = new PassengerManager();
        this.flightScheduleManager = new FlightScheduleManager(this.airportManager, this.routeManager);

        this.airplanes = new HashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.random = new Random();

        // 1. System Initialization
        initializeSystem();
    }

    private void initializeSystem() throws ListException {
        System.out.println("--- System Initialization ---");

        // 1.1 Load Airports
        // This method will now handle both file loading and fallback to defaults if the file is missing/malformed.
        loadAirportsFromFile("airports.json");

        // NEW STEP: Add all loaded airports as vertices to the graph
        System.out.println("[INIT] Adding airports as graph vertices...");
        try {
            DoublyLinkedList allLoadedAirports = airportManager.getAllAirports();
            if (allLoadedAirports != null && !allLoadedAirports.isEmpty()) {
                for (int i = 0; i < allLoadedAirports.size(); i++) {
                    Airport airport = (Airport) allLoadedAirports.get(i);
                    routeManager.getGraph().addVertex(airport.getCode()); // Add each airport's code as a vertex
                }
                System.out.println("[INIT] All " + routeManager.getGraph().getNumVertices() + " airports added as graph vertices.");
            } else {
                System.out.println("[WARN] No airports found in AirportManager to add as graph vertices.");
            }
        } catch (ListException e) {
            System.err.println("[ERROR] Failed to add airports as graph vertices: " + e.getMessage());
        }


        // 1.2 Generate Random Routes (after airports are loaded AND added as graph vertices)
        // Ensure there are enough airports in the *graph* to generate routes
        if (routeManager.getGraph().getNumVertices() < 2) { // Check graph's vertex count
            System.err.println("[ERROR] Not enough airports (vertices) in the graph to generate routes. Please ensure at least 2 airports are loaded.");
        } else {
            System.out.println("[INIT] Generating random routes between airports...");
            // Adjust min/max routes and weights as desired
            routeManager.getGraph().generateRandomRoutes(3, 7, 100, 3000); // Between 3 and 7 routes per airport, 100-3000 weight
            System.out.println("[INIT] Random routes generated.");
        }


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

    // Helper to load airports from a CSV file
    private void loadAirportsFromFile(String filename) {
        System.out.println("[INIT] Attempting to load airports from " + filename + "...");
        int loadedCount = 0;
        try (Reader reader = Files.newBufferedReader(Paths.get(filename))) {
            Gson gson = new Gson();
            // Use TypeToken to correctly deserialize a List of Airport objects
            List<Airport> airportListFromFile = gson.fromJson(reader, new TypeToken<List<Airport>>(){}.getType());

            if (airportListFromFile != null) {
                for (Airport airport : airportListFromFile) {
                    if (loadedCount >= 20) {
                        System.out.println("[INIT] Reached max 20 airports from file. Stopping.");
                        break;
                    }
                    try {
                        airportManager.createAirport(airport.getCode(), airport.getName(), airport.getCountry());
                        loadedCount++;
                    } catch (ListException e) {
                        System.err.println("[ERROR] Failed to create airport '" + airport.getCode() + "' from file: " + e.getMessage());
                    }
                }
            }

            if (loadedCount < 15) {
                System.out.println("[WARN] Only " + loadedCount + " airports loaded from file. Minimum 15 suggested.");
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Could not read airports file '" + filename + "': " + e.getMessage());
            System.out.println("[INFO] Generating some default airports instead.");
            // Fallback: create some default airports if file fails
            addDefaultAirports();
        } finally {
            try {
                if (airportManager.getAllAirports().isEmpty()) {
                    System.out.println("[INFO] No airports loaded/created, adding emergency default airports.");
                    addDefaultAirports();
                }
                System.out.println("[INIT] Total airports loaded/created: " + airportManager.getAllAirports().size() + ".");
            } catch (ListException e) {
                System.err.println("[ERROR] Problem getting final airport count: " + e.getMessage());
            }
        }
    }

    // New helper method to add default airports
    private void addDefaultAirports() {
        try {
            // Only add if they don't already exist or if airport list is empty
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
        } catch (ListException ex) {
            System.err.println("[ERROR] Failed to create default airports: " + ex.getMessage());
        }
    }


    public void addAirplane(String id, int capacity, String initialLocationAirportCode) {
        try {
            // Ensure the airport exists before adding the airplane
            if (airportManager.findAirport(initialLocationAirportCode) != null) {
                if (!airplanes.containsKey(id)) { // Prevent adding duplicate airplanes
                    airplanes.put(id, new Airplane(id, capacity, initialLocationAirportCode));
                    System.out.println("[INFO] Airplane " + id + " added at " + initialLocationAirportCode + ".");
                } else {
                    System.out.println("[WARN] Airplane " + id + " already exists. Skipping.");
                }
            } else {
                System.err.println("[ERROR] Cannot add airplane " + id + ". Initial location " + initialLocationAirportCode + " is not a valid airport.");
            }
        } catch (ListException e) { // Catch ListException from findAirport
            System.err.println("[ERROR] Problem finding airport for new airplane: " + e.getMessage());
        }
    }


    // 2. Generación de vuelos
    private void generateRandomFlightBasedOnRules() {
        // Handle all exceptions internally to keep the scheduler running
        try {
            System.out.println("\n--- Generating new flight request ---");

            // Find the 5 airports with the most routes
            DoublyLinkedList allAirportsList = airportManager.getAllAirports();
            if (allAirportsList.isEmpty()) {
                System.out.println("[WARN] No airports available to generate flights. Cannot generate flight.");
                return;
            }

            // Convert DoublyLinkedList to ArrayList for easier sorting
            List<Airport> topAirports = new ArrayList<>();
            for (int i = 0; i < allAirportsList.size(); i++) {
                Airport airport = (Airport) allAirportsList.get(i); // get() might throw ListException from DoublyLinkedList
                if (airport.getStatus() == Airport.AirportStatus.ACTIVE) { // Only consider active airports
                    topAirports.add(airport);
                }
            }

            if (topAirports.isEmpty()) {
                System.out.println("[WARN] No active airports to generate flights. Cannot generate flight.");
                return;
            }

            // Sort by outgoing route count (descending)
            // CORRECTED: Use airport.getCode() to get the airport code for the graph lookup
            Collections.sort(topAirports, Comparator.comparingInt(airport -> routeManager.getGraph().getOutgoingRouteCount(airport.getCode())).reversed());


            List<Airport> selectedOrigins = new ArrayList<>();
            // Select up to 5 of the top airports (or fewer if fewer than 5 active airports)
            for (int i = 0; i < Math.min(5, topAirports.size()); i++) {
                selectedOrigins.add(topAirports.get(i));
            }

            if (selectedOrigins.isEmpty()) {
                System.out.println("[WARN] Could not select suitable origin airports. Cannot generate flight.");
                return;
            }

            // Pick a random origin from the selected top airports
            Airport originAirport = selectedOrigins.get(random.nextInt(selectedOrigins.size()));
            String originCode = originAirport.getCode();

            // Find a random destination that is not the origin and has a route
            List<String> allAirportCodes = routeManager.getGraph().getAllAirportCodes();
            String destinationCode = null;
            int attempts = 0; // Safeguard against infinite loops if no valid routes
            final int MAX_ATTEMPTS = 50; // Max attempts to find a valid destination

            while (destinationCode == null || destinationCode.equals(originCode) ||
                    routeManager.calculateShortestRoute(originCode, destinationCode) == Integer.MAX_VALUE) {

                if (allAirportCodes.isEmpty()) { // No airports at all to pick destination from
                    System.out.println("[WARN] No destination airports available. Cannot generate flight.");
                    return;
                }

                if (attempts >= MAX_ATTEMPTS) {
                    System.out.println("[WARN] Failed to find a valid destination with a route from " + originCode + " after " + MAX_ATTEMPTS + " attempts. Cannot generate flight.");
                    return;
                }

                destinationCode = allAirportCodes.get(random.nextInt(allAirportCodes.size()));

                attempts++;
            }

            // Select random airplane (prefer one at the origin airport if available)
            Airplane selectedAirplane = null;
            // Try to find an airplane currently at the origin airport
            for (Airplane airplane : airplanes.values()) {
                if (airplane.getCurrentLocationAirportCode().equals(originCode)) {
                    selectedAirplane = airplane;
                    break;
                }
            }
            // If no airplane at origin, pick any available
            if (selectedAirplane == null) {
                List<Airplane> availableAirplanes = new ArrayList<>(airplanes.values());
                if (availableAirplanes.isEmpty()) {
                    System.out.println("[WARN] No airplanes available to generate flights. Cannot generate flight.");
                    return;
                }
                selectedAirplane = availableAirplanes.get(random.nextInt(availableAirplanes.size()));
            }

            // Generate random capacity for the flight
            int capacity = selectedAirplane.getCapacity(); // Use airplane's capacity as max
            if (capacity < 100) capacity = 100; // Minimum capacity for a flight
            capacity = random.nextInt(capacity / 2) + capacity / 2; // Make it 50%-100% of airplane's capacity

            String flightNumber = "FL" + (random.nextInt(900) + 100); // e.g., FL123
            LocalDateTime departureTime = LocalDateTime.now().plusHours(random.nextInt(12) + 1); // 1-12 hours from now

            // Create the flight
            flightScheduleManager.createFlight(flightNumber, originCode, destinationCode, departureTime, capacity);

            // Simulate some passengers buying tickets
            int passengersBuyingTickets = random.nextInt(capacity / 2) + 1; // 1 to half capacity of the flight
            System.out.println("[INFO] Attempting to process tickets for " + passengersBuyingTickets + " passengers.");
            for (int i = 0; i < passengersBuyingTickets; i++) {
                // Pick a random passenger (for simplicity, from existing ones)
                String passengerId = "100" + (random.nextInt(5) + 1); // Assuming 1001-1005 exist
                Passenger p = passengerManager.searchPassenger(passengerId);
                if (p != null) {
                    flightScheduleManager.processTicketPurchase(p, originCode, destinationCode);
                } else {
                    System.err.println("[ERROR] Passenger " + passengerId + " not found for ticket purchase simulation.");
                }
            }

            // Simulate the flight with the selected airplane
            flightScheduleManager.simulateFlight(flightNumber, airportManager, passengerManager, selectedAirplane);

        } catch (ListException | StackException e) {
            System.err.println("[ERROR] Error during random flight generation: " + e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected exceptions to prevent scheduler from stopping
            System.err.println("[FATAL ERROR] An unexpected error occurred during flight generation: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Start and stop simulation
    public void startSimulation(long flightGenerationIntervalSeconds, long simulationDurationSeconds) {
        System.out.println("\n--- Starting Flight Simulation ---");
        System.out.println("Flights will be generated every " + flightGenerationIntervalSeconds + " seconds.");
        System.out.println("Simulation will run for " + simulationDurationSeconds + " seconds.");

        // Schedule flight generation
        scheduler.scheduleAtFixedRate(this::generateRandomFlightBasedOnRules, 1, flightGenerationIntervalSeconds, TimeUnit.SECONDS);

        // Schedule the stop of the simulation
        scheduler.schedule(() -> {
            try {
                stopSimulation();
            } catch (ListException e) {
                System.err.println("[ERROR] Error stopping simulation: " + e.getMessage());
            }
        }, simulationDurationSeconds, TimeUnit.SECONDS);
    }

    public void stopSimulation() throws ListException {
        System.out.println("\n--- Stopping Flight Simulation ---");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) { // Give it a bit more time to finish tasks
                scheduler.shutdownNow(); // Force shutdown if tasks don't complete
                System.out.println("[WARN] Forced shutdown of scheduler. Some tasks might not have completed.");
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt(); // Restore the interrupted status
            System.err.println("[ERROR] Simulation shutdown was interrupted.");
        }
        System.out.println("\n--- Flight Simulation Stopped ---");

        // Print all airplane histories at the end
        System.out.println("\n--- Airplane Flight Histories ---");
        if (airplanes.isEmpty()) {
            System.out.println("No airplanes in the system.");
        } else {
            airplanes.values().forEach(Airplane::printFlightHistory);
        }
        System.out.println("---------------------------------");


        // Print passenger histories (optional)
        System.out.println("\n--- Passenger Flight Histories ---");
        if (passengerManager.getPassengerCount() == 0) {
            System.out.println("No passengers registered or with flight history.");
        } else {
            passengerManager.showFlightHistory("1001");
            passengerManager.showFlightHistory("1002");
            passengerManager.showFlightHistory("1003");
        }
        System.out.println("----------------------------------");


        // List final states of airports and flights
        System.out.println("\n--- Final Airport Status ---");
        airportManager.listAirports(true, true);
        System.out.println("\n--- Final Flight Schedule ---");
        flightScheduleManager.listAllFlights();
        System.out.println("-----------------------------");
    }

    // Main method to run the simulation
    public static void main(String[] args) {
        try {
            FlightSimulator simulator = new FlightSimulator();
            simulator.startSimulation(10, 60);

        } catch (ListException e) {
            System.err.println("[FATAL ERROR] Failed to initialize Flight Simulator: " + e.getMessage());
            e.printStackTrace();
        }
    }
}