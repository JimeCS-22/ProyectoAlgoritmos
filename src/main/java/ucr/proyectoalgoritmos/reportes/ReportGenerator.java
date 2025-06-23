package ucr.proyectoalgoritmos.reportes; // O el paquete que corresponda

// Importaciones de iText 7
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.properties.UnitValue; // Para configurar ancho de tabla con porcentaje

// Importaciones de tus clases de dominio
import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.flight.FlightSimulator;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList; // Asumo que esta clase existe
import ucr.proyectoalgoritmos.Domain.list.ListException; // Asumo que esta excepción existe
import ucr.proyectoalgoritmos.Domain.passenger.Passenger; // Asumo que Passenger está en Domain.person
import ucr.proyectoalgoritmos.Domain.airplane.Airplane; // Asumo que Airplane está en Domain.airplane
import ucr.proyectoalgoritmos.util.ListConverter;


import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap; // Para mantener el orden después de ordenar
import java.util.Map;
import java.util.stream.Collectors;

public class ReportGenerator {

    private FlightSimulator flightSimulator;

    public ReportGenerator(FlightSimulator flightSimulator) {
        this.flightSimulator = flightSimulator;
        // La creación del directorio "reports" la manejaremos en la aplicación principal
        // o en los tests, para dar más flexibilidad en la ruta.
    }

    /**
     * Genera un reporte en PDF de los Top 5 aeropuertos con más vuelos salientes.
     * @param filePath Ruta completa donde se guardará el archivo PDF (ej. "reports/Top5AirportsReport.pdf")
     * @throws IOException Si ocurre un error al escribir el PDF.
     */
    public void generateTop5AirportsReport(String filePath) throws IOException {
        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            document.add(new Paragraph("Reporte: Top 5 Aeropuertos con más Vuelos Salientes")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            Map<String, Integer> outgoingFlightsCount = new HashMap<>();
            try {
                // Asumo que getScheduledFlights() devuelve CircularDoublyLinkedList
                CircularDoublyLinkedList rawFlights = flightSimulator.getFlightScheduleManager().getScheduledFlights();
                DoublyLinkedList allFlights = ListConverter.convertToDoublyLinkedList(rawFlights); // Usando tu ListConverter

                if (allFlights != null && !allFlights.isEmpty()) {
                    for (int i = 0; i < allFlights.size(); i++) {
                        Flight flight = (Flight) allFlights.get(i); // Se hace un cast a Flight
                        if (flight != null && flight.getOriginAirportCode() != null) {
                            outgoingFlightsCount.put(flight.getOriginAirportCode(),
                                    outgoingFlightsCount.getOrDefault(flight.getOriginAirportCode(), 0) + 1);
                        }
                    }
                }
            } catch (ListException e) {
                System.err.println("Error ListException al obtener vuelos para el reporte de aeropuertos: " + e.getMessage());
                document.add(new Paragraph("Error al cargar datos de vuelos: " + e.getMessage()).setFontColor(DeviceRgb.RED));
            } catch (Exception e) {
                System.err.println("Error general al obtener vuelos para el reporte de aeropuertos: " + e.getMessage());
                document.add(new Paragraph("Error inesperado al cargar datos: " + e.getMessage()).setFontColor(DeviceRgb.RED));
            }

            Map<String, Integer> top5Airports = outgoingFlightsCount.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(5)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1, LinkedHashMap::new));

