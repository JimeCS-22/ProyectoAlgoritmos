package ucr.proyectoalgoritmos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class HelloApplication extends Application {
    private static BorderPane root = new BorderPane();

    @Override
    public void start(Stage stage) throws IOException {
        // Cargar el contenido principal (hello-view.fxml)
        FXMLLoader mainLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        root.setCenter(mainLoader.load());  // Establecer como centro del BorderPane

        // Cargar la barra de menú
        FXMLLoader menuLoader = new FXMLLoader(HelloApplication.class.getResource("admin-menubar.fxml"));
        MenuBar menuBar = menuLoader.load();
        root.setTop(menuBar);  // Establecer la barra de menú en la parte superior

        // Configurar la escena
        Scene scene = new Scene(root, 1380, 688);  // Tamaño más adecuado para una aplicación con menú
        stage.setTitle("Airport and flight route management system");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}