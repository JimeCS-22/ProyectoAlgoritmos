module ucr.proyectoalgoritmos {
    requires javafx.controls;
    requires javafx.fxml;
    // --- IMPORTANTE: Si estás usando Jackson, necesitas estos REQUIRES ---
    // requires com.google.gson; // <-- Si ya no usas GSON para nada, puedes eliminar esta línea
    requires com.google.gson;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind; // A menudo es necesario con databind
    // requires com.fasterxml.jackson.annotation; // Puede que necesites esta si usas anotaciones de Jackson

    // -------------------------------------------------------------------
    // ABRE LOS PAQUETES DE DOMINIO A JACKSON PARA REFLEXIÓN
    // Cada paquete que contenga clases cuyos campos privados Jackson necesite leer/escribir
    // debe ser ABIERTO a 'com.fasterxml.jackson.databind'.
    // Si tus clases tienen getters/setters públicos y un constructor sin argumentos,
    // a menudo Jackson no necesita acceso directo a los campos, pero es una buena práctica
    // abrir los paquetes de dominio si experimentas InaccessibleObjectException.

    opens ucr.proyectoalgoritmos.Domain.aeropuetos to com.fasterxml.jackson.databind;
    opens ucr.proyectoalgoritmos.Domain.airplane to com.fasterxml.jackson.databind;
    opens ucr.proyectoalgoritmos.Domain.passenger to com.fasterxml.jackson.databind;
    // Si Domain.flight también tiene clases que Jackson serializa/deserializa, abrelo.
    opens ucr.proyectoalgoritmos.Domain.flight to com.fasterxml.jackson.databind; // <-- Posiblemente necesites esta
    opens ucr.proyectoalgoritmos.Domain.list to com.fasterxml.jackson.databind;
    opens ucr.proyectoalgoritmos.Domain.queue to com.fasterxml.jackson.databind;
    opens ucr.proyectoalgoritmos.Domain.route to com.fasterxml.jackson.databind;
    opens ucr.proyectoalgoritmos.Domain.stack to com.fasterxml.jackson.databind;


    // Si tu paquete utiljson (donde está AirportJson) tiene clases con campos privados
    // que Jackson necesite acceder para serialización/deserialización, también ábrelo.
    // Esto es menos común para clases UtilJson, pero no está de más si hay problemas.
    opens ucr.proyectoalgoritmos.UtilJson to com.fasterxml.jackson.databind;
    opens ucr.proyectoalgoritmos.Serializer to com.fasterxml.jackson.databind; // Los deserializadores pueden necesitar esto

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