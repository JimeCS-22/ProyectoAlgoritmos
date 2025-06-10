module ucr.proyectoalgoritmos {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.desktop;

    // Open packages for reflection (Gson needs access to private fields)
    opens ucr.proyectoalgoritmos.Domain.aeropuetos to com.google.gson;
    opens ucr.proyectoalgoritmos.Domain.airplane to com.google.gson;
    opens ucr.proyectoalgoritmos.Domain.passenger to com.google.gson;
    opens ucr.proyectoalgoritmos.Domain.flight;
    opens ucr.proyectoalgoritmos.Domain.list to com.google.gson;
    opens ucr.proyectoalgoritmos.Domain.queue to com.google.gson; // <--- ADD THIS NEW CRITICAL LINE!
    opens ucr.proyectoalgoritmos.route to com.google.gson;
    opens ucr.proyectoalgoritmos.Domain.stack to com.google.gson;
    // If your Node class is in a separate package (e.g., if Node is generic and used by multiple structures
    // and its own package), you might need an opens for that package too.
    // If your queue's Node is inside the Domain.queue package, the above line covers it.

    // Open/Export packages for JavaFX and general access
    opens ucr.proyectoalgoritmos to javafx.fxml;
    exports ucr.proyectoalgoritmos;

    exports ucr.proyectoalgoritmos.Controller;
    opens ucr.proyectoalgoritmos.Controller to javafx.fxml;

    // Export your domain packages if other modules might use them
    exports ucr.proyectoalgoritmos.Domain.aeropuetos;
    exports ucr.proyectoalgoritmos.Domain.airplane;
    exports ucr.proyectoalgoritmos.Domain.flight;
    exports ucr.proyectoalgoritmos.Domain.list;
    exports ucr.proyectoalgoritmos.Domain.passenger; // Typo 'passanger' vs 'passenger' - check your actual package name!
//    exports ucr.proyectoalgoritmos.Domain.passenger; // If you refactored it
    exports ucr.proyectoalgoritmos.Domain.queue; // <--- Make sure this is exported too
    exports ucr.proyectoalgoritmos.Domain.stack;
    exports ucr.proyectoalgoritmos.route;
    exports ucr.proyectoalgoritmos.util;
}