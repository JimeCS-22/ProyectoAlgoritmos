package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ucr.proyectoalgoritmos.HelloApplication;

import java.io.IOException;

import static java.lang.System.load;


public class HelloController {

    //    @Deprecated
//    public void Home(ActionEvent actionEvent) {
//
//        this.bp.setCenter(ap);
//        this.txtMessage.setText("Proyecto");
//    }

    @Deprecated
    public void Exit(ActionEvent actionEvent) {

        System.exit(0);
    }

//    @Deprecated
//    public void exampleOnMousePressed(Event event) {
//
//        this.txtMessage.setText("Loading Example. Please wait!!!");
//    }

    private void load(String form) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(form));

    }

    @Deprecated
    public void LoginOnAction(ActionEvent actionEvent) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ucr/proyectoalgoritmos/Login.fxml"));
        Parent root = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setTitle("Login");
        stage.setScene(new Scene(root));
        stage.show();

    }

}