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

    // Constructor now throws IOException as initializeSystem might
    public FlightSimulator() throws ListException, IOException {
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

    // initializeSystem now throws IOException and ListException
    private void initializeSystem() throws ListException, IOException {
        System.out.println("--- System Initialization ---");

        // 1.1 Load Airports
        loadAirportsFromFile("airports.json");

        // Add all loaded airports as vertices to the graph (no try-catch here, ListException propagates)
        System.out.println("[INIT] Adding airports as graph vertices...");
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


        // 1.2 Generate Random Routes (after airports are loaded AND added as graph vertices)
        if (routeManager.getGraph().getNumVertices() < 2) {
            System.err.println("[ERROR] Not enough airports (vertices) in the graph to generate routes. Please ensure at least 2 airports are loaded.");
        } else {
            System.out.println("[INIT] Generating random routes between airports...");
            routeManager.getGraph().generateRandomRoutes(3, 7, 100, 3000);
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

    // loadAirportsFromFile now throws ListException. IOException is still handled internally for fallback.
    private void loadAirportsFromFile(String filename) throws ListException {
        System.out.println("[INIT] Attempting to load airports from " + filename + "...");
        int loadedCount = 0;
        try (Reader reader = Files.newBufferedReader(Paths.get(filename))) {
            Gson gson = new Gson();
            List<Airport> airportListFromFile = gson.fromJson(reader, new TypeToken<List<Airport>>() {
            }.getType());

            if (airportListFromFile != null) {
                for (Airport airport : airportListFromFile) {
                    if (loadedCount >= 20) {
                        System.out.println("[INIT] Reached max 20 airports from file. Stopping.");
                        break;
                    }
                    // No try-catch here, ListException from createAirport propagates up
                    airportManager.createAirport(airport.getCode(), airport.getName(), airport.getCountry());
                    loadedCount++;
                }
            }

            if (loadedCount < 15) {
                System.out.println("[WARN] Only " + loadedCount + " airports loaded from file. Minimum 15 suggested.");
            }

        } catch (IOException e) { // Keep this catch for fallback logic
            System.err.println("[ERROR] Could not read airports file '" + filename + "': " + e.getMessage());
            System.out.println("[INFO] Generating some default airports instead.");
            addDefaultAirports();
        } finally {
            // Remove try-catch for ListException here, ListException propagates up
            if (airportManager.getAllAirports().isEmpty()) {
                System.out.println("[INFO] No airports loaded/created, adding emergency default airports.");
                addDefaultAirports();
            }
            System.out.println("[INIT] Total airports loaded/created: " + airportManager.getAllAirports().size() + ".");
        }
    }

    // addDefaultAirports now throws ListException
    private void addDefaultAirports() throws ListException {
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
    }


    // generateRandomFlightBasedOnRules now throws ListException and StackException
    private void generateRandomFlightBasedOnRules() throws ListException, StackException {
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
            Airport airport = (Airport) allAirportsList.get(i);
            if (airport.getStatus() == Airport.AirportStatus.ACTIVE) {
                topAirports.add(airport);
            }
        }

        if (topAirports.isEmpty()) {
            System.out.println("[WARN] No active airports to generate flights. Cannot generate flight.");
            return;
        }

        // Sort by outgoing route count (descending)
        Collections.sort(topAirports, new Comparator<Airport>() {
            @Override
            public int compare(Airport airport1, Airport airport2) {
                // Get outgoing route counts for both airports
                int count1 = routeManager.getGraph().getOutgoingRouteCount(airport1.getCode());
                int count2 = routeManager.getGraph().getOutgoingRouteCount(airport2.getCode());

                // For descending order, compare count2 to count1
                return Integer.compare(count2, count1);
            }
        });

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
        int attempts = 0;
        final int MAX_ATTEMPTS = 50;

        while (destinationCode == null || destinationCode.equals(originCode) ||
                routeManager.calculateShortestRoute(originCode, destinationCode) == Integer.MAX_VALUE) {

            if (allAirportCodes.isEmpty()) {
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
        for (Airplane airplane : airplanes.values()) {
            if (airplane.getCurrentLocationAirportCode().equals(originCode)) {
                selectedAirplane = airplane;
                break;
            }
        }
        if (selectedAirplane == null) {
            List<Airplane> availableAirplanes = new ArrayList<>(airplanes.values());
            if (availableAirplanes.isEmpty()) {
                System.out.println("[WARN] No airplanes available to generate flights. Cannot generate flight.");
                return;
            }
            selectedAirplane = availableAirplanes.get(random.nextInt(availableAirplanes.size()));
        }

        // Generate random capacity for the flight
        int capacity = selectedAirplane.getCapacity();
        if (capacity < 100) capacity = 100;
        capacity = random.nextInt(capacity / 2) + capacity / 2;

        String flightNumber = "FL" + (random.nextInt(900) + 100);
        LocalDateTime departureTime = LocalDateTime.now().plusHours(random.nextInt(12) + 1);

        // Create the flight
        flightScheduleManager.createFlight(flightNumber, originCode, destinationCode, departureTime, capacity);

        // Simulate some passengers buying tickets
        int passengersBuyingTickets = random.nextInt(capacity / 2) + 1;
        System.out.println("[INFO] Attempting to process tickets for " + passengersBuyingTickets + " passengers.");
        for (int i = 0; i < passengersBuyingTickets; i++) {
            String passengerId = "100" + (random.nextInt(5) + 1);
            Passenger p = passengerManager.searchPassenger(passengerId);
            if (p != null) {
                flightScheduleManager.processTicketPurchase(p, originCode, destinationCode);
            } else {
                System.err.println("[ERROR] Passenger " + passengerId + " not found for ticket purchase simulation.");
            }
        }

        // Simulate the flight with the selected airplane
        flightScheduleManager.simulateFlight(flightNumber, airportManager, passengerManager, selectedAirplane);
    }


    // startSimulation now declares RuntimeException for the lambda
    public void startSimulation(long flightGenerationIntervalSeconds, long simulationDurationSeconds) {
        System.out.println("\n--- Starting Flight Simulation ---");
        System.out.println("Flights will be generated every " + flightGenerationIntervalSeconds + " seconds.");
        System.out.println("Simulation will run for " + simulationDurationSeconds + " seconds.");

        // Schedule flight generation (lambda needs to handle checked exceptions or wrap them)
        scheduler.scheduleAtFixedRate(() -> {
            try {
                generateRandomFlightBasedOnRules();
            } catch (ListException | StackException e) {
                // Wrap checked exceptions in RuntimeException for the Runnable context
                throw new RuntimeException("Error generating random flight: " + e.getMessage(), e);
            }
        }, 1, flightGenerationIntervalSeconds, TimeUnit.SECONDS);

        // Schedule the stop of the simulation (lambda needs to handle checked exceptions or wrap them)
        scheduler.schedule(() -> {
            try {
                stopSimulation();
            } catch (ListException e) {
                // Wrap checked exceptions in RuntimeException for the Runnable context
                throw new RuntimeException("Error during simulation shutdown: " + e.getMessage(), e);
            }
        }, simulationDurationSeconds, TimeUnit.SECONDS);
    }

    // stopSimulation now throws ListException and RuntimeException (for InterruptedException)
    public void stopSimulation() throws ListException {
        System.out.println("\n--- Stopping Flight Simulation ---");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                System.out.println("[WARN] Forced shutdown of scheduler. Some tasks might not have completed.");
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt(); // Restore the interrupted status
            System.err.println("[ERROR] Simulation shutdown was interrupted.");
            throw new RuntimeException("Simulation shutdown interrupted", e); // Wrap in RuntimeException
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
            passengerManager.showFlightHistory("1001");
            passengerManager.showFlightHistory("1002");
            passengerManager.showFlightHistory("1003");
        }
        System.out.println("----------------------------------");


        System.out.println("\n--- Final Airport Status ---");
        airportManager.listAirports(true, true);
        System.out.println("\n--- Final Flight Schedule ---");
        flightScheduleManager.listAllFlights();
        System.out.println("-----------------------------");
    }

    // Main method now catches all potential exceptions thrown from the constructor
    public static void main(String[] args) {
        try {
            FlightSimulator simulator = new FlightSimulator();
            simulator.startSimulation(10, 60);

        } catch (ListException e) {
            System.err.println("[FATAL ERROR] Failed to initialize Flight Simulator due to ListException: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("[FATAL ERROR] Failed to initialize Flight Simulator due to IOException (e.g., airports file issue): " + e.getMessage());
            e.printStackTrace();
        } catch (RuntimeException e) {
            // Catch RuntimeException that might be thrown from scheduled tasks (like InterruptedException)
            System.err.println("[FATAL ERROR] An unexpected runtime error occurred during simulation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}