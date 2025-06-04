module ucr.proyectoalgoritmos {
    requires javafx.controls;
    requires javafx.fxml;


    opens ucr.proyectoalgoritmos to javafx.fxml;
    exports ucr.proyectoalgoritmos;
}