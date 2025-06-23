module ucr.proyectoalgoritmos {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.google.gson;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;

    requires kernel; // Déjalos si los usas para PDF o algo similar
    requires layout; // Déjalos si los usas para PDF o algo similar


    // ----- Directivas 'opens' consolidadas para Jackson y GSON -----
    // Cada paquete se abre a ambos módulos en una sola línea
    opens ucr.proyectoalgoritmos.Domain.aeropuetos to com.fasterxml.jackson.databind, com.google.gson;
    opens ucr.proyectoalgoritmos.Domain.airplane to com.fasterxml.jackson.databind, com.google.gson;
    opens ucr.proyectoalgoritmos.Domain.passenger to com.fasterxml.jackson.databind, com.google.gson;
    opens ucr.proyectoalgoritmos.Domain.flight to com.fasterxml.jackson.databind, com.google.gson;
    opens ucr.proyectoalgoritmos.Domain.list to com.fasterxml.jackson.databind, com.google.gson;
    opens ucr.proyectoalgoritmos.Domain.queue to com.fasterxml.jackson.databind, com.google.gson;
    opens ucr.proyectoalgoritmos.Domain.route to com.fasterxml.jackson.databind, com.google.gson; // ¡Corregido aquí!
    opens ucr.proyectoalgoritmos.Domain.stack to com.fasterxml.jackson.databind, com.google.gson;

    opens ucr.proyectoalgoritmos.UtilJson to com.fasterxml.jackson.databind, com.google.gson;
    opens ucr.proyectoalgoritmos.Serializer to com.fasterxml.jackson.databind, com.google.gson; // ¡Corregido aquí!


    // --- Resto de tus directivas (sin cambios, ya que no son duplicados) ---
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
    exports ucr.proyectoalgoritmos.util; // Asegúrate de que este paquete exista
    exports ucr.proyectoalgoritmos.Serializer;
    exports ucr.proyectoalgoritmos.UtilJson;
    exports ucr.proyectoalgoritmos.Controller.AirportController;
    opens ucr.proyectoalgoritmos.Controller.AirportController to javafx.fxml;
    exports ucr.proyectoalgoritmos.Controller.FlightController;
    opens ucr.proyectoalgoritmos.Controller.FlightController to javafx.fxml;
    exports ucr.proyectoalgoritmos.Controller.PassengerController;
    opens ucr.proyectoalgoritmos.Controller.PassengerController to javafx.fxml;
    exports ucr.proyectoalgoritmos.Controller.UserController;
    opens ucr.proyectoalgoritmos.Controller.UserController to javafx.fxml;
}