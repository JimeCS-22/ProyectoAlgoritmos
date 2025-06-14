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
import ucr.proyectoalgoritmos.Domain.queue.QueueException; // Importación correcta para QueueException
import ucr.proyectoalgoritmos.Domain.stack.StackException;
import ucr.proyectoalgoritmos.route.RouteManager; // Importación correcta para RouteManager

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
    private RouteManager routeManager; // Ahora se usa el RouteManager en lugar del Graph directamente

    private Map<String, Airplane> airplanes;
    private ScheduledExecutorService scheduler;
    private Random random;
    private AtomicInteger flightCounter;

    private volatile boolean flightLimitMessagePrinted = false;
    private final int MAX_FLIGHTS_TO_GENERATE = 15; // Límite de vuelos para la simulación

    public FlightSimulator() throws ListException, IOException, TreeException {
        this.airportManager = new AirportManager();
        this.routeManager = new RouteManager(airportManager); // Pasa airportManager al constructor de RouteManager
        this.passengerManager = new PassengerManager();
        this.flightScheduleManager = new FlightScheduleManager(this.airportManager, this.routeManager);

        this.airplanes = new HashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        this.random = new Random();
        this.flightCounter = new AtomicInteger(0);

        initializeSystem();
    }

    /**
     * Inicializa el sistema cargando aeropuertos, rutas, aviones y pasajeros.
     * @throws ListException Si hay un problema con las listas.
     * @throws IOException Si hay un problema con la lectura de archivos.
     * @throws TreeException Si hay un problema con el árbol AVL.
     */
    private void initializeSystem() throws ListException, IOException, TreeException {
        System.out.println("...Inicializando sistema...");
        loadAirportsFromFile("airports.json");

        // Añadir vértices (aeropuertos) al grafo de rutas desde AirportManager
        DoublyLinkedList allLoadedAirports = airportManager.getAllAirports();
        if (allLoadedAirports != null && !allLoadedAirports.isEmpty()) {
            for (int i = 0; i < allLoadedAirports.size(); i++) {
                try {
                    Airport airport = (Airport) allLoadedAirports.get(i);
                    // Asegura que los vértices se añadan al grafo interno de RouteManager
                    routeManager.getGraph().addVertex(airport.getCode());
                } catch (ListException e) {
                    System.err.println("ERROR: No se pudo obtener aeropuerto del índice " + i + " al añadir al grafo: " + e.getMessage());
                }
            }
        } else {
            System.err.println("ADVERTENCIA: No hay aeropuertos cargados o la lista está vacía.");
        }

        // Cargar o generar rutas
        if (routeManager.getGraph().getNumVertices() < 2) {
            System.err.println("ERROR: No hay suficientes aeropuertos en el grafo para cargar o generar rutas. Asegúrese de que se carguen al menos 2 aeropuertos.");
        } else {
            try {
                routeManager.loadRoutesFromJson("routes.json");
                // System.out.println("SIMULADOR: Rutas cargadas desde 'routes.json'.");
            } catch (IOException e) {
                // Si falla la carga del JSON, se generan rutas aleatorias
                // System.err.println("ADVERTENCIA: No se pudieron cargar las rutas desde 'routes.json': " + e.getMessage());
                // System.out.println("Generando rutas aleatorias como alternativa.");
                routeManager.getGraph().generateRandomRoutes(3, 7, 30, 600);
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
        //System.out.println("SIMULADOR: Aviones predefinidos añadidos.");

        // Registrar pasajeros predefinidos (gestionados por PassengerManager con AVL)
        try {
            passengerManager.registerPassenger("1001", "Alice Smith", "USA");
            passengerManager.registerPassenger("1002", "Bob Johnson", "Canada");
            passengerManager.registerPassenger("1003", "Carlos Garcia", "Mexico");
            passengerManager.registerPassenger("1004", "Diana Miller", "UK");
            passengerManager.registerPassenger("1005", "Eve Brown", "Germany");
            // System.out.println("SIMULADOR: Pasajeros predefinidos registrados.");
        } catch (TreeException e) {
            System.err.println("ERROR: Fallo al registrar pasajeros predefinidos: " + e.getMessage());
        }
        System.out.println("Sistema inicializado con éxito.");
    }

    /**
     * Carga aeropuertos desde un archivo JSON o añade aeropuertos por defecto si falla.
     * @param filename Nombre del archivo JSON de aeropuertos.
     * @throws ListException Si hay un problema con las listas de aeropuertos.
     */
    private void loadAirportsFromFile(String filename) throws ListException {
        int loadedCount = 0;
        try (Reader reader = Files.newBufferedReader(Paths.get(filename))) {
            Gson gson = new Gson();
            List<Airport> airportListFromFile = gson.fromJson(reader, new TypeToken<List<Airport>>() {
            }.getType());

            if (airportListFromFile != null && !airportListFromFile.isEmpty()) {
                for (Airport airport : airportListFromFile) {
                    if (loadedCount >= 20) { // Limita la cantidad de aeropuertos cargados
                        //System.out.println("ADVERTENCIA: Se ha alcanzado el límite de 20 aeropuertos del archivo.");
                        break;
                    }
                    // AirportManager usa DoublyLinkedList internamente
                    airportManager.createAirport(airport.getCode(), airport.getName(), airport.getCountry());
                    loadedCount++;
                }
                // System.out.println("SIMULADOR: " + loadedCount + " aeropuertos cargados desde '" + filename + "'.");
            } else {
                // System.out.println("ADVERTENCIA: El archivo '" + filename + "' está vacío o no contiene datos válidos. No se cargaron aeropuertos del archivo.");
            }

            if (loadedCount < 15) {
                // System.out.println("ADVERTENCIA: Solo se cargaron " + loadedCount + " aeropuertos del archivo. Se sugiere un mínimo de 15 para una simulación robusta.");
            }

        } catch (IOException e) {
            //System.err.println("ERROR: No se pudo leer el archivo de aeropuertos '" + filename + "': " + e.getMessage());
            //System.out.println("SIMULADOR: Generando algunos aeropuertos predeterminados en su lugar.");
            addDefaultAirports();
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("ERROR: El archivo JSON de aeropuertos '" + filename + "' tiene un formato inválido: " + e.getMessage());
            addDefaultAirports();
        } finally {
            try {
                if (airportManager.getAllAirports().isEmpty()) {
                    //System.out.println("SIMULADOR: No se cargaron aeropuertos por ningún método; añadiendo aeropuertos predeterminados de emergencia.");
                    addDefaultAirports();
                }
            } catch (ListException e) {
                System.err.println("ERROR: No se pudo verificar si la lista de aeropuertos está vacía: " + e.getMessage());
            }
        }
    }

    /**
     * Añade un conjunto de aeropuertos por defecto si no se pudieron cargar desde un archivo.
     * @throws ListException Si hay un problema al añadir aeropuertos a AirportManager.
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
     * Añade un nuevo avión al simulador y lo registra en el aeropuerto inicial.
     * @param id El ID del avión.
     * @param capacity La capacidad de pasajeros del avión.
     * @param initialLocationAirportCode El código del aeropuerto donde se ubica inicialmente.
     * @throws ListException Si hay un problema con las listas de aeropuertos.
     */
    public void addAirplane(String id, int capacity, String initialLocationAirportCode) throws ListException {
        if (airportManager.findAirport(initialLocationAirportCode) != null) {
            if (!airplanes.containsKey(id)) {
                airplanes.put(id, new Airplane(id, capacity, initialLocationAirportCode));
            } else {
                //System.out.println("SIMULADOR: El avión " + id + " ya existe. Se omite la adición.");
            }
        } else {
            System.err.println("ERROR: No se puede añadir el avión " + id + ". La ubicación inicial " + initialLocationAirportCode + " no es un aeropuerto válido.");
        }
    }

    /**
     * Genera un vuelo aleatorio siguiendo las reglas de la simulación.
     * @throws ListException Si hay un problema con las listas internas.
     * @throws QueueException Si hay un problema con las colas de pasajeros.
     * @throws TreeException Si hay un problema con el árbol AVL de pasajeros.
     */
    private void generateRandomFlightBasedOnRules() throws ListException, QueueException, TreeException { // Cambiado StackException a QueueException
        if (flightCounter.get() >= MAX_FLIGHTS_TO_GENERATE) {
            if (!flightLimitMessagePrinted) {
                System.out.println("ADVERTENCIA: Límite de " + MAX_FLIGHTS_TO_GENERATE + " vuelos alcanzado. No se generarán más vuelos.");
                flightLimitMessagePrinted = true;
            }
            return;
        }
        //System.out.println("\nIntentando generar nuevo vuelo." );

        DoublyLinkedList allAirportsList = airportManager.getAllAirports(); // Usa DoublyLinkedList
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
            }
        }

        if (activeAirports.size() < 2) {
            System.out.println("ADVERTENCIA: Se requieren al menos 2 aeropuertos activos para generar vuelos. Actualmente hay " + activeAirports.size() + ". Retornando.");
            return;
        }

        // Ordenar aeropuertos por número de rutas salientes para priorizar orígenes
        Collections.sort(activeAirports, new Comparator<Airport>() {
            @Override
            public int compare(Airport airport1, Airport airport2) {
                // Usa getOutgoingRouteCount de DirectedSinglyLinkedListGraph
                int count1 = routeManager.getGraph().getOutgoingRouteCount(airport1.getCode());
                int count2 = routeManager.getGraph().getOutgoingRouteCount(airport2.getCode());
                return Integer.compare(count2, count1); // Orden descendente
            }
        });

        List<Airport> selectedOrigins = new ArrayList<>();
        // Seleccionar hasta 5 aeropuertos con más rutas salientes como posibles orígenes
        for (int i = 0; i < Math.min(5, activeAirports.size()); i++) {
            selectedOrigins.add(activeAirports.get(i));
        }

        if (selectedOrigins.isEmpty()) {
            //System.out.println("ADVERTENCIA: No se pudieron seleccionar aeropuertos de origen. Asegúrate de tener suficientes aeropuertos activos y conectados. Retornando.");
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

            // Usar todos los códigos de aeropuerto del grafo como destinos potenciales
            List<String> potentialDestinationCodes = new ArrayList<>();
            try {
                // getAirportCodes devuelve SinglyLinkedList
                SinglyLinkedList graphAirportCodes = routeManager.getGraph().getAllAirportCodes();
                for (int i = 0; i < graphAirportCodes.size(); i++) {
                    potentialDestinationCodes.add((String) graphAirportCodes.get(i));
                }
            } catch (ListException e) {
                System.err.println("ERROR: No se pudieron obtener los códigos de aeropuerto del grafo para destino: " + e.getMessage());
                routeFindingAttempts++;
                continue;
            }

            if (potentialDestinationCodes.isEmpty()) {
                System.out.println("ADVERTENCIA: No hay códigos de aeropuerto disponibles o suficientes en el grafo para seleccionar un destino. Retornando.");
                return;
            }

            String potentialDestinationCode = null;
            if (!potentialDestinationCodes.isEmpty()) {
                potentialDestinationCode = potentialDestinationCodes.get(random.nextInt(potentialDestinationCodes.size()));
            }

            // Asegurarse de que origen y destino sean diferentes y que exista una ruta (Dijkstra)
            if (potentialDestinationCode != null && !potentialDestinationCode.equals(originCode) &&
                    routeManager.calculateShortestRoute(originCode, potentialDestinationCode) != Integer.MAX_VALUE) {
                destinationCode = potentialDestinationCode;
                break; // Ruta válida encontrada
            }
            routeFindingAttempts++;
        }

        if (destinationCode == null) {
            System.out.println("ADVERTENCIA: No se pudo encontrar una ruta válida entre aeropuertos después de " + MAX_ROUTE_FINDING_ATTEMPTS + " intentos. No se puede generar el vuelo. Retornando.");
            return;
        }

        Airplane selectedAirplane = null;
        List<Airplane> idleAirplanes = new ArrayList<>();
        // Encontrar aviones IDLE en el aeropuerto de origen
        for (Airplane airplane : airplanes.values()) {
            if (airplane.getStatus() == AirplaneStatus.IDLE && airplane.getCurrentLocationAirportCode() != null && airplane.getCurrentLocationAirportCode().equals(originCode)) {
                idleAirplanes.add(airplane);
            }
        }

        if (idleAirplanes.isEmpty()) {
            //System.out.println("ADVERTENCIA: No hay aviones IDLE disponibles en el aeropuerto de origen (" + originCode + ") para el vuelo. Retornando y esperando que se liberen en el siguiente ciclo.");
            return;
        }

        selectedAirplane = idleAirplanes.get(random.nextInt(idleAirplanes.size()));
        selectedAirplane.setStatus(AirplaneStatus.ASSIGNED); // Avión asignado al vuelo

        String flightNumber;
        Flight newFlight = null;
        int flightNumberAttempts = 0;
        final int MAX_FLIGHT_NUMBER_ATTEMPTS = 50;

        do {
            flightNumber = "FL" + (random.nextInt(900) + 100); // Genera FL100 a FL999
            try {
                // FlightScheduleManager crea objetos Flight (gestionados por CircularDoublyLinkedList)
                newFlight = flightScheduleManager.createFlight(flightNumber, originCode, destinationCode,
                        LocalDateTime.now().plusSeconds(random.nextInt(3) + 1), 0, selectedAirplane.getCapacity());
                break;
            } catch (ListException e) {
                flightNumberAttempts++;
                if (flightNumberAttempts >= MAX_FLIGHT_NUMBER_ATTEMPTS) {
                    System.err.println("ERROR: No se pudo generar un número de vuelo único después de " + MAX_FLIGHT_NUMBER_ATTEMPTS + " intentos. No se puede programar el vuelo. Retornando.");
                    return;
                }
            }
        } while (true);

        if (newFlight == null) {
            System.err.println("ERROR: Fallo inesperado al crear el objeto Flight. Retornando.");
            return;
        }

        newFlight.setAirplane(selectedAirplane);

        int estimatedDurationRealistic = routeManager.calculateShortestRoute(originCode, destinationCode); // Dijkstra
        if (estimatedDurationRealistic == Integer.MAX_VALUE || estimatedDurationRealistic == 0) {
            estimatedDurationRealistic = 120 + random.nextInt(180); // Duración por defecto si no se encuentra ruta
        }
        newFlight.setEstimatedDurationMinutes(estimatedDurationRealistic);

        System.out.println("\n--- Programando Vuelo " + flightNumber + " ---");
        // Asegúrate de que getAirportName exista en AirportManager o usa findAirport().getName()
        System.out.println("De: " + (airportManager.findAirport(originCode) != null ? airportManager.findAirport(originCode).getName() : originCode) + " (" + originCode + ")");
        System.out.println("A: " + (airportManager.findAirport(destinationCode) != null ? airportManager.findAirport(destinationCode).getName() : destinationCode) + " (" + destinationCode + ")");
        System.out.println("Avión asignado: " + selectedAirplane.getId() + " (Capacidad: " + selectedAirplane.getCapacity() + ")");
        System.out.println("Salida programada: " + newFlight.getDepartureTime().toLocalDate());
        System.out.println("Duración estimada: " + (int) Math.round((double) newFlight.getEstimatedDurationMinutes() / 60.0) + " h.");

        // --- Simulación de compra de billetes y embarque de pasajeros ---
        int passengersToBoard = random.nextInt(selectedAirplane.getCapacity() / 2) + 1; // Al menos 1 pasajero, hasta la mitad de la capacidad
        passengersToBoard = Math.min(passengersToBoard, selectedAirplane.getCapacity() - newFlight.getOccupancy()); // No exceder asientos disponibles
        passengersToBoard = Math.min(passengersToBoard, passengerManager.getPassengerCount()); // No asignar más pasajeros de los disponibles

        System.out.println("Compra de " + passengersToBoard + " billetes para el vuelo " + flightNumber + ".");

        List<Passenger> allAvailablePassengersFromAVL = null;
        try {
            // PassengerManager's internal AVL tree: inOrderList() devuelve DoublyLinkedList, no List
            DoublyLinkedList dll = passengerManager.passengers.inOrderList();
            if (dll != null && !dll.isEmpty()) {
                allAvailablePassengersFromAVL = new ArrayList<>();
                for (int i = 0; i < dll.size(); i++) {
                    allAvailablePassengersFromAVL.add((Passenger) dll.get(i));
                }
            }
        } catch (ListException e) {
            System.err.println("ERROR: No se pudo obtener la lista de pasajeros del AVL: " + e.getMessage());
            return;
        }


        if (allAvailablePassengersFromAVL == null || allAvailablePassengersFromAVL.isEmpty()) {
            System.out.println("ADVERTENCIA: No hay pasajeros registrados en el sistema para simular la compra de billetes en el vuelo " + flightNumber + ". El vuelo podría salir vacío.");
        } else {
            List<Passenger> shuffledPassengers = new ArrayList<>(allAvailablePassengersFromAVL); // Crear una nueva lista para mezclar
            Collections.shuffle(shuffledPassengers); // Mezclar para seleccionar pasajeros aleatoriamente

            int actualPassengersToAssign = Math.min(passengersToBoard, shuffledPassengers.size());

            for (int i = 0; i < actualPassengersToAssign; i++) {
                Passenger p = shuffledPassengers.get(i);
                if (p != null) {
                    try {
                        flightScheduleManager.processTicketPurchase(p, newFlight); // Asignar pasajeros al vuelo
                        passengerManager.processTicketPurchase(p, newFlight); // Actualizar historial de vuelos del pasajero
                    } catch (ListException | QueueException | StackException e) { // Catch QueueException aquí
                        System.err.println("ERROR: Fallo al procesar billete para pasajero " + p.getId() + " en el vuelo " + flightNumber + ": " + e.getMessage());
                    }
                } else {
                    System.err.println("ADVERTENCIA: Pasajero nulo en la lista mezclada. No se pudo asignar al vuelo " + flightNumber + ".");
                }
            }
        }
        System.out.println(newFlight.getOccupancy() + " pasajeros asignados al vuelo " + flightNumber + " de " + newFlight.getCapacity() + " asientos.");
        // --- Fin de la simulación de compra de billetes ---


        long delaySecondsToTakeoff = Duration.between(LocalDateTime.now(), newFlight.getDepartureTime()).getSeconds();
        if (delaySecondsToTakeoff < 0) delaySecondsToTakeoff = 0;

        final String fNum = flightNumber;
        final Airplane plane = selectedAirplane;
        final String finalOriginCode = originCode;
        final String finalDestinationCode = destinationCode;
        // Factor de aceleración de la simulación (e.g., 100 minutos de vuelo simulados en 60 segundos reales)
        final long simulatedDurationForLogicSeconds = (long) (newFlight.getEstimatedDurationMinutes() * 60 / 100);


        // Despegue del Vuelo
        scheduler.schedule(() -> {
            try {
                Flight currentFlight = flightScheduleManager.findFlight(fNum); // findFlight busca en CircularDoublyLinkedList
                if (currentFlight != null) {
                    currentFlight.setStatus(Flight.FlightStatus.IN_PROGRESS);
                    plane.setStatus(AirplaneStatus.IN_FLIGHT); // Avión en vuelo
                    plane.setCurrentLocationAirportCode(null); // Avión en el aire
                    System.out.println("Vuelo " + fNum + " ha despegado de " + (airportManager.findAirport(finalOriginCode) != null ? airportManager.findAirport(finalOriginCode).getName() : finalOriginCode) + ".\nEstado: IN_PROGRESS.\nAvión " + plane.getId() + " ahora en IN_FLIGHT.");
                } else {
                    System.err.println("Vuelo " + fNum + " no encontrado al intentar iniciar su simulación.");
                    return;
                }
            } catch (ListException e) {
                System.err.println("ERROR: Durante la simulación de inicio de vuelo " + fNum + ": " + e.getMessage());
            } catch (Exception e) {
                System.err.println("ERROR: Excepción inesperada durante la simulación de inicio del vuelo " + fNum + ": " + e.getMessage());
            }
        }, delaySecondsToTakeoff, TimeUnit.SECONDS);


        // Animación Visual del Vuelo
        scheduler.schedule(() -> {
            try {
                System.out.println("\n-----------------------------------------------------");
                System.out.println("VUELO EN CURSO ✈️: " + fNum + " (" + plane.getId() + ")");
                System.out.print((airportManager.findAirport(finalOriginCode) != null ? airportManager.findAirport(finalOriginCode).getName() : finalOriginCode) + " >>>>>>>>>>>>>>>>✈️");
                System.out.flush();

                long animationSteps = 30;
                long actualVisualAnimationDurationMillis = 1500;
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
                Thread.currentThread().interrupt();
                System.err.println("ERROR: Animación de vuelo " + fNum + " interrumpida: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("ERROR: Excepción inesperada durante la animación del vuelo " + fNum + ": " + e.getMessage());
            }
        }, delaySecondsToTakeoff, TimeUnit.SECONDS);


        // Finalización del Vuelo
        scheduler.schedule(() -> {
            try {
                Flight currentFlight = flightScheduleManager.findFlight(fNum); // findFlight busca en CircularDoublyLinkedList
                if (currentFlight != null) {
                    currentFlight.setStatus(Flight.FlightStatus.COMPLETED);
                    System.out.println("Vuelo " + fNum + " en estado: COMPLETED.");

                    plane.setStatus(AirplaneStatus.IDLE); // El avión vuelve a IDLE
                    plane.setCurrentLocationAirportCode(finalDestinationCode); // Actualiza la ubicación del avión
                    // Añadir vuelo al historial del avión (LinkedStack)
                    plane.addFlightToHistory(currentFlight);
                    System.out.println("Vuelo " + fNum + " ha completado su viaje a " + (airportManager.findAirport(finalDestinationCode) != null ? airportManager.findAirport(finalDestinationCode).getName() : finalDestinationCode) + ". Avión " + plane.getId() + " ubicado en " + (airportManager.findAirport(finalDestinationCode) != null ? airportManager.findAirport(finalDestinationCode).getName() : finalDestinationCode) + ".");

                    // Los pasajeros desembarcan (se borran del vuelo)
                    currentFlight.clearPassengers();

                    // Remueve el vuelo completado de la lista de vuelos programados en FSM (CircularDoublyLinkedList)
                    try {
                        flightScheduleManager.getScheduledFlights().remove(currentFlight);
                    } catch (ListException e) {
                        System.err.println("ERROR: No se pudo remover el vuelo completado " + fNum + " de la lista de vuelos programados: " + e.getMessage());
                    }


                } else {
                    System.err.println("ERROR: Vuelo " + fNum + " no encontrado al intentar finalizar su simulación lógica.");
                }

            } catch (Exception e) {
                System.err.println("ERROR: Excepción inesperada durante la finalización lógica del vuelo " + fNum + ": " + e.getMessage());
            }
        }, delaySecondsToTakeoff + simulatedDurationForLogicSeconds, TimeUnit.SECONDS);

        flightCounter.incrementAndGet();
        System.out.println("Vuelo " + flightNumber + " programado exitosamente.");
    }

    /**
     * Inicia el proceso de simulación, programando la generación de vuelos y el fin de la simulación.
     * @param flightGenerationIntervalSeconds Intervalo en segundos para generar nuevos vuelos.
     * @param simulationDurationSeconds Duración total de la simulación en segundos.
     */
    public void startSimulation(long flightGenerationIntervalSeconds, long simulationDurationSeconds) {
        System.out.println("--- INICIANDO SIMULACIÓN DE VUELOS ---");
        /*System.out.println("SIMULADOR: Intervalo de generación de vuelos: " + flightGenerationIntervalSeconds + " segundos.");
        System.out.println("SIMULADOR: Duración total de la simulación: " + simulationDurationSeconds + " segundos.");
        System.out.println("SIMULADOR: Límite de vuelos a generar: " + MAX_FLIGHTS_TO_GENERATE);
        */

        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (flightCounter.get() < MAX_FLIGHTS_TO_GENERATE) {
                    generateRandomFlightBasedOnRules();
                } else {
                    if (!flightLimitMessagePrinted) {
                        System.out.println("\nLímite de " + MAX_FLIGHTS_TO_GENERATE + " vuelos alcanzado. No se generarán más vuelos.");
                        flightLimitMessagePrinted = true;
                    }
                }
            } catch (ListException | QueueException | TreeException e) { // Catch QueueException
                System.err.println("ERROR: Error al intentar generar un vuelo aleatorio: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("ERROR: Un error inesperado ocurrió durante la generación de vuelos: " + e.getMessage());
            }
        }, 0, flightGenerationIntervalSeconds, TimeUnit.SECONDS);

        scheduler.schedule(() -> {
            try {
                System.out.println("\nTiempo de simulación finalizado. Deteniendo la simulación...");
                stopSimulation();
            } catch (ListException | TreeException e) {
                System.err.println("ERROR: Error durante el apagado de la simulación: " + e.getMessage());
            }
        }, simulationDurationSeconds, TimeUnit.SECONDS);
    }

    /**
     * Detiene la simulación de vuelos y muestra un resumen.
     * @throws ListException Si hay un problema con las listas al obtener datos.
     * @throws TreeException Si hay un problema con el árbol AVL al obtener datos.
     */
    public void stopSimulation() throws ListException, TreeException {
        System.out.println("\n--- DETENIENDO SIMULACIÓN DE VUELOS ---");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                //System.err.println("ADVERTENCIA: Algunas tareas no terminaron en el tiempo especificado. Forzando apagado inmediato...");
                scheduler.shutdownNow(); // Forzar apagado si no termina a tiempo
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt(); // Restablecer el estado de interrupción
            //System.err.println("ERROR: La simulación fue interrumpida durante el apagado.");
            throw new RuntimeException("Simulación interrumpida durante el apagado", e);
        }
        System.out.println("Simulador apagado.\n");

        System.out.println("\n--- Historial de Vuelos de Aviones ---");
        if (airplanes.isEmpty()) {
            System.out.println("Actualmente los aviones no cuentan con un historial.");
        } else {
            // Cada Airplane tiene un LinkedStack para su historial
            airplanes.values().forEach(Airplane::printFlightHistory);
        }

        System.out.println("\n--- Historial de Vuelos de Pasajeros ---");
        if (passengerManager.getPassengerCount() == 0) {
            System.out.println("No hay pasajeros registrados o con historial de vuelos.");
        } else {
            try {
                // PassengerManager's getAllPassengerIds() devuelve una SinglyLinkedList
                SinglyLinkedList allPassengerIds = passengerManager.getAllPassengerIds();
                if (allPassengerIds != null && !allPassengerIds.isEmpty()) {
                    for (int i = 0; i < allPassengerIds.size(); i++) {
                        // PassengerManager usa AVL internamente para buscar Pasajeros y su historial es LinkedQueue
                        String passengerId = (String) allPassengerIds.get(i);
                        passengerManager.showFlightHistory(passengerId); // Llamar al método para mostrar el historial
                    }
                } else {
                    System.out.println("No hay IDs de pasajeros en la lista para mostrar el historial.");
                }
            } catch (ListException | TreeException | QueueException e) { // Añadido QueueException
                System.err.println("ERROR al mostrar historial de vuelos de pasajeros: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        try {
            FlightSimulator simulator = new FlightSimulator();

            long flightGenerationIntervalSeconds = 5; // Generar un nuevo vuelo cada 5 segundos
            long simulationDurationSeconds = 300;    // Ejecutar la simulación durante 300 segundos (5 minutos)

            simulator.startSimulation(flightGenerationIntervalSeconds, simulationDurationSeconds);

        } catch (ListException | IOException | TreeException e) {
            System.err.println("ERROR CRÍTICO: La simulación no pudo iniciarse debido a un error de inicialización: " + e.getMessage());
            e.printStackTrace();
        }
    }
}