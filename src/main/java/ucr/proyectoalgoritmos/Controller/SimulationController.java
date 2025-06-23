package ucr.proyectoalgoritmos.Controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import javafx.util.Duration;

import ucr.proyectoalgoritmos.Domain.TreeException;
import ucr.proyectoalgoritmos.Domain.flight.FlightSimulator;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.reportes.ReportGenerator;
import ucr.proyectoalgoritmos.util.ListConverter;

import java.io.File; //Maneja rutas de archivo
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class SimulationController implements Initializable {

    @FXML
    private AnchorPane simulationPanel;
    @FXML
    private Slider speedSlider;
    @FXML
    private Line flightPathLine;
    @FXML
    private Label lblHeading;
    @FXML
    private Label lblSpeed;
    @FXML
    private Button btnPauseSimulation;
    @FXML
    private Label lblFlightInfo;
    @FXML
    private Button btnResetSimulation;
    @FXML
    private Label lblElapsedTime;
    @FXML
    private Button btnStartSimulation;
    @FXML
    private Label lblAltitude;
    @FXML
    private ImageView airplaneIcon;

    private FlightSimulator flightSimulator;
    private ReportGenerator reportGenerator;
    private Timeline updateTimeline;
    private Timeline movementTimeline;
    private String currentVisualizedFlightNumber = null;

    private boolean isSimulationRunning = false;
    private boolean isSimulationPaused = false;
    private boolean isSimulatorInitialized = false;

    private static final double MIN_SIM_SPEED = 0.5;
    private static final double MAX_SIM_SPEED = 5.0;
    private static final double DEFAULT_SIM_SPEED = 1.0;

    private static final int MIN_AIRPLANE_SPEED_KMH = 0;
    private static final int MAX_AIRPLANE_SPEED_KMH = 900;

    private static final long FIXED_UI_FLIGHT_DURATION_SECONDS = 60; //Duración de la animación

    private Map<String, Point2D> airportCoordinates = new HashMap<>();

    private List<Point2D> fixedRoutePoints = new ArrayList<>();

    // Ruta para guardar los reportes
    private static final String REPORTS_OUTPUT_DIR = "C:/data/reports";


    /**
     * Inicializa el controlador de la simulación. Se encarga de instanciar el simulador de vuelos
     * y el generador de reportes. Configura los controles de la interfaz de usuario como botones
     * y el slider de velocidad, y prepara las líneas de tiempo para la actualización de datos
     * y el movimiento del avión.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            flightSimulator = new FlightSimulator();
            reportGenerator = new ReportGenerator(flightSimulator);
            isSimulatorInitialized = true;
        } catch (ListException | IOException | TreeException e) {
            System.err.println("ERROR : Fallo al inicializar FlightSimulator: " + e.getMessage());
            e.printStackTrace();
            isSimulatorInitialized = false;
        }

        if (!isSimulatorInitialized) {
            btnStartSimulation.setDisable(true);
            btnPauseSimulation.setDisable(true);
            btnResetSimulation.setDisable(true);
            speedSlider.setDisable(true);
            lblFlightInfo.setText("ERROR: Simulador no disponible. Revise la consola.");
            return;
        }

        btnStartSimulation.setDisable(false);
        btnPauseSimulation.setDisable(true);
        btnResetSimulation.setDisable(true);

        speedSlider.setMin(MIN_SIM_SPEED);
        speedSlider.setMax(MAX_SIM_SPEED);
        speedSlider.setValue(DEFAULT_SIM_SPEED);
        speedSlider.setBlockIncrement(0.5);
        speedSlider.setMajorTickUnit(1.0);
        speedSlider.setMinorTickCount(1);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMouseTransparent(true);
        speedSlider.setFocusTraversable(false);

        resetLiveFlightDataLabels();

        updateTimeline = new Timeline(new KeyFrame(Duration.millis(120), event -> {
            updateLiveFlightData();
        }));
        updateTimeline.setCycleCount(Timeline.INDEFINITE);

        airportCoordinates.put("SJO", new Point2D(150, 400));
        airportCoordinates.put("LIR", new Point2D(80, 250));
        airportCoordinates.put("MIA", new Point2D(450, 100));
        airportCoordinates.put("JFK", new Point2D(600, 50));
        airportCoordinates.put("LAX", new Point2D(50, 50));

        fixedRoutePoints.add(new Point2D(150, 400));
        fixedRoutePoints.add(new Point2D(250, 350));
        fixedRoutePoints.add(new Point2D(350, 280));
        fixedRoutePoints.add(new Point2D(450, 200));
        fixedRoutePoints.add(new Point2D(550, 150));
        fixedRoutePoints.add(new Point2D(650, 100));

        airplaneIcon.setVisible(false);
        airplaneIcon.setOpacity(0.0);
        flightPathLine.setVisible(false);
    }

    /**
     * Inicia la simulación al presionar el botón "Iniciar".
     * Comienza la línea de tiempo para la actualización de datos en vivo y habilita la lógica
     * para procesar automáticamente el siguiente vuelo programado.
     */
    @FXML
    public void startSimulation(ActionEvent actionEvent) {
        if (!isSimulationRunning && isSimulatorInitialized) {
            flightSimulator.startSimulation(2, 120); //Duración de la simulación
            updateTimeline.play();

            isSimulationRunning = true;
            isSimulationPaused = false;

            btnStartSimulation.setDisable(true);
            btnPauseSimulation.setDisable(false);
            btnResetSimulation.setDisable(true);

            // Avanza al primer vuelo inmediatamente al iniciar
            processNextFlightAutomatically();
        }
    }

    /**
     * Pausa o reanuda la simulación al presionar el botón correspondiente.
     * Detiene o reanuda las líneas de tiempo de actualización de datos y movimiento del avión.
     */
    @FXML
    public void pauseSimulation(ActionEvent actionEvent) {
        if (isSimulationRunning && isSimulatorInitialized) {
            if (!isSimulationPaused) {
                updateTimeline.stop();
                if (movementTimeline != null) {
                    movementTimeline.pause();
                }
                isSimulationPaused = true;
                btnPauseSimulation.setText("Reanudar");

                btnStartSimulation.setDisable(true);
                btnResetSimulation.setDisable(false);
            } else {
                updateTimeline.play();
                if (movementTimeline != null) {
                    movementTimeline.play();
                }
                isSimulationPaused = false;
                btnPauseSimulation.setText("Pausar");

                btnStartSimulation.setDisable(true);
                btnResetSimulation.setDisable(true);
            }
        }
    }

    /**
     * Procesa automáticamente el siguiente vuelo en la simulación.
     * Si hay un vuelo programado, lo activa y comienza su movimiento.
     * Si no hay más vuelos, finaliza la simulación y genera los reportes.
     */
    private void processNextFlightAutomatically() {
        if (isSimulatorInitialized) {
            try {
                Flight activatedFlight = flightSimulator.advanceToNextScheduledFlight();

                if (activatedFlight != null) {
                    resetLiveFlightDataLabels();
                    startAirplaneMovement(activatedFlight.getFlightNumber());
                } else {
                    lblFlightInfo.setText("No hay más vuelos programados. Simulación finalizada.");
                    resetLiveFlightDataLabels();
                    speedSlider.setValue(MIN_SIM_SPEED);
                    stopAirplaneMovement();
                    isSimulationRunning = false;
                    updateTimeline.stop();
                    btnStartSimulation.setDisable(false);
                    btnPauseSimulation.setDisable(true);
                    btnResetSimulation.setDisable(false);

                    // AQUÍ ES DONDE SE LLAMA A LA GENERACIÓN DE REPORTES AUTOMÁTICAMENTE
                    generateReportsAutomatically();
                }
            } catch (Exception e) {
                System.err.println("ERROR ACTION: Fallo al avanzar al siguiente vuelo automáticamente: " + e.getMessage());
                e.printStackTrace();
                lblFlightInfo.setText("ERROR: No se pudo avanzar al siguiente vuelo automáticamente.");
                stopAirplaneMovement();
            }
        }
    }

    /**
     * Reinicia la simulación. Detiene cualquier simulación en curso, reinicializa el simulador
     * y el generador de reportes a su estado inicial, y restablece los controles de la interfaz de usuario.
     */
    @FXML
    public void resetSimulation(ActionEvent actionEvent) {
        if (isSimulatorInitialized) {
            if (isSimulationRunning || isSimulationPaused) {
                flightSimulator.shutdownSimulation();
            }
            updateTimeline.stop();
            stopAirplaneMovement();

            try {
                flightSimulator = new FlightSimulator();
                reportGenerator = new ReportGenerator(flightSimulator);
                isSimulatorInitialized = true;
            } catch (ListException | IOException | TreeException e) {
                System.err.println("ERROR : Fallo al reinicializar FlightSimulator: " + e.getMessage());
                e.printStackTrace();
                isSimulatorInitialized = false;
                btnStartSimulation.setDisable(true);
                btnPauseSimulation.setDisable(true);
                btnResetSimulation.setDisable(true);
                speedSlider.setDisable(true);
                lblFlightInfo.setText("ERROR: Reinicialización fallida.");
                return;
            }

            isSimulationRunning = false;
            isSimulationPaused = false;

            btnStartSimulation.setDisable(false);
            btnPauseSimulation.setDisable(true);
            btnResetSimulation.setDisable(true);
            speedSlider.setValue(DEFAULT_SIM_SPEED);

            resetLiveFlightDataLabels();
            lblFlightInfo.setText("Simulación lista. Presione 'Iniciar'.");
            airplaneIcon.setOpacity(0.0);
        }
    }

    /**
     * Actualiza los datos en tiempo real del vuelo actualmente en progreso en la interfaz de usuario.
     * Recupera la información del vuelo del simulador y la muestra en las etiquetas correspondientes.
     * También ajusta la velocidad del slider y maneja el cambio entre vuelos.
     */
    private void updateLiveFlightData() {
        if (flightSimulator == null || !isSimulatorInitialized) {
            resetLiveFlightDataLabels();
            lblFlightInfo.setText("Simulador no inicializado.");
            speedSlider.setValue(MIN_SIM_SPEED);
            stopAirplaneMovement();
            return;
        }

        try {
            DoublyLinkedList scheduledFlightsList = ListConverter.convertToDoublyLinkedList(
                    flightSimulator.getFlightScheduleManager().getScheduledFlights()
            );

            Flight flightInProgress = null;
            if (scheduledFlightsList != null && !scheduledFlightsList.isEmpty()) {
                for (int i = 0; i < scheduledFlightsList.size(); i++) {
                    Flight currentFlight = (Flight) scheduledFlightsList.get(i);
                    if (currentFlight.getStatus() == Flight.FlightStatus.IN_PROGRESS) {
                        flightInProgress = currentFlight;
                        break;
                    }
                }
            }

            if (flightInProgress != null) {
                String originCode = flightInProgress.getOriginAirportCode();
                String destinationCode = flightInProgress.getDestinationAirportCode();

                FlightSimulator.FlightData data = flightSimulator.getFlightInProgressData(flightInProgress.getFlightNumber());

                if (data != null) {
                    lblFlightInfo.setText(String.format("Vuelo: %s (%s → %s)",
                            flightInProgress.getFlightNumber(),
                            originCode,
                            destinationCode));

                    lblAltitude.setText(String.format("%,d m", data.getCurrentAltitude()));
                    lblSpeed.setText(String.format("%,d km/h", data.getCurrentSpeed()));
                    lblHeading.setText(String.format("%d°", (data.getCurrentHeading() + 360) % 360));

                    long elapsedSeconds = Math.max(0, data.getElapsedTimeSeconds());

                    long hours = elapsedSeconds / 3600;
                    long minutes = (elapsedSeconds % 3600) / 60;
                    long seconds = elapsedSeconds % 60;
                    lblElapsedTime.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

                    double mappedSpeed = mapValue(data.getCurrentSpeed(),
                            MIN_AIRPLANE_SPEED_KMH, MAX_AIRPLANE_SPEED_KMH,
                            MIN_SIM_SPEED, MAX_SIM_SPEED);
                    speedSlider.setValue(mappedSpeed);

                    if (!flightInProgress.getFlightNumber().equals(currentVisualizedFlightNumber)) {
                        startAirplaneMovement(flightInProgress.getFlightNumber());
                    }

                    // Verifica si el vuelo actual ha terminado
                    if (flightInProgress.getStatus() == Flight.FlightStatus.COMPLETED || progressRatioReachedCompletion()) {
                        processNextFlightAutomatically(); // Intenta avanzar al siguiente vuelo
                    }

                } else {
                    lblFlightInfo.setText(String.format("Vuelo: %s (%s → %s) (Finalizando...)",
                            flightInProgress.getFlightNumber(), originCode, destinationCode));
                    resetLiveFlightDataLabels();
                    speedSlider.setValue(MIN_SIM_SPEED);
                    stopAirplaneMovement();
                    // Si el data es nulo, significa que el vuelo probablemente terminó.
                    processNextFlightAutomatically();
                }
            } else {
                lblFlightInfo.setText("No hay vuelos en progreso. Buscando el siguiente...");
                resetLiveFlightDataLabels();
                speedSlider.setValue(MIN_SIM_SPEED);
                stopAirplaneMovement();
                // Si no hay vuelos en progreso, intenta avanzar al siguiente automáticamente
                processNextFlightAutomatically();
            }
        } catch (ListException e) {
            System.err.println("ERROR : ListException al obtener vuelos programados: " + e.getMessage());
            e.printStackTrace();
            lblFlightInfo.setText("ERROR al cargar vuelos.");
            resetLiveFlightDataLabels();
            speedSlider.setValue(MIN_SIM_SPEED);
            stopAirplaneMovement();
        }
    }

    /**
     * Determina si la duración de la animación del vuelo actual ha alcanzado su duración fijada.
     */
    private boolean progressRatioReachedCompletion() {
        if (currentVisualizedFlightNumber != null) {
            FlightSimulator.FlightData flightData = flightSimulator.getFlightInProgressData(currentVisualizedFlightNumber);
            if (flightData != null) {
                // Consideramos que el vuelo ha "completado" su visualización
                // si ha excedido la duración fijada.
                return flightData.getElapsedTimeSeconds() >= FIXED_UI_FLIGHT_DURATION_SECONDS;
            }
        }
        return false;
    }

    /**
     * Mapea un valor de un rango de entrada a un rango de salida.
     * Utilizado para ajustar la velocidad del slider(barra que representa la velocidad)
     * en función de la velocidad real del avión.
     */
    private double mapValue(double value, double inMin, double inMax, double outMin, double outMax) {
        if (inMax - inMin == 0) {
            return outMin;
        }
        value = Math.max(inMin, Math.min(inMax, value));
        double mapped = (value - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
        return mapped;
    }

    /**
     * Reinicia las etiquetas de datos de vuelo en vivo a sus valores predeterminados (cero).
     */
    private void resetLiveFlightDataLabels() {
        lblAltitude.setText("0 m");
        lblSpeed.setText("0 km/h");
        lblHeading.setText("0°");
        lblElapsedTime.setText("00:00:00");
    }

    /**
     * Inicia la animación visual del movimiento del avión en la interfaz de usuario para un vuelo específico.
     * Mueve el icono del avión a lo largo de una ruta fija, actualiza su rotación y lo hace visible.
     */
    public void startAirplaneMovement(String flightNumber) {
        if (movementTimeline != null) {
            movementTimeline.stop();
        }

        this.currentVisualizedFlightNumber = flightNumber;
        airplaneIcon.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), airplaneIcon);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        flightPathLine.setVisible(true);

        movementTimeline = new Timeline(
                new KeyFrame(Duration.millis(30), event -> {
                    if (currentVisualizedFlightNumber != null) {
                        // Aquí obtenemos los datos más recientes del vuelo en progreso
                        FlightSimulator.FlightData flightData = flightSimulator.getFlightInProgressData(currentVisualizedFlightNumber);

                        if (flightData != null) {
                            double progressRatio = (double) flightData.getElapsedTimeSeconds() / FIXED_UI_FLIGHT_DURATION_SECONDS;
                            progressRatio = Math.min(1.0, progressRatio); // Asegura que no exceda 1.0

                            Point2D newPosition = calculatePositionOnFixedRoute(progressRatio);

                            airplaneIcon.setLayoutX(newPosition.getX() - airplaneIcon.getFitWidth() / 2);
                            airplaneIcon.setLayoutY(newPosition.getY() - airplaneIcon.getFitHeight() / 2);

                            if (fixedRoutePoints.size() > 1) {
                                int currentSegmentIndex = (int) (progressRatio * (fixedRoutePoints.size() - 1));
                                currentSegmentIndex = Math.min(currentSegmentIndex, fixedRoutePoints.size() - 2);
                                currentSegmentIndex = Math.max(0, currentSegmentIndex);

                                Point2D p1 = fixedRoutePoints.get(currentSegmentIndex);
                                Point2D p2 = fixedRoutePoints.get(currentSegmentIndex + 1);

                                double angle = Math.toDegrees(Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX()));
                                airplaneIcon.setRotate(angle + 90);
                            }

                            // Si el progreso para este vuelo llega al 100%,
                            // la visualización de este vuelo ha "terminado"
                            if (progressRatio >= 1.0) {
                                stopAirplaneMovement();
                                processNextFlightAutomatically();
                            }
                        } else {
                            stopAirplaneMovement();
                            processNextFlightAutomatically();
                        }
                    } else {
                        stopAirplaneMovement();
                    }
                })
        );
        movementTimeline.setCycleCount(Timeline.INDEFINITE);
        movementTimeline.play();
    }

    /**
     * Detiene la animación visual del movimiento del avión.
     * Hace que el icono del avión se desvanezca y se oculte, y también oculta la línea de la trayectoria.
     */
    public void stopAirplaneMovement() {
        if (movementTimeline != null) {
            movementTimeline.stop();
            movementTimeline = null;
        }

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), airplaneIcon);
        fadeOut.setFromValue(airplaneIcon.getOpacity());
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            airplaneIcon.setVisible(false);
            airplaneIcon.setOpacity(0.0);
            flightPathLine.setVisible(false);
        });
        fadeOut.play();

        currentVisualizedFlightNumber = null;
    }

    /**
     * Calcula la posición del avión en la ruta fija en función de un ratio de progreso.
     */
    private Point2D calculatePositionOnFixedRoute(double progressRatio) {
        if (fixedRoutePoints.isEmpty()) {
            return new Point2D(0, 0);
        }
        if (fixedRoutePoints.size() == 1) {
            return fixedRoutePoints.get(0);
        }

        progressRatio = Math.max(0.0, Math.min(1.0, progressRatio));

        double segmentLength = 1.0 / (fixedRoutePoints.size() - 1);

        int currentSegmentIndex = (int) Math.floor(progressRatio / segmentLength);

        currentSegmentIndex = Math.min(currentSegmentIndex, fixedRoutePoints.size() - 2);
        currentSegmentIndex = Math.max(0, currentSegmentIndex);

        Point2D startPoint = fixedRoutePoints.get(currentSegmentIndex);
        Point2D endPoint = fixedRoutePoints.get(currentSegmentIndex + 1);

        double segmentProgress = (progressRatio - (currentSegmentIndex * segmentLength)) / segmentLength;
        segmentProgress = Math.max(0.0, Math.min(1.0, segmentProgress));

        double x = startPoint.getX() + (endPoint.getX() - startPoint.getX()) * segmentProgress;
        double y = startPoint.getY() + (endPoint.getY() - startPoint.getY()) * segmentProgress;

        return new Point2D(x, y);
    }

    /**
     * Genera automáticamente los reportes PDF al finalizar la simulación.
     * Crea el directorio de reportes si no existe y luego invoca los métodos
     * del ReportGenerator para crear los diferentes tipos de reportes.
     */
    private void generateReportsAutomatically() {
        if (!isSimulatorInitialized) {
            System.err.println("ERROR REPORT: Simulador no inicializado. No se pueden generar reportes automáticamente.");
            lblFlightInfo.setText("ERROR: No se pueden generar reportes. Simulador no inicializado.");
            return;
        }
        if (reportGenerator == null) {
            System.err.println("ERROR REPORT: ReportGenerator no inicializado.");
            lblFlightInfo.setText("ERROR: No se pueden generar reportes. Objeto de reporte nulo.");
            return;
        }

        lblFlightInfo.setText("Simulación finalizada. Generando reportes...");

        // 1. Asegúrate de que el directorio exista.
        File reportsDirectory = new File(REPORTS_OUTPUT_DIR);
        if (!reportsDirectory.exists()) {
            if (reportsDirectory.mkdirs()) {
            } else {
                System.err.println("ERROR ACTION: No se pudo crear el directorio de reportes: " + reportsDirectory.getAbsolutePath() + ". Por favor, verifica los permisos.");
                lblFlightInfo.setText("ERROR: No se pudo crear el directorio " + REPORTS_OUTPUT_DIR);
                return; // Salir si no se puede crear el directorio
            }
        }

        try {
            // Generar cada reporte
            reportGenerator.generateTop5AirportsReport(reportsDirectory.getAbsolutePath() + File.separator + "Reporte_Top_Aeropuertos.pdf");
            reportGenerator.generateMostUsedRoutesReport(reportsDirectory.getAbsolutePath() + File.separator + "Reporte_Rutas_Mas_Usadas.pdf");
            reportGenerator.generateTopPassengersReport(reportsDirectory.getAbsolutePath() + File.separator + "Reporte_Pasajeros_Frecuentes.pdf");
            reportGenerator.generateAverageOccupancyReport(reportsDirectory.getAbsolutePath() + File.separator + "Reporte_Ocupacion_Promedio.pdf");

            lblFlightInfo.setText("¡Reportes generados exitosamente en: " + REPORTS_OUTPUT_DIR + "!");

        } catch (IOException e) {
            System.err.println("ERROR REPORT: Error al generar reportes: " + e.getMessage());
            e.printStackTrace();
            lblFlightInfo.setText("ERROR al generar reportes. Verifique la consola.");
        } catch (Exception e) {
            System.err.println("ERROR REPORT: Error inesperado al generar reportes: " + e.getMessage());
            e.printStackTrace();
            lblFlightInfo.setText("ERROR inesperado al generar reportes.");
        }
    }
}