            if (top5Airports.isEmpty()) {
                document.add(new Paragraph("No hay datos disponibles para este reporte.")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(20));
            } else {
                Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2}));
                table.setWidth(UnitValue.createPercentValue(80));
                table.setMarginLeft(document.getLeftMargin());
                table.setMarginRight(document.getRightMargin());

                table.addHeaderCell(createCell("Rank", true));
                table.addHeaderCell(createCell("Aeropuerto", true));
                table.addHeaderCell(createCell("Vuelos Salientes", true));

                int rank = 1;
                for (Map.Entry<String, Integer> entry : top5Airports.entrySet()) {
                    table.addCell(createCell(String.valueOf(rank++), false));
                    table.addCell(createCell(entry.getKey(), false));
                    table.addCell(createCell(String.valueOf(entry.getValue()), false));
                }
                document.add(table);
            }

            System.out.println("Reporte de Top 5 Aeropuertos generado en: " + filePath);
        }
    }

    /**
     * Genera un reporte en PDF de las rutas más utilizadas.
     * @param filePath Ruta completa donde se guardará el archivo PDF (ej. "reports/MostUsedRoutesReport.pdf")
     * @throws IOException Si ocurre un error al escribir el PDF.
     */
    public void generateMostUsedRoutesReport(String filePath) throws IOException {
        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            document.add(new Paragraph("Reporte: Rutas Más Utilizadas")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            Map<String, Integer> routeCounts = new HashMap<>();
            try {
                CircularDoublyLinkedList rawFlights = flightSimulator.getFlightScheduleManager().getScheduledFlights();
                DoublyLinkedList allFlights = ListConverter.convertToDoublyLinkedList(rawFlights);

                if (allFlights != null && !allFlights.isEmpty()) {
                    for (int i = 0; i < allFlights.size(); i++) {
                        Flight flight = (Flight) allFlights.get(i);
                        if (flight != null && flight.getOriginAirportCode() != null && flight.getDestinationAirportCode() != null) {
                            String route = flight.getOriginAirportCode() + " - " + flight.getDestinationAirportCode();
                            routeCounts.put(route, routeCounts.getOrDefault(route, 0) + 1);
                        }
                    }
                }
            } catch (ListException e) {
                System.err.println("Error ListException al obtener vuelos para el reporte de rutas: " + e.getMessage());
                document.add(new Paragraph("Error al cargar datos de rutas: " + e.getMessage()).setFontColor(DeviceRgb.RED));
            } catch (Exception e) {
                System.err.println("Error general al obtener vuelos para el reporte de rutas: " + e.getMessage());
                document.add(new Paragraph("Error inesperado al cargar datos: " + e.getMessage()).setFontColor(DeviceRgb.RED));
            }

            Map<String, Integer> topRoutes = routeCounts.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(10)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1, LinkedHashMap::new));

            if (topRoutes.isEmpty()) {
                document.add(new Paragraph("No hay datos disponibles para este reporte.")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(20));
            } else {
                Table table = new Table(UnitValue.createPercentArray(new float[]{1, 4, 1.5f}));
                table.setWidth(UnitValue.createPercentValue(80));
                table.setMarginLeft(document.getLeftMargin());
                table.setMarginRight(document.getRightMargin());

                table.addHeaderCell(createCell("Rank", true));
                table.addHeaderCell(createCell("Ruta", true));
                table.addHeaderCell(createCell("Usos", true));

                int rank = 1;
                for (Map.Entry<String, Integer> entry : topRoutes.entrySet()) {
                    table.addCell(createCell(String.valueOf(rank++), false));
                    table.addCell(createCell(entry.getKey(), false));
                    table.addCell(createCell(String.valueOf(entry.getValue()), false));
                }
                document.add(table);
            }

            System.out.println("Reporte de Rutas Más Utilizadas generado en: " + filePath);
        }
    }

    /**
     * Genera un reporte en PDF de los pasajeros con más vuelos realizados.
     * @param filePath Ruta completa donde se guardará el archivo PDF (ej. "reports/TopPassengersReport.pdf")
     * @throws IOException Si ocurre un error al escribir el PDF.
     */
    public void generateTopPassengersReport(String filePath) throws IOException {
        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            document.add(new Paragraph("Reporte: Pasajeros con Más Vuelos Realizados")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            Map<String, Integer> passengerFlightCounts = new HashMap<>();

            try {
                CircularDoublyLinkedList rawFlights = flightSimulator.getFlightScheduleManager().getScheduledFlights();
                DoublyLinkedList allFlights = ListConverter.convertToDoublyLinkedList(rawFlights);

                if (allFlights != null && !allFlights.isEmpty()) {
                    for (int i = 0; i < allFlights.size(); i++) {
                        Flight flight = (Flight) allFlights.get(i);
                        if (flight != null && flight.getPassengers() != null) {
                            CircularDoublyLinkedList rawPassengers = flight.getPassengers();
                            DoublyLinkedList passengers = ListConverter.convertToDoublyLinkedList(rawPassengers);

                            if (passengers != null && !passengers.isEmpty()) {
                                for (int j = 0; j < passengers.size(); j++) {
                                    Passenger passenger = (Passenger) passengers.get(j);
                                    if (passenger != null && passenger.getId() != null) {
                                        passengerFlightCounts.put(passenger.getId(),
                                                passengerFlightCounts.getOrDefault(passenger.getId(), 0) + 1);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (ListException e) {
                System.err.println("Error ListException al obtener pasajeros para el reporte: " + e.getMessage());
                document.add(new Paragraph("Error al cargar datos de pasajeros: " + e.getMessage()).setFontColor(DeviceRgb.RED));
            } catch (Exception e) {
                System.err.println("Error general al obtener pasajeros para el reporte: " + e.getMessage());
                document.add(new Paragraph("Error inesperado al cargar datos: " + e.getMessage()).setFontColor(DeviceRgb.RED));
            }

            Map<String, Integer> topPassengers = passengerFlightCounts.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(10)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1, LinkedHashMap::new));

            if (topPassengers.isEmpty()) {
                document.add(new Paragraph("No hay datos disponibles para este reporte.")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(20));
            } else {
                Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2}));
                table.setWidth(UnitValue.createPercentValue(80));
                table.setMarginLeft(document.getLeftMargin());
                table.setMarginRight(document.getRightMargin());

                table.addHeaderCell(createCell("Rank", true));
                table.addHeaderCell(createCell("Pasajero ID", true));
                table.addHeaderCell(createCell("Vuelos Realizados", true));

                int rank = 1;
                for (Map.Entry<String, Integer> entry : topPassengers.entrySet()) {
                    table.addCell(createCell(String.valueOf(rank++), false));
                    table.addCell(createCell(entry.getKey(), false));
                    table.addCell(createCell(String.valueOf(entry.getValue()), false));
                }
                document.add(table);
            }

            System.out.println("Reporte de Pasajeros con Más Vuelos generado en: " + filePath);
        }
    }

    /**
     * Genera un reporte en PDF del porcentaje de ocupación promedio por vuelo.
     * @param filePath Ruta completa donde se guardará el archivo PDF (ej. "reports/AverageOccupancyReport.pdf")
     * @throws IOException Si ocurre un error al escribir el PDF.
     */
    public void generateAverageOccupancyReport(String filePath) throws IOException {
        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            document.add(new Paragraph("Reporte: Porcentaje de Ocupación Promedio por Vuelo")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            double totalOccupancyPercentage = 0;
            int flightCount = 0;

            try {
                CircularDoublyLinkedList rawFlights = flightSimulator.getFlightScheduleManager().getScheduledFlights();
                DoublyLinkedList allFlights = ListConverter.convertToDoublyLinkedList(rawFlights);

                if (allFlights != null && !allFlights.isEmpty()) {
                    for (int i = 0; i < allFlights.size(); i++) {
                        Flight flight = (Flight) allFlights.get(i);
                        if (flight != null && flight.getPassengers() != null && flight.getAirplane() != null) {
                            CircularDoublyLinkedList rawPassengers = flight.getPassengers();
                            DoublyLinkedList passengers = ListConverter.convertToDoublyLinkedList(rawPassengers);

                            if (passengers != null) {
                                int currentPassengers = passengers.size();
                                // Asumo que tu clase Airplane tiene un método getCapacity()
                                Airplane airplane = (Airplane) flight.getAirplane(); // Necesario el cast si Airplane no está como un tipo fijo
                                int maxCapacity = airplane.getCapacity();

                                if (maxCapacity > 0) {
                                    double occupancy = (double) currentPassengers / maxCapacity * 100;
                                    totalOccupancyPercentage += occupancy;
                                    flightCount++;
                                }
                            }
                        }
                    }
                }
            } catch (ListException e) {
                System.err.println("Error ListException al obtener datos de ocupación para el reporte: " + e.getMessage());
                document.add(new Paragraph("Error al cargar datos de ocupación: " + e.getMessage()).setFontColor(DeviceRgb.RED));
            } catch (Exception e) {
                System.err.println("Error general al obtener datos de ocupación para el reporte: " + e.getMessage());
                document.add(new Paragraph("Error inesperado al cargar datos: " + e.getMessage()).setFontColor(DeviceRgb.RED));
            }

            double averageOccupancy = (flightCount > 0) ? (totalOccupancyPercentage / flightCount) : 0;

            if (flightCount == 0) {
                document.add(new Paragraph("No hay datos de vuelos para calcular la ocupación.")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(20));
            } else {
                document.add(new Paragraph(String.format("Número Total de Vuelos Analizados: %d", flightCount))
                        .setMarginLeft(document.getLeftMargin() + 10));
                document.add(new Paragraph(String.format("Porcentaje de Ocupación Promedio: %.2f%%", averageOccupancy))
                        .setMarginLeft(document.getLeftMargin() + 10));
            }

            System.out.println("Reporte de Ocupación Promedio generado en: " + filePath);
        }
    }

    /**
     * Método auxiliar para crear celdas de tabla con formato.
     * @param content El texto de la celda.
     * @param isHeader Si es una celda de encabezado (negrita, centrado).
     * @return La celda de iText.
     */
    private Cell createCell(String content, boolean isHeader) {
        Paragraph p = new Paragraph(content);
        if (isHeader) {
            p.setBold();
            p.setTextAlignment(TextAlignment.CENTER);
        } else {
            p.setTextAlignment(TextAlignment.LEFT);
        }
        Cell cell = new Cell().add(p);
        cell.setBorder(Border.NO_BORDER); // Puedes cambiar esto para añadir bordes si lo deseas
        return cell;
    }
}