module ucr.proyectoalgoritmos {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.google.gson;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind; // A menudo es necesario con databind


    opens ucr.proyectoalgoritmos.Domain.aeropuetos to com.fasterxml.jackson.databind;
    opens ucr.proyectoalgoritmos.Domain.airplane to com.fasterxml.jackson.databind;
    opens ucr.proyectoalgoritmos.Domain.passenger to com.fasterxml.jackson.databind;

    opens ucr.proyectoalgoritmos.Domain.flight to com.fasterxml.jackson.databind; // <-- Posiblemente necesites esta
    opens ucr.proyectoalgoritmos.Domain.list to com.fasterxml.jackson.databind;
    opens ucr.proyectoalgoritmos.Domain.queue to com.fasterxml.jackson.databind;
    opens ucr.proyectoalgoritmos.Domain.route to com.fasterxml.jackson.databind;
    opens ucr.proyectoalgoritmos.Domain.stack to com.fasterxml.jackson.databind;

    opens ucr.proyectoalgoritmos.UtilJson to com.fasterxml.jackson.databind;
    opens ucr.proyectoalgoritmos.Serializer to com.fasterxml.jackson.databind;

    opens ucr.proyectoalgoritmos to javafx.fxml;
    exports ucr.proyectoalgoritmos;

    exports ucr.proyectoalgoritmos.Controller;
    opens ucr.proyectoalgoritmos.Controller to javafx.fxml;

    // Export your domain packages if other modules might use them
    exports ucr.proyectoalgoritmos.Domain.aeropuetos;
    exports ucr.proyectoalgoritmos.Domain.airplane;
    exports ucr.proyectoalgoritmos.Domain.flight;
    exports ucr.proyectoalgoritmos.Domain.list;
    exports ucr.proyectoalgoritmos.Domain.passenger;
    exports ucr.proyectoalgoritmos.Domain.queue;
    exports ucr.proyectoalgoritmos.Domain.stack;
    exports ucr.proyectoalgoritmos.Domain.route;
    exports ucr.proyectoalgoritmos.util;
    exports ucr.proyectoalgoritmos.Serializer; // Exporta tu paquete de serializadores
    exports ucr.proyectoalgoritmos.UtilJson;
    exports ucr.proyectoalgoritmos.Controller.AirportController;
    opens ucr.proyectoalgoritmos.Controller.AirportController to javafx.fxml;
    exports ucr.proyectoalgoritmos.Controller.FlightController;
    opens ucr.proyectoalgoritmos.Controller.FlightController to javafx.fxml; // Exporta tu paquete de UtilJson
}