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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ScheduledFuture;

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
    private final int MAX_FLIGHTS_TO_GENERATE = 15;

    private volatile boolean paused = false;

    private Map<String, FlightData> inProgressFlightsData;
    private final Map<String, ScheduledFuture<?>> flightDataTasks;

    // --- Clase Interna: FlightData (para datos en tiempo real ) ---
    public static class FlightData {
        public String flightNumber;
        public int currentAltitude;
        public int currentSpeed;
        public int currentHeading;
        public long elapsedTimeSeconds;
        public LocalDateTime departureTime;
        public long estimatedFlightDurationMinutes;

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
            this.estimatedFlightDurationMinutes = 0;
        }

        public int getCurrentAltitude() { return currentAltitude; }
        public int getCurrentSpeed() { return currentSpeed; }
        public int getCurrentHeading() { return currentHeading; }
        public long getElapsedTimeSeconds() { return elapsedTimeSeconds; }
        public long getEstimatedFlightDurationMinutes() { return estimatedFlightDurationMinutes; }

        public void setCurrentAltitude(int currentAltitude) { this.currentAltitude = currentAltitude; }
        public void setCurrentSpeed(int currentSpeed) { this.currentSpeed = currentSpeed; }
        public void setCurrentHeading(int currentHeading) { this.currentHeading = currentHeading; }
        public void setElapsedTimeSeconds(long elapsedTimeSeconds) { this.elapsedTimeSeconds = elapsedTimeSeconds; }
        public void setEstimatedFlightDurationMinutes(long estimatedFlightDurationMinutes) { this.estimatedFlightDurationMinutes = estimatedFlightDurationMinutes; }
    }

    public FlightSimulator() throws ListException, IOException, TreeException {
        this.airportManager = new AirportManager();
        this.routeManager = new RouteManager(airportManager);
        this.passengerManager = new PassengerManager();
        this.flightScheduleManager = new FlightScheduleManager(this.airportManager, this.routeManager);

        this.airplanes = new HashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        this.random = new Random();
        this.flightCounter = new AtomicInteger(0);

        this.inProgressFlightsData = new ConcurrentHashMap<>();
        this.flightDataTasks = new ConcurrentHashMap<>();

        initializeSystem();
    }

    /**
     * Inicializa el sistema cargando aeropuertos, rutas, aviones y pasajeros de ejemplo.
     */
    private void initializeSystem() throws ListException, IOException, TreeException {
        loadAirportsFromFile("airports.json");

        DoublyLinkedList allLoadedAirports = airportManager.getAllAirports();
        if (allLoadedAirports != null && !allLoadedAirports.isEmpty()) {
            for (int i = 0; i < allLoadedAirports.size(); i++) {
                Airport airport = (Airport) allLoadedAirports.get(i);
                routeManager.getGraph().addVertex(airport.getCode());
            }
        }

        if (routeManager.getGraph().getNumVertices() < 2) {
            routeManager.getGraph().generateRandomRoutes(3, 7, 30, 600);
        } else {
            try {
                routeManager.loadRoutesFromJson("routes.json");
            } catch (IOException e) {
                routeManager.getGraph().generateRandomRoutes(3, 7, 30, 600);
            }
        }

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
            throw new TreeException("Error al registrar pasajeros predefinidos");
        }
    }

    /**
     * Carga aeropuertos desde un archivo JSON.
     */
    private void loadAirportsFromFile(String filename) throws ListException {
        try (Reader reader = Files.newBufferedReader(Paths.get(filename))) {
            Gson gson = new Gson();
            List<Airport> airportListFromFile = gson.fromJson(reader, new TypeToken<List<Airport>>() {}.getType());

            if (airportListFromFile != null && !airportListFromFile.isEmpty()) {
                for (Airport airport : airportListFromFile) {
                    airportManager.createAirport(airport.getCode(), airport.getName(), airport.getCountry());
                }
            }
        } catch (IOException e) {
            addDefaultAirports();
        } catch (com.google.gson.JsonSyntaxException e) {
            addDefaultAirports();
        } finally {
            try {
                if (airportManager.getAllAirports().isEmpty()) {
                    addDefaultAirports();
                }
            } catch (ListException e) {
                throw new ListException("Error al verificar lista de aeropuertos");
            }
        }
    }

    /**
     * Añade aeropuertos por defecto si no se pueden cargar desde archivo.
     */
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

    /**
     * Añade un avión al sistema con una ubicación inicial.
     */
    public void addAirplane(String id, int capacity, String initialLocationAirportCode) throws ListException {
        if (airportManager.findAirport(initialLocationAirportCode) != null) {
            if (!airplanes.containsKey(id)) {
                airplanes.put(id, new Airplane(id, capacity, initialLocationAirportCode));
            }
        }
    }

    /**
     * Activa un vuelo cambiando su estado a IN_PROGRESS y actualizando el estado del avión.
     */
    public void activateFlight(Flight flight) {
        if (flight == null) return;

        try {
            flight.setStatus(Flight.FlightStatus.IN_PROGRESS);
            flight.setActualDepartureTime(LocalDateTime.now());

            Airplane assignedPlane = flight.getAirplane();
            if (assignedPlane != null) {
                assignedPlane.setStatus(Airplane.AirplaneStatus.IN_FLIGHT);
                assignedPlane.setLocationInFlight();
            }

            FlightData newFlightData = new FlightData(flight.getFlightNumber(), flight.getActualDepartureTime());
            newFlightData.setEstimatedFlightDurationMinutes(flight.getEstimatedDurationMinutes());
            inProgressFlightsData.put(flight.getFlightNumber(), newFlightData);

        } catch (Exception e) {
            flight.setStatus(Flight.FlightStatus.SCHEDULED);
            Airplane assignedPlane = flight.getAirplane();
            if (assignedPlane != null) {
                assignedPlane.setStatus(AirplaneStatus.IDLE);
                assignedPlane.setLocationType(Airplane.AirplaneLocationType.AIRPORT);
            }
            inProgressFlightsData.remove(flight.getFlightNumber());
        }
    }

    public FlightScheduleManager getFlightScheduleManager() {
        return flightScheduleManager;
    }

    /**
     * Obtiene los datos de un vuelo en progreso.
     */
    public FlightData getFlightInProgressData(String flightNumber) {
        return inProgressFlightsData.get(flightNumber);
    }

    /**
     * Actualiza los datos de simulación de un vuelo en progreso.
     */
    private void updateFlightInProgressData(String flightNumber, Flight flight) throws ListException, StackException {
        if (flight.getStatus() != Flight.FlightStatus.IN_PROGRESS) {
            if (inProgressFlightsData.containsKey(flightNumber)) {
                inProgressFlightsData.remove(flightNumber);
            }
            ScheduledFuture<?> task = flightDataTasks.remove(flightNumber);
            if (task != null) {
                task.cancel(true);
            }
            return;
        }

        FlightData data = inProgressFlightsData.get(flightNumber);
        if (data == null) {
            data = new FlightData(flightNumber, flight.getActualDepartureTime() != null ? flight.getActualDepartureTime() : LocalDateTime.now());
            data.setEstimatedFlightDurationMinutes(flight.getEstimatedDurationMinutes());
            inProgressFlightsData.put(flightNumber, data);
        }

        Duration duration = Duration.between(data.departureTime, LocalDateTime.now());
        data.setElapsedTimeSeconds(Math.max(0, duration.getSeconds()));

        long totalSimulatedDurationSeconds = data.getEstimatedFlightDurationMinutes() * 60;
        if (totalSimulatedDurationSeconds <= 0) {
            totalSimulatedDurationSeconds = 10 * 60;
        }

        double progressRatio = (double) data.getElapsedTimeSeconds() / totalSimulatedDurationSeconds;
        progressRatio = Math.min(1.0, progressRatio);

        if (data.getElapsedTimeSeconds() >= totalSimulatedDurationSeconds) {
            flight.setStatus(Flight.FlightStatus.COMPLETED);
            flight.setActualArrivalTime(LocalDateTime.now());

            Airplane assignedPlane = flight.getAirplane();
            if (assignedPlane != null) {
                assignedPlane.setCurrentLocationAirportCode(flight.getDestinationAirportCode());
                assignedPlane.setStatus(Airplane.AirplaneStatus.IDLE);
                assignedPlane.setLocationType(Airplane.AirplaneLocationType.AIRPORT);
                assignedPlane.addFlightToHistory(flight);
            }

            inProgressFlightsData.remove(flightNumber);
            ScheduledFuture<?> task = flightDataTasks.remove(flightNumber);
            if (task != null) {
                task.cancel(true);
            }

            try {
                flightScheduleManager.removeFlight(flight.getFlightNumber());
            } catch (Exception e) {
                throw new ListException("Error al remover vuelo completado");
            }
            return;
        }

        double accelerationPhaseEnd = 0.10;
        double cruisePhaseEnd = 0.85;
        int maxAltitude = 11000;
        int maxSpeed = 900;

        if (progressRatio < accelerationPhaseEnd) {
            double phaseProgress = progressRatio / accelerationPhaseEnd;
            data.setCurrentAltitude((int) (maxAltitude * phaseProgress));
            data.setCurrentSpeed((int) (maxSpeed * phaseProgress));
            data.setCurrentHeading(calculateTargetHeading(flight.getOriginAirportCode(), flight.getDestinationAirportCode()));
        }
        else if (progressRatio < cruisePhaseEnd) {
            data.setCurrentAltitude(maxAltitude + random.nextInt(200) - 100);
            data.setCurrentSpeed(maxSpeed + random.nextInt(40) - 20);
            data.setCurrentHeading(calculateTargetHeading(flight.getOriginAirportCode(), flight.getDestinationAirportCode()));
        }
        else {
            double phaseProgress = (progressRatio - cruisePhaseEnd) / (1.0 - cruisePhaseEnd);
            data.setCurrentAltitude((int) (maxAltitude * (1.0 - phaseProgress)));
            data.setCurrentSpeed((int) (maxSpeed * (1.0 - phaseProgress)));
            data.setCurrentHeading(calculateTargetHeading(flight.getOriginAirportCode(), flight.getDestinationAirportCode()));
        }

        data.setCurrentAltitude(Math.max(0, data.getCurrentAltitude()));
        data.setCurrentSpeed(Math.max(0, data.getCurrentSpeed()));
        data.setCurrentHeading((data.getCurrentHeading() + 360) % 360);

        if (data.getElapsedTimeSeconds() >= totalSimulatedDurationSeconds - 5 && data.getElapsedTimeSeconds() < totalSimulatedDurationSeconds) {
            data.setCurrentAltitude(Math.max(0, data.getCurrentAltitude() - (int)(data.currentAltitude / (double)(totalSimulatedDurationSeconds - data.getElapsedTimeSeconds() + 1))));
            data.setCurrentSpeed(Math.max(0, data.getCurrentSpeed() - (int)(data.currentSpeed / (double)(totalSimulatedDurationSeconds - data.getElapsedTimeSeconds() + 1))));
        } else if (data.getElapsedTimeSeconds() >= totalSimulatedDurationSeconds) {
            data.setCurrentAltitude(0);
            data.setCurrentSpeed(0);
        }
    }

    /**
     * Calcula el rumbo entre dos aeropuertos.
     */
    private int calculateTargetHeading(String originCode, String destinationCode) {
        if (originCode.equals("SJO") && destinationCode.equals("LIR")) return 270;
        if (originCode.equals("LIR") && destinationCode.equals("SJO")) return 90;
        if (originCode.equals("SJO") && destinationCode.equals("MIA")) return 45;
        if (originCode.equals("MIA") && destinationCode.equals("SJO")) return 225;
        if (originCode.equals("LAX") && destinationCode.equals("JFK")) return 75;
        if (originCode.equals("JFK") && destinationCode.equals("LAX")) return 255;
        return (int) (Math.random() * 360);
    }

    /**
     * Genera un vuelo aleatorio basado en las reglas del sistema.
     */
    private void generateRandomFlightBasedOnRules() throws ListException, TreeException {
        if (paused) return;
        if (flightCounter.get() >= MAX_FLIGHTS_TO_GENERATE) return;

        DoublyLinkedList allAirportsList = airportManager.getAllAirports();
        if (allAirportsList.isEmpty()) return;

        List<Airport> activeAirports = new ArrayList<>();
        for (int i = 0; i < allAirportsList.size(); i++) {
            Airport airport = (Airport) allAirportsList.get(i);
            if (airport.getStatus() == Airport.AirportStatus.ACTIVE) {
                activeAirports.add(airport);
            }
        }

        if (activeAirports.size() < 2) return;

        Collections.sort(activeAirports, (airport1, airport2) -> {
            int count1 = routeManager.getGraph().getOutgoingRouteCount(airport1.getCode());
            int count2 = routeManager.getGraph().getOutgoingRouteCount(airport2.getCode());
            return Integer.compare(count2, count1);
        });

        List<Airport> selectedOrigins = new ArrayList<>();
        for (int i = 0; i < Math.min(5, activeAirports.size()); i++) {
            selectedOrigins.add(activeAirports.get(i));
        }

        if (selectedOrigins.isEmpty()) return;

        Airport originAirport = null;
        String originCode = null;
        String destinationCode = null;
        int routeFindingAttempts = 0;
        final int MAX_ROUTE_FINDING_ATTEMPTS = 100;

        while (routeFindingAttempts < MAX_ROUTE_FINDING_ATTEMPTS) {
            originAirport = selectedOrigins.get(random.nextInt(selectedOrigins.size()));
            originCode = originAirport.getCode();

            List<String> potentialDestinationCodes = new ArrayList<>();
            SinglyLinkedList graphAirportCodes = routeManager.getGraph().getAllAirportCodes();
            for (int i = 0; i < graphAirportCodes.size(); i++) {
                potentialDestinationCodes.add((String) graphAirportCodes.get(i));
            }

            if (potentialDestinationCodes.isEmpty()) return;

            String potentialDestinationCode = potentialDestinationCodes.get(random.nextInt(potentialDestinationCodes.size()));

            if (!potentialDestinationCode.equals(originCode) &&
                    routeManager.calculateShortestRoute(originCode, potentialDestinationCode) != Integer.MAX_VALUE) {
                destinationCode = potentialDestinationCode;
                break;
            }
            routeFindingAttempts++;
        }

        if (destinationCode == null) return;

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

        if (idleAirplanesAtOrigin.isEmpty()) return;

        selectedAirplane = idleAirplanesAtOrigin.get(random.nextInt(idleAirplanesAtOrigin.size()));
        selectedAirplane.setStatus(AirplaneStatus.ASSIGNED);

        String flightNumber = null;
        Flight newFlight = null;
        int flightNumberAttempts = 0;
        final int MAX_FLIGHT_NUMBER_ATTEMPTS = 50;

        do {
            flightNumber = "FL" + (random.nextInt(900) + 100);
            newFlight = flightScheduleManager.createFlight(flightNumber, originCode, destinationCode,
                    LocalDateTime.now().plusMinutes(random.nextInt(10) + 1),
                    0,
                    selectedAirplane.getCapacity());
            flightNumberAttempts++;
        } while (newFlight == null && flightNumberAttempts < MAX_FLIGHT_NUMBER_ATTEMPTS);

        if (newFlight == null) {
            if (selectedAirplane != null) {
                selectedAirplane.setStatus(AirplaneStatus.IDLE);
            }
            return;
        }

        newFlight.setAirplane(selectedAirplane);
        int estimatedDurationRealistic = routeManager.calculateShortestRoute(originCode, destinationCode);
        if (estimatedDurationRealistic == Integer.MAX_VALUE || estimatedDurationRealistic == 0) {
            estimatedDurationRealistic = 120 + random.nextInt(180);
        }
        newFlight.setEstimatedDurationMinutes(Math.max(10, estimatedDurationRealistic / 3));

        int passengersToBoard = random.nextInt(selectedAirplane.getCapacity() / 2) + 1;
        passengersToBoard = Math.min(passengersToBoard, selectedAirplane.getCapacity() - newFlight.getOccupancy());
        passengersToBoard = Math.min(passengersToBoard, passengerManager.getPassengerCount());

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
            throw new ListException("Error al obtener pasajeros");
        }

        if (allAvailablePassengersFromAVL != null && !allAvailablePassengersFromAVL.isEmpty()) {
            List<Passenger> shuffledPassengers = new ArrayList<>(allAvailablePassengersFromAVL);
            Collections.shuffle(shuffledPassengers);

            int actualPassengersAssigned = 0;
            for (int i = 0; i < passengersToBoard && i < shuffledPassengers.size(); i++) {
                Passenger p = shuffledPassengers.get(i);
                if (p != null) {
                    try {
                        flightScheduleManager.processTicketPurchase(p, newFlight);
                        passengerManager.processTicketPurchase(p, newFlight);
                        actualPassengersAssigned++;
                    } catch (ListException | QueueException e) {
                        throw new TreeException("Error al procesar ticket");
                    }
                }
            }
        }

        flightCounter.incrementAndGet();
    }

    /**
     * Inicia la simulación con intervalos específicos.
     */
    public void startSimulation(long flightGenerationIntervalSeconds, long totalSimulationDurationSeconds) {
        paused = false;
        flightLimitMessagePrinted = false;
        flightCounter.set(0);

        scheduler.scheduleAtFixedRate(() -> {
            if (!paused) {
                try {
                    if (flightCounter.get() < MAX_FLIGHTS_TO_GENERATE) {
                        generateRandomFlightBasedOnRules();
                    }
                } catch (ListException | TreeException e) {
                    throw new RuntimeException("Error en generación de vuelos");
                }
            }
        }, 0, flightGenerationIntervalSeconds, TimeUnit.SECONDS);

        scheduler.scheduleAtFixedRate(() -> {
            if (!paused) {
                for (String flightNumber : new HashSet<>(inProgressFlightsData.keySet())) {
                    try {
                        Flight flight = flightScheduleManager.findFlight(flightNumber);
                        if (flight != null) {
                            if (flight.getStatus() == Flight.FlightStatus.IN_PROGRESS) {
                                updateFlightInProgressData(flightNumber, flight);
                            } else {
                                inProgressFlightsData.remove(flightNumber);
                                ScheduledFuture<?> task = flightDataTasks.remove(flightNumber);
                                if (task != null) {
                                    task.cancel(true);
                                }
                            }
                        } else {
                            inProgressFlightsData.remove(flightNumber);
                            ScheduledFuture<?> task = flightDataTasks.remove(flightNumber);
                            if (task != null) {
                                task.cancel(true);
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Error en actualización de vuelo");
                    }
                }
            }
        }, 0, 1, TimeUnit.SECONDS);

        if (totalSimulationDurationSeconds > 0) {
            scheduler.schedule(this::shutdownSimulation, totalSimulationDurationSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * Apaga la simulación y limpia recursos.
     */
    public void shutdownSimulation() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                scheduler.shutdownNow();
            }
        }

        for (ScheduledFuture<?> task : flightDataTasks.values()) {
            task.cancel(true);
        }
        flightDataTasks.clear();
        inProgressFlightsData.clear();

        try {
            DoublyLinkedList allFlights = ListConverter.convertToDoublyLinkedList(flightScheduleManager.getScheduledFlights());
            for (int i = 0; i < allFlights.size(); i++) {
                Flight f = (Flight) allFlights.get(i);
                if (f.getStatus() == Flight.FlightStatus.IN_PROGRESS || f.getStatus() == Flight.FlightStatus.COMPLETED) {
                    f.setStatus(Flight.FlightStatus.SCHEDULED);
                    Airplane assignedPlane = f.getAirplane();
                    if (assignedPlane != null && assignedPlane.getStatus() != AirplaneStatus.IDLE) {
                        assignedPlane.setStatus(AirplaneStatus.IDLE);
                        if (f.getDestinationAirportCode() != null) {
                            assignedPlane.setCurrentLocationAirportCode(f.getDestinationAirportCode());
                        } else {
                            assignedPlane.setCurrentLocationAirportCode(f.getOriginAirportCode());
                        }
                        assignedPlane.setLocationType(Airplane.AirplaneLocationType.AIRPORT);
                    }
                }
            }
        } catch (ListException e) {
            throw new RuntimeException("Error al resetear estados");
        }

        printSimulationSummary();
    }

    /**
     * Imprime un resumen de la simulación.
     */
    public void printSimulationSummary() {
        try {
            printAirplaneFlightHistory();
        } catch (ListException e) {
            throw new RuntimeException("Error al imprimir historial");
        }
    }

    /**
     * Imprime el historial de vuelos por avión.
     */
    private void printAirplaneFlightHistory() throws ListException {
        for (Airplane airplane : airplanes.values()) {
            LinkedStack history = airplane.getFlightHistory();
            if (history != null && !history.isEmpty()) {
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
                    throw new ListException("Error al acceder historial");
                }
            }
        }
    }

    /**
     * Pausa la simulación.
     */
    public void pauseSimulator() {
        this.paused = true;
    }

    /**
     * Reanuda la simulación.
     */
    public void resumeSimulator() {
        this.paused = false;
    }

    /**
     * Avanza al siguiente vuelo programado.
     */
    public Flight advanceToNextScheduledFlight() throws ListException {
        CircularDoublyLinkedList rawScheduledFlights = flightScheduleManager.getScheduledFlights();
        DoublyLinkedList scheduledFlights = ListConverter.convertToDoublyLinkedList(rawScheduledFlights);

        if (scheduledFlights.isEmpty()) {
            return null;
        }

        Flight nextFlightToProcess = null;
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
            return null;
        }

        nextFlightToProcess.setStatus(Flight.FlightStatus.IN_PROGRESS);
        nextFlightToProcess.setActualDepartureTime(LocalDateTime.now());

        if (!inProgressFlightsData.containsKey(nextFlightToProcess.getFlightNumber())) {
            FlightData newFlightData = new FlightData(nextFlightToProcess.getFlightNumber(),
                    nextFlightToProcess.getActualDepartureTime());
            newFlightData.setEstimatedFlightDurationMinutes(nextFlightToProcess.getEstimatedDurationMinutes());
            inProgressFlightsData.put(nextFlightToProcess.getFlightNumber(), newFlightData);
        }

        return nextFlightToProcess;
    }

    /**
     * Finaliza un vuelo en progreso.
     */
    private void endCurrentInProgressFlight() throws ListException, StackException {
        Flight currentInProgress = null;

        for (String flightNumber : new HashSet<>(inProgressFlightsData.keySet())) {
            Flight f = flightScheduleManager.findFlight(flightNumber);
            if (f != null && f.getStatus() == Flight.FlightStatus.IN_PROGRESS) {
                currentInProgress = f;
                break;
            }
        }

        if (currentInProgress != null) {
            currentInProgress.setStatus(Flight.FlightStatus.COMPLETED);
            currentInProgress.setActualArrivalTime(LocalDateTime.now());

            Airplane assignedPlane = currentInProgress.getAirplane();
            if (assignedPlane != null) {
                assignedPlane.setCurrentLocationAirportCode(currentInProgress.getDestinationAirportCode());
                assignedPlane.setStatus(Airplane.AirplaneStatus.IDLE);
                assignedPlane.setLocationType(Airplane.AirplaneLocationType.AIRPORT);
                assignedPlane.addFlightToHistory(currentInProgress);
            }

            inProgressFlightsData.remove(currentInProgress.getFlightNumber());
            ScheduledFuture<?> task = flightDataTasks.remove(currentInProgress.getFlightNumber());
            if (task != null) {
                task.cancel(true);
            }

            try {
                flightScheduleManager.removeFlight(currentInProgress.getFlightNumber());
            } catch (Exception e) {
                throw new ListException("Error al remover vuelo");
            }
        }
    }
}