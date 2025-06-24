package ucr.proyectoalgoritmos.reportes;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.properties.UnitValue;

import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.flight.FlightSimulator;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;
import ucr.proyectoalgoritmos.Domain.airplane.Airplane;
import ucr.proyectoalgoritmos.util.ListConverter;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportGenerator {

    private FlightSimulator flightSimulator;

    public ReportGenerator(FlightSimulator flightSimulator) {
        this.flightSimulator = flightSimulator;
    }

    /**
     * Genera un reporte en PDF de los Top 5 aeropuertos con más vuelos salientes.
     */
    public void generateTop5AirportsReport(String filePath) throws IOException {
        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            addReportTitle(document, "Reporte: Top 5 Aeropuertos con más Vuelos Salientes");

            Map<String, Integer> outgoingFlightsCount = countOutgoingFlights();

            Map<String, Integer> top5Airports = sortAndLimitResults(outgoingFlightsCount, 5);

            if (top5Airports.isEmpty()) {
                addNoDataMessage(document);
            } else {
                addResultsTable(document, top5Airports, "Aeropuerto", "Vuelos Salientes");
            }
        }
    }

    /**
     * Genera un reporte en PDF de las rutas más utilizadas.
     */
    public void generateMostUsedRoutesReport(String filePath) throws IOException {
        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            addReportTitle(document, "Reporte: Rutas Más Utilizadas");

            Map<String, Integer> routeCounts = countRouteUsage();

            Map<String, Integer> topRoutes = sortAndLimitResults(routeCounts, 10);

            if (topRoutes.isEmpty()) {
                addNoDataMessage(document);
            } else {
                addResultsTable(document, topRoutes, "Ruta", "Usos");
            }
        }
    }

    /**
     * Genera un reporte en PDF de los pasajeros con más vuelos realizados.
     */
    public void generateTopPassengersReport(String filePath) throws IOException {
        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            addReportTitle(document, "Reporte: Pasajeros con Más Vuelos Realizados");

            Map<String, Integer> passengerFlightCounts = countPassengerFlights();

            Map<String, Integer> topPassengers = sortAndLimitResults(passengerFlightCounts, 10);

            if (topPassengers.isEmpty()) {
                addNoDataMessage(document);
            } else {
                addResultsTable(document, topPassengers, "Pasajero ID", "Vuelos Realizados");
            }
        }
    }

    /**
     * Genera un reporte en PDF del porcentaje de ocupación promedio por vuelo.
     */
    public void generateAverageOccupancyReport(String filePath) throws IOException {
        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            addReportTitle(document, "Reporte: Porcentaje de Ocupación Promedio por Vuelo");

            OccupancyData occupancyData = calculateOccupancyData();

            if (occupancyData.flightCount == 0) {
                addNoDataMessage(document);
            } else {
                addOccupancyResults(document, occupancyData);
            }
        }
    }

    /**
     * Métodos auxiliares para la generación de reportes
     */

    private Map<String, Integer> countOutgoingFlights() {
        Map<String, Integer> outgoingFlightsCount = new HashMap<>();
        try {
            CircularDoublyLinkedList rawFlights = flightSimulator.getFlightScheduleManager().getScheduledFlights();
            DoublyLinkedList allFlights = ListConverter.convertToDoublyLinkedList(rawFlights);

            if (allFlights != null && !allFlights.isEmpty()) {
                for (int i = 0; i < allFlights.size(); i++) {
                    Flight flight = (Flight) allFlights.get(i);
                    if (flight != null && flight.getOriginAirportCode() != null) {
                        outgoingFlightsCount.put(flight.getOriginAirportCode(),
                                outgoingFlightsCount.getOrDefault(flight.getOriginAirportCode(), 0) + 1);
                    }
                }
            }
        } catch (ListException e) {
            throw new RuntimeException("Error al contar vuelos salientes");
        }
        return outgoingFlightsCount;
    }

    private Map<String, Integer> countRouteUsage() {
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
            throw new RuntimeException("Error al contar rutas");
        }
        return routeCounts;
    }

    private Map<String, Integer> countPassengerFlights() {
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
            throw new RuntimeException("Error al contar vuelos de pasajeros");
        }
        return passengerFlightCounts;
    }

    private OccupancyData calculateOccupancyData() {
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
                            Airplane airplane = (Airplane) flight.getAirplane();
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
            throw new RuntimeException("Error al calcular ocupación");
        }

        return new OccupancyData(totalOccupancyPercentage, flightCount);
    }

    private Map<String, Integer> sortAndLimitResults(Map<String, Integer> data, int limit) {
        return data.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    private void addReportTitle(Document document, String title) {
        document.add(new Paragraph(title)
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));
    }

    private void addNoDataMessage(Document document) {
        document.add(new Paragraph("No hay datos disponibles para este reporte.")
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));
    }

    private void addResultsTable(Document document, Map<String, Integer> data, String column1, String column2) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2}));
        table.setWidth(UnitValue.createPercentValue(80));
        table.setMarginLeft(document.getLeftMargin());
        table.setMarginRight(document.getRightMargin());

        table.addHeaderCell(createCell("Rank", true));
        table.addHeaderCell(createCell(column1, true));
        table.addHeaderCell(createCell(column2, true));

        int rank = 1;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            table.addCell(createCell(String.valueOf(rank++), false));
            table.addCell(createCell(entry.getKey(), false));
            table.addCell(createCell(String.valueOf(entry.getValue()), false));
        }
        document.add(table);
    }

    private void addOccupancyResults(Document document, OccupancyData data) {
        double averageOccupancy = (data.flightCount > 0) ? (data.totalOccupancyPercentage / data.flightCount) : 0;

        document.add(new Paragraph(String.format("Número Total de Vuelos Analizados: %d", data.flightCount))
                .setMarginLeft(document.getLeftMargin() + 10));
        document.add(new Paragraph(String.format("Porcentaje de Ocupación Promedio: %.2f%%", averageOccupancy))
                .setMarginLeft(document.getLeftMargin() + 10));
    }

    private Cell createCell(String content, boolean isHeader) {
        Paragraph p = new Paragraph(content);
        if (isHeader) {
            p.setBold();
            p.setTextAlignment(TextAlignment.CENTER);
        } else {
            p.setTextAlignment(TextAlignment.LEFT);
        }
        Cell cell = new Cell().add(p);
        cell.setBorder(Border.NO_BORDER);
        return cell;
    }

    private static class OccupancyData {
        double totalOccupancyPercentage;
        int flightCount;

        OccupancyData(double totalOccupancyPercentage, int flightCount) {
            this.totalOccupancyPercentage = totalOccupancyPercentage;
            this.flightCount = flightCount;
        }
    }
